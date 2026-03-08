package com.example.beautysalon.service;

import com.example.beautysalon.entity.Client;
import com.example.beautysalon.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private ClientRepository clientRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Client client = clientRepository.findByEmail(email);
        if (client == null) {
            throw new UsernameNotFoundException("Користувача не знайдено");
        }

        return User.builder()
                .username(client.getName()) // Тепер Spring Security вважає "іменем" поле Name
                .password(client.getPassword())
                .roles("USER")
                .build();
    }
}