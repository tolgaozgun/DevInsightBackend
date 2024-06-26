package com.bilkent.devinsight.security;


import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.http.HttpHeaders;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final static String TOKEN_BEARER_PREFIX = "Bearer";

    @Autowired
    private JWTUserService jwtUserService;

    @Autowired
    private JWTUtils jwtUtils;

    /**
     * @pre make sure that token is not null
     * @param token
     * @return
     * @throws Exception
     */
    public static String getTokenWithoutBearer(String token) throws Exception {
        if (token == null) {
            throw new Exception("Token is not found!");
        }

        return token.substring(TOKEN_BEARER_PREFIX.length());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String tokenHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        System.out.println("path: " + request.getServletPath());

        String username = "";
        String token = "";
        String currentEndpoint = request.getServletPath();

        if (tokenHeader == null || !tokenHeader.startsWith(TOKEN_BEARER_PREFIX)) {
            System.out.println("no token is found");
            filterChain.doFilter(request, response);
            return;
        }

        try { //????
            token = getTokenWithoutBearer(tokenHeader);
        } catch (Exception exc) {
            exc.printStackTrace();
            try {
                throw exc;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        boolean isCurrentRefresh = currentEndpoint.equals("/auth/refresh");

        try {
            // set username according to the token type
            if (isCurrentRefresh) {
                username = jwtUtils.extractRefreshUsername(token);
            } else {
                username = jwtUtils.extractAccessUsername(token);
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails user = jwtUserService.loadUserByUsername(username);

                if (isCurrentRefresh && jwtUtils.validateRefreshToken(token, user) || jwtUtils.validateAccessToken(token, user)) {
                    System.out.println("here is here: " + user.getPassword());
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
                // else if (JWTUtils.validateAccessToken(token, user)) {
                //     UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                //     authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                //     SecurityContextHolder.getContext().setAuthentication(authToken);
                // }
                else {
                    System.out.println("token is not valid");
                }
            } else {
                System.out.println("username is not valid");
            }

            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            System.out.println("JWT token cannot be verified");
            throw e;
        } catch (IllegalArgumentException e) {
            System.out.println("Unable to get JWT Token");
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}