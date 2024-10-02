package app.backend;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtExample {
    static SecretKey secretKey = getSigningKey();
    private static final long validityInMilliseconds = 3600_000; // 1h

    public static String createToken(String username, String role, Integer id) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);


        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey)
                .claim("role", role)
                .claim("id", id)
                .compact();
    }

    static SecretKey getSigningKey() {
        return Jwts.SIG.HS256.key().build();
    }

    public static boolean validateToken(String token) {
        try {
            if (token == null || token.equals("null"))
                return true;

            JwtParser parser = Jwts.parser()
                    .verifyWith(secretKey)
                    .build();

            Claims claims = parser.parseSignedClaims(token).getPayload();
            return !claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            return false;
        }
    }


    public static String getUserRole(String token) {
        try {
            JwtParser parser = Jwts.parser()
                    .verifyWith(secretKey)
                    .build();
            Claims claims = parser.parseSignedClaims(token).getPayload();

            return claims.get("role", String.class); // Извлекаем роль пользователя из токена
        } catch (JwtException e) {
            return null;
        }
    }

    public static Integer getUserID(String token) {
        try {
            JwtParser parser = Jwts.parser()
                    .verifyWith(secretKey)
                    .build();
            Claims claims = parser.parseSignedClaims(token).getPayload();

            return claims.get("id", Integer.class); // Извлекаем роль пользователя из токена
        } catch (JwtException e) {
            return null;
        }
    }

}
