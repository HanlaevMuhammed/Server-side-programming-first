package com.rbac;

import java.util.*;
import java.util.stream.Collectors;

public class CommandRegistry {
    public static void registerAll(CommandParser parser) {
        parser.registerCommand("help", "Show this help", (scanner, system) -> parser.printHelp());
        parser.registerCommand("stats", "Show system statistics", (scanner, system) -> {
            System.out.println(system.generateStatistics());
        });
        parser.registerCommand("clear", "Clear screen", (scanner, system) -> {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        });
        // exit обрабатывается в главном цикле отдельно

        parser.registerCommand("user-list", "List all users", (scanner, system) -> {
            List<User> users = system.getUserManager().findAll();
            if (users.isEmpty()) {
                System.out.println("No users found.");
                return;
            }
            System.out.println("Users:");
            System.out.printf("%-20s %-30s %-30s\n", "Username", "Full Name", "Email");
            for (User u : users) {
                System.out.printf("%-20s %-30s %-30s\n", u.username(), u.fullName(), u.email());
            }
        });

        parser.registerCommand("user-create", "Create a new user", (scanner, system) -> {
            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Enter full name: ");
            String fullName = scanner.nextLine().trim();
            System.out.print("Enter email: ");
            String email = scanner.nextLine().trim();
            try {
                User user = User.create(username, fullName, email);
                system.getUserManager().add(user);
                System.out.println("User created successfully.");
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("user-view", "View user details", (scanner, system) -> {
            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();
            User user = system.getUserManager().findByUsername(username).orElse(null);
            if (user == null) {
                System.out.println("User not found.");
                return;
            }
            System.out.println("User: " + user.format());
            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
            System.out.println("Assigned roles:");
            if (assignments.isEmpty()) {
                System.out.println("  No roles assigned.");
            } else {
                for (RoleAssignment ra : assignments) {
                    System.out.printf("  - %s (%s) %s\n", ra.role().getName(), ra.assignmentType(),
                            ra.isActive() ? "active" : "inactive");
                }
            }
            Set<Permission> permissions = system.getAssignmentManager().getUserPermissions(user);
            System.out.println("Effective permissions:");
            if (permissions.isEmpty()) {
                System.out.println("  No permissions.");
            } else {
                permissions.forEach(p -> System.out.println("  " + p.format()));
            }
        });

        parser.registerCommand("user-update", "Update user details", (scanner, system) -> {
            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();
            User existing = system.getUserManager().findByUsername(username).orElse(null);
            if (existing == null) {
                System.out.println("User not found.");
                return;
            }
            System.out.print("Enter new full name (or press Enter to keep current): ");
            String newFullName = scanner.nextLine().trim();
            if (newFullName.isEmpty()) newFullName = existing.fullName();
            System.out.print("Enter new email (or press Enter to keep current): ");
            String newEmail = scanner.nextLine().trim();
            if (newEmail.isEmpty()) newEmail = existing.email();
            try {
                system.getUserManager().update(username, newFullName, newEmail);
                System.out.println("User updated successfully.");
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("user-delete", "Delete a user", (scanner, system) -> {
            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();
            User user = system.getUserManager().findByUsername(username).orElse(null);
            if (user == null) {
                System.out.println("User not found.");
                return;
            }
            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
            if (!assignments.isEmpty()) {
                System.out.println("User has active assignments. Deleting will also remove them.");
            }
            System.out.print("Are you sure? (yes/no): ");
            String confirm = scanner.nextLine().trim();
            if (confirm.equalsIgnoreCase("yes")) {
                for (RoleAssignment ra : assignments) {
                    system.getAssignmentManager().remove(ra);
                }
                system.getUserManager().remove(user);
                System.out.println("User deleted.");
            } else {
                System.out.println("Deletion cancelled.");
            }
        });

        parser.registerCommand("user-search", "Search users by filter", (scanner, system) -> {
            System.out.println("Choose filter:");
            System.out.println("1. Username contains");
            System.out.println("2. Email contains");
            System.out.println("3. Email domain");
            System.out.println("4. Full name contains");
            System.out.print("Enter choice (1-4): ");
            String choice = scanner.nextLine().trim();
            UserFilter filter = null;
            switch (choice) {
                case "1":
                    System.out.print("Enter substring: ");
                    String sub = scanner.nextLine().trim();
                    filter = UserFilters.byUsernameContains(sub);
                    break;
                case "2":
                    System.out.print("Enter email substring: ");
                    sub = scanner.nextLine().trim();
                    filter = UserFilters.byEmail(sub);
                    break;
                case "3":
                    System.out.print("Enter domain (e.g., @example.com): ");
                    String domain = scanner.nextLine().trim();
                    filter = UserFilters.byEmailDomain(domain);
                    break;
                case "4":
                    System.out.print("Enter full name substring: ");
                    sub = scanner.nextLine().trim();
                    filter = UserFilters.byFullNameContains(sub);
                    break;
                default:
                    System.out.println("Invalid choice.");
                    return;
            }
            List<User> results = system.getUserManager().findByFilter(filter);
            if (results.isEmpty()) {
                System.out.println("No users found.");
            } else {
                System.out.println("Found users:");
                System.out.printf("%-20s %-30s %-30s\n", "Username", "Full Name", "Email");
                for (User u : results) {
                    System.out.printf("%-20s %-30s %-30s\n", u.username(), u.fullName(), u.email());
                }
            }
        });

        parser.registerCommand("role-list", "List all roles", (scanner, system) -> {
            List<Role> roles = system.getRoleManager().findAll();
            if (roles.isEmpty()) {
                System.out.println("No roles found.");
                return;
            }
            System.out.println("Roles:");
            System.out.printf("%-20s %-30s %-10s %s\n", "Name", "Description", "Permissions", "ID");
            for (Role r : roles) {
                System.out.printf("%-20s %-30s %-10d %s\n", r.getName(), r.getDescription(),
                        r.getPermissions().size(), r.getId());
            }
        });

        parser.registerCommand("role-create", "Create a new role", (scanner, system) -> {
            System.out.print("Enter role name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Enter description: ");
            String desc = scanner.nextLine().trim();
            try {
                Role role = new Role(name, desc);
                system.getRoleManager().add(role);
                System.out.println("Role created. Now you can add permissions.");
                boolean adding = true;
                while (adding) {
                    System.out.print("Add permission? (yes/no): ");
                    String ans = scanner.nextLine().trim();
                    if (!ans.equalsIgnoreCase("yes")) {
                        adding = false;
                        continue;
                    }
                    System.out.print("Enter permission name (e.g., READ): ");
                    String pName = scanner.nextLine().trim();
                    System.out.print("Enter resource (e.g., users): ");
                    String resource = scanner.nextLine().trim();
                    System.out.print("Enter description: ");
                    String pDesc = scanner.nextLine().trim();
                    try {
                        Permission perm = new Permission(pName, resource, pDesc);
                        system.getRoleManager().addPermissionToRole(name, perm);
                        System.out.println("Permission added.");
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("role-view", "View role details", (scanner, system) -> {
            System.out.print("Enter role name: ");
            String name = scanner.nextLine().trim();
            Role role = system.getRoleManager().findByName(name).orElse(null);
            if (role == null) {
                System.out.println("Role not found.");
                return;
            }
            System.out.println(role.format());
        });

        parser.registerCommand("role-update", "Update role name/description", (scanner, system) -> {
            System.out.print("Enter role name: ");
            String oldName = scanner.nextLine().trim();
            Role role = system.getRoleManager().findByName(oldName).orElse(null);
            if (role == null) {
                System.out.println("Role not found.");
                return;
            }
            System.out.print("Enter new name (or press Enter to keep): ");
            String newName = scanner.nextLine().trim();
            if (newName.isEmpty()) newName = role.getName();
            System.out.print("Enter new description (or press Enter to keep): ");
            String newDesc = scanner.nextLine().trim();
            if (newDesc.isEmpty()) newDesc = role.getDescription();
            try {
                Role updated = new Role(newName, newDesc);
                for (Permission p : role.getPermissions()) {
                    updated.addPermission(p);
                }
                system.getRoleManager().remove(role);
                system.getRoleManager().add(updated);
                System.out.println("Role updated.");
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("role-delete", "Delete a role", (scanner, system) -> {
            System.out.print("Enter role name: ");
            String name = scanner.nextLine().trim();
            Role role = system.getRoleManager().findByName(name).orElse(null);
            if (role == null) {
                System.out.println("Role not found.");
                return;
            }
            // Проверим, есть ли назначения
            List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role);
            if (!assignments.isEmpty()) {
                System.out.println("Role is assigned to users:");
                assignments.forEach(a -> System.out.println("  - " + a.user().username()));
                System.out.print("Delete anyway? (yes/no): ");
                String ans = scanner.nextLine().trim();
                if (!ans.equalsIgnoreCase("yes")) {
                    System.out.println("Deletion cancelled.");
                    return;
                }
                // Удаляем назначения
                assignments.forEach(a -> system.getAssignmentManager().remove(a));
            }
            system.getRoleManager().remove(role);
            System.out.println("Role deleted.");
        });

        parser.registerCommand("role-add-permission", "Add permission to role", (scanner, system) -> {
            System.out.print("Enter role name: ");
            String name = scanner.nextLine().trim();
            Role role = system.getRoleManager().findByName(name).orElse(null);
            if (role == null) {
                System.out.println("Role not found.");
                return;
            }
            System.out.print("Enter permission name (e.g., READ): ");
            String pName = scanner.nextLine().trim();
            System.out.print("Enter resource (e.g., users): ");
            String resource = scanner.nextLine().trim();
            System.out.print("Enter description: ");
            String pDesc = scanner.nextLine().trim();
            try {
                Permission perm = new Permission(pName, resource, pDesc);
                system.getRoleManager().addPermissionToRole(name, perm);
                System.out.println("Permission added.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("role-remove-permission", "Remove permission from role", (scanner, system) -> {
            System.out.print("Enter role name: ");
            String name = scanner.nextLine().trim();
            Role role = system.getRoleManager().findByName(name).orElse(null);
            if (role == null) {
                System.out.println("Role not found.");
                return;
            }
            List<Permission> perms = new ArrayList<>(role.getPermissions());
            if (perms.isEmpty()) {
                System.out.println("Role has no permissions.");
                return;
            }
            System.out.println("Permissions:");
            for (int i = 0; i < perms.size(); i++) {
                System.out.printf("%d. %s\n", i+1, perms.get(i).format());
            }
            System.out.print("Enter number to remove: ");
            String numStr = scanner.nextLine().trim();
            try {
                int idx = Integer.parseInt(numStr) - 1;
                if (idx < 0 || idx >= perms.size()) {
                    System.out.println("Invalid number.");
                    return;
                }
                Permission toRemove = perms.get(idx);
                system.getRoleManager().removePermissionFromRole(name, toRemove);
                System.out.println("Permission removed.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid number.");
            }
        });

        parser.registerCommand("role-search", "Search roles by filter", (scanner, system) -> {
            System.out.println("Choose filter:");
            System.out.println("1. Name contains");
            System.out.println("2. Has specific permission");
            System.out.println("3. Has at least N permissions");
            System.out.print("Enter choice (1-3): ");
            String choice = scanner.nextLine().trim();
            RoleFilter filter = null;
            switch (choice) {
                case "1":
                    System.out.print("Enter name substring: ");
                    String sub = scanner.nextLine().trim();
                    filter = RoleFilters.byNameContains(sub);
                    break;
                case "2":
                    System.out.print("Enter permission name: ");
                    String pName = scanner.nextLine().trim();
                    System.out.print("Enter resource: ");
                    String resource = scanner.nextLine().trim();
                    filter = RoleFilters.hasPermission(pName, resource);
                    break;
                case "3":
                    System.out.print("Enter minimum number of permissions: ");
                    String nStr = scanner.nextLine().trim();
                    try {
                        int n = Integer.parseInt(nStr);
                        filter = RoleFilters.hasAtLeastNPermissions(n);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number.");
                        return;
                    }
                    break;
                default:
                    System.out.println("Invalid choice.");
                    return;
            }
            List<Role> results = system.getRoleManager().findByFilter(filter);
            if (results.isEmpty()) {
                System.out.println("No roles found.");
            } else {
                System.out.println("Found roles:");
                System.out.printf("%-20s %-30s %-10s\n", "Name", "Description", "Permissions");
                for (Role r : results) {
                    System.out.printf("%-20s %-30s %-10d\n", r.getName(), r.getDescription(),
                            r.getPermissions().size());
                }
            }
        });

        parser.registerCommand("report-users-async", "Generate users report in background", (scanner, system) -> {
            system.getExecutor().submit(() -> {
                ReportGenerator rg = new ReportGenerator(system);
                String report = rg.generateUsersReport();
                AuditLog.log("Async report generated:\n" + report);
                System.out.println("Report generation completed. Check console for output.");
            });
            System.out.println("Report generation started in background.");
        });

        parser.registerCommand("audit-log", "Show audit log", (scanner, system) -> {
            System.out.println("Audit log is printed to console in real time.");
        });

        parser.registerCommand("save-async", "Save data to file in background", (scanner, system) -> {
            System.out.print("Enter file path: ");
            String filePath = scanner.nextLine().trim();
            system.getExecutor().submit(() -> {
                try {
                    AuditLog.log("Saving data to file: " + filePath);
                    system.saveToFile(filePath);
                    AuditLog.log("Data saved to file: " + filePath);
                    System.out.println("Save completed.");
                } catch (Exception e) {
                    AuditLog.log("Save failed: " + e.getMessage());
                    System.out.println("Save failed: " + e.getMessage());
                }
            });
            System.out.println("Save started in background.");
        });

        parser.registerCommand("assign-role", "Assign a role to a user", (scanner, system) -> {
            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();
            User user = system.getUserManager().findByUsername(username).orElse(null);
            if (user == null) {
                System.out.println("User not found.");
                return;
            }
            List<Role> roles = system.getRoleManager().findAll();
            if (roles.isEmpty()) {
                System.out.println("No roles available.");
                return;
            }
            System.out.println("Available roles:");
            for (int i = 0; i < roles.size(); i++) {
                System.out.printf("%d. %s\n", i+1, roles.get(i).getName());
            }
            System.out.print("Select role number: ");
            String roleNum = scanner.nextLine().trim();
            try {
                int idx = Integer.parseInt(roleNum) - 1;
                if (idx < 0 || idx >= roles.size()) {
                    System.out.println("Invalid number.");
                    return;
                }
                Role role = roles.get(idx);
                System.out.print("Assignment type (permanent/temporary): ");
                String type = scanner.nextLine().trim().toLowerCase();
                boolean isPermanent = type.startsWith("p");
                String expiresAt = null;
                if (!isPermanent) {
                    System.out.print("Enter expiration date (yyyy-MM-dd HH:mm): ");
                    expiresAt = scanner.nextLine().trim();
                }
                System.out.print("Enter reason (optional): ");
                String reason = scanner.nextLine().trim();
                if (reason.isEmpty()) reason = null;
                AssignmentMetadata metadata = AssignmentMetadata.now(system.getCurrentUser(), reason);
                RoleAssignment assignment;
                if (isPermanent) {
                    assignment = new PermanentAssignment(user, role, metadata);
                } else {
                    assignment = new TemporaryAssignment(user, role, metadata, expiresAt, false);
                }
                system.getAssignmentManager().add(assignment);
                System.out.println("Role assigned successfully. Assignment ID: " + assignment.assignmentId());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number.");
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("revoke-role", "Revoke a role from a user", (scanner, system) -> {
            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();
            User user = system.getUserManager().findByUsername(username).orElse(null);
            if (user == null) {
                System.out.println("User not found.");
                return;
            }
            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user).stream()
                    .filter(RoleAssignment::isActive).collect(Collectors.toList());
            if (assignments.isEmpty()) {
                System.out.println("No active assignments for this user.");
                return;
            }
            System.out.println("Active assignments:");
            for (int i = 0; i < assignments.size(); i++) {
                RoleAssignment ra = assignments.get(i);
                System.out.printf("%d. %s (%s) assigned at %s\n", i+1, ra.role().getName(),
                        ra.assignmentType(), ra.metadata().assignedAt());
            }
            System.out.print("Select assignment number to revoke: ");
            String numStr = scanner.nextLine().trim();
            try {
                int idx = Integer.parseInt(numStr) - 1;
                if (idx < 0 || idx >= assignments.size()) {
                    System.out.println("Invalid number.");
                    return;
                }
                RoleAssignment toRevoke = assignments.get(idx);
                if (toRevoke instanceof PermanentAssignment) {
                    ((PermanentAssignment) toRevoke).revoke();
                    System.out.println("Assignment revoked.");
                } else {
                    system.getAssignmentManager().remove(toRevoke);
                    System.out.println("Assignment removed.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number.");
            }
        });

        parser.registerCommand("assignment-list", "List all assignments", (scanner, system) -> {
            List<RoleAssignment> all = system.getAssignmentManager().findAll();
            if (all.isEmpty()) {
                System.out.println("No assignments.");
                return;
            }
            System.out.printf("%-10s %-20s %-20s %-10s %-8s %-20s\n", "ID", "User", "Role", "Type", "Status", "Assigned At");
            for (RoleAssignment ra : all) {
                System.out.printf("%-10s %-20s %-20s %-10s %-8s %-20s\n",
                        ra.assignmentId().substring(0, Math.min(8, ra.assignmentId().length())),
                        ra.user().username(),
                        ra.role().getName(),
                        ra.assignmentType(),
                        ra.isActive() ? "active" : "inactive",
                        ra.metadata().assignedAt());
            }
        });

        parser.registerCommand("assignment-list-user", "List assignments for a user", (scanner, system) -> {
            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();
            User user = system.getUserManager().findByUsername(username).orElse(null);
            if (user == null) {
                System.out.println("User not found.");
                return;
            }
            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
            if (assignments.isEmpty()) {
                System.out.println("No assignments for this user.");
                return;
            }
            for (RoleAssignment ra : assignments) {
                if (ra instanceof AbstractRoleAssignment ara) {
                    System.out.println(ara.summary());
                } else {
                    System.out.printf("[%s] %s -> %s, active=%s\n",
                            ra.assignmentType(), ra.user().username(), ra.role().getName(), ra.isActive());
                }
                System.out.println("---");
            }
        });

        parser.registerCommand("assignment-list-role", "List users with a specific role", (scanner, system) -> {
            System.out.print("Enter role name: ");
            String roleName = scanner.nextLine().trim();
            Role role = system.getRoleManager().findByName(roleName).orElse(null);
            if (role == null) {
                System.out.println("Role not found.");
                return;
            }
            List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role);
            if (assignments.isEmpty()) {
                System.out.println("No users assigned this role.");
                return;
            }
            System.out.println("Users with role " + roleName + ":");
            for (RoleAssignment ra : assignments) {
                System.out.println("  " + ra.user().username() + " (" + (ra.isActive() ? "active" : "inactive") + ")");
            }
        });

        parser.registerCommand("assignment-active", "List active assignments", (scanner, system) -> {
            List<RoleAssignment> active = system.getAssignmentManager().getActiveAssignments();
            if (active.isEmpty()) {
                System.out.println("No active assignments.");
                return;
            }
            System.out.printf("%-20s %-20s %-10s %-20s\n", "User", "Role", "Type", "Assigned At");
            for (RoleAssignment ra : active) {
                System.out.printf("%-20s %-20s %-10s %-20s\n",
                        ra.user().username(),
                        ra.role().getName(),
                        ra.assignmentType(),
                        ra.metadata().assignedAt());
            }
        });

        parser.registerCommand("assignment-expired", "List expired assignments", (scanner, system) -> {
            List<RoleAssignment> expired = system.getAssignmentManager().getExpiredAssignments();
            if (expired.isEmpty()) {
                System.out.println("No expired assignments.");
                return;
            }
            System.out.printf("%-20s %-20s %-20s\n", "User", "Role", "Expired At");
            for (RoleAssignment ra : expired) {
                if (ra instanceof TemporaryAssignment temp) {
                    System.out.printf("%-20s %-20s %-20s\n",
                            ra.user().username(),
                            ra.role().getName(),
                            temp.getExpiresAt());
                } else {
                    // Постоянные не истекают, но если неактивны, то отозваны
                    System.out.printf("%-20s %-20s %-20s\n",
                            ra.user().username(),
                            ra.role().getName(),
                            "revoked");
                }
            }
        });

        parser.registerCommand("assignment-extend", "Extend a temporary assignment", (scanner, system) -> {
            System.out.print("Enter assignment ID: ");
            String id = scanner.nextLine().trim();
            Optional<RoleAssignment> opt = system.getAssignmentManager().findById(id);
            if (opt.isEmpty()) {
                System.out.println("Assignment not found.");
                return;
            }
            RoleAssignment ra = opt.get();
            if (!(ra instanceof TemporaryAssignment)) {
                System.out.println("Only temporary assignments can be extended.");
                return;
            }
            System.out.print("Enter new expiration date (yyyy-MM-dd HH:mm): ");
            String newDate = scanner.nextLine().trim();
            try {
                system.getAssignmentManager().extendTemporaryAssignment(id, newDate);
                System.out.println("Assignment extended.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("assignment-search", "Search assignments by filter", (scanner, system) -> {
            System.out.println("Choose filter:");
            System.out.println("1. By user");
            System.out.println("2. By role");
            System.out.println("3. By type (permanent/temporary)");
            System.out.println("4. By status (active/inactive)");
            System.out.println("5. Assigned after date");
            System.out.println("6. Expiring before date");
            System.out.print("Enter choice (1-6): ");
            String choice = scanner.nextLine().trim();
            AssignmentFilter filter = null;
            switch (choice) {
                case "1":
                    System.out.print("Enter username: ");
                    String uname = scanner.nextLine().trim();
                    filter = AssignmentFilters.byUsername(uname);
                    break;
                case "2":
                    System.out.print("Enter role name: ");
                    String rname = scanner.nextLine().trim();
                    filter = AssignmentFilters.byRoleName(rname);
                    break;
                case "3":
                    System.out.print("Enter type (PERMANENT/TEMPORARY): ");
                    String type = scanner.nextLine().trim().toUpperCase();
                    filter = AssignmentFilters.byType(type);
                    break;
                case "4":
                    System.out.print("Enter status (active/inactive): ");
                    String status = scanner.nextLine().trim().toLowerCase();
                    if (status.equals("active")) {
                        filter = AssignmentFilters.activeOnly();
                    } else {
                        filter = AssignmentFilters.inactiveOnly();
                    }
                    break;
                case "5":
                    System.out.print("Enter date (yyyy-MM-dd HH:mm): ");
                    String date = scanner.nextLine().trim();
                    filter = AssignmentFilters.assignedAfter(date);
                    break;
                case "6":
                    System.out.print("Enter date (yyyy-MM-dd HH:mm): ");
                    date = scanner.nextLine().trim();
                    filter = AssignmentFilters.expiringBefore(date);
                    break;
                default:
                    System.out.println("Invalid choice.");
                    return;
            }
            List<RoleAssignment> results = system.getAssignmentManager().findByFilter(filter);
            if (results.isEmpty()) {
                System.out.println("No assignments found.");
            } else {
                System.out.println("Found assignments:");
                System.out.printf("%-10s %-20s %-20s %-10s %-8s %-20s\n", "ID", "User", "Role", "Type", "Status", "Assigned At");
                for (RoleAssignment ra : results) {
                    System.out.printf("%-10s %-20s %-20s %-10s %-8s %-20s\n",
                            ra.assignmentId().substring(0, Math.min(8, ra.assignmentId().length())),
                            ra.user().username(),
                            ra.role().getName(),
                            ra.assignmentType(),
                            ra.isActive() ? "active" : "inactive",
                            ra.metadata().assignedAt());
                }
            }
        });

        parser.registerCommand("permissions-user", "Show all permissions of a user", (scanner, system) -> {
            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();
            User user = system.getUserManager().findByUsername(username).orElse(null);
            if (user == null) {
                System.out.println("User not found.");
                return;
            }
            Set<Permission> perms = system.getAssignmentManager().getUserPermissions(user);
            if (perms.isEmpty()) {
                System.out.println("User has no permissions.");
                return;
            }
            // Группируем по ресурсу
            Map<String, List<Permission>> byResource = perms.stream()
                    .collect(Collectors.groupingBy(Permission::resource));
            System.out.println("Permissions for user " + username + ":");
            for (Map.Entry<String, List<Permission>> entry : byResource.entrySet()) {
                System.out.println("  Resource: " + entry.getKey());
                for (Permission p : entry.getValue()) {
                    System.out.println("    " + p.name() + ": " + p.description());
                }
            }
        });

        parser.registerCommand("permissions-check", "Check if user has a specific permission", (scanner, system) -> {
            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();
            User user = system.getUserManager().findByUsername(username).orElse(null);
            if (user == null) {
                System.out.println("User not found.");
                return;
            }
            System.out.print("Enter permission name (e.g., READ): ");
            String pName = scanner.nextLine().trim();
            System.out.print("Enter resource (e.g., users): ");
            String resource = scanner.nextLine().trim();
            boolean has = system.getAssignmentManager().userHasPermission(user, pName, resource);
            if (has) {
                // Найдём, из какой роли
                List<Role> roles = system.getAssignmentManager().findByUser(user).stream()
                        .filter(RoleAssignment::isActive)
                        .map(RoleAssignment::role)
                        .filter(r -> r.hasPermission(pName, resource))
                        .collect(Collectors.toList());
                System.out.println("Yes, user has this permission from role(s): " +
                        roles.stream().map(Role::getName).collect(Collectors.joining(", ")));
            } else {
                System.out.println("No, user does not have this permission.");
            }
        });

        parser.registerCommand("save", "Save data to file", (scanner, system) -> {
            System.out.print("Enter file path: ");
            String filePath = scanner.nextLine().trim();
            try {
                system.saveToFile(filePath);
                System.out.println("Data saved.");
            } catch (Exception e) {
                System.out.println("Save failed: " + e.getMessage());
            }
        });

        parser.registerCommand("load", "Load data from file", (scanner, system) -> {
            System.out.print("Enter file path: ");
            String filePath = scanner.nextLine().trim();
            try {
                system.loadFromFile(filePath);
                System.out.println("Data loaded.");
            } catch (Exception e) {
                System.out.println("Load failed: " + e.getMessage());
            }
        });
    }
}