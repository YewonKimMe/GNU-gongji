package site.gnu_gongji.GnuGongji.security.oauth2.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import site.gnu_gongji.GnuGongji.security.oauth2.OAuth2AuthorizationRequestCookieRepository;
import site.gnu_gongji.GnuGongji.tool.CookieUtils;

import java.io.IOException;

import static site.gnu_gongji.GnuGongji.security.oauth2.OAuth2AuthorizationRequestCookieRepository.REDIRECT_URI_PARAMETER_COOKIE_NAME;

@RequiredArgsConstructor
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final OAuth2AuthorizationRequestCookieRepository oAuth2AuthorizationRequestCookieRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String targetUrl = CookieUtils.getCookie(request, REDIRECT_URI_PARAMETER_COOKIE_NAME)
                .map(Cookie::getValue)
                .orElse("/");

        targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("error", exception.getMessage())
                .build().toUriString();

        oAuth2AuthorizationRequestCookieRepository.removeAuthorizationRequestCookies(request, response);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
