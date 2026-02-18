package com.rbac;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) {
    
    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    // Компактный конструктор с валидацией
    public AssignmentMetadata {
        if (assignedBy == null || assignedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("AssignedBy cannot be null or empty");
        }
        if (assignedAt == null || assignedAt.trim().isEmpty()) {
            throw new IllegalArgumentException("AssignedAt cannot be null or empty");
        }
        // reason может быть null или пустым (опционально)
    }
    
    // Статический метод для создания с текущей датой/временем
    public static AssignmentMetadata now(String assignedBy, String reason) {
        String now = LocalDateTime.now().format(FORMATTER);
        return new AssignmentMetadata(assignedBy, now, reason);
    }
    
    // Метод для форматированного вывода
    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Assigned by: %s at %s", assignedBy, assignedAt));
        if (reason != null && !reason.trim().isEmpty()) {
            sb.append(String.format("\nReason: %s", reason));
        }
        return sb.toString();
    }
    
    // Переопределяем toString для удобного вывода
    @Override
    public String toString() {
        return format();
    }
}
