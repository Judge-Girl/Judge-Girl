/*
 * Copyright 2020 Johnny850807 (Waterball) 潘冠辰
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package tw.waterball.judgegirl.springboot.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tw.waterball.judgegirl.commons.token.jwt.JwtTokenService;
import tw.waterball.judgegirl.springboot.profiles.productions.Jwt;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Jwt
@Configuration
public class JwtConfiguration {

    @Bean
    public JwtTokenService jwtTokenService(@Value("${jwt.secret}") String secret,
                                           @Value("${jwt.exp}") long expiration) {
        return new JwtTokenService(secret, expiration);
    }
}
