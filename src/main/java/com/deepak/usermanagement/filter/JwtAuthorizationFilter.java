package com.deepak.usermanagement.filter;

import com.deepak.usermanagement.utility.JWTTokenProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static com.deepak.usermanagement.utils.GlobalConstants.OPTIONS_HTTP_METHOD;
import static com.deepak.usermanagement.utils.GlobalConstants.TOKEN_PREFIX;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private JWTTokenProvider jwtTokenProvider;

    public JwtAuthorizationFilter(JWTTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {

        if(httpServletRequest.getMethod().equalsIgnoreCase(OPTIONS_HTTP_METHOD)) {
            httpServletResponse.setStatus(HttpStatus.OK.value());
        } else {
            String authorizationHeader = httpServletRequest.getHeader(AUTHORIZATION);
            if(StringUtils.isEmpty(authorizationHeader) || !authorizationHeader.startsWith(TOKEN_PREFIX)) {
                filterChain.doFilter(httpServletRequest, httpServletResponse);
                return;
            }
            String token = authorizationHeader.substring(TOKEN_PREFIX.length());
            String userName = jwtTokenProvider.getSubject(token);
            if(jwtTokenProvider.isTokenValid(userName, token)) {
                List<GrantedAuthority> authorities = jwtTokenProvider.getAuthorities(token);
                Authentication authentication = jwtTokenProvider.getAuthentication(userName, authorities, httpServletRequest);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }else {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
