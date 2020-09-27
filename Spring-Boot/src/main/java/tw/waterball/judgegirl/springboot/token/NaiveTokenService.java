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

package tw.waterball.judgegirl.springboot.token;

import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.springboot.profiles.Dev;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Dev
@Component
public class NaiveTokenService implements TokenService {
    private Date exp = new Date(TimeUnit.HOURS.toMillis(4));

    @Override
    public Token createToken(Identity identity) {
        if (identity.isAdmin()) {
            return Token.ofAdmin("admin");
        }
        Date expirationTime = new Date(System.currentTimeMillis() + this.exp.getTime());
        return Token.ofStudent(identity.getStudentId(),
                String.format("%d:%d", identity.getStudentId(), expirationTime.getTime()),
                expirationTime);
    }

    @Override
    public Token renewToken(String token) throws TokenInvalidException {
        return createToken(parseAndValidate(token));
    }

    @Override
    public Token parseAndValidate(String token) throws TokenInvalidException {
        try {
            String[] splits = token.split(",");
            if (splits[0].equals("admin")) {
                return Token.ofAdmin("admin");
            }
            long expirationTime = Long.parseLong(splits[1]);
            validateExpiration(expirationTime);
            int studentId = Integer.parseInt(splits[0]);
            return Token.ofStudent(studentId, token, new Date(expirationTime));
        } catch (ArrayIndexOutOfBoundsException err) {
            throw new TokenInvalidException("Token invalid.", err);
        }
    }


    private void validateExpiration(long expirationTime) {
        if (System.currentTimeMillis() >= expirationTime) {
            throw new TokenInvalidException("The token is expired.");
        }
    }
}
