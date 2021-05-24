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

package tw.waterball.judgegirl.commons.token;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import tw.waterball.judgegirl.commons.exceptions.ForbiddenAccessException;
import tw.waterball.judgegirl.commons.utils.HttpHeaderUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.String.format;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public interface TokenService {
    Token createToken(Identity identity);

    Token renewToken(String token) throws TokenInvalidException;

    Token parseAndValidate(@Nullable String token) throws TokenInvalidException;

    default <T> T returnIfAdmin(String authorization, Function<Token, T> tokenFunction) {
        TokenService.Token token = parseBearerTokenAndValidate(authorization);
        if (token.isAdmin()) {
            return tokenFunction.apply(token);
        } else {
            throw new ForbiddenAccessException(format("Student(id=%d) cannot access privileged resources.", token.getStudentId()));
        }
    }

    default void ifAdminToken(String authorization, Consumer<Token> tokenConsumer) {
        TokenService.Token token = parseBearerTokenAndValidate(authorization);
        if (token.isAdmin()) {
            tokenConsumer.accept(token);
        } else {
            throw new TokenInvalidException("Admin-Only.");
        }
    }

    default <T> T returnIfGranted(int ownerStudentId, String authorization, Function<Token, T> tokenFunction) {
        TokenService.Token token = parseBearerTokenAndValidate(authorization);
        if (token.canAccessStudent(ownerStudentId)) {
            return tokenFunction.apply(token);
        } else {
            throw new ForbiddenAccessException(format("Student(id=%d) cannot access resources owned by student(id=%d)",
                    token.getStudentId(), ownerStudentId));
        }
    }

    default void ifTokenValid(int studentId, String authorization, Consumer<Token> tokenConsumer) {
        TokenService.Token token = parseBearerTokenAndValidate(authorization);
        if (token.canAccessStudent(studentId)) {
            tokenConsumer.accept(token);
        } else {
            throw new TokenInvalidException("Authentication failed.");
        }
    }

    default TokenService.Token parseBearerTokenAndValidate(String authorization) {
        String tokenString = HttpHeaderUtils.parseBearerToken(authorization);
        return parseAndValidate(tokenString);
    }

    @Getter
    @Setter
    class Token extends TokenService.Identity {
        @Nullable
        private String token;
        @Nullable
        private Date expiration;

        public Token(boolean isAdmin, int studentId, @Nullable String token, @Nullable Date expiration) {
            super(isAdmin, studentId);
            this.token = token;
            this.expiration = expiration;
        }

        public static Token ofStudent(int studentId, String token, Date expiration) {
            return new Token(false, studentId, token, expiration);
        }

        public static Token ofAdmin(int studentId, String token, Date expiration) {
            return new Token(true, studentId, token, expiration);
        }

        public static Token ofGuest() {
            return new Token(false, Identity.GUEST_STUDENT_ID, null, null);
        }

        @Nullable
        @Override
        public String toString() {
            return token;
        }
    }

    class Identity {
        public static final String KEY_STUDENT_ID = "studentId";
        public static final String KEY_IS_ADMIN = "isAdmin";
        private static final int GUEST_STUDENT_ID = Integer.MIN_VALUE;
        private final int studentId;
        private final boolean isAdmin;

        public Identity(boolean isAdmin, int studentId) {
            this.isAdmin = isAdmin;
            this.studentId = studentId;
        }

        public static Identity student(int studentId) {
            return new Identity(false, studentId);
        }

        public static Identity admin(int studentId) {
            return new Identity(true, studentId);
        }

        public static Identity guest() {
            return new Identity(false, GUEST_STUDENT_ID);
        }

        public Map<String, Object> getClaimMap() {
            Map<String, Object> claim = new HashMap<>();
            claim.put(KEY_IS_ADMIN, isAdmin);
            if (studentId != GUEST_STUDENT_ID) {
                claim.put(KEY_STUDENT_ID, studentId);
            }
            return claim;
        }

        public boolean isAdmin() {
            return isAdmin;
        }

        public int getStudentId() {
            return studentId;
        }

        /**
         * @return given a student's id, return whether this identity can access resources of student's
         */
        public boolean canAccessStudent(int studentId) {
            return isAdmin || this.studentId == studentId;
        }
    }
}
