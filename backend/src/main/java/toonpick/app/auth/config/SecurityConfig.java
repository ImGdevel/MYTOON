package toonpick.app.auth.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import toonpick.app.auth.handler.LoginFailureHandler;
import toonpick.app.auth.handler.LoginSuccessHandler;
import toonpick.app.auth.handler.LogoutHandler;
import toonpick.app.auth.filter.CustomLogoutFilter;
import toonpick.app.auth.filter.JwtAuthorizationFilter;
import toonpick.app.auth.filter.LoginAuthenticationFilter;
import toonpick.app.auth.handler.OAuth2SuccessHandler;
import toonpick.app.auth.jwt.JwtTokenValidator;
import toonpick.app.auth.service.OAuth2UserService;
import toonpick.app.util.ErrorResponseSender;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtTokenValidator jwtTokenValidator;
    private final OAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler OAuth2SuccessHandler;

    private final LoginSuccessHandler successHandler;
    private final LoginFailureHandler failureHandler;
    private final LogoutHandler logoutHandler;

    private final ErrorResponseSender errorResponseSender;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {
                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        CorsConfiguration configuration = new CorsConfiguration();
                        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
                        configuration.setAllowedMethods(Collections.singletonList("*"));
                        configuration.setAllowedHeaders(Collections.singletonList("*"));
                        configuration.setAllowCredentials(true);
                        configuration.setMaxAge(3600L);
                        configuration.setExposedHeaders(Arrays.asList("Set-Cookie", "Authorization"));
                        return configuration;
                    }
                }))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                        .successHandler(OAuth2SuccessHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/*").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**", "/api/**").permitAll()
                        .requestMatchers("/", "/login", "/join", "/reissue").permitAll()
                        .requestMatchers("/hello").hasRole("USER")
                        .requestMatchers("/admin").hasRole("ADMIN")

                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterAt(
                        new LoginAuthenticationFilter(authenticationManager(authenticationConfiguration), successHandler, failureHandler),
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterBefore(
                        new CustomLogoutFilter(jwtTokenValidator, logoutHandler),
                        LogoutFilter.class
                )
                .addFilterAfter(
                        new JwtAuthorizationFilter(jwtTokenValidator, errorResponseSender),
                        OAuth2LoginAuthenticationFilter.class
                );

        return http.build();
    }
}
