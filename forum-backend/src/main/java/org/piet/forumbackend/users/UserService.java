package org.piet.forumbackend.users;

import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.security.SecurityUserDto;
import org.piet.forumbackend.users.dtos.RegisterUserDto;
import org.piet.forumbackend.users.entities.User;
import org.piet.forumbackend.users.exceptions.UserNotLoggedInException;
import org.piet.forumbackend.users.repos.UserRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    public User getUserByUsername(String username){
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(
                messageSource.getMessage("error.users.username_not_found",
                        new Object[]{username},
                        LocaleContextHolder.getLocale()
                )
        ));
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

    public User getUserFromAuth(Authentication auth) throws UserNotLoggedInException {
        if (auth == null || !(auth.getPrincipal() instanceof SecurityUserDto su)){
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

    public User getUserFromAuthOrNull(Authentication auth){
        if (auth == null || !(auth.getPrincipal() instanceof SecurityUserDto su)){
            return null;
        }
        return userRepository.findById(su.getId()).orElse(null);
    }
}
