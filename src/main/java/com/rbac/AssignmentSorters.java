package com.rbac;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

public final class AssignmentSorters {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private AssignmentSorters() {
    }

    public static Comparator<RoleAssignment> byUsername() {
        return Comparator.comparing(a -> a.user().username());
    }

    public static Comparator<RoleAssignment> byRoleName() {
        return Comparator.comparing(a -> a.role().getName());
    }

    public static Comparator<RoleAssignment> byAssignmentDate() {
        return Comparator.comparing(a -> LocalDateTime.parse(a.metadata().assignedAt(), FORMATTER));
    }
}