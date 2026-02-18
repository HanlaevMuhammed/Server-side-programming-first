package com.rbac;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Role {
    private final String id;
    private final String name;
    private final String description;
    private final Set<Permission> permissions;
    
    // Конструктор
    public Role(String name, String description) {
        this.id = generateId();
        this.name = validateName(name);
        this.description = validateDescription(description);
        this.permissions = new HashSet<>();
    }
    
    // Приватный конструктор для создания с уже существующим ID (например, при загрузке из файла)
    private Role(String id, String name, String description, Set<Permission> permissions) {
        this.id = id;
        this.name = validateName(name);
        this.description = validateDescription(description);
        this.permissions = new HashSet<>(permissions);
    }
    
    // Генерация уникального ID
    private String generateId() {
        return "role_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    // Валидация имени
    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be null or empty");
        }
        return name.trim();
    }
    
    // Валидация описания
    private String validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Role description cannot be null or empty");
        }
        return description.trim();
    }
    
    // Геттеры
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    // Методы для управления правами
    public void addPermission(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Permission cannot be null");
        }
        permissions.add(permission);
    }
    
    public void removePermission(Permission permission) {
        permissions.remove(permission);
    }
    
    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }
    
    public boolean hasPermission(String permissionName, String resource) {
        return permissions.stream()
                .anyMatch(p -> p.name().equals(permissionName) && 
                              p.resource().equals(resource));
    }
    
    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }
    
    // Форматированный вывод
    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Role: %s [ID: %s]\n", name, id));
        sb.append(String.format("Description: %s\n", description));
        sb.append(String.format("Permissions (%d):\n", permissions.size()));
        
        for (Permission p : permissions) {
            sb.append(String.format(" - %s\n", p.format()));
        }
        
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return id.equals(role.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("Role{id='%s', name='%s', permissions=%d}", 
                           id, name, permissions.size());
    }
    
    // Фабричный метод для создания копии (например, при загрузке из файла)
    public static Role createFromExisting(String id, String name, String description, 
                                         Set<Permission> permissions) {
        return new Role(id, name, description, permissions);
    }
}
