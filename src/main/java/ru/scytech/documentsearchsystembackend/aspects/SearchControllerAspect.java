package ru.scytech.documentsearchsystembackend.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.scytech.documentsearchsystembackend.model.results.SearchResult;
import ru.scytech.documentsearchsystembackend.security.DomainAccessManager;

import java.util.List;
import java.util.stream.Collectors;

@Aspect
@Profile("secure")
@Component
public class SearchControllerAspect {
    private DomainAccessManager domainAccessManager;

    public SearchControllerAspect(DomainAccessManager domainAccessManager) {
        this.domainAccessManager = domainAccessManager;
    }

    @Around("execution(* ru.scytech.documentsearchsystembackend.controllers.SearchController.searchPhrase(..))")
    public Object checkDomainPermissions(ProceedingJoinPoint joinPoint) throws Throwable {
        var result = (ResponseEntity<List<? extends SearchResult>>) joinPoint.proceed();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var userAuths = authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        var filteredResultBody = result.getBody().stream().filter(it -> {
            var domain = it.getDomain();
            var domainAuthorities = domainAccessManager.getDomainAuthorities(domain);
            if (domainAuthorities.isEmpty())
                return true;
            domainAuthorities.retainAll(userAuths);
            return !domainAuthorities.isEmpty();
        }).collect(Collectors.toList());
        return ResponseEntity.ok(filteredResultBody);
    }
}
