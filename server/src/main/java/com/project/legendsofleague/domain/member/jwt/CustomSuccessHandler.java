package com.project.legendsofleague.domain.member.jwt;

import com.project.legendsofleague.domain.member.dto.CustomOAuth2Member;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;


@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        CustomOAuth2Member customOAuth2MemberDetails = (CustomOAuth2Member) authentication.getPrincipal();

        String username = customOAuth2MemberDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String token = jwtUtil.createJwt(username, role, 30 * 60 * 1000L);

        ResponseCookie cookie = createCookieTemp("Authorization", token);
        response.addHeader("Set-Cookie", cookie.toString());
        response.sendRedirect("https://develolment.site/success");
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(30 * 60);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setHttpOnly(false);

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
}
