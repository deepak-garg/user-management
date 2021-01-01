package com.deepak.usermanagement.controller;

import com.deepak.usermanagement.exception.domain.ExceptionHandling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/user")
public class UserController extends ExceptionHandling {

    @GetMapping("/home")
    public String showUser() {
         return "application works";
    }
}
