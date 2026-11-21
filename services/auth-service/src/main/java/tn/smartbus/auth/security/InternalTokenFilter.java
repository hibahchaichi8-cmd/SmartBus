package tn.smartbus.auth.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class InternalTokenFilter extends OncePerRequestFilter {
  private final byte[] secret;

  public InternalTokenFilter(@Value("${app.internal-token}") String token) {
    if (token.length() < 32)
      throw new IllegalStateException("INTERNAL_TOKEN must have at least 32 characters");
    secret = token.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    if (request.getRequestURI().startsWith("/internal/")) {
      String token = request.getHeader("X-Service-Token");
      if (token == null || !MessageDigest.isEqual(secret, token.getBytes(StandardCharsets.UTF_8))) {
        response.setStatus(403);
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":403,\"message\":\"Access denied\"}");
        return;
      }
    }
    chain.doFilter(request, response);
  }
}
