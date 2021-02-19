package com.projekt.projekt.Security;

import io.jsonwebtoken.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtil {
    private String secret = "joachim";

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    ///    Remove bearer from header to validate token    ///

    public String shortToken(String token) {
        if(token!=null) {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
        }
        return token;
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {

        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(SignatureAlgorithm.HS256, secret).compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }



    public String validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return "Valid";
        } catch (SignatureException ex) {
            return "Invalid token Signature";
        } catch (MalformedJwtException ex) {
            return "Invalid token";
        } catch (ExpiredJwtException ex) {
            return "Expired token";
        } catch (UnsupportedJwtException ex) {
            return "Unsupported token exception";
        } catch (IllegalArgumentException ex) {
            return "Stop modifying token!";
        } catch (ArrayIndexOutOfBoundsException ex) {
            return "Stop modifying token!";
        }
    }
}
