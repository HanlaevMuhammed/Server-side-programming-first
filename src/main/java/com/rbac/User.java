package com.rbac;

import java.util.regex.Pattern;

public record User(String username, String fullName, String email) {
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");
    
    // Компактный конструктор для валидации
    public User {
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }
        if (fullName == null) {
            throw new IllegalArgumentException("Full name cannot be null");
        }
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
        
        // Проверка на пустые строки (после trim)
        String trimmedUsername = username.trim();
        String trimmedFullName = fullName.trim();
        String trimmedEmail = email.trim();
        
        if (trimmedUsername.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (trimmedFullName.isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }
        if (trimmedEmail.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        
        // Валидация username (3-20 символов, только буквы, цифры, подчеркивание)
        if (!USERNAME_PATTERN.matcher(trimmedUsername).matches()) {
            throw new IllegalArgumentException(
                "Username must be 3-20 characters long and contain only letters, digits, and underscore"
            );
        }
        
        // Валидация email (должен содержать @ и точку после @)
        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
    
    // Статический фабричный метод
    public static User create(String username, String fullName, String email) {
        return new User(username, fullName, email);
    }
    
    // Метод для форматированного вывода
    public String format() {
        return String.format("%s (%s) <%s>", username, fullName, email);
    }
}
