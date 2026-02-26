package com.rbac;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class AssignmentFilters {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private AssignmentFilters() {
    }

    public static AssignmentFilter byUser(User user) {
        return a -> a.user().equals(user);
    }

    public static AssignmentFilter byUsername(String username) {
        return a -> a.user().username().equals(username);
    }

    public static AssignmentFilter byRole(Role role) {
        return a -> a.role().equals(role);
    }

    public static AssignmentFilter byRoleName(String roleName) {
        return a -> a.role().getName().equals(roleName);
    }

    public static AssignmentFilter activeOnly() {
        return RoleAssignment::isActive;
    }

    public static AssignmentFilter inactiveOnly() {
        return a -> !a.isActive();
    }

    public static AssignmentFilter byType(String type) {
        return a -> a.assignmentType().equals(type);
    }

    public static AssignmentFilter assignedBy(String username) {
        return a -> a.metadata().assignedBy().equals(username);
    }

    public static AssignmentFilter assignedAfter(String date) {
        LocalDateTime threshold = LocalDateTime.parse(date, FORMATTER);
        return a -> {
            LocalDateTime assignedAt = LocalDateTime.parse(a.metadata().assignedAt(), FORMATTER);
            return assignedAt.isAfter(threshold);
        };
    }

    public static AssignmentFilter expiringBefore(String date) {
        LocalDateTime threshold = LocalDateTime.parse(date, FORMATTER);
        return a -> {
            if (a instanceof TemporaryAssignment temp) {
                LocalDateTime expiresAt = LocalDateTime.parse(temp.getExpiresAt(), FORMATTER);
                return expiresAt.isBefore(threshold);
            }
            return false;
        };
    }
}