package site.gnu_gongji.GnuGongji.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import site.gnu_gongji.GnuGongji.security.oauth2.OAuth2UserPrincipal;
import site.gnu_gongji.GnuGongji.security.oauth2.enums.Role;
import site.gnu_gongji.GnuGongji.security.oauth2.enums.TokenDurationTime;
import site.gnu_gongji.GnuGongji.security.oauth2.enums.TokenType;
import site.gnu_gongji.GnuGongji.service.UserManageService;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static site.gnu_gongji.GnuGongji.security.oauth2.enums.JWTClaimKey.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class TokenManger {

    private final UserManageService userManageService;

    private static final long ACCESS_TOKEN_EXPIRE_TIME_IN_MILLISECONDS_VALUE = 1000 * 60 * 30; // 30분
    private static final long ACCESS_TOKEN_EXPIRE_TIME_IN_TIME = 3; // 3시간

    @Value("${mycustom.jwt.secret}")
    private String jwtSecret;

    public boolean validateJwtToken(String token, TokenType tokenType, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 기존 비밀키 생성
            SecretKey existingSecretKey = getSecretKey(jwtSecret);

            // jwt 검증
            Jwts.parser()
                    .verifyWith(existingSecretKey)
                    .build()
                    .parseSignedClaims(token);

            // 여기서 만약 refreshToken 이 올바르다면, jwt 토큰을 하나 생성해서 넣어줘야겠는데
            // TODO: RefreshToken 일 경우 -> 새로운 JWT 토큰을 생성해서 클라이언트로 보내주기
            if (tokenType == TokenType.REFRESH) {

                // TODO getClaim 으로 refreshToken 으로부터 사용자 정보를 얻어 jwt 토큰을 생성

                //response.setHeader("Authorization", "Bearer " + NEWTOKEN);
            }
            return true;

        } catch (UnsupportedJwtException | MalformedJwtException exception) {
            log.debug("JWT is not valid");
        } catch (SignatureException exception) {
            log.debug("JWT signature validation fails");
        } catch (ExpiredJwtException exception) {

            log.debug("JWT is expired");

            // jwt 만료 상황
            if (tokenType == TokenType.ACCESS) {
                return checkRefreshToken(token, request, response);
            } else if (tokenType == TokenType.REFRESH) {

                Map<String, Object> claim = getClaim(token);

                if (claim == null) return false;

                String provider = String.valueOf(claim.get(PROVIDER.getClaimKey()));
                String oauth2Id = String.valueOf(claim.get(OAUTH2_ID.getClaimKey()));
                String username = String.valueOf(claim.get(USERNAME.getClaimKey()));
                String authorities = String.valueOf(claim.get(AUTHORITIES.getClaimKey()));

                // 토큰 재생성, 사용자 정보에 저장
                String newRefreshToken = createRefreshToken(
                        TokenType.REFRESH,
                        TokenDurationTime.REFRESH,
                        (String) claim.get(SUBJECT.getClaimKey()),
                        provider,
                        oauth2Id,
                        username,
                        authorities);

                return userManageService.updateRefreshToken(oauth2Id, provider, newRefreshToken);
            }

        } catch (IllegalArgumentException exception) {
            log.debug("JWT is null or empty or only whitespace");
        } catch (Exception exception) {
            log.debug("JWT validation fails", exception);
        }
        return false; // SecurityContext.clearContext() -> Exception(501 response)
    }

    private boolean checkRefreshToken(String invalidAccessJwt, HttpServletRequest request, HttpServletResponse response) {

        Map<String, Object> claim = getClaim(invalidAccessJwt);

        if (claim == null) return false;

        String oauth2Id = String.valueOf(claim.get(OAUTH2_ID.getClaimKey()));
        String oauth2Provider = String.valueOf(claim.get(PROVIDER.getClaimKey()));

        if (!StringUtils.hasText(oauth2Id) || !StringUtils.hasText(oauth2Provider)) {
            return false;
        }
        Optional<site.gnu_gongji.GnuGongji.entity.User> findUserOpt = userManageService.findOAuth2User(oauth2Id, oauth2Provider);

        if (findUserOpt.isEmpty()) return false;

        site.gnu_gongji.GnuGongji.entity.User user = findUserOpt.get();

        String refreshToken = user.getRefreshToken();

        return validateJwtToken(refreshToken, TokenType.REFRESH, request, response);
    }

    private String createRefreshToken(TokenType tokenType, TokenDurationTime tokenDurationTime, String subject, String provider, String oauth2Id, String username, String authorities) {
        SecretKey secretKey = getSecretKey(jwtSecret);

        Instant expirationTime = Instant.now().plus(Duration.ofHours(tokenDurationTime.getTime()));

        return Jwts.builder()
                .issuer(ISSUER.getClaimKey())
                .subject(subject)
                .expiration(Date.from(expirationTime))
                .claim(TYPE.getClaimKey(), tokenType.getTokenName())
                .claim(PROVIDER.getClaimKey(), provider)
                .claim(OAUTH2_ID.getClaimKey(), oauth2Id)
                .claim(USERNAME.getClaimKey(), username)
                .claim(AUTHORITIES.getClaimKey(), authorities)
                .signWith(secretKey)
                .compact();
    }

    private Map<String, Object> getClaim(String jwtToken) {
        try {
            // '.' 기준 분할
            String[] tokenParts = jwtToken.split("\\.");

            if (tokenParts.length < 2) {
                throw new IllegalArgumentException("유효하지 않은 JWT 형식입니다.");
            }

            // 두 번째 부분인 payload를 Base64Url로 디코딩
            String payload = new String(Base64.getUrlDecoder().decode(tokenParts[1]), StandardCharsets.UTF_8);

            // JSON 파싱을 위해 Jackson ObjectMapper를 사용
            ObjectMapper objectMapper = new ObjectMapper();

            return objectMapper.readValue(payload, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
            //return null; // 예외 발생 시 null 반환 또는 예외 처리
        }
    }

    public String createJwtToken(Authentication authentication, OAuth2User oAuth2User, TokenType tokenType, TokenDurationTime tokenDurationTime) {

        OAuth2AuthenticationToken oAuth2AuthenticationToken = null;

        OAuth2UserPrincipal oAuth2UserPrincipal = null;

        if (authentication instanceof OAuth2AuthenticationToken) {
            oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
        }
        if (oAuth2User instanceof OAuth2UserPrincipal) {
            oAuth2UserPrincipal = (OAuth2UserPrincipal) oAuth2User;
        }

        String jwt = getJWT(authentication, oAuth2AuthenticationToken, oAuth2UserPrincipal, tokenType, tokenDurationTime);

        log.debug("oAuth id={}", oAuth2UserPrincipal.getUserInfo().getId());
        log.debug("provider={}", oAuth2AuthenticationToken!= null ? oAuth2AuthenticationToken.getAuthorizedClientRegistrationId() : null);
        log.debug("generated jwt = {}", jwt);

        return jwt;
    }

    private String getJWT(Authentication authentication, OAuth2AuthenticationToken oAuth2AuthenticationToken, OAuth2UserPrincipal oAuth2UserPrincipal, TokenType tokenType, TokenDurationTime tokenDurationTime) {

        SecretKey secretKey = getSecretKey(jwtSecret);

        Instant expirationTime = Instant.now().plus(Duration.ofHours(tokenDurationTime.getTime()));

        if (oAuth2UserPrincipal == null || oAuth2AuthenticationToken == null) {
            throw new BadCredentialsException("OAuth2 인증 정보가 없습니다.");
        }
        return Jwts.builder()
                .issuer(ISSUER.getClaimKey())
                .subject(authentication.getName())
                .expiration(Date.from(expirationTime))
                .claim(TYPE.getClaimKey(), tokenType.getTokenName())
                .claim(PROVIDER.getClaimKey(), oAuth2AuthenticationToken.getAuthorizedClientRegistrationId())
                .claim(OAUTH2_ID.getClaimKey(), oAuth2UserPrincipal.getUserInfo().getId())
                .claim(USERNAME.getClaimKey(), oAuth2UserPrincipal.getUserInfo().getId())
                .claim(AUTHORITIES.getClaimKey(), populateAuthorities(authentication.getAuthorities()))
                .signWith(secretKey)
                .compact();
    }

    public Authentication getAuth(String jwtToken) {

        Map<String, Object> claims = getClaim(jwtToken);
        if (claims == null) return null;
        String username = String.valueOf(claims.get(USERNAME.getClaimKey()));
        String authorities = String.valueOf(claims.get(AUTHORITIES.getClaimKey()));
        List<GrantedAuthority> authoritiesList = AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);
        UserDetails user = new User(username, "", authoritiesList);

        return new UsernamePasswordAuthenticationToken(user, "", authoritiesList);
    }

    public SecretKey getSecretKey(String jwtSecret) {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String populateAuthorities(Collection<? extends GrantedAuthority> collection) {
        Set<String> authoritiesSet = new HashSet<>();

        if (collection.isEmpty()) {
            return Role.PREFIX.getValue() + Role.USER.getValue();
        }

        for (GrantedAuthority authority : collection) {
            authoritiesSet.add(authority.getAuthority());
        }

        return String.join(",", authoritiesSet);
    }
}
