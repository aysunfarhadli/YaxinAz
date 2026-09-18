package com.yaxinaz.util;

import org.slf4j.MDC;

import com.yaxinaz.common.web.CorrelationIdFilter;

public final class CorrelationIdHolder {

    private CorrelationIdHolder() {
    }

    public static String get() {
        String value = MDC.get(CorrelationIdFilter.MDC_KEY);
        return value != null ? value : "unknown";
    }
}
