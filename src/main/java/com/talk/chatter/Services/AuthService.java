package com.talk.chatter.Services;

import com.talk.chatter.DTO.UserLoginDTO;
import com.talk.chatter.DTO.UserSignupDTO;
import com.talk.chatter.Entities.Users;
import com.talk.chatter.Repository.UserRepository;
import com.talk.chatter.Utils.BcryptEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private BCryptPasswordEncoder bcryptEncoder;
    @Autowired private JwtService jwtService;
    @Autowired private AuthenticationManager authManager;

    public String signUp(UserSignupDTO userSignupDTO){
        //data validation check will be done here
        if(userRepository.existsByEmail(userSignupDTO.getEmail())){
            return "Error:Email is already registered.";
        }
        Users user = new Users();
        user.setFirstName(userSignupDTO.getFirstName());
        user.setLastName(userSignupDTO.getLastName());
        user.setEmail(userSignupDTO.getEmail());
        user.setUserName(userSignupDTO.getUserName());
        user.setPassword(bcryptEncoder.encode(userSignupDTO.getPassword()));
        userRepository.save(user);
        return jwtService.generateToken(user.getEmail());
    }

    public String login(UserLoginDTO userLoginDTO){
        try{
            Authentication authentication = authManager
                    .authenticate(new UsernamePasswordAuthenticationToken(userLoginDTO.getEmail(), userLoginDTO.getPassword()));
            if(authentication.isAuthenticated()){
                return jwtService.generateToken(userLoginDTO.getEmail());
            }
        }catch(UsernameNotFoundException e){
            return "Error : User does not exist!";
        }catch(BadCredentialsException e){
            return "Error : Wrong Credentials!";
        }catch(Exception e){
            return "Error : Authentication failed - " + e.getMessage();
        }
        return "Error : Unknown Error occured. Login Failed.";
    }

}
