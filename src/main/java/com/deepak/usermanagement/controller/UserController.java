package com.deepak.usermanagement.controller;

import com.deepak.usermanagement.exception.domain.EmailExistException;
import com.deepak.usermanagement.exception.domain.ExceptionHandling;
import com.deepak.usermanagement.exception.domain.UsernameExistException;
import com.deepak.usermanagement.model.User;
import com.deepak.usermanagement.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping(path = {"/","/user"})
public class UserController extends ExceptionHandling {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody final User user) throws UsernameExistException, EmailExistException {
         final User newUser = userService.register(user);
         return new ResponseEntity<>(newUser, CREATED);
    }
}
