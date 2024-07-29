package com.EticPlus_POC.controller;

import com.EticPlus_POC.models.User;
import com.EticPlus_POC.service.UserService;
import com.EticPlus_POC.utility.AuthenticationRequest;
import com.EticPlus_POC.utility.AuthenticationResponse;
import com.EticPlus_POC.utility.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            if (user.getPackageType() == null) {
                return ResponseEntity.badRequest().body("Please select a valid package type.");
            }
            User savedUser = userService.registerUser(user);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed");
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) {
        try {
            final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getStoreName());

            if (passwordEncoder.matches(authenticationRequest.getPassword(), userDetails.getPassword())) {
                final String jwt = jwtUtil.generateToken(userDetails);
                return ResponseEntity.ok(new AuthenticationResponse(jwt));
            } else {
                return ResponseEntity.badRequest().body("Invalid credentials");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Authentication failed");
        }
    }
}
