package com.talk.chatter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String all(){
        return ("<h1>Hey Fella!</h1>");
    }
    @GetMapping("/user")
    public String user(){
        //return ("<h1>Hey User!</h1>");
        return "User Remarks - 80";
    }
    @GetMapping("/admin")
    public String admin(){
        //return ("<h1>Hey Admin!</h1>");
        return "Admin Remarks - 100";
    }
}
