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

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static tw.waterball.judgegirl.commons.utils.Dates.NEVER_EXPIRED_IN_LIFETIME_DATE;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public interface TokenService {
    Token createToken(Identity identity);

    Token renewToken(String token) throws TokenInvalidException;

    Token parseAndValidate(String token) throws TokenInvalidException;
    
    @Getter
    @Setter
    class Token extends TokenService.Identity {
        private String token;
        private Date expiration;

        private Token(String token, Date expiration) {
            super(true);
            this.token = token;
            this.expiration = expiration;
        }

        private Token(int studentId, String token, Date expiration) {
            super(studentId);
            this.token = token;
            this.expiration = expiration;
        }

        public static Token ofStudent(int studentId, String token, Date expiration) {
            return new Token(studentId, token, expiration);
        }

        public static Token ofAdmin(String token) {
            return new Token(token, NEVER_EXPIRED_IN_LIFETIME_DATE);
        }

        @Override
        public String toString() {
            return token;
        }
    }

    class Identity {
        public final static String KEY_STUDENT_ID = "studentId";
        public final static String KEY_IS_ADMIN = "isAdmin";
        private int studentId = Integer.MIN_VALUE;
        private boolean isAdmin;

        public Identity(int studentId) {
            this.studentId = studentId;
        }

        private Identity(boolean isAdmin) {
            this.isAdmin = isAdmin;
        }

        public static Identity admin() {
            return new Identity(true);
        }

        public Map<String, Object> getClaimMap() {
            return isAdmin() ?
                    singletonMap(KEY_IS_ADMIN, true) :
                    singletonMap(KEY_STUDENT_ID, studentId);
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
