package com.protocol.supplychainx.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * AOP Aspect to automatically add business context to logs for service layer methods.
 * This aspect intercepts service methods and adds relevant business identifiers to MDC.
 */
@Aspect
@Component
@Slf4j
public class BusinessLoggingAspect {

    /**
     * Around advice for all service methods in procurement module
     */
    @Around("execution(* com.protocol.supplychainx.procurement.service..*.*(..))")
    public Object logProcurementOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        LogContext.setLogType(LogType.BUSINESS);

        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // Extract business ID from method arguments if available
        extractBusinessId(methodName, args, "SUP", "MAT", "SO");

        try {
            log.debug("Executing procurement operation: {}", methodName);
            Object result = joinPoint.proceed();
            log.debug("Procurement operation completed: {}", methodName);
            return result;
        } catch (Exception e) {
            log.error("Procurement operation failed: {} - Error: {}", methodName, e.getMessage());
            throw e;
        } finally {
            // Don't clear MDC here as it's managed by the interceptor
        }
    }

    /**
     * Around advice for all service methods in production module
     */
    @Around("execution(* com.protocol.supplychainx.production.service..*.*(..))")
    public Object logProductionOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        LogContext.setLogType(LogType.BUSINESS);

        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // Extract business ID from method arguments if available
        extractBusinessId(methodName, args, "PROD", "PO", "BOM");

        try {
            log.debug("Executing production operation: {}", methodName);
            Object result = joinPoint.proceed();
            log.debug("Production operation completed: {}", methodName);
            return result;
        } catch (Exception e) {
            log.error("Production operation failed: {} - Error: {}", methodName, e.getMessage());
            throw e;
        }
    }

    /**
     * Around advice for all service methods in delivery module
     */
    @Around("execution(* com.protocol.supplychainx.delivery.service..*.*(..))")
    public Object logDeliveryOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        LogContext.setLogType(LogType.BUSINESS);

        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // Extract business ID from method arguments if available
        extractBusinessId(methodName, args, "CUST", "CO", "DEL");

        try {
            log.debug("Executing delivery operation: {}", methodName);
            Object result = joinPoint.proceed();
            log.debug("Delivery operation completed: {}", methodName);
            return result;
        } catch (Exception e) {
            log.error("Delivery operation failed: {} - Error: {}", methodName, e.getMessage());
            throw e;
        }
    }

    /**
     * Extract business ID from method arguments
     */
    private void extractBusinessId(String methodName, Object[] args, String... prefixes) {
        if (args != null && args.length > 0) {
            // Check if first argument is a Long (ID)
            if (args[0] instanceof Long) {
                Long id = (Long) args[0];
                String prefix = determinePrefix(methodName, prefixes);
                LogContext.setBusinessId(prefix + "-" + id);
            }
        }
    }

    /**
     * Determine the appropriate prefix based on method name
     */
    private String determinePrefix(String methodName, String... prefixes) {
        String lowerMethodName = methodName.toLowerCase();

        if (lowerMethodName.contains("supplier")) return "SUP";
        if (lowerMethodName.contains("material")) return "MAT";
        if (lowerMethodName.contains("supplyorder")) return "SO";
        if (lowerMethodName.contains("product") && !lowerMethodName.contains("production")) return "PROD";
        if (lowerMethodName.contains("productionorder")) return "PO";
        if (lowerMethodName.contains("bom") || lowerMethodName.contains("billofmaterial")) return "BOM";
        if (lowerMethodName.contains("customer")) return "CUST";
        if (lowerMethodName.contains("customerorder") || lowerMethodName.contains("order")) return "CO";
        if (lowerMethodName.contains("delivery")) return "DEL";

        // Default to first prefix
        return prefixes.length > 0 ? prefixes[0] : "BIZ";
    }
}

