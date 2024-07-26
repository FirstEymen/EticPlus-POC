package com.EticPlus_POC.service;

import com.EticPlus_POC.models.User;
import com.EticPlus_POC.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String storeName) throws UsernameNotFoundException {
        User user = userRepository.findByStoreName(storeName)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with store name: " + storeName));

        return new org.springframework.security.core.userdetails.User(user.getStoreName(), user.getPassword(),
                new ArrayList<>());
    }
}
