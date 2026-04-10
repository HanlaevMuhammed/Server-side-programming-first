package com.rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static org.junit.jupiter.api.Assertions.*;

class RBACSystemTest {
    private RBACSystem system;

    @BeforeEach
    void setUp() {
        system = new RBACSystem();
    }

    @Test
    void initializeCreatesDefaultData() {
        assertTrue(system.getUserManager().exists("admin"));
        assertTrue(system.getRoleManager().exists("Admin"));
        assertTrue(system.getRoleManager().exists("Manager"));
        assertTrue(system.getRoleManager().exists("Viewer"));
        assertEquals(1, system.getAssignmentManager().count());
    }

    @Test
    void generateStatisticsContainsExpectedInfo() {
        String stats = system.generateStatistics();
        assertTrue(stats.contains("Users: 1"));
        assertTrue(stats.contains("Roles: 3"));
        assertTrue(stats.contains("Assignments: 1 total"));
        assertTrue(stats.contains("Top 3 roles"));
    }

    @Test
    void setAndGetCurrentUser() {
        system.setCurrentUser("testuser");
        assertEquals("testuser", system.getCurrentUser());
    }

    @Test
    void saveAndLoadRoundTrip() throws Exception {
        User user = User.create("alex_user", "Alex User", "alex@example.com");
        system.getUserManager().add(user);
        Role role = new Role("Operator", "Ops role");
        role.addPermission(new Permission("READ", "users", "Read users"));
        system.getRoleManager().add(role);
        String future = LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        TemporaryAssignment assignment = new TemporaryAssignment(
                user, role, AssignmentMetadata.now("admin", "test"), future, false);
        system.getAssignmentManager().add(assignment);

        Path tempFile = Files.createTempFile("rbac-", ".txt");
        system.saveToFile(tempFile.toString());

        RBACSystem loaded = new RBACSystem();
        loaded.loadFromFile(tempFile.toString());

        assertTrue(loaded.getUserManager().exists("alex_user"));
        assertTrue(loaded.getRoleManager().exists("Operator"));
        assertFalse(loaded.getAssignmentManager().findByFilter(a -> a.user().username().equals("alex_user")).isEmpty());
    }
}