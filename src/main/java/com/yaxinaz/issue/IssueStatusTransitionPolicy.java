package com.yaxinaz.issue;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Explicit allowed-transition table (spec section 30). Status changes outside this table are
 * rejected rather than silently accepted - the workflow state machine is enforced in Java, not left
 * to whatever an admin happens to click.
 */
public final class IssueStatusTransitionPolicy {

    private static final Map<IssueStatus, Set<IssueStatus>> ALLOWED = new EnumMap<>(IssueStatus.class);

    static {
        ALLOWED.put(IssueStatus.OPEN, EnumSet.of(IssueStatus.ACKNOWLEDGED, IssueStatus.REJECTED));
        ALLOWED.put(IssueStatus.ACKNOWLEDGED, EnumSet.of(IssueStatus.IN_PROGRESS, IssueStatus.REJECTED));
        ALLOWED.put(IssueStatus.IN_PROGRESS, EnumSet.of(IssueStatus.WAITING_FOR_VENDOR, IssueStatus.RESOLVED));
        ALLOWED.put(IssueStatus.WAITING_FOR_VENDOR, EnumSet.of(IssueStatus.IN_PROGRESS));
        // RESOLVED -> OPEN represents the REOPENED activity from spec section 30.
        ALLOWED.put(IssueStatus.RESOLVED, EnumSet.of(IssueStatus.CLOSED, IssueStatus.OPEN));
        ALLOWED.put(IssueStatus.CLOSED, EnumSet.noneOf(IssueStatus.class));
        ALLOWED.put(IssueStatus.REJECTED, EnumSet.noneOf(IssueStatus.class));
    }

    private IssueStatusTransitionPolicy() {
    }

    public static boolean isAllowed(IssueStatus from, IssueStatus to) {
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }

    public static boolean isReopen(IssueStatus from, IssueStatus to) {
        return from == IssueStatus.RESOLVED && to == IssueStatus.OPEN;
    }
}
