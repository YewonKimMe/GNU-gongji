package site.gnu_gongji.GnuGongji.security.oauth2.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import site.gnu_gongji.GnuGongji.dto.UserCreateDto;
import site.gnu_gongji.GnuGongji.security.TokenManger;
import site.gnu_gongji.GnuGongji.security.oauth2.*;
import site.gnu_gongji.GnuGongji.security.oauth2.enums.OAuth2Provider;
import site.gnu_gongji.GnuGongji.security.oauth2.enums.TokenDurationTime;
import site.gnu_gongji.GnuGongji.security.oauth2.enums.TokenType;
import site.gnu_gongji.GnuGongji.service.UserManageService;
import site.gnu_gongji.GnuGongji.tool.CookieUtils;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

import static site.gnu_gongji.GnuGongji.security.oauth2.OAuth2AuthorizationRequestCookieRepository.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OAuth2AuthorizationRequestCookieRepository oAuth2AuthorizationRequestCookieRepository;

    private final OAuth2UnlinkManager oAuth2UnlinkManager;

    private final TokenManger tokenManger;

    private final UserManageService userManageService;

    private final String error = "error";
    private final String process = "process";


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        if (response.isCommitted()) { // 응답 객체가 이미 커밋된 경우
            log.debug("Response has already been committed, Unable to redirect to targetUrl");
            return;
        }

        String targetUrl = determineTargetUrl(request, response, authentication);

        customClearAuthenticationAttributes(request, response); // 요청 객체에 있는 인증 관련 요소를 제거하고 쿠키도 제거

        if (targetUrl.contains(error)) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Requested OAuth2 processing failed.");
        } else {
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAMETER_COOKIE_NAME).map(Cookie::getValue);

        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());

        String mode = CookieUtils.getCookie(request, MODE_PARAM_COOKIE_NAME).map(Cookie::getValue).orElse("");

        OAuth2UserPrincipal oAuth2UserPrincipal = getOAuth2UserPrincipal(authentication);

        log.debug("redirectUri={}, targetUrl={}, mode={}", redirectUri, targetUrl, mode);

        if (null == oAuth2UserPrincipal) {
            return UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam(error, "LoginProcessFailed")
                    .build().toUriString();
        }

        return switch (mode.toLowerCase()) {

            case "login" -> handleLoginProcess(authentication, oAuth2UserPrincipal, targetUrl);

            case "unlink" -> handleUnlinkProcess(oAuth2UserPrincipal, targetUrl);

            default -> handleDefaultProcess(mode, targetUrl);
        };
    }

    private String handleLoginProcess(Authentication authentication, OAuth2UserPrincipal oAuth2UserPrincipal, String targetUrl) {

        ObjectMapper objectMapper = new ObjectMapper();

        log.info("email={}", oAuth2UserPrincipal.getUserInfo().getEmail());

        // 토큰 생성, ROLE 추가
        String accessToken = tokenManger.createJwtToken(authentication, oAuth2UserPrincipal, TokenType.ACCESS, TokenDurationTime.ACCESS);
        String refreshToken = tokenManger.createJwtToken(authentication, oAuth2UserPrincipal, TokenType.REFRESH, TokenDurationTime.REFRESH);


        // Duplication check
        if (userManageService.isUserAlreadyExist(oAuth2UserPrincipal.getUserInfo().getEmail())) {
            return UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam(process, "login")
                    .queryParam("exist", "true")
                    .queryParam("accessToken", accessToken)
                    .build().toUriString();
        }

        // UserInfo DB Save, RefreshToken DB Save
        UserCreateDto oAuth2User = userManageService.createOAuth2User(oAuth2UserPrincipal, refreshToken);

        try {
            return UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("exist", "false")
                    .queryParam("accessToken", accessToken)
                    .queryParam("user", Base64.getEncoder().encodeToString(objectMapper.writeValueAsString(oAuth2User).getBytes()))
                    .build().toUriString();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String handleUnlinkProcess(OAuth2UserPrincipal oAuth2UserPrincipal, String targetUrl) {
        String accessToken = oAuth2UserPrincipal.getUserInfo().getAccessToken();
        OAuth2Provider provider = oAuth2UserPrincipal.getUserInfo().getProvider();

        // UNLINK
        oAuth2UnlinkManager.processUnlink(provider, accessToken, oAuth2UserPrincipal);

        // DB 유저 정보 제거
        boolean result = userManageService.deleteOAuth2User(oAuth2UserPrincipal.getUserInfo().getId(), provider);

        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam(process, "unlink")
                .queryParam("userDelete", String.valueOf(result))
                .queryParam("status", result ? "success" : "fail")
                .build()
                .toUriString();
    }

    private String handleDefaultProcess(String mode, String targetUrl) {
        log.debug("[OAuth2 Login Failed] mode={}, ", mode);

        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam(error, "LoginProcessFailed")
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
