package com.example.fincredex.service.impl;


import com.example.fincredex.model.dto.UserDTO;
import com.example.fincredex.model.entities.User;
import com.example.fincredex.model.response.IamResponse;
import com.example.fincredex.repository.UserRepository;
import com.example.fincredex.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService,UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRole().name())
                .build();
    }

    @Override
    public IamResponse<UserDTO> getById(Long userId) {
        return null;
    }
}