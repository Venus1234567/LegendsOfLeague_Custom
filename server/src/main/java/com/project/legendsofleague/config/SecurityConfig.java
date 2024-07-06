package com.project.legendsofleague.config;

import com.project.legendsofleague.domain.member.jwt.CustomAccessDeniedHandler;
import com.project.legendsofleague.domain.member.jwt.CustomSuccessHandler;
import com.project.legendsofleague.domain.member.jwt.CustomUnsuccessHandler;
import com.project.legendsofleague.domain.member.jwt.InternalFilterExceptionHandler;
import com.project.legendsofleague.domain.member.jwt.JWTFilter;
import com.project.legendsofleague.domain.member.jwt.JWTUtil;
import com.project.legendsofleague.domain.member.jwt.LoginFilter;
import com.project.legendsofleague.domain.member.repository.MemberRepository;
import com.project.legendsofleague.domain.member.service.CustomOAuth2MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
//    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final MemberRepository memberRepository;

    private final InternalFilterExceptionHandler internalFilterExceptionHandler;

    private final CustomOAuth2MemberService customOAuth2MemberService;

    private final CustomSuccessHandler customSuccessHandler;

    private final AuthenticationConfiguration configuration;

    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    private final CustomUnsuccessHandler customUnsuccessHandler;

    private final JWTUtil jwtUtil;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
                .csrf((auth) -> auth.disable());


        httpSecurity
                .formLogin((auth) -> auth.disable());

        httpSecurity
                .httpBasic((auth) -> auth.disable());

        httpSecurity
                .oauth2Login((oauth2) -> oauth2
                        .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
                                .userService(customOAuth2MemberService))
                        .successHandler(customSuccessHandler)
                );

        httpSecurity
                .addFilterBefore(new JWTFilter(jwtUtil, memberRepository), LoginFilter.class);

        httpSecurity
                .addFilterAt(new LoginFilter(authenticationManager(configuration), jwtUtil), UsernamePasswordAuthenticationFilter.class);

        httpSecurity
                .exceptionHandling((auth) -> {
                    auth.authenticationEntryPoint(customUnsuccessHandler)
                            .accessDeniedHandler(customAccessDeniedHandler);
                })
        ;

        httpSecurity
                .logout()
                .logoutUrl("/logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .deleteCookies("Authorization")
                .permitAll();

        httpSecurity
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/", "/login",
                                "/register", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .requestMatchers("/my/**", "/ex").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/").hasAnyRole("ADMIN", "USER")
                        .anyRequest().authenticated()
                );

        httpSecurity
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return httpSecurity.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

}
