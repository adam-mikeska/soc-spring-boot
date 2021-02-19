package com.projekt.projekt.Security;

import com.projekt.projekt.Models.User;
import com.projekt.projekt.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class UserPrincipalDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    public UserPrincipalDetailsService() {
    }

    public UserPrincipalDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        UserPrincipal userPrincipal = new UserPrincipal(user);
        if (user == null) {
            throw new UsernameNotFoundException("User account with this email address does not exist.");
        }return userPrincipal;
    }
}
