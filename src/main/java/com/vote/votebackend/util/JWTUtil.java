package com.vote.votebackend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JWTUtil {
    private static final SecretKey secretKey;
    private static final Long accessTokenExpiresIn;
    private static final Long refreshTokenExpiresIn;

    static {
        String secretKeyString ="pleasesubscribemyyoutubechanelForJeonjaeman";   //배포시 변경요망
        secretKey = new SecretKeySpec(secretKeyString.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        accessTokenExpiresIn = 3600L * 1000;
        refreshTokenExpiresIn = 604800L * 1000;
    }

    //JWT username, role 파싱

    public static String getUsername(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("sub", String.class);
    }

    public static String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    //JWT 유효검증
    public static Boolean isValid(String token, Boolean isAccessToken){
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String type = claims.get("type", String.class);
            if (type==null) return false;

            if(isAccessToken && !type.equals("access")) return false;
            if(!isAccessToken && !type.equals("refresh")) return false;

            return true;
        }
        catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }


    //JWT 생성 (Access/Refresh)
    public static String createJWT(String username, String role, Boolean isAccess){

        Long now = System.currentTimeMillis();
        Long expired = isAccess? accessTokenExpiresIn:refreshTokenExpiresIn;
        String type = isAccess?"access":"refresh";

        return Jwts.builder()
                .claim("sub",username)
                .claim("role",role)
                .claim("type",type)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expired))
                .signWith(secretKey)
                .compact();

    }
}
