package com.rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RoleManagerTest {
    private RoleManager roleManager;
    private Role adminRole;
    private Role viewerRole;
    private Permission readUsers;
    private Permission writeUsers;

    @BeforeEach
    void setUp() {
        roleManager = new RoleManager();
        readUsers = new Permission("READ", "users", "Can read users");
        writeUsers = new Permission("WRITE", "users", "Can write users");
        adminRole = new Role("Administrator", "Admin role");
        adminRole.addPermission(readUsers);
        adminRole.addPermission(writeUsers);
        viewerRole = new Role("Viewer", "View only");
        viewerRole.addPermission(readUsers);
    }

    @Test
    void addAndFindById() {
        roleManager.add(adminRole);
        assertEquals(1, roleManager.count());
        Optional<Role> found = roleManager.findById(adminRole.getId());
        assertTrue(found.isPresent());
        assertEquals(adminRole, found.get());
    }

    @Test
    void addDuplicateNameThrows() {
        roleManager.add(adminRole);
        Role anotherAdmin = new Role("Administrator", "Another admin");
        assertThrows(IllegalArgumentException.class, () -> roleManager.add(anotherAdmin));
    }

    @Test
    void findByName() {
        roleManager.add(adminRole);
        Optional<Role> found = roleManager.findByName("Administrator");
        assertTrue(found.isPresent());
        assertEquals(adminRole, found.get());
    }

    @Test
    void remove() {
        roleManager.add(adminRole);
        assertTrue(roleManager.remove(adminRole));
        assertEquals(0, roleManager.count());
        assertFalse(roleManager.remove(adminRole));
    }

    @Test
    void findByFilter() {
        roleManager.add(adminRole);
        roleManager.add(viewerRole);
        RoleFilter filter = RoleFilters.hasPermission("READ", "users");
        List<Role> result = roleManager.findByFilter(filter);
        assertEquals(2, result.size());
    }

    @Test
    void addPermissionToRole() {
        roleManager.add(viewerRole);
        Permission deleteUsers = new Permission("DELETE", "users", "Can delete");
        roleManager.addPermissionToRole("Viewer", deleteUsers);
        Optional<Role> updated = roleManager.findByName("Viewer");
        assertTrue(updated.isPresent());
        assertTrue(updated.get().hasPermission(deleteUsers));
    }

    @Test
    void findRolesWithPermission() {
        roleManager.add(adminRole);
        roleManager.add(viewerRole);
        List<Role> result = roleManager.findRolesWithPermission("READ", "users");
        assertEquals(2, result.size());
        result = roleManager.findRolesWithPermission("WRITE", "users");
        assertEquals(1, result.size());
    }
}