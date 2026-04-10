package com.rbac;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RBACSystem {
    private static final int DEFAULT_SCHEDULER_INITIAL_DELAY_SECONDS = 10;
    private static final int DEFAULT_SCHEDULER_PERIOD_SECONDS = 30;
    private final UserManager userManager;
    private final RoleManager roleManager;
    private final AssignmentManager assignmentManager;
    private String currentUser;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public RBACSystem() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager(userManager, roleManager);
        this.currentUser = "system";
        initialize();
        startScheduledTasks();
    }

    public ExecutorService getExecutor() { return executor; }

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

    public void startScheduledTasks() {
        startScheduledTasks(DEFAULT_SCHEDULER_INITIAL_DELAY_SECONDS, DEFAULT_SCHEDULER_PERIOD_SECONDS);
    }

    public void startScheduledTasks(int initialDelaySeconds, int periodSeconds) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Проверка выполняется на копии списка без долгой блокировки.
                List<RoleAssignment> all = assignmentManager.findAll();
                long expiredTemporary = all.stream()
                        .filter(a -> a instanceof TemporaryAssignment)
                        .filter(a -> !a.isActive())
                        .count();
                if (expiredTemporary > 0) {
                    AuditLog.log("Expired temporary assignments detected: " + expiredTemporary);
                }
                String stats = generateStatistics();
                AuditLog.log("Periodic stats:\n" + stats);
            } catch (Exception e) {
                AuditLog.log("Error in scheduled task: " + e.getMessage());
            }
        }, initialDelaySeconds, periodSeconds, TimeUnit.SECONDS);
    }

    public void saveToFile(String filePath) {
        Objects.requireNonNull(filePath, "File path cannot be null");
        List<String> lines = new ArrayList<>();

        for (User user : userManager.findAll()) {
            lines.add("USER|" + esc(user.username()) + "|" + esc(user.fullName()) + "|" + esc(user.email()));
        }
        for (Role role : roleManager.findAll()) {
            lines.add("ROLE|" + esc(role.getName()) + "|" + esc(role.getDescription()));
            for (Permission permission : role.getPermissions()) {
                lines.add("PERMISSION|" + esc(role.getName()) + "|" + esc(permission.name()) + "|"
                        + esc(permission.resource()) + "|" + esc(permission.description()));
            }
        }
        for (RoleAssignment assignment : assignmentManager.findAll()) {
            if (assignment instanceof PermanentAssignment p) {
                lines.add("ASSIGNMENT|PERMANENT|" + esc(assignment.user().username()) + "|" + esc(assignment.role().getName())
                        + "|" + esc(assignment.metadata().assignedBy()) + "|" + esc(assignment.metadata().assignedAt())
                        + "|" + esc(nvl(assignment.metadata().reason())) + "|" + p.isRevoked());
            } else if (assignment instanceof TemporaryAssignment t) {
                lines.add("ASSIGNMENT|TEMPORARY|" + esc(assignment.user().username()) + "|" + esc(assignment.role().getName())
                        + "|" + esc(assignment.metadata().assignedBy()) + "|" + esc(assignment.metadata().assignedAt())
                        + "|" + esc(nvl(assignment.metadata().reason())) + "|" + esc(t.getExpiresAt())
                        + "|" + t.isAutoRenew());
            }
        }

        try {
            java.nio.file.Files.write(java.nio.file.Path.of(filePath), lines);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to save data: " + e.getMessage(), e);
        }
    }

    public void loadFromFile(String filePath) {
        Objects.requireNonNull(filePath, "File path cannot be null");
        try {
            List<String> lines = java.nio.file.Files.readAllLines(java.nio.file.Path.of(filePath));
            userManager.clear();
            roleManager.clear();
            assignmentManager.clear();

            Map<String, Role> loadedRoles = new HashMap<>();
            List<String[]> assignmentRows = new ArrayList<>();

            for (String line : lines) {
                if (line == null || line.isBlank()) {
                    continue;
                }
                String[] parts = split(line);
                switch (parts[0]) {
                    case "USER" -> userManager.add(User.create(unesc(parts[1]), unesc(parts[2]), unesc(parts[3])));
                    case "ROLE" -> {
                        Role role = new Role(unesc(parts[1]), unesc(parts[2]));
                        roleManager.add(role);
                        loadedRoles.put(role.getName(), role);
                    }
                    case "PERMISSION" -> {
                        Role role = loadedRoles.get(unesc(parts[1]));
                        if (role != null) {
                            role.addPermission(new Permission(unesc(parts[2]), unesc(parts[3]), unesc(parts[4])));
                        }
                    }
                    case "ASSIGNMENT" -> assignmentRows.add(parts);
                    default -> throw new IllegalArgumentException("Unknown row type: " + parts[0]);
                }
            }

            for (String[] parts : assignmentRows) {
                String type = parts[1];
                User user = userManager.findByUsername(unesc(parts[2]))
                        .orElseThrow(() -> new IllegalArgumentException("Unknown user in assignment"));
                Role role = roleManager.findByName(unesc(parts[3]))
                        .orElseThrow(() -> new IllegalArgumentException("Unknown role in assignment"));
                AssignmentMetadata metadata = new AssignmentMetadata(unesc(parts[4]), unesc(parts[5]), denull(unesc(parts[6])));

                if ("PERMANENT".equals(type)) {
                    PermanentAssignment assignment = new PermanentAssignment(user, role, metadata);
                    if (Boolean.parseBoolean(parts[7])) {
                        assignment.revoke();
                    }
                    assignmentManager.add(assignment);
                } else if ("TEMPORARY".equals(type)) {
                    TemporaryAssignment assignment = new TemporaryAssignment(
                            user, role, metadata, unesc(parts[7]), Boolean.parseBoolean(parts[8]));
                    assignmentManager.add(assignment);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load data: " + e.getMessage(), e);
        }
    }

    public void shutdown() {
        scheduler.shutdownNow();
        executor.shutdown();
        AuditLog.shutdown();
    }

    private static String esc(String value) {
        return value.replace("\\", "\\\\").replace("|", "\\|");
    }

    private static String unesc(String value) {
        StringBuilder sb = new StringBuilder();
        boolean escaped = false;
        for (char c : value.toCharArray()) {
            if (escaped) {
                sb.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String[] split(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        boolean escaped = false;
        for (char c : line.toCharArray()) {
            if (escaped) {
                token.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '|') {
                parts.add(token.toString());
                token.setLength(0);
            } else {
                token.append(c);
            }
        }
        parts.add(token.toString());
        return parts.toArray(new String[0]);
    }

    private static String nvl(String value) {
        return value == null ? "" : value;
    }

    private static String denull(String value) {
        return value == null || value.isBlank() ? null : value;
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