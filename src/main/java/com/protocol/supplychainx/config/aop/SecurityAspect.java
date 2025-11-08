package com.protocol.supplychainx.config.aop;

import com.protocol.supplychainx.common.enums.RoleUtilisateur;
import com.protocol.supplychainx.common.exceptions.UnauthorizedException;
import com.protocol.supplychainx.user.entity.User;
import com.protocol.supplychainx.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Base64;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityAspect {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Around("@annotation(com.protocol.supplychainx.config.aop.SecuredEndpoint)")
    public Object checkAuthentication(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        
        // Get headers
        String email = request.getHeader("X-User-Email");
        String password = request.getHeader("X-User-Password");

        log.debug("Security check - Email: {}", email);

        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            throw new UnauthorizedException("Authentication headers missing. Please provide X-User-Email and X-User-Password headers.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        SecuredEndpoint annotation = signature.getMethod().getAnnotation(SecuredEndpoint.class);
        RoleUtilisateur[] allowedRoles = annotation.allowedRoles();

        if (allowedRoles.length > 0) {
            boolean hasRole = Arrays.asList(allowedRoles).contains(user.getRole());
            if (!hasRole) {
                throw new UnauthorizedException("Access denied. Required roles: " + Arrays.toString(allowedRoles));
            }
        }

        log.info("User {} authenticated successfully with role {}", email, user.getRole());

        request.setAttribute("authenticatedUser", user);

        return joinPoint.proceed();
    }
}
