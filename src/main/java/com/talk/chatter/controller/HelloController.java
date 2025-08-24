package com.talk.chatter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// e.g., src/main/java/com/example/app/HelloController.java
@RestController
public class HelloController {
    @GetMapping("/") public String hello() { return "OK"; }
}

