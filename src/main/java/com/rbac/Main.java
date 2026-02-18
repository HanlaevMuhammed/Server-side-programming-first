package com.rbac;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Testing User validation ===");
        
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
}
