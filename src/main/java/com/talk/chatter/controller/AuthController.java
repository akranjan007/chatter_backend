package com.talk.chatter.controller;

import com.talk.chatter.DTO.UserLoginDTO;
import com.talk.chatter.DTO.UserSignupDTO;
import com.talk.chatter.Entities.Users;
import com.talk.chatter.Repository.UserRepository;
import com.talk.chatter.Services.AuthService;
import com.talk.chatter.Services.JwtService;
import com.talk.chatter.Utils.MyUserDetails;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashMap;
import java.util.Map;


@RestController
@CrossOrigin
@RequestMapping("/auth")
public class AuthController {

    @Autowired private AuthService authService;
    @Autowired private UserRepository userRepo;
    @Autowired private JwtService jwtService;
    @Autowired private MyUserDetails myUserDetails;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody UserLoginDTO userLoginDTO){
        Map<String, Object> response = new HashMap<>();
        String token = authService.login(userLoginDTO);
        Users user = userRepo.findByEmail(userLoginDTO.getEmail()).get();
        if(token!=null && !token.startsWith("Error")){
            response.put("success", true);
            response.put("token", token);
            response.put("user", userLoginDTO.getEmail());
            response.put("userObj", user);
            return ResponseEntity.ok(response);
        }
        response.put("success",false);
        response.put("message", token);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> signUp(@RequestBody UserSignupDTO userSignupDTO){
        String token = authService.signUp(userSignupDTO);
        Map<String, Object> response = new HashMap<>();
        try{
            if(token!=null && !token.startsWith("Error:Email")){
                response.put("success", true);
                response.put("message", "Signup Successful!");
                response.put("token", token);
            }else{
                response.put("success", false);
                response.put("message", "Email already exists.");
            }
            return ResponseEntity.ok(response);
        }catch (Exception e){
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(){
        Map<String, Object> response = new HashMap<>();
        try{
            SecurityContextHolder.clearContext(); //Also clear localstorage from client device (frontend)
            response.put("success", true);
            response.put("message", "Logout Successful!");
            return ResponseEntity.ok(response);
        }catch(Exception e){
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/token-check")
    public ResponseEntity<?> checkToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7); // remove "Bearer "

        try {
            String userName = jwtService.extractUserName(token);
            UserDetails userDetails = myUserDetails.loadUserByUsername(userName);

            if (jwtService.validateToken(token, userDetails)) {
                return ResponseEntity.ok(Map.of("message", "Token is valid"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }

        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token expired");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalid or malformed");
        }
    }
}
