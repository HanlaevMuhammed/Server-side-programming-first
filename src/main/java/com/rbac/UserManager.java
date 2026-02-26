package com.rbac;

import java.util.*;
import java.util.stream.Collectors;

public class UserManager implements Repository<User> {
    private final Map<String, User> usersByUsername = new HashMap<>();

    @Override
    public void add(User user) {
        Objects.requireNonNull(user, "User cannot be null");
        String username = user.username();
        if (usersByUsername.containsKey(username)) {
            throw new IllegalArgumentException("User with username '" + username + "' already exists");
        }
        usersByUsername.put(username, user);
    }

    @Override
    public boolean remove(User user) {
        Objects.requireNonNull(user, "User cannot be null");
        return usersByUsername.remove(user.username(), user);
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(usersByUsername.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(usersByUsername.values());
    }

    @Override
    public int count() {
        return usersByUsername.size();
    }

    @Override
    public void clear() {
        usersByUsername.clear();
    }

    // Дополнительные методы
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(usersByUsername.get(username));
    }

    public Optional<User> findByEmail(String email) {
        return usersByUsername.values().stream()
                .filter(user -> user.email().equals(email))
                .findFirst();
    }

    public List<User> findByFilter(UserFilter filter) {
        Objects.requireNonNull(filter, "Filter cannot be null");
        return usersByUsername.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<User> findAll(UserFilter filter, Comparator<User> sorter) {
        Objects.requireNonNull(filter, "Filter cannot be null");
        Objects.requireNonNull(sorter, "Sorter cannot be null");
        return usersByUsername.values().stream()
                .filter(filter::test)
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    public boolean exists(String username) {
        return usersByUsername.containsKey(username);
    }

    public void update(String username, String newFullName, String newEmail) {
        User existing = usersByUsername.get(username);
        if (existing == null) {
            throw new IllegalArgumentException("User with username '" + username + "' not found");
        }
        User updatedUser = User.create(username, newFullName, newEmail);
        usersByUsername.put(username, updatedUser);
    }
}