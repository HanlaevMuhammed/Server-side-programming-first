package com.rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
}