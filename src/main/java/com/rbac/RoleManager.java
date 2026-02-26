package com.rbac;

import java.util.*;
import java.util.stream.Collectors;

public class RoleManager implements Repository<Role> {
    private final Map<String, Role> rolesById = new HashMap<>();
    private final Map<String, Role> rolesByName = new HashMap<>();

    @Override
    public void add(Role role) {
        Objects.requireNonNull(role, "Role cannot be null");
        String id = role.getId();
        String name = role.getName();
        if (rolesById.containsKey(id)) {
            throw new IllegalArgumentException("Role with id '" + id + "' already exists");
        }
        if (rolesByName.containsKey(name)) {
            throw new IllegalArgumentException("Role with name '" + name + "' already exists");
        }
        rolesById.put(id, role);
        rolesByName.put(name, role);
    }

    @Override
    public boolean remove(Role role) {
        Objects.requireNonNull(role, "Role cannot be null");
        // Проверка на наличие назначений будет добавлена позже через AssignmentManager
        Role removed = rolesById.remove(role.getId());
        if (removed != null) {
            rolesByName.remove(role.getName());
            return true;
        }
        return false;
    }

    @Override
    public Optional<Role> findById(String id) {
        return Optional.ofNullable(rolesById.get(id));
    }

    @Override
    public List<Role> findAll() {
        return new ArrayList<>(rolesById.values());
    }

    @Override
    public int count() {
        return rolesById.size();
    }

    @Override
    public void clear() {
        rolesById.clear();
        rolesByName.clear();
    }

    public Optional<Role> findByName(String name) {
        return Optional.ofNullable(rolesByName.get(name));
    }

    public List<Role> findByFilter(RoleFilter filter) {
        Objects.requireNonNull(filter, "Filter cannot be null");
        return rolesById.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        Objects.requireNonNull(filter, "Filter cannot be null");
        Objects.requireNonNull(sorter, "Sorter cannot be null");
        return rolesById.values().stream()
                .filter(filter::test)
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    public boolean exists(String name) {
        return rolesByName.containsKey(name);
    }

    public void addPermissionToRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Role with name '" + roleName + "' not found");
        }
        role.addPermission(permission);
    }

    public void removePermissionFromRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Role with name '" + roleName + "' not found");
        }
        role.removePermission(permission);
    }

    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        return rolesById.values().stream()
                .filter(role -> role.hasPermission(permissionName, resource))
                .collect(Collectors.toList());
    }
}