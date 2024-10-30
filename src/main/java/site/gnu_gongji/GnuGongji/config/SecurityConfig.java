package site.gnu_gongji.GnuGongji.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import site.gnu_gongji.GnuGongji.security.CustomAuthenticationEntryPoint;
import site.gnu_gongji.GnuGongji.security.JwtValidatorFilter;
import site.gnu_gongji.GnuGongji.security.SecurityConst;
import site.gnu_gongji.GnuGongji.security.oauth2.CustomOAuth2UserService;
import site.gnu_gongji.GnuGongji.security.oauth2.OAuth2AuthorizationRequestCookieRepository;
import site.gnu_gongji.GnuGongji.security.oauth2.handler.OAuth2AuthenticationFailureHandler;
import site.gnu_gongji.GnuGongji.security.oauth2.handler.OAuth2AuthenticationSuccessHandler;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final OAuth2AuthorizationRequestCookieRepository oAuth2AuthorizationRequestCookieRepository;

    private final CustomOAuth2UserService customOAuth2UserService;

    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    private final JwtValidatorFilter jwtValidatorFilter;

    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Value("${mycustom.cors.dev-url}")
    private String devCorsAllowedURL;

    @Value("${mycustom.cors.prod-url}")
    private String prodCorsAllowedURL;

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .securityContext(configurer -> configurer
                        .requireExplicitSave(false))
                .sessionManagement(
                                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .cors(corsConfig -> corsConfig
                                .configurationSource(
                                        request -> {
                                            CorsConfiguration config = new CorsConfiguration();

                                            config.setAllowedHeaders(Collections.singletonList("*"));
                                            config.setExposedHeaders(List.of(SecurityConst.AUTH_HEADER.getValue()));
                                            config.setAllowedMethods(Collections.singletonList("*"));
                                            config.setMaxAge(3600L); // preflight request cache 유효 시간
                                            config.setAllowCredentials(true); // cors 요청에서 자격 증명 전송 허용

                                            config.setAllowedOrigins(List.of(
                                                    "http://localhost:3000",
                                                    devCorsAllowedURL,
                                                    prodCorsAllowedURL
                                            ));
                                            return config;
                                        }
                                )
                )
                .authorizeHttpRequests((requests) -> requests
                                .requestMatchers("/api/v1/user/**").hasRole("USER")
                                .requestMatchers("/api/v1/admin/**", "/api/v1/test/**", "/actuator/**").hasRole("ADMIN")
                                .requestMatchers("/api/v1/info/**").hasRole("USER")
                                .requestMatchers("/api/v1/firebase/**").hasRole("USER")
                                .requestMatchers("/oauth2/**", "/login/**").permitAll()
                                .anyRequest().denyAll()
                )
                .oauth2Login(configurer -> configurer.
                        authorizationEndpoint(config -> config.authorizationRequestRepository(oAuth2AuthorizationRequestCookieRepository)).
                        userInfoEndpoint(config -> config.userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler))
                .exceptionHandling(exceptionHandlingConfigurer -> exceptionHandlingConfigurer
                        .authenticationEntryPoint(authenticationEntryPoint))
                .addFilterBefore(jwtValidatorFilter, BasicAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
