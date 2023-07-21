package com.demo.jwtdemo.controller;

import com.demo.jwtdemo.dto.PasswordDTO;
import com.demo.jwtdemo.dto.UserDTO;
import com.demo.jwtdemo.model.User;
import com.demo.jwtdemo.repository.UserRepository;
import com.demo.jwtdemo.util.JWTUtil;
import com.mongodb.MongoWriteException;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final JWTUtil jwtUtil;

    public UserController(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, JWTUtil jwtUtil) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody UserDTO userDTO) {
        String encryptedPassword = bCryptPasswordEncoder.encode(userDTO.getPassword());
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(encryptedPassword);
        try {
            userRepository.save(user);
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserDTO userDTO) {
        User user = userRepository.findByUsername(userDTO.getUsername());
        if (user != null && bCryptPasswordEncoder.matches(userDTO.getPassword(), user.getPassword())) {
            String token = generateToken(user.getUsername());
            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PutMapping("/password")
    public ResponseEntity<String> editPassword(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody PasswordDTO passwordDTO
    ) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            Claims claims = jwtUtil.parseToken(token);
            String userId = claims.getId();
            String username = claims.getSubject();
            User user = userRepository.findById(userId).orElse(null);
            if(user != null && user.getUsername().equals(username) && bCryptPasswordEncoder.matches(passwordDTO.getCurrentPassword(), user.getPassword())) {
                String encryptedPassword = bCryptPasswordEncoder.encode(passwordDTO.getNewPassword());
                user.setPassword(encryptedPassword);
                userRepository.save(user);
                return ResponseEntity.ok("Password updated successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access.");
            }
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token.");
        }
    }

    private String generateToken(String username) {
        // Generate JWT token
        return jwtUtil.generateToken(userRepository.findByUsername(username).getId(),username);
    }
}
