package com.rbac;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class AssignmentManager implements Repository<RoleAssignment> {
    private final ConcurrentMap<String, RoleAssignment> assignmentsById = new ConcurrentHashMap<>();
    private final UserManager userManager;
    private final RoleManager roleManager;

    public AssignmentManager(UserManager userManager, RoleManager roleManager) {
        this.userManager = userManager;
        this.roleManager = roleManager;
    }

    @Override
    public void add(RoleAssignment assignment) {
        Objects.requireNonNull(assignment, "Assignment cannot be null");
        User user = assignment.user();
        Role role = assignment.role();
        if (!userManager.exists(user.username())) {
            throw new IllegalArgumentException("User " + user.username() + " does not exist");
        }
        if (!roleManager.exists(role.getName())) {
            throw new IllegalArgumentException("Role " + role.getName() + " does not exist");
        }
        // атомарная проверка дубликата
        boolean alreadyAssigned = assignmentsById.values().stream()
                .anyMatch(a -> a.user().equals(user) && a.role().equals(role) && a.isActive());
        if (alreadyAssigned) {
            throw new IllegalArgumentException("User already has an active assignment for role " + role.getName());
        }
        RoleAssignment previous = assignmentsById.putIfAbsent(assignment.assignmentId(), assignment);
        if (previous != null) {
            throw new IllegalArgumentException("Assignment with id " + assignment.assignmentId() + " already exists");
        }
    }

    @Override
    public boolean remove(RoleAssignment assignment) {
        Objects.requireNonNull(assignment, "Assignment cannot be null");
        return assignmentsById.remove(assignment.assignmentId(), assignment);
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        return Optional.ofNullable(assignmentsById.get(id));
    }

    @Override
    public List<RoleAssignment> findAll() {
        return new ArrayList<>(assignmentsById.values());
    }

    @Override
    public int count() {
        return assignmentsById.size();
    }

    @Override
    public void clear() {
        assignmentsById.clear();
    }

    public List<RoleAssignment> findByUser(User user) {
        Objects.requireNonNull(user, "User cannot be null");
        return assignmentsById.values().stream()
                .filter(a -> a.user().equals(user))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByRole(Role role) {
        Objects.requireNonNull(role, "Role cannot be null");
        return assignmentsById.values().stream()
                .filter(a -> a.role().equals(role))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        Objects.requireNonNull(filter, "Filter cannot be null");
        return assignmentsById.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        Objects.requireNonNull(filter, "Filter cannot be null");
        Objects.requireNonNull(sorter, "Sorter cannot be null");
        return assignmentsById.values().stream()
                .filter(filter::test)
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> getActiveAssignments() {
        return assignmentsById.values().stream()
                .filter(RoleAssignment::isActive)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> getExpiredAssignments() {
        return assignmentsById.values().stream()
                .filter(a -> !a.isActive())
                .collect(Collectors.toList());
    }

    public boolean userHasRole(User user, Role role) {
        return assignmentsById.values().stream()
                .anyMatch(a -> a.user().equals(user) && a.role().equals(role) && a.isActive());
    }

    public boolean userHasPermission(User user, String permissionName, String resource) {
        return assignmentsById.values().stream()
                .filter(a -> a.user().equals(user) && a.isActive())
                .map(RoleAssignment::role)
                .anyMatch(role -> role.hasPermission(permissionName, resource));
    }

    public Set<Permission> getUserPermissions(User user) {
        return assignmentsById.values().stream()
                .filter(a -> a.user().equals(user) && a.isActive())
                .flatMap(a -> a.role().getPermissions().stream())
                .collect(Collectors.toSet());
    }

    public void revokeAssignment(String assignmentId) {
        RoleAssignment assignment = assignmentsById.get(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment with id " + assignmentId + " not found");
        }
        if (assignment instanceof PermanentAssignment perm) {
            perm.revoke();
        } else {
            throw new IllegalArgumentException("Only permanent assignments can be revoked, use extend for temporary");
        }
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        RoleAssignment assignment = assignmentsById.get(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment with id " + assignmentId + " not found");
        }
        if (assignment instanceof TemporaryAssignment temp) {
            temp.extend(newExpirationDate);
        } else {
            throw new IllegalArgumentException("Only temporary assignments can be extended");
        }
    }
}

public List<RoleAssignment> findByFilterParallel(AssignmentFilter filter) {
    Objects.requireNonNull(filter, "Filter cannot be null");
    return assignmentsById.values().parallelStream()
            .filter(filter::test)
            .collect(Collectors.toList());
}