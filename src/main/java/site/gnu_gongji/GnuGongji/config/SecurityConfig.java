package site.gnu_gongji.GnuGongji.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import site.gnu_gongji.GnuGongji.security.oauth2.CustomOAuth2UserService;
import site.gnu_gongji.GnuGongji.security.oauth2.OAuth2AuthorizationRequestCookieRepository;

@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final OAuth2AuthorizationRequestCookieRepository oAuth2AuthorizationRequestCookieRepository;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .securityContext(configurer -> configurer
                        .requireExplicitSave(false))
                .sessionManagement(
                                session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/api/v1/user/**").hasRole("USER")
                        .requestMatchers("/api/v1/subscribe/**").hasRole("USER")
                        .anyRequest().permitAll())
                .oauth2Login(configurer -> configurer.
                        authorizationEndpoint(config -> config.authorizationRequestRepository(oAuth2AuthorizationRequestCookieRepository)).
                        userInfoEndpoint(config -> config.userService(customOAuth2UserService)));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
