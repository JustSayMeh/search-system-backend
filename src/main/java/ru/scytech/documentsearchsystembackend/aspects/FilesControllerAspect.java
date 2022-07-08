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
import ru.scytech.documentsearchsystembackend.security.DomainAccessManager;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Aspect
@Profile("secure")
@Component
public class FilesControllerAspect {
    private DomainAccessManager domainAccessManager;

    public FilesControllerAspect(DomainAccessManager domainAccessManager) {
        this.domainAccessManager = domainAccessManager;
    }

    @Around("execution(* ru.scytech.documentsearchsystembackend.controllers.FilesController.checkDomainPermissionsBefore*(..))")
    public Object checkDomainPermissions(ProceedingJoinPoint joinPoint) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var userAuths = authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        Object[] args = joinPoint.getArgs();
        String domain = args[0].toString();
        var domainAuthorities = domainAccessManager.getDomainAuthorities(domain);
        if (domainAuthorities.isEmpty()) {
            return joinPoint.proceed();
        }
        domainAuthorities.retainAll(userAuths);
        if (domainAuthorities.isEmpty())
            return ResponseEntity.status(403).build();
        return joinPoint.proceed();
    }

    @Around("execution(* ru.scytech.documentsearchsystembackend.controllers.FilesController.checkAdminPermissionsBefore*(..))")
    public Object checkAdminPermissions(ProceedingJoinPoint joinPoint) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var userAuths = authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        if (domainAccessManager.isAdmin(userAuths)) {
            return joinPoint.proceed();
        }
        return ResponseEntity.status(403).build();
    }

    @Around("execution(java.util.List<String> ru.scytech.documentsearchsystembackend.controllers.FilesController.checkDomainPermissionsAfter*(..))")
    public Object checkDomainPermissionsAfterReturnList(ProceedingJoinPoint joinPoint) throws Throwable {
        var result = (List<String>) joinPoint.proceed();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var userAuths = authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        return result
                .stream()
                .filter(it -> {
                    var domainAuthorities = domainAccessManager.getDomainAuthorities(it);
                    if (domainAuthorities.isEmpty())
                        return true;
                    domainAuthorities.retainAll(userAuths);
                    return !domainAuthorities.isEmpty();
                }).collect(Collectors.toList());
    }

    @Around("execution(java.util.Map<String, java.util.Set<String>> ru.scytech.documentsearchsystembackend.controllers.FilesController.checkDomainPermissionsAfter*(..))")
    public Object checkDomainPermissionsAfterReturnMap(ProceedingJoinPoint joinPoint) throws Throwable {
        var result = (Map<String, Set<String>>) joinPoint.proceed();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var userAuths = authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        return result.entrySet()
                .stream()
                .filter(it -> {
                    var domainAuthorities = domainAccessManager.getDomainAuthorities(it.getKey());
                    if (domainAuthorities.isEmpty())
                        return true;
                    domainAuthorities.retainAll(userAuths);
                    return !domainAuthorities.isEmpty();
                }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
