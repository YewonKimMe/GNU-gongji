package site.gnu_gongji.GnuGongji.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

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

        if (tokenManger.validateJwtToken(jwtToken)) {
            log.debug("JWT Valid={}", jwtToken);
            Authentication authentication = tokenManger.getAuth(jwtToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {

        String servletPath = request.getServletPath();

        AntPathMatcher pathMatcher = new AntPathMatcher();

        return pathMatcher.match("/api/v1/oauth2/**", servletPath);
    }

    private String getBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader(SecurityConst.AUTH_HEADER.getValue());

        if (StringUtils.hasText(authHeader) && authHeader.startsWith(SecurityConst.BEARER_PREFIX.getValue())) {
            return authHeader.substring(SecurityConst.BEARER_PREFIX.getValue().length());
        }

        return null;
    }
}
