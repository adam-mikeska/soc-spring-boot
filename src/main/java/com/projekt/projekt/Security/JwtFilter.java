package com.projekt.projekt.Security;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.projekt.projekt.Models.User;
import com.projekt.projekt.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserPrincipalDetailsService userPrincipalDetailsService;

    private int MAX_REQUESTS_PER_SECOND = 5;

    private LoadingCache<String, Integer> requestCountsPerIpAddress;

    public JwtFilter() {
        super();
        requestCountsPerIpAddress = CacheBuilder.newBuilder().
                expireAfterWrite(1, TimeUnit.SECONDS).build(new CacheLoader<String, Integer>() {
            public Integer load(String key) {
                return 0;
            }
        });
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = httpServletRequest.getHeader("token");
        String token = null;
        String email = null;
        String clientIpAddress = getClientIP(httpServletRequest);

        if (isMaximumRequestsPerSecondExceeded(clientIpAddress, httpServletRequest)) {
            httpServletResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpServletResponse.getWriter().write("Too many requests");
            return;
        }

        if (authorizationHeader != null) {
            if(!validateToken(authorizationHeader).equals("Valid")){
                httpServletResponse.setStatus(HttpStatus.FORBIDDEN.value());
                httpServletResponse.getWriter().write(validateToken(authorizationHeader));
                return;
            }
            token = authorizationHeader.substring(7);
            email = jwtUtil.extractEmail(token);
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null && jwtUtil.validateToken(token).equals("Valid") && userRepository.existsByEmail(jwtUtil.extractEmail(token))) {
            UserDetails userDetails = userPrincipalDetailsService.loadUserByUsername(email);
            if (jwtUtil.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }

        }
           filterChain.doFilter(httpServletRequest, httpServletResponse);
           return;
    }

    private boolean isMaximumRequestsPerSecondExceeded(String clientIpAddress, HttpServletRequest request) {
        int requests = 0;
        try {
            requests = requestCountsPerIpAddress.get(clientIpAddress);
            if (requests > MAX_REQUESTS_PER_SECOND) {
                requestCountsPerIpAddress.put(clientIpAddress, requests);
                return true;
            }
        } catch (ExecutionException e) {
            requests = 0;
        }
        if (!request.getMethod().equals("GET")) {
            requests++;
            requestCountsPerIpAddress.put(clientIpAddress, requests);
        }
        return false;
    }

    public String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    public String validateToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = jwtUtil.shortToken(token);
        } else {
            return "Token is not valid";
        }

        if (jwtUtil.validateToken(token).equals("Valid")) {
            User user = userRepository.findByEmail(jwtUtil.extractEmail(token));
            if (!userRepository.existsByEmail(jwtUtil.extractEmail(token))) {
                return "User account with this email address does not exist.";
            } else if (!user.getNonLocked()) {
                return "Your account is locked till:" + user.getLockedTill();
            } else {
                return "Valid";
            }
        } else {
            if (jwtUtil.validateToken(token).equals("Expired token")) {
                return "User token has expired!";
            } else {
                return "Token is not valid";
            }
        }
    }
    
}
