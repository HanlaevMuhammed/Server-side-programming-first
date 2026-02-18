package com.rbac;

import java.util.Objects;
import java.util.regex.Pattern;

public record Permission(String name, String resource, String description) {
    
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Z_]+$");
    private static final Pattern RESOURCE_PATTERN = Pattern.compile("^[a-z]+$");
    
    // Канонический конструктор с валидацией и нормализацией
    public Permission {
        // Проверка на null
        Objects.requireNonNull(name, "Permission name cannot be null");
        Objects.requireNonNull(resource, "Resource cannot be null");
        Objects.requireNonNull(description, "Description cannot be null");
        
        // Нормализация
        String normalizedName = name.trim().toUpperCase();
        String normalizedResource = resource.trim().toLowerCase();
        String normalizedDescription = description.trim();
        
        // Проверка на пустые строки после нормализации
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("Permission name cannot be empty");
        }
        if (normalizedResource.isEmpty()) {
            throw new IllegalArgumentException("Resource cannot be empty");
        }
        if (normalizedDescription.isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        
        // Валидация name (только заглавные буквы и подчеркивание)
        if (!NAME_PATTERN.matcher(normalizedName).matches()) {
            throw new IllegalArgumentException(
                "Permission name must contain only uppercase letters and underscores"
            );
        }
        
        // Валидация resource (только строчные буквы)
        if (!RESOURCE_PATTERN.matcher(normalizedResource).matches()) {
            throw new IllegalArgumentException(
                "Resource must contain only lowercase letters"
            );
        }
        
        // Присваиваем нормализованные значения
        name = normalizedName;
        resource = normalizedResource;
        description = normalizedDescription;
    }
    
    // Метод для форматированного вывода
    public String format() {
        return String.format("%s on %s: %s", name, resource, description);
    }
    
    // Метод для поиска по шаблонам
    public boolean matches(String namePattern, String resourcePattern) {
        boolean nameMatches = namePattern == null || 
                              namePattern.isEmpty() || 
                              name.contains(namePattern) ||
                              name.matches(namePattern);
        
        boolean resourceMatches = resourcePattern == null || 
                                  resourcePattern.isEmpty() || 
                                  resource.contains(resourcePattern) ||
                                  resource.matches(resourcePattern);
        
        return nameMatches && resourceMatches;
    }
}
