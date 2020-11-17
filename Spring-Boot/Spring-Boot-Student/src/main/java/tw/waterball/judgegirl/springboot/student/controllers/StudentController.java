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

package tw.waterball.judgegirl.springboot.student.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.bind.annotation.*;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.commons.utils.HttpHeaderUtils;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.springboot.student.api.LegacyStudentAPI;
import tw.waterball.judgegirl.commons.token.TokenInvalidException;
import tw.waterball.judgegirl.commons.token.TokenService;

import java.util.Optional;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@CrossOrigin
@RestController
@TestPropertySource(locations= "classpath:test.properties")
@RequestMapping("/api/students")
public class StudentController {
    private LegacyStudentAPI studentAPI;
    private TokenService tokenService;

    @Autowired
    public StudentController(LegacyStudentAPI studentAPI, TokenService tokenService) {
        this.studentAPI = studentAPI;
        this.tokenService = tokenService;
    }

    @PostMapping(path = "/auth")
    public ResponseEntity auth(@RequestHeader("Authorization") String authorization) {
        String tokenString = HttpHeaderUtils.parseBearerToken(authorization);
        try {
            TokenService.Token token = tokenService.parseAndValidate(tokenString);
            Optional<Student> studentOptional = studentAPI.getStudentById(token.getStudentId());
            if (studentOptional.isPresent()) {
                return ResponseEntity.ok(new LoginResponse(token.getStudentId(), studentOptional.get().getAccount(),
                        token.getToken(), token.getExpiration().getTime()));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (TokenInvalidException err) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping(path = "/login")
    public LoginResponse login(@RequestParam String account, @RequestParam String password) {
        int studentId = studentAPI.authenticate(account, password);
        TokenService.Token token = tokenService.createToken(new TokenService.Identity(studentId));
        return new LoginResponse(studentId, account, token.toString(), token.getExpiration().getTime());
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<Student> getStudentById(@PathVariable int studentId,
                                                  @RequestHeader("Authorization") String authorization) {
        String tokenString = HttpHeaderUtils.parseBearerToken(authorization);
        try {
            TokenService.Token token = tokenService.parseAndValidate(tokenString);
            if (token.getStudentId() == studentId) {
                Optional<Student> studentOptional = studentAPI.getStudentById(studentId);
                return studentOptional.map(ResponseEntity::ok)
                        .orElseGet(() -> ResponseEntity.notFound().build());
            }
        } catch (TokenInvalidException err) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        throw new RuntimeException("Should not reach here.");
    }

    // TODO issue: cannot reuse spring-commons/CommonExceptionAdvices
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({NotFoundException.class})
    public void handleExceptions() {
        // Nothing to do
    }

}
