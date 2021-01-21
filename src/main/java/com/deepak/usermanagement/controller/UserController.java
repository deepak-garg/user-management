package com.deepak.usermanagement.controller;

import com.deepak.usermanagement.exception.domain.EmailExistException;
import com.deepak.usermanagement.exception.domain.ExceptionHandling;
import com.deepak.usermanagement.exception.domain.UsernameExistException;
import com.deepak.usermanagement.model.User;
import com.deepak.usermanagement.model.UserPrincipal;
import com.deepak.usermanagement.service.UserService;
import com.deepak.usermanagement.utility.JWTTokenProvider;
import com.deepak.usermanagement.utils.GlobalConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(path = {"/","/user"})
public class UserController extends ExceptionHandling {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JWTTokenProvider jwtTokenProvider;

    public UserController(UserService userService,
                          AuthenticationManager authenticationManager,
                          JWTTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody final User user) throws UsernameExistException, EmailExistException {
         final User newUser = userService.register(user);
         return new ResponseEntity<>(CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody final User user) {
        authenticate(user.getUserName(), user.getPassword());
        UserPrincipal userPrincipal = (UserPrincipal)userService.loadUserByUsername(user.getUserName());
        HttpHeaders jwtHeader = getJwtHeaders(userPrincipal);
        return new ResponseEntity<>(jwtHeader, OK);
    }

    private HttpHeaders getJwtHeaders(UserPrincipal userPrincipal) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(GlobalConstants.JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(userPrincipal));
        return httpHeaders;
    }

    private void authenticate(String userName, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, password));
    }
}
