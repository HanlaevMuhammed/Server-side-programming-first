package com.rbac;

import java.util.*;
import java.util.stream.Collectors;

public class RBACSystem {
    private final UserManager userManager;
    private final RoleManager roleManager;
    private final AssignmentManager assignmentManager;
    private String currentUser;

    public RBACSystem() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager(userManager, roleManager);
        this.currentUser = "system";
        initialize();
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public AssignmentManager getAssignmentManager() {
        return assignmentManager;
    }

    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void initialize() {
        // Создаем предустановленные права
        Permission readUsers = new Permission("READ", "users", "Can view users");
        Permission writeUsers = new Permission("WRITE", "users", "Can create/edit users");
        Permission deleteUsers = new Permission("DELETE", "users", "Can delete users");
        Permission readRoles = new Permission("READ", "roles", "Can view roles");
        Permission writeRoles = new Permission("WRITE", "roles", "Can create/edit roles");
        Permission deleteRoles = new Permission("DELETE", "roles", "Can delete roles");
        Permission readAssignments = new Permission("READ", "assignments", "Can view assignments");
        Permission writeAssignments = new Permission("WRITE", "assignments", "Can manage assignments");
        Permission readReports = new Permission("READ", "reports", "Can view reports");

        // Создаем роли
        Role adminRole = new Role("Admin", "Full system access");
        adminRole.addPermission(readUsers);
        adminRole.addPermission(writeUsers);
        adminRole.addPermission(deleteUsers);
        adminRole.addPermission(readRoles);
        adminRole.addPermission(writeRoles);
        adminRole.addPermission(deleteRoles);
        adminRole.addPermission(readAssignments);
        adminRole.addPermission(writeAssignments);
        adminRole.addPermission(readReports);

        Role managerRole = new Role("Manager", "Can manage users and view reports");
        managerRole.addPermission(readUsers);
        managerRole.addPermission(writeUsers);
        managerRole.addPermission(readRoles);
        managerRole.addPermission(readAssignments);
        managerRole.addPermission(readReports);

        Role viewerRole = new Role("Viewer", "Read-only access");
        viewerRole.addPermission(readUsers);
        viewerRole.addPermission(readRoles);
        viewerRole.addPermission(readAssignments);
        viewerRole.addPermission(readReports);

        // Добавляем роли в менеджер
        roleManager.add(adminRole);
        roleManager.add(managerRole);
        roleManager.add(viewerRole);

        // Создаем тестового администратора
        User adminUser = User.create("admin", "System Administrator", "admin@example.com");
        userManager.add(adminUser);

        // Назначаем роль Admin администратору
        AssignmentMetadata metadata = AssignmentMetadata.now("system", "Initial setup");
        PermanentAssignment adminAssignment = new PermanentAssignment(adminUser, adminRole, metadata);
        assignmentManager.add(adminAssignment);

        // Устанавливаем текущего пользователя как admin (для дальнейших действий)
        this.currentUser = "admin";
    }

    public String generateStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== System Statistics ===\n");
        sb.append(String.format("Users: %d\n", userManager.count()));
        sb.append(String.format("Roles: %d\n", roleManager.count()));
        long totalAssignments = assignmentManager.count();
        long activeAssignments = assignmentManager.getActiveAssignments().size();
        long expiredAssignments = assignmentManager.getExpiredAssignments().size();
        sb.append(String.format("Assignments: %d total, %d active, %d expired\n",
                totalAssignments, activeAssignments, expiredAssignments));

        // Среднее количество ролей на пользователя
        double avgRoles = userManager.findAll().stream()
                .mapToInt(u -> assignmentManager.findByUser(u).size())
                .average().orElse(0);
        sb.append(String.format("Average roles per user: %.2f\n", avgRoles));

        // Топ-3 самых популярных ролей
        Map<Role, Long> roleCount = assignmentManager.findAll().stream()
                .filter(RoleAssignment::isActive)
                .collect(Collectors.groupingBy(RoleAssignment::role, Collectors.counting()));
        List<Map.Entry<Role, Long>> sorted = roleCount.entrySet().stream()
                .sorted(Map.Entry.<Role, Long>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());
        sb.append("Top 3 roles:\n");
        for (int i = 0; i < sorted.size(); i++) {
            Map.Entry<Role, Long> entry = sorted.get(i);
            sb.append(String.format("  %d. %s (%d users)\n", i+1, entry.getKey().getName(), entry.getValue()));
        }
        return sb.toString();
    }
}