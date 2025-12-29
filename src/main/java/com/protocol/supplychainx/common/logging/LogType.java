package com.protocol.supplychainx.common.logging;

/**
 * Enum representing different types of logs for classification in Elasticsearch
 */
public enum LogType {
    /**
     * Application-level logs (technical, configuration, startup/shutdown)
     */
    APPLICATION,

    /**
     * Security-related logs (authentication, authorization, access control)
     */
    SECURITY,

    /**
     * Business logic logs (procurement, production, delivery operations)
     */
    BUSINESS
}

