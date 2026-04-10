package com.rbac;

import java.util.*;
import java.util.stream.Collectors;

public class ReportGenerator {
    private final RBACSystem system;

    public ReportGenerator(RBACSystem system) {
        this.system = system;
    }

    public String generateUsersReport() {
        List<User> users = system.getUserManager().findAll();
        if (users.isEmpty()) return "No users.";
        String body = users.parallelStream()
                .map(user -> String.format("%s (%s) - roles: %d",
                        user.username(),
                        user.email(),
                        system.getAssignmentManager().findByUser(user).size()))
                .sorted()
                .collect(Collectors.joining("\n"));
        return "Users report:\n" + body + "\n";
    }

    public String generatePermissionsMatrix() {
        List<User> users = system.getUserManager().findAll();
        String body = users.parallelStream()
                .map(user -> {
                    Set<String> userResources = system.getAssignmentManager().getUserPermissions(user).stream()
                            .map(Permission::resource)
                            .collect(Collectors.toCollection(TreeSet::new));
                    return String.format("%s: %s", user.username(), userResources);
                })
                .sorted()
                .collect(Collectors.joining("\n"));
        return "Permissions matrix (user -> resources):\n" + body + "\n";
    }
}