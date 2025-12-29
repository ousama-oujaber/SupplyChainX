package com.protocol.supplychainx.common.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

/**
 * HTTP Interceptor to automatically populate MDC context for all requests.
 * This ensures all logs have consistent contextual information.
 */
@Component
@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Generate unique request ID for tracing
        String requestId = UUID.randomUUID().toString();
        LogContext.setRequestId(requestId);

        // Capture HTTP information
        String endpoint = request.getRequestURI();
        String method = request.getMethod();
        LogContext.setEndpoint(endpoint);
        LogContext.setHttpMethod(method);

        // Capture client IP address
        String ipAddress = getClientIpAddress(request);
        LogContext.setIpAddress(ipAddress);

        // Capture session ID if available
        if (request.getSession(false) != null) {
            LogContext.setSessionId(request.getSession().getId());
        }

        // Capture authenticated user information
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {

            String userId = authentication.getName();
            LogContext.setUserId(userId);

            // Get user role(s)
            String userRole = authentication.getAuthorities().stream()
                    .map(Object::toString)
                    .findFirst()
                    .orElse("UNKNOWN");
            LogContext.setUserRole(userRole);
        }

        // Determine log type based on endpoint
        LogType logType = determineLogType(endpoint);
        LogContext.setLogType(logType);

        // Log request start
        log.info("Request started: {} {} from {} [RequestID: {}]",
                method, endpoint, ipAddress, requestId);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                          Object handler, ModelAndView modelAndView) {
        // Capture response status
        int status = response.getStatus();
        LogContext.setHttpStatus(status);

        // Log security events for 401/403
        if (status == 401 || status == 403) {
            LogContext.setLogType(LogType.SECURITY);
            log.warn("Access denied: {} {} returned status {} [User: {}, RequestID: {}]",
                    LogContext.getHttpMethod(),
                    LogContext.getEndpoint(),
                    status,
                    LogContext.getUserId() != null ? LogContext.getUserId() : "anonymous",
                    LogContext.getRequestId());
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                               Object handler, Exception ex) {
        // Log request completion
        String requestId = LogContext.getRequestId();
        int status = response.getStatus();
        String method = LogContext.getHttpMethod();
        String endpoint = LogContext.getEndpoint();

        if (ex != null) {
            log.error("Request failed: {} {} with status {} [RequestID: {}]",
                    method, endpoint, status, requestId, ex);
        } else {
            log.info("Request completed: {} {} with status {} [RequestID: {}]",
                    method, endpoint, status, requestId);
        }

        // Clear MDC context to avoid memory leaks
        LogContext.clearAll();
    }

    /**
     * Determine log type based on endpoint path
     */
    private LogType determineLogType(String endpoint) {
        if (endpoint == null) {
            return LogType.APPLICATION;
        }

        // Security-related endpoints
        if (endpoint.contains("/login") || endpoint.contains("/logout")
                || endpoint.contains("/auth") || endpoint.contains("/users")) {
            return LogType.SECURITY;
        }

        // Business endpoints
        if (endpoint.contains("/suppliers") || endpoint.contains("/raw-materials")
                || endpoint.contains("/supply-orders") || endpoint.contains("/products")
                || endpoint.contains("/production-orders") || endpoint.contains("/customers")
                || endpoint.contains("/customer-orders") || endpoint.contains("/deliveries")
                || endpoint.contains("/bom")) {
            return LogType.BUSINESS;
        }

        // Everything else is application-level
        return LogType.APPLICATION;
    }

    /**
     * Get client IP address from request, considering proxy headers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Handle multiple IPs (take the first one)
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }
}

