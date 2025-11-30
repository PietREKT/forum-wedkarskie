package org.piet.forumbackend.users.core.services;

import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.security.SecurityUserDto;
import org.piet.forumbackend.users.core.dtos.RegisterUserDto;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.piet.forumbackend.users.core.repos.UserRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;


    @Override
    public User getCurrentUser() throws UserNotLoggedInException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return getUserFromAuth(authentication);
    }

    public User getUserByUsername(String username) throws NotFoundException {
        return userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException(
                messageSource.getMessage("error.users.username_not_found",
                        new Object[]{username},
                        LocaleContextHolder.getLocale()
                )
        ));
    }

    @Override
    public Optional<User> getUserByUsernameOpt(String username){
        return userRepository.findByUsername(username);
    }

    @Override
    public User getUserById(UUID id) throws NotFoundException {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException(
                messageSource.getMessage("error.users.id_not_found",
                        new Object[]{id},
                        LocaleContextHolder.getLocale()
                )
        ));
    }

    @Override
    public Optional<User> getUserByIdOpt(UUID id) {
        return userRepository.findById(id);
    }

    public User registerUser(RegisterUserDto dto) {
        User u = new User();
        u.setEmail(dto.getEmail());
        u.setName(dto.getName());
        u.setSurname(dto.getSurname());
        u.setPassword(passwordEncoder.encode(dto.getPassword()));
        u.setUsername(dto.getUsername());

        return userRepository.save(u);
    }

    public User getUserFromAuth(Authentication auth) throws UserNotLoggedInException {
        if (auth == null || !(auth.getPrincipal() instanceof SecurityUserDto su)) {
            throw new UserNotLoggedInException(
                    messageSource.getMessage("error.users.not_logged_in", null, LocaleContextHolder.getLocale())
            );
        }
        return userRepository.findById(su.getId()).orElseThrow(() -> new UsernameNotFoundException(
                messageSource.getMessage("error.users.id_not_found",
                        new Object[]{su.getId()},
                        LocaleContextHolder.getLocale()
                )
        ));
    }

    public User getUserFromAuthOrNull(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof SecurityUserDto su)) {
            return null;
        }
        return userRepository.findById(su.getId()).orElse(null);
    }
}
