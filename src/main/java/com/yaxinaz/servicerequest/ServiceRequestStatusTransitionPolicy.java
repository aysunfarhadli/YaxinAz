package com.yaxinaz.servicerequest;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** Explicit allowed-transition table (spec sections 51/68), same discipline as IssueStatusTransitionPolicy. */
public final class ServiceRequestStatusTransitionPolicy {

    private static final Map<ServiceRequestStatus, Set<ServiceRequestStatus>> ALLOWED = new EnumMap<>(ServiceRequestStatus.class);

    static {
        ALLOWED.put(ServiceRequestStatus.REQUESTED, EnumSet.of(ServiceRequestStatus.ACCEPTED, ServiceRequestStatus.DECLINED));
        ALLOWED.put(ServiceRequestStatus.ACCEPTED, EnumSet.of(ServiceRequestStatus.IN_PROGRESS, ServiceRequestStatus.CANCELLED));
        ALLOWED.put(ServiceRequestStatus.IN_PROGRESS, EnumSet.of(ServiceRequestStatus.COMPLETED, ServiceRequestStatus.CANCELLED));
        ALLOWED.put(ServiceRequestStatus.DECLINED, EnumSet.noneOf(ServiceRequestStatus.class));
        ALLOWED.put(ServiceRequestStatus.COMPLETED, EnumSet.noneOf(ServiceRequestStatus.class));
        ALLOWED.put(ServiceRequestStatus.CANCELLED, EnumSet.noneOf(ServiceRequestStatus.class));
    }

    private ServiceRequestStatusTransitionPolicy() {
    }

    public static boolean isAllowed(ServiceRequestStatus from, ServiceRequestStatus to) {
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }
}
