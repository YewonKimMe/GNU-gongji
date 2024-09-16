package site.gnu_gongji.GnuGongji.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
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
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class TokenManger {

    private static final long ACCESS_TOKEN_EXPIRE_TIME_IN_MILLISECONDS_VALUE = 1000 * 60 * 30; // 30분
    private static final long ACCESS_TOKEN_EXPIRE_TIME_IN_TIME = 3; // 3시간

    @Value("${mycustom.jwt.secret}")
    private String jwtSecret;
    private Key key;

    @PostConstruct
    public void init() {
        byte[] key = Decoders.BASE64URL.decode(jwtSecret);
        this.key = Keys.hmacShaKeyFor(key);
    }

    public boolean validateJwtToken(String token) {
        try {
            // 기존 비밀키 생성
            SecretKey existingSecretKey = getSecretKey(jwtSecret);

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
        } catch (IllegalArgumentException exception) {
            log.error("JWT is null or empty or only whitespace");
        } catch (Exception exception) {
            log.error("JWT validation fails", exception);
        }
        return false;
    }

    public String createJwtToken(Authentication authentication) {

        OAuth2AuthenticationToken oAuth2AuthenticationToken = null;

        if (authentication instanceof OAuth2AuthenticationToken) {
            oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
        }

        SecretKey secretKey = getSecretKey(jwtSecret);

        Instant expirationTime = Instant.now().plus(Duration.ofHours(ACCESS_TOKEN_EXPIRE_TIME_IN_TIME));
        String jwt = Jwts.builder()
                .issuer("GNU-GONGJI")
                .subject(authentication.getName())
                .expiration(Date.from(expirationTime))
                .claim("provider", oAuth2AuthenticationToken != null ? oAuth2AuthenticationToken.getAuthorizedClientRegistrationId() : "")
                .claim("username", authentication.getName())
                .claim("authorities", populateAuthorities(authentication.getAuthorities()))
                .signWith(secretKey)
                .compact();
        log.debug("provider={}", oAuth2AuthenticationToken!= null ? oAuth2AuthenticationToken.getAuthorizedClientRegistrationId() : "None");
        log.debug("generated jwt = {}", jwt);
        return jwt;
    }

    public Authentication getAuth(String jwtToken) {

        SecretKey secretKey = getSecretKey(jwtSecret);

        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(jwtToken)
                .getPayload();
        String username = String.valueOf(claims.get("username"));
        String authorities = String.valueOf(claims.get("authorities"));
        List<GrantedAuthority> authoritiesList = AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);
        UserDetails user = new User(username, "", authoritiesList);

        return new UsernamePasswordAuthenticationToken(user, "", authoritiesList);
    }

    public SecretKey getSecretKey(String jwtSecret) {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String populateAuthorities(Collection<? extends GrantedAuthority> collection) {
        Set<String> authoritiesSet = new HashSet<>();
        for (GrantedAuthority authority : collection) {
            authoritiesSet.add(authority.getAuthority());
        }

        return String.join(",", authoritiesSet);
    }
}
