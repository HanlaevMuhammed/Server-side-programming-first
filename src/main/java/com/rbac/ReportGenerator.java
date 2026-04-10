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
        StringBuilder sb = new StringBuilder("Users report:\n");
        users.parallelStream().forEach(user -> {
            String line = String.format("%s (%s) - roles: %d\n",
                    user.username(),
                    user.email(),
                    system.getAssignmentManager().findByUser(user).size());
            synchronized (sb) {
                sb.append(line);
            }
        });
        return sb.toString();
    }

    public String generatePermissionsMatrix() {
        List<User> users = system.getUserManager().findAll();
        Set<String> allResources = system.getAssignmentManager().findAll().stream()
                .flatMap(a -> a.role().getPermissions().stream())
                .map(Permission::resource)
                .collect(Collectors.toSet());
        StringBuilder matrix = new StringBuilder("Permissions matrix (user -> resources):\n");
        users.parallelStream().forEach(user -> {
            Set<String> userResources = system.getAssignmentManager().getUserPermissions(user).stream()
                    .map(Permission::resource)
                    .collect(Collectors.toSet());
            String line = String.format("%s: %s\n", user.username(), userResources);
            synchronized (matrix) {
                matrix.append(line);
            }
        });
        return matrix.toString();
    }
}