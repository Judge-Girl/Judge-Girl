/*
 *  Copyright 2020 Johnny850807 (Waterball) 潘冠辰
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package tw.waterball.judgegirl.springboot.token.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tw.waterball.judgegirl.springboot.profiles.productions.Jwt;
import tw.waterball.judgegirl.springboot.token.TokenInvalidException;
import tw.waterball.judgegirl.springboot.token.TokenService;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import static tw.waterball.judgegirl.commons.utils.Dates.NEVER_EXPIRED;

/**
 * @author johnny850807@gmail.com (Waterball))
 */
@Jwt
@Service
public class JwtTokenService implements TokenService {
    private final SecretKey key;
    private final Date expiration;
    private Queue queue = new LinkedList();

    public JwtTokenService(@Value("${jwt.secret}") String secret,
                           @Value("${jwt.exp}") long expiration) {
        key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiration = new Date(expiration);
    }

    @Override
    public Token createToken(Identity identity) {
        Date expirationDate;
        if (identity.isAdmin()) {
            return Token.ofAdmin(compactTokenString(NEVER_EXPIRED, identity));
        }
        expirationDate = new Date((System.currentTimeMillis() + this.expiration.getTime()));
        return Token.ofStudent(identity.getStudentId(), compactTokenString(expirationDate, identity), expirationDate);
    }

    @Override
    public Token renewToken(String token) throws TokenInvalidException {
        return createToken(parseAndValidate(token));
    }

    @Override
    public Token parseAndValidate(String token) throws TokenInvalidException {
        Jws<Claims> jwt;
        try {
            jwt = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build().parseClaimsJws(token);
        } catch (JwtException err) {
            throw new TokenInvalidException(err);
        }

        io.jsonwebtoken.Claims claims = (io.jsonwebtoken.Claims) jwt.getBody();
        if (claims.containsKey(Identity.KEY_IS_ADMIN)) {
            if ((boolean) claims.get(Identity.KEY_IS_ADMIN)) {
                return Token.ofAdmin(compactTokenString(claims.getExpiration(), Identity.admin()));
            }
        }
        Identity identity = new Identity((int) claims.get(Identity.KEY_STUDENT_ID));
        return Token.ofStudent(identity.getStudentId(),
                compactTokenString(claims.getExpiration(), identity), claims.getExpiration());
    }

    private String compactTokenString(Date expirationDate, Identity identity) {
        return Jwts.builder()
                .setClaims(identity.getClaimMap())
                .setExpiration(expirationDate)
                .signWith(key).compact();
    }
}