package com.rbac;

import java.util.Objects;
import java.util.UUID;

public abstract class AbstractRoleAssignment implements RoleAssignment {
    private final String assignmentId;
    private final User user;
    private final Role role;
    private final AssignmentMetadata metadata;
    
    public AbstractRoleAssignment(User user, Role role, AssignmentMetadata metadata) {
        this.assignmentId = generateId();
        this.user = Objects.requireNonNull(user, "User cannot be null");
        this.role = Objects.requireNonNull(role, "Role cannot be null");
        this.metadata = Objects.requireNonNull(metadata, "Metadata cannot be null");
    }
    
    // Защищенный конструктор для создания с существующим ID (например, при загрузке из файла)
    protected AbstractRoleAssignment(String assignmentId, User user, Role role, AssignmentMetadata metadata) {
        this.assignmentId = assignmentId;
        this.user = Objects.requireNonNull(user, "User cannot be null");
        this.role = Objects.requireNonNull(role, "Role cannot be null");
        this.metadata = Objects.requireNonNull(metadata, "Metadata cannot be null");
    }
    
    private String generateId() {
        return "assign_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    @Override
    public String assignmentId() {
        return assignmentId;
    }
    
    @Override
    public User user() {
        return user;
    }
    
    @Override
    public Role role() {
        return role;
    }
    
    @Override
    public AssignmentMetadata metadata() {
        return metadata;
    }
    
    // Абстрактные методы
    @Override
    public abstract boolean isActive();
    
    @Override
    public abstract String assignmentType();
    
    // Общий метод для краткого описания
    public String summary() {
        String status = isActive() ? "ACTIVE" : "INACTIVE";
        return String.format("[%s] %s assigned to %s by %s at %s\nReason: %s\nStatus: %s",
            assignmentType(),
            role.getName(),
            user.username(),
            metadata.assignedBy(),
            metadata.assignedAt(),
            metadata.reason() != null ? metadata.reason() : "No reason provided",
            status);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractRoleAssignment that = (AbstractRoleAssignment) o;
        return assignmentId.equals(that.assignmentId);
    }
    
    @Override
    public int hashCode() {
        return assignmentId.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("%s{id='%s', user='%s', role='%s', active=%s}",
            getClass().getSimpleName(),
            assignmentId,
            user.username(),
            role.getName(),
            isActive());
    }
}
