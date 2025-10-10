package org.piet.forumbackend.users;

import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.users.dtos.RegisterUserDto;
import org.piet.forumbackend.users.entities.User;
import org.piet.forumbackend.users.repos.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getUserByUsername(String username){
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Username " + username + " not found!"));
    }

    public User registerUser(RegisterUserDto dto){
        User u = new User();
        u.setEmail(dto.getEmail());
        u.setName(dto.getName());
        u.setSurname(dto.getSurname());
        u.setPassword(passwordEncoder.encode(dto.getPassword()));
        u.setUsername(dto.getUsername());

        return userRepository.save(u);
    }
}
