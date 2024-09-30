package site.gnu_gongji.GnuGongji.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import site.gnu_gongji.GnuGongji.security.oauth2.enums.TokenType;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtValidatorFilter extends OncePerRequestFilter {

    private final TokenManger tokenManger;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String jwtToken = getBearerToken(request);

        if (jwtToken == null) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        if (tokenManger.validateJwtToken(jwtToken, TokenType.ACCESS)) { // JWT 검증
            log.debug("JWT Valid={}", jwtToken);
            Authentication authentication = tokenManger.getAuth(jwtToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else { // SecurityContextHolder clear, AuthenticationException entrypoint catch, client 는 인증정보 제거
            log.error("JWT Error, path={}", request.getServletPath());
            SecurityContextHolder.clearContext();
            request.setAttribute("BadCdEx", new BadCredentialsException("잘못된 인증 토큰입니다."));
            //throw new BadCredentialsException("잘못된 인증 토큰입니다.");
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {

        String servletPath = request.getServletPath();

        AntPathMatcher pathMatcher = new AntPathMatcher();

        return !pathMatcher.match("/api/v1/**", servletPath);
    }

    private String getBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader(SecurityConst.AUTH_HEADER.getValue());

        if (StringUtils.hasText(authHeader) && authHeader.startsWith(SecurityConst.BEARER_PREFIX.getValue())) {
            return authHeader.substring(SecurityConst.BEARER_PREFIX.getValue().length());
        }

        return null;
    }
}
