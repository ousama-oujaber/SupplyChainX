package com.protocol.supplychainx.common.logging;

import lombok.experimental.UtilityClass;
import org.slf4j.MDC;


@UtilityClass
public class LogContext {

    // MDC Keys
    public static final String USER_ID = "userId";
    public static final String USER_ROLE = "userRole";
    public static final String BUSINESS_ID = "businessId";
    public static final String ENDPOINT = "endpoint";
    public static final String HTTP_METHOD = "httpMethod";
    public static final String HTTP_STATUS = "httpStatus";
    public static final String LOG_TYPE = "logType";
    public static final String REQUEST_ID = "requestId";
    public static final String SESSION_ID = "sessionId";
    public static final String IP_ADDRESS = "ipAddress";

    public static void setUserId(String userId) {
        if (userId != null) {
            MDC.put(USER_ID, userId);
        }
    }

    public static void setUserRole(String userRole) {
        if (userRole != null) {
            MDC.put(USER_ROLE, userRole);
        }
    }

    public static void setBusinessId(String businessId) {
        if (businessId != null) {
            MDC.put(BUSINESS_ID, businessId);
        }
    }

    public static void setEndpoint(String endpoint) {
        if (endpoint != null) {
            MDC.put(ENDPOINT, endpoint);
        }
    }

    /**
     * Set HTTP method in MDC
     */
    public static void setHttpMethod(String httpMethod) {
        if (httpMethod != null) {
            MDC.put(HTTP_METHOD, httpMethod);
        }
    }

    /**
     * Set HTTP status code in MDC
     */
    public static void setHttpStatus(Integer httpStatus) {
        if (httpStatus != null) {
            MDC.put(HTTP_STATUS, httpStatus.toString());
        }
    }

    /**
     * Set log type in MDC (APPLICATION, SECURITY, BUSINESS)
     */
    public static void setLogType(LogType logType) {
        if (logType != null) {
            MDC.put(LOG_TYPE, logType.name());
        }
    }

    /**
     * Set request ID in MDC for tracing
     */
    public static void setRequestId(String requestId) {
        if (requestId != null) {
            MDC.put(REQUEST_ID, requestId);
        }
    }

    /**
     * Set session ID in MDC
     */
    public static void setSessionId(String sessionId) {
        if (sessionId != null) {
            MDC.put(SESSION_ID, sessionId);
        }
    }

    /**
     * Set client IP address in MDC
     */
    public static void setIpAddress(String ipAddress) {
        if (ipAddress != null) {
            MDC.put(IP_ADDRESS, ipAddress);
        }
    }

    /**
     * Get user ID from MDC
     */
    public static String getUserId() {
        return MDC.get(USER_ID);
    }

    /**
     * Get user role from MDC
     */
    public static String getUserRole() {
        return MDC.get(USER_ROLE);
    }

    /**
     * Get business ID from MDC
     */
    public static String getBusinessId() {
        return MDC.get(BUSINESS_ID);
    }

    /**
     * Get request ID from MDC
     */
    public static String getRequestId() {
        return MDC.get(REQUEST_ID);
    }

    /**
     * Get HTTP method from MDC
     */
    public static String getHttpMethod() {
        return MDC.get(HTTP_METHOD);
    }

    /**
     * Get endpoint from MDC
     */
    public static String getEndpoint() {
        return MDC.get(ENDPOINT);
    }

    /**
     * Clear specific MDC key
     */
    public static void clear(String key) {
        MDC.remove(key);
    }

    /**
     * Clear all MDC context
     */
    public static void clearAll() {
        MDC.clear();
    }

    /**
     * Clear user context (userId and userRole)
     */
    public static void clearUserContext() {
        MDC.remove(USER_ID);
        MDC.remove(USER_ROLE);
    }

    /**
     * Clear business context (businessId)
     */
    public static void clearBusinessContext() {
        MDC.remove(BUSINESS_ID);
    }

    /**
     * Clear HTTP context (endpoint, method, status)
     */
    public static void clearHttpContext() {
        MDC.remove(ENDPOINT);
        MDC.remove(HTTP_METHOD);
        MDC.remove(HTTP_STATUS);
    }
}

