package com.example.beautysalon.service;

import com.example.beautysalon.entity.Client;
import com.example.beautysalon.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private ClientRepository clientRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        // ВИПРАВЛЕНО: Використовуємо ланцюжок Optional для пошуку за Email або Ім'ям
        Client client = clientRepository.findByEmail(login)
                .or(() -> clientRepository.findByName(login))
                .orElseThrow(() -> new UsernameNotFoundException("Користувача не знайдено з логіном: " + login));

        // Повертаємо об'єкт User, який розуміє Spring Security
        return new User(
                client.getEmail(), // Або client.getName(), залежно від того, що ти хочеш бачити в Principal
                client.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(client.getRole()))
        );
    }
}