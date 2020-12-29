package com.deepak.usermanagement.utility;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.deepak.usermanagement.model.UserPrincipal;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.deepak.usermanagement.utils.GlobalConstants.AUTHORITIES;
import static com.deepak.usermanagement.utils.GlobalConstants.COMPANY_NAME;
import static com.deepak.usermanagement.utils.GlobalConstants.TOKEN_CANNOT_BE_VERIFIED;
import static com.deepak.usermanagement.utils.GlobalConstants.TOKEN_EXPIRATION_TIME;

@Component
public class JWTTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    public String generateJwtToken(UserPrincipal userPrincipal) {
        String[] claims = getClaimsFromUser(userPrincipal);
        return JWT.create()
                .withIssuer(COMPANY_NAME)
                .withIssuedAt(new Date())
                .withSubject(userPrincipal.getUsername())
                .withArrayClaim(AUTHORITIES, claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(secret.getBytes()));

    }

    public List<GrantedAuthority> getAuthorities(String token) {
        String[] claims = getClaimsFromToken(token);
        return Arrays.stream(claims)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public Authentication getAuthentication(String userName,
                                            List<GrantedAuthority> authorities,
                                            HttpServletRequest request) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new
                UsernamePasswordAuthenticationToken(userName, null, authorities);
        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return usernamePasswordAuthenticationToken;
    }

    public boolean isTokenValid(String userName, String token) {
        JWTVerifier jwtVerifier = getJWTVerifier();
        return StringUtils.isNotEmpty(userName) && !isTokenExpired(jwtVerifier, token);
    }

    public String getSubject(String token) {
        return getJWTVerifier().verify(token).getSubject();
    }

    private String[] getClaimsFromUser(UserPrincipal userPrincipal) {
        List<String> authorities = new ArrayList<>();
        for(GrantedAuthority grantedAuthority : userPrincipal.getAuthorities()) {
            authorities.add(grantedAuthority.getAuthority());
        }
        return authorities.toArray(new String[0]);
    }

    private String[] getClaimsFromToken(String token) {
        JWTVerifier jwtVerifier = getJWTVerifier();
        return jwtVerifier
                .verify(token)
                .getClaim(AUTHORITIES)
                .asArray(String.class);
    }

    private JWTVerifier getJWTVerifier() {
        JWTVerifier jwtVerifier;
        try{
            Algorithm algorithm = Algorithm.HMAC512(secret);
            jwtVerifier = JWT.require(algorithm).withIssuer(COMPANY_NAME).build();
        }catch (JWTVerificationException ex) {
            throw new JWTVerificationException(TOKEN_CANNOT_BE_VERIFIED);
        }
        return jwtVerifier;
    }

    private boolean isTokenExpired(JWTVerifier jwtVerifier, String token) {
        Date expirationDate = jwtVerifier.verify(token).getExpiresAt();
        return expirationDate.before(new Date());
    }
}
