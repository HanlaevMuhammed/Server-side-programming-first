package com.rbac;

public class Main {
    public static void main(String[] args) {
        testUser();
        testPermission();
    }
    
    private static void testUser() {
        System.out.println("\n=== Testing User validation ===");
        
        // Тест 1: Успешное создание
        try {
            User user1 = User.create("john_doe", "John Doe", "john@example.com");
            System.out.println("✓ Success: " + user1.format());
        } catch (IllegalArgumentException e) {
            System.out.println("✗ Failed: " + e.getMessage());
        }
        
        // Тест 2: Слишком короткий username
        try {
            User user2 = User.create("jo", "John Doe", "john@example.com");
            System.out.println("✗ Should fail: " + user2.format());
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Correctly failed: " + e.getMessage());
        }
        
        // Тест 3: Спецсимволы в username
        try {
            User user3 = User.create("john@doe", "John Doe", "john@example.com");
            System.out.println("✗ Should fail: " + user3.format());
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Correctly failed: " + e.getMessage());
        }
        
        // Тест 4: Неверный email
        try {
            User user4 = User.create("jane_doe", "Jane Doe", "jane.example.com");
            System.out.println("✗ Should fail: " + user4.format());
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Correctly failed: " + e.getMessage());
        }
        
        // Тест 5: Пустое полное имя
        try {
            User user5 = User.create("bob", "", "bob@example.com");
            System.out.println("✗ Should fail: " + user5.format());
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Correctly failed: " + e.getMessage());
        }
    }
    
    private static void testPermission() {
        System.out.println("\n=== Testing Permission validation ===");
        
        // Тест 1: Успешное создание
        try {
            Permission p1 = new Permission("READ", "users", "Can read user data");
            System.out.println("✓ Success: " + p1.format());
        } catch (IllegalArgumentException e) {
            System.out.println("✗ Failed: " + e.getMessage());
        }
        
        // Тест 2: Автоматическое преобразование регистра
        try {
            Permission p2 = new Permission("  write  ", "  REPORTS  ", "  Can modify reports  ");
            System.out.println("✓ Success: " + p2.format());
        } catch (IllegalArgumentException e) {
            System.out.println("✗ Failed: " + e.getMessage());
        }
        
        // Тест 3: Неверный формат name (строчные буквы)
        try {
            Permission p3 = new Permission("Read", "users", "Description");
            System.out.println("✗ Should fail: " + p3.format());
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Correctly failed: " + e.getMessage());
        }
        
        // Тест 4: Неверный формат resource (заглавные буквы)
        try {
            Permission p4 = new Permission("READ", "USERS", "Description");
            System.out.println("✗ Should fail: " + p4.format());
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Correctly failed: " + e.getMessage());
        }
        
        // Тест 5: Пустое описание
        try {
            Permission p5 = new Permission("READ", "users", "");
            System.out.println("✗ Should fail: " + p5.format());
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Correctly failed: " + e.getMessage());
        }
        
        System.out.println("\n=== Testing Permission.matches() ===");
        
        Permission p = new Permission("READ", "users", "Can read users");
        
        // Тест matches
        System.out.println("matches(READ, users): " + p.matches("READ", "users")); // true
        System.out.println("matches(READ, null): " + p.matches("READ", null)); // true
        System.out.println("matches(null, users): " + p.matches(null, "users")); // true
        System.out.println("matches(WRITE, users): " + p.matches("WRITE", "users")); // false
        System.out.println("matches(READ, reports): " + p.matches("READ", "reports")); // false
    }
}
