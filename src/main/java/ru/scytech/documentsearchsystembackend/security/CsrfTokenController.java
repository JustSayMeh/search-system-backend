package ru.scytech.documentsearchsystembackend.security;

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/csrfToken")
@Profile("secure")
public class CsrfTokenController {
    @GetMapping("/")
    public CsrfToken get(HttpServletRequest request, SecurityProperties.User user) {
        return (CsrfToken) request.getAttribute(CsrfToken.class.getName());

    }

    @GetMapping("/role")
    public List<String> getUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream().map(it -> it.getAuthority()).collect(Collectors.toList());
    }
}
