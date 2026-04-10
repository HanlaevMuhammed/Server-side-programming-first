package com.rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserManagerTest {
    private UserManager userManager;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        user1 = User.create("john_doe", "John Doe", "john@example.com");
        user2 = User.create("jane_doe", "Jane Doe", "jane@example.com");
    }

    @Test
    void addAndFind() {
        userManager.add(user1);
        assertEquals(1, userManager.count());
        Optional<User> found = userManager.findByUsername("john_doe");
        assertTrue(found.isPresent());
        assertEquals(user1, found.get());
    }

    @Test
    void addDuplicateThrows() {
        userManager.add(user1);
        assertThrows(IllegalArgumentException.class, () -> userManager.add(user1));
    }

    @Test
    void remove() {
        userManager.add(user1);
        assertTrue(userManager.remove(user1));
        assertEquals(0, userManager.count());
        assertFalse(userManager.remove(user1));
    }

    @Test
    void findById() {
        userManager.add(user1);
        Optional<User> found = userManager.findById("john_doe");
        assertTrue(found.isPresent());
        assertEquals(user1, found.get());
    }

    @Test
    void findByEmail() {
        userManager.add(user1);
        Optional<User> found = userManager.findByEmail("john@example.com");
        assertTrue(found.isPresent());
        assertEquals(user1, found.get());
    }

    @Test
    void exists() {
        userManager.add(user1);
        assertTrue(userManager.exists("john_doe"));
        assertFalse(userManager.exists("jane_doe"));
    }

    @Test
    void update() {
        userManager.add(user1);
        userManager.update("john_doe", "John Updated", "john.new@example.com");
        Optional<User> updated = userManager.findByUsername("john_doe");
        assertTrue(updated.isPresent());
        assertEquals("John Updated", updated.get().fullName());
        assertEquals("john.new@example.com", updated.get().email());
    }

    @Test
    void findByFilter() {
        userManager.add(user1);
        userManager.add(user2);
        UserFilter filter = UserFilters.byUsernameContains("john");
        List<User> result = userManager.findByFilter(filter);
        assertEquals(1, result.size());
        assertEquals(user1, result.get(0));
    }

    @Test
    void findByFilterParallel() {
        userManager.add(user1);
        userManager.add(user2);
        List<User> result = userManager.findByFilterParallel(UserFilters.byEmailDomain("@example.com"));
        assertEquals(2, result.size());
    }

    @Test
    void findAllWithFilterAndSorter() {
        userManager.add(user1);
        userManager.add(user2);
        UserFilter filter = UserFilters.byFullNameContains("Doe");
        List<User> result = userManager.findAll(filter, UserSorters.byUsername());
        assertEquals(2, result.size());
        assertEquals("jane_doe", result.get(0).username());
        assertEquals("john_doe", result.get(1).username());
    }
}