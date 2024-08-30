package com.example.tasker.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.tasker.repository.SystemUserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    protected static final String USER_DETAILS_BY_USERNAME_CACHE = "user_details_by_username";

    private final SystemUserRepository systemUserRepository;

    public UserDetailsServiceImpl(SystemUserRepository systemUserRepository) {
        this.systemUserRepository = systemUserRepository;
    }

    @Override
    @Cacheable(key = "#username", value = USER_DETAILS_BY_USERNAME_CACHE)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return systemUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
