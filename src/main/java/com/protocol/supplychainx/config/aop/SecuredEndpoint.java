package com.protocol.supplychainx.config.aop;

import com.protocol.supplychainx.common.enums.RoleUtilisateur;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SecuredEndpoint {
    RoleUtilisateur[] allowedRoles() default {};
}
