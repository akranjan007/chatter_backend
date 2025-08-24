package com.talk.chatter.config;

import com.talk.chatter.Entities.Users;
import com.talk.chatter.Repository.UserRepository;
import com.talk.chatter.Services.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired private JwtService jwtService;
    @Autowired private UserDetailsService userDetailsService;
    @Autowired private UserRepository userRepo;
    //@Autowired private Principal principal;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        //System.out.println(">>> beforeHandshake called");

        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getParameter("token");
            System.out.println(">>> Token received: " + token);

            if (token != null) {
                try {
                    // Check token expiration
                    if (jwtService.isTokenExpired(token)) {
                        System.out.println(">>> Token is expired");

                        if (response instanceof ServletServerHttpResponse servletResponse) {
                            HttpServletResponse servletHttpResp = servletResponse.getServletResponse();
                            servletHttpResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            servletHttpResp.getWriter().write("TokenExpired");
                            servletHttpResp.getWriter().flush();
                            servletHttpResp.getWriter().close();
                        }

                        return false;
                    }

                    String username = jwtService.extractUserName(token);
                    System.out.println(">>> Username extracted: " + username);

                    if (username != null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        Optional<Users> optionUser = userRepo.findByEmail(username);

                        if (optionUser.isPresent() && jwtService.validateToken(token, userDetails)) {
                            System.out.println(">>> Token validated successfully for: " + username);
                            attributes.put("username", username);
                            attributes.put("userId", optionUser.get().getUserId());
                            return true;
                        } else {
                            System.out.println(">>> User not found or token invalid");
                        }
                    } else {
                        System.out.println(">>> Username extraction failed");
                    }
                } catch (Exception e) {
                    System.out.println(">>> Token parsing/validation error: " + e.getMessage());
                    if (response instanceof ServletServerHttpResponse servletResponse) {
                        HttpServletResponse servletHttpResp = servletResponse.getServletResponse();
                        servletHttpResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        servletHttpResp.getWriter().write("MalformedToken");
                        servletHttpResp.getWriter().flush();
                        servletHttpResp.getWriter().close();
                    }
                    return false;
                }
            } else {
                System.out.println(">>> Token is null");
            }
        }

        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
