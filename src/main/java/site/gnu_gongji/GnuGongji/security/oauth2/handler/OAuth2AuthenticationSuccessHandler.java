package site.gnu_gongji.GnuGongji.security.oauth2.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import site.gnu_gongji.GnuGongji.security.TokenManger;
import site.gnu_gongji.GnuGongji.security.oauth2.OAuth2AuthorizationRequestCookieRepository;
import site.gnu_gongji.GnuGongji.security.oauth2.OAuth2Provider;
import site.gnu_gongji.GnuGongji.security.oauth2.OAuth2UnlinkManager;
import site.gnu_gongji.GnuGongji.security.oauth2.OAuth2UserPrincipal;
import site.gnu_gongji.GnuGongji.tool.CookieUtils;

import java.io.IOException;
import java.util.Optional;

import static site.gnu_gongji.GnuGongji.security.oauth2.OAuth2AuthorizationRequestCookieRepository.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OAuth2AuthorizationRequestCookieRepository oAuth2AuthorizationRequestCookieRepository;

    private final OAuth2UnlinkManager oAuth2UnlinkManager;

    private final TokenManger tokenManger;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) { // 응답 객체가 이미 커밋된 경우
            log.debug("Response has already been committed, Unable to redirect to targetUrl: " + targetUrl);
            return;
        }
        customClearAuthenticationAttributes(request, response); // 요청 객체에 있는 인증 관련 요소를 제거하고 쿠키도 제거

        getRedirectStrategy().sendRedirect(request, response, targetUrl);

    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAMETER_COOKIE_NAME).map(Cookie::getValue);

        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());

        String mode = CookieUtils.getCookie(request, MODE_PARAM_COOKIE_NAME).map(Cookie::getValue).orElse("");

        OAuth2UserPrincipal oAuth2UserPrincipal = getOAuth2UserPrincipal(authentication);

        if (null == oAuth2UserPrincipal) {
            return UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("error", "LoginFailed")
                    .build().toUriString();
        }

        if ("login".equalsIgnoreCase(mode)) {
            // TODO: DB Save
            // TODO: AccessToken publish, RefreshToken publish
            // TODO: RefreshToken DB Save
            log.info("email={}, name={}, nickname={}, accessToken={}", oAuth2UserPrincipal.getUserInfo().getEmail(),
                    oAuth2UserPrincipal.getUserInfo().getName(),
                    oAuth2UserPrincipal.getUserInfo().getNickname(),
                    oAuth2UserPrincipal.getUserInfo().getAccessToken()
            );

            String accessToken = tokenManger.createJwtToken(authentication);
            String refreshToken = "TEST_REFRESH";

            return UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("access_token", accessToken)
                    // TODO: RefreshToken 실제로 바꾸기
                    .queryParam("refresh_token", refreshToken)
                    .build().toUriString();
        } else if ("unlink".equalsIgnoreCase(mode)) {
            String accessToken = oAuth2UserPrincipal.getUserInfo().getAccessToken();
            OAuth2Provider provider = oAuth2UserPrincipal.getUserInfo().getProvider();

            // TODO: DB 유저 정보 제거
            // TODO: RefreshToken 제거
            oAuth2UnlinkManager.processUnlink(provider, accessToken, oAuth2UserPrincipal);

            return UriComponentsBuilder.fromUriString(targetUrl)
                    .build()
                    .toUriString();
        }
        log.debug("login process failed");
        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("error", "LoginProcessFailed")
                .build()
                .toUriString();
    }

    private OAuth2UserPrincipal getOAuth2UserPrincipal(Authentication authentication) {

        Object principal = authentication.getPrincipal();

        if (principal instanceof OAuth2UserPrincipal) {
            return (OAuth2UserPrincipal) principal;
        }
        return null;

    }

    protected void customClearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {

        super.clearAuthenticationAttributes(request);

        oAuth2AuthorizationRequestCookieRepository.removeAuthorizationRequestCookies(request, response);
    }

}
