package com.project.legendsofleague.domain.member.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.legendsofleague.domain.member.dto.CustomMemberDetails;
import com.project.legendsofleague.domain.member.dto.LoginDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;

@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    private final JWTUtil jwtUtil;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        LoginDto loginDto = new LoginDto();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ServletInputStream inputStream = request.getInputStream();
            String messageBody = StreamUtils.copyToString(inputStream,
                    StandardCharsets.UTF_8);
            loginDto = objectMapper.readValue(messageBody, LoginDto.class);

            System.out.println(loginDto.getUsername());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String username = loginDto.getUsername();
        String password = loginDto.getPassword();

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);

        return authenticationManager.authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        CustomMemberDetails customMemberDetails = (CustomMemberDetails) authResult.getPrincipal();

        String username = customMemberDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authResult.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();

        String token = jwtUtil.createJwt(username, role, 30 * 60 * 1000L);

        ResponseCookie cookie = createCookieTemp("Authorization", token);
        response.addHeader("Set-Cookie", cookie.toString());

    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(30 * 60);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }

    private ResponseCookie createCookieTemp(String key, String value) {
        return ResponseCookie.from(key, value)
                .path("/")
                .sameSite("None")
                .httpOnly(false)
                .secure(true)
                .maxAge(30 * 60)
                .build();
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(401);
    }
}

