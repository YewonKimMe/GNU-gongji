package site.gnu_gongji.GnuGongji.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    public boolean validateJwtToken(String token, TokenType tokenType) {
        try {
            // 기존 비밀키 생성
            SecretKey existingSecretKey = getSecretKey(jwtSecret);

            // jwt 검증
            Jwts.parser()
                    .verifyWith(existingSecretKey)
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (UnsupportedJwtException | MalformedJwtException exception) {
            log.error("JWT is not valid");
        } catch (SignatureException exception) {
            log.error("JWT signature validation fails");
        } catch (ExpiredJwtException exception) {

            log.error("JWT is expired");

            // jwt 만료 상황
            if (tokenType == TokenType.ACCESS) {
                return checkRefreshToken(token, jwtSecret);
            } else if (tokenType == TokenType.REFRESH) {

                Claims claim = getClaim(token, jwtSecret);

                String provider = String.valueOf(claim.get(PROVIDER.getClaimKey()));
                String oauth2Id = String.valueOf(claim.get(OAUTH2_ID.getClaimKey()));
                String username = String.valueOf(claim.get(USERNAME.getClaimKey()));
                String authorities = String.valueOf(claim.get(AUTHORITIES.getClaimKey()));

                // 토큰 재생성, 사용자 정보에 저장
                String newRefreshToken = createRefreshToken(
                        TokenType.REFRESH,
                        TokenDurationTime.REFRESH,
                        claim.getSubject(),
                        provider,
                        oauth2Id,
                        username,
                        authorities);

                userManageService.updateRefreshToken(oauth2Id, provider, newRefreshToken);
                // TODO 새 ACCESS TOKEN 을 클라이언트로 어떻게 반환할건지
                return true;
            }

        } catch (IllegalArgumentException exception) {
            log.error("JWT is null or empty or only whitespace");
        } catch (Exception exception) {
            log.error("JWT validation fails", exception);
        }
        return false;
    }

    private boolean checkRefreshToken(String invalidAccessJwt, String jwtSecret) {

        Claims claim = getClaim(invalidAccessJwt, jwtSecret);

        String oauth2Id = String.valueOf(claim.get(OAUTH2_ID.getClaimKey()));
        String oauth2Provider = String.valueOf(claim.get(PROVIDER.getClaimKey()));

        if (!StringUtils.hasText(oauth2Id) || !StringUtils.hasText(oauth2Provider)) {
            return false;
        }
        Optional<site.gnu_gongji.GnuGongji.entity.User> findUserOpt = userManageService.findOAuth2User(oauth2Id, oauth2Provider);

        if (findUserOpt.isEmpty()) return false;

        site.gnu_gongji.GnuGongji.entity.User user = findUserOpt.get();

        String refreshToken = user.getRefreshToken();

        return validateJwtToken(refreshToken, TokenType.REFRESH);
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

    private Claims getClaim(String jwtToken, String jwtSecret) {
        SecretKey secretKey = getSecretKey(jwtSecret);

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(jwtToken)
                .getPayload();
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

        return Jwts.builder()
                .issuer(ISSUER.getClaimKey())
                .subject(authentication.getName())
                .expiration(Date.from(expirationTime))
                .claim(TYPE.getClaimKey(), tokenType.getTokenName())
                .claim(PROVIDER.getClaimKey(), oAuth2AuthenticationToken != null ? oAuth2AuthenticationToken.getAuthorizedClientRegistrationId() : null)
                .claim(OAUTH2_ID.getClaimKey(), oAuth2UserPrincipal != null ? oAuth2UserPrincipal.getUserInfo().getId() : null)
                .claim(USERNAME.getClaimKey(), authentication.getName())
                .claim(AUTHORITIES.getClaimKey(), populateAuthorities(authentication.getAuthorities()))
                .signWith(secretKey)
                .compact();
    }

    public Authentication getAuth(String jwtToken) {

        SecretKey secretKey = getSecretKey(jwtSecret);

        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(jwtToken)
                .getPayload();
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
