package com.rbac;

public class Main {
    public static void main(String[] args) {
        testUser();
        testPermission();
        testRole();
        testAssignmentMetadata();
        testAssignments();
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
            System.out.println("✓ Success: " + p2.format() + " (normalized)");
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
        System.out.println("matches(READ, users): " + p.matches("READ", "users"));
        System.out.println("matches(READ, null): " + p.matches("READ", null));
        System.out.println("matches(null, users): " + p.matches(null, "users"));
        System.out.println("matches(WRITE, users): " + p.matches("WRITE", "users"));
        System.out.println("matches(READ, reports): " + p.matches("READ", "reports"));
    }
    
    private static void testRole() {
        System.out.println("\n=== Testing Role ===");
        
        // Создаем несколько разрешений
        Permission readUsers = new Permission("READ", "users", "Can view users");
        Permission writeUsers = new Permission("WRITE", "users", "Can create/edit users");
        Permission deleteUsers = new Permission("DELETE", "users", "Can delete users");
        Permission readReports = new Permission("READ", "reports", "Can view reports");
        
        // Создаем роль
        Role adminRole = new Role("Administrator", "Full system access");
        System.out.println("✓ Role created: " + adminRole);
        
        // Добавляем разрешения
        adminRole.addPermission(readUsers);
        adminRole.addPermission(writeUsers);
        adminRole.addPermission(deleteUsers);
        adminRole.addPermission(readReports);
        System.out.println("✓ Added 4 permissions");
        
        // Проверяем наличие разрешений
        System.out.println("\n=== Testing hasPermission ===");
        System.out.println("Has READ on users: " + 
            adminRole.hasPermission("READ", "users"));
        System.out.println("Has WRITE on users: " + 
            adminRole.hasPermission("WRITE", "users"));
        System.out.println("Has DELETE on reports: " + 
            adminRole.hasPermission("DELETE", "reports"));
        
        // Проверяем contains через объект Permission
        System.out.println("\n=== Testing contains permission object ===");
        System.out.println("Contains READ on users: " + 
            adminRole.hasPermission(readUsers));
        
        // Создаем временный объект для сравнения
        Permission deleteReports = new Permission("DELETE", "reports", "Can delete reports");
        System.out.println("Contains DELETE on reports: " + 
            adminRole.hasPermission(deleteReports));
        
        // Удаляем разрешение
        adminRole.removePermission(readReports);
        System.out.println("\n✓ Removed READ on reports");
        System.out.println("Has READ on reports after removal: " + 
            adminRole.hasPermission("READ", "reports"));
        
        // Проверяем неизменяемость возвращаемой коллекции
        System.out.println("\n=== Testing unmodifiable collection ===");
        try {
            adminRole.getPermissions().clear();
            System.out.println("✗ Should not be able to modify");
        } catch (UnsupportedOperationException e) {
            System.out.println("✓ Correctly prevents modification");
        }
        
        // Тестируем форматированный вывод
        System.out.println("\n=== Testing format() ===");
        System.out.println(adminRole.format());
        
        // Тестируем equals/hashCode
        System.out.println("=== Testing equals/hashCode ===");
        Role anotherAdmin = new Role("Administrator", "Full system access");
        System.out.println("Roles with same name but different IDs are equal? " + 
            adminRole.equals(anotherAdmin));
        
        // Создаем роль с предопределенным ID
        Role loadedRole = Role.createFromExisting(
            adminRole.getId(), 
            "Manager", 
            "Manager role", 
            adminRole.getPermissions()
        );
        System.out.println("Role created from existing with same ID are equal? " + 
            adminRole.equals(loadedRole));
    }
    
    private static void testAssignmentMetadata() {
        System.out.println("\n=== Testing AssignmentMetadata ===");
        
        // Тест 1: Создание с указанием всех полей
        AssignmentMetadata meta1 = new AssignmentMetadata("admin", "2026-02-18 20:30", "Initial setup");
        System.out.println("✓ Created: " + meta1.format());
        
        // Тест 2: Создание с текущей датой
        AssignmentMetadata meta2 = AssignmentMetadata.now("john_doe", "Project assignment");
        System.out.println("✓ Created now: " + meta2.format());
        
        // Тест 3: Создание без причины (опционально)
        AssignmentMetadata meta3 = AssignmentMetadata.now("jane_doe", null);
        System.out.println("✓ Created without reason: " + meta3.format());
        
        // Тест 4: Валидация - пустой assignedBy
        try {
            AssignmentMetadata meta4 = new AssignmentMetadata("", "2026-02-18 20:30", "test");
            System.out.println("✗ Should fail: " + meta4);
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Correctly failed: " + e.getMessage());
        }
        
        // Тест 5: Валидация - null assignedAt
        try {
            AssignmentMetadata meta5 = new AssignmentMetadata("admin", null, "test");
            System.out.println("✗ Should fail: " + meta5);
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Correctly failed: " + e.getMessage());
        }
    }
    
    private static void testAssignments() {
        System.out.println("\n=== Testing Assignments ===");
        
        // Создаем тестовые данные
        User user = User.create("john_doe", "John Doe", "john@example.com");
        
        Permission readUsers = new Permission("READ", "users", "Can read users");
        Permission writeUsers = new Permission("WRITE", "users", "Can write users");
        
        Role adminRole = new Role("Administrator", "Admin role");
        adminRole.addPermission(readUsers);
        adminRole.addPermission(writeUsers);
        
        Role viewerRole = new Role("Viewer", "View only");
        viewerRole.addPermission(readUsers);
        
        AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Test assignment");
        
        System.out.println("\n=== Testing PermanentAssignment ===");
        
        // Создаем постоянное назначение
        PermanentAssignment permAssign = new PermanentAssignment(user, adminRole, metadata);
        System.out.println("✓ Created: " + permAssign);
        System.out.println("Type: " + permAssign.assignmentType());
        System.out.println("Active: " + permAssign.isActive());
        System.out.println("Revoked: " + permAssign.isRevoked());
        System.out.println("\nSummary:\n" + permAssign.summary());
        
        // Отзываем назначение
        permAssign.revoke();
        System.out.println("\nAfter revoke:");
        System.out.println("Active: " + permAssign.isActive());
        System.out.println("Revoked: " + permAssign.isRevoked());
        System.out.println("Summary:\n" + permAssign.summary());
        
        System.out.println("\n=== Testing TemporaryAssignment ===");
        
        // Создаем временное назначение (истекает через 7 дней)
        String futureDate = java.time.LocalDateTime.now().plusDays(7).format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        
        TemporaryAssignment tempAssign = new TemporaryAssignment(
            user, viewerRole, metadata, futureDate, false);
        
        System.out.println("✓ Created: " + tempAssign);
        System.out.println("Type: " + tempAssign.assignmentType());
        System.out.println("Expires: " + tempAssign.getExpiresAt());
        System.out.println("Active: " + tempAssign.isActive());
        System.out.println("Expired: " + tempAssign.isExpired());
        System.out.println("Time remaining: " + tempAssign.getTimeRemaining());
        System.out.println("\nSummary:\n" + tempAssign.summary());
        
        // Продлеваем назначение
        String laterDate = java.time.LocalDateTime.now().plusDays(30).format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        tempAssign.extend(laterDate);
        System.out.println("\nAfter extension:");
        System.out.println("Expires: " + tempAssign.getExpiresAt());
        System.out.println("Time remaining: " + tempAssign.getTimeRemaining());
        
        // Тест с auto-renew
        TemporaryAssignment autoRenewAssign = new TemporaryAssignment(
            user, viewerRole, metadata, futureDate, true);
        System.out.println("\nAuto-renew enabled: " + autoRenewAssign.isAutoRenew());
        
        // Создаем просроченное назначение для теста
        String pastDate = java.time.LocalDateTime.now().minusDays(1).format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        
        TemporaryAssignment expiredAssign = new TemporaryAssignment(
            user, viewerRole, metadata, pastDate, false);
        System.out.println("\nExpired assignment:");
        System.out.println("Expires: " + expiredAssign.getExpiresAt());
        System.out.println("Expired: " + expiredAssign.isExpired());
        System.out.println("Active: " + expiredAssign.isActive());
        System.out.println("Time remaining: " + expiredAssign.getTimeRemaining());
    }
}
