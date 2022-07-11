package ru.scytech.documentsearchsystembackend.security;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.scytech.documentsearchsystembackend.model.SystemUser;
import ru.scytech.documentsearchsystembackend.services.SecurityRepository;

import java.util.Optional;

@Service
@Profile("secure")
public class FileAuthenticationProvider implements UserDetailsService {
    private SecurityRepository securityRepository;

    public FileAuthenticationProvider(SecurityRepository securityRepository) {
        this.securityRepository = securityRepository;

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<SystemUser> userOptional = securityRepository.getUserByName(username);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found!");
        }
        SystemUser user = userOptional.get();
        String[] roles = user.getAuthorities().stream().toArray(String[]::new);
        return User.withUsername(username).password(user.getPassword()).authorities(roles).build();
    }
}
