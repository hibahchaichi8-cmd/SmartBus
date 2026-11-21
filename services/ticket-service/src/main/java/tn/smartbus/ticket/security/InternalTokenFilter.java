package tn.smartbus.ticket.security;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

public class InternalTokenFilter extends OncePerRequestFilter {

    private final byte[] expectedToken;

    public InternalTokenFilter(String token) {

        if (token == null || token.length() < 32) {
            throw new IllegalStateException(
                    "INTERNAL_TOKEN must contain at least 32 characters"
            );
        }

        expectedToken = token.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/internal/")) {

            String receivedToken =
                    request.getHeader("X-Service-Token");

            if (receivedToken == null
                    || !MessageDigest.isEqual(
                            expectedToken,
                            receivedToken.getBytes(StandardCharsets.UTF_8)
                    )) {

                response.setStatus(403);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                response.getWriter().write(
                        "{\"status\":403,\"message\":\"Access denied\"}"
                );

                return;
            }
        }

        chain.doFilter(request, response);
    }
}