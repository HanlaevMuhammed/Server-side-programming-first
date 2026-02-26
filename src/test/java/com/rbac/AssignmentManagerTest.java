package com.rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentManagerTest {
    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;
    private User user;
    private Role adminRole;
    private Role viewerRole;
    private Permission readUsers;
    private AssignmentMetadata metadata;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        roleManager = new RoleManager();
        assignmentManager = new AssignmentManager(userManager, roleManager);

        user = User.create("john_doe", "John Doe", "john@example.com");
        userManager.add(user);

        readUsers = new Permission("READ", "users", "Can read");
        Permission writeUsers = new Permission("WRITE", "users", "Can write");
        adminRole = new Role("Administrator", "Admin");
        adminRole.addPermission(readUsers);
        adminRole.addPermission(writeUsers);
        viewerRole = new Role("Viewer", "Viewer");
        viewerRole.addPermission(readUsers);

        roleManager.add(adminRole);
        roleManager.add(viewerRole);

        metadata = AssignmentMetadata.now("admin", "Test");
    }

    @Test
    void addPermanentAssignment() {
        PermanentAssignment pa = new PermanentAssignment(user, adminRole, metadata);
        assignmentManager.add(pa);
        assertEquals(1, assignmentManager.count());
        Optional<RoleAssignment> found = assignmentManager.findById(pa.assignmentId());
        assertTrue(found.isPresent());
    }

    @Test
    void addTemporaryAssignment() {
        String future = LocalDateTime.now().plusDays(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        TemporaryAssignment ta = new TemporaryAssignment(user, viewerRole, metadata, future, false);
        assignmentManager.add(ta);
        assertEquals(1, assignmentManager.count());
    }

    @Test
    void addDuplicateAssignmentThrows() {
        PermanentAssignment pa = new PermanentAssignment(user, adminRole, metadata);
        assignmentManager.add(pa);
        PermanentAssignment pa2 = new PermanentAssignment(user, adminRole, metadata);
        assertThrows(IllegalArgumentException.class, () -> assignmentManager.add(pa2));
    }

    @Test
    void addAssignmentForNonexistentUserThrows() {
        User ghost = User.create("ghost", "Ghost", "ghost@example.com");
        PermanentAssignment pa = new PermanentAssignment(ghost, adminRole, metadata);
        assertThrows(IllegalArgumentException.class, () -> assignmentManager.add(pa));
    }

    @Test
    void findByUser() {
        PermanentAssignment pa = new PermanentAssignment(user, adminRole, metadata);
        assignmentManager.add(pa);
        List<RoleAssignment> result = assignmentManager.findByUser(user);
        assertEquals(1, result.size());
        assertEquals(pa, result.get(0));
    }

    @Test
    void userHasRole() {
        PermanentAssignment pa = new PermanentAssignment(user, adminRole, metadata);
        assignmentManager.add(pa);
        assertTrue(assignmentManager.userHasRole(user, adminRole));
        assertFalse(assignmentManager.userHasRole(user, viewerRole));
    }

    @Test
    void userHasPermission() {
        PermanentAssignment pa = new PermanentAssignment(user, adminRole, metadata);
        assignmentManager.add(pa);
        assertTrue(assignmentManager.userHasPermission(user, "READ", "users"));
        assertTrue(assignmentManager.userHasPermission(user, "WRITE", "users"));
        assertFalse(assignmentManager.userHasPermission(user, "DELETE", "users"));
    }

    @Test
    void getUserPermissions() {
        PermanentAssignment pa = new PermanentAssignment(user, adminRole, metadata);
        assignmentManager.add(pa);
        Set<Permission> perms = assignmentManager.getUserPermissions(user);
        assertEquals(2, perms.size());
        assertTrue(perms.contains(readUsers));
    }

    @Test
    void revokeAssignment() {
        PermanentAssignment pa = new PermanentAssignment(user, adminRole, metadata);
        assignmentManager.add(pa);
        assignmentManager.revokeAssignment(pa.assignmentId());
        assertFalse(pa.isActive());
        assertTrue(pa.isRevoked());
    }

    @Test
    void extendTemporaryAssignment() {
        String future = LocalDateTime.now().plusDays(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        TemporaryAssignment ta = new TemporaryAssignment(user, viewerRole, metadata, future, false);
        assignmentManager.add(ta);
        String newDate = LocalDateTime.now().plusDays(10).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        assignmentManager.extendTemporaryAssignment(ta.assignmentId(), newDate);
        assertEquals(newDate, ta.getExpiresAt());
    }
}