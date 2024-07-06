package com.project.legendsofleague.domain.member.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class InternalFilterExceptionHandler extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException ex) {
            log.error("jwt 만료", ex);
            handleJwtException(response, ex);
        } catch (BadCredentialsException ex2) {
            log.error("비밀번호 오류", ex2);
            handleBadCredentialException(response, ex2);
        } catch (InternalAuthenticationServiceException ex3) {
            log.error("존재하지 않는 유저", ex3);
            handleUsernameNotFoundException(response, ex3);
        } catch (Exception e) {
            log.error("Unhandled exception", e);
            handleGenericException(response, e);
        }
    }

    private void handleJwtException(HttpServletResponse response, ExpiredJwtException ex) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"JWT has expired. Please reauthenticate.\"}");
    }

    private void handleBadCredentialException(HttpServletResponse response, BadCredentialsException ex) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"JWT has expired. Please reauthenticate.\"}");
    }

    private void handleUsernameNotFoundException(HttpServletResponse response, InternalAuthenticationServiceException ex) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"JWT has expired. Please reauthenticate.\"}");
    }


    private void handleGenericException(HttpServletResponse response, Exception ex) throws IOException {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setContentType("application/json");
        response.getWriter().write(String.format("{\"error\": \"An internal error occurred: %s\"}", ex.getMessage()));
    }
}
