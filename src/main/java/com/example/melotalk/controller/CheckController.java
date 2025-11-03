package com.example.melotalk.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CheckController {

    @GetMapping("/check")
    public String check(Authentication authentication) {
        System.out.println("Principal class: " + authentication.getPrincipal().getClass());
        System.out.println("Authentication name: " + authentication.getName());
        return "ok";
    }
}
