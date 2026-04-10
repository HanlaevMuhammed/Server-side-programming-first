package com.rbac;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class UserManager implements Repository<User> {
    private final ConcurrentMap<String, User> usersByUsername = new ConcurrentHashMap<>();

    @Override
    public void add(User user) {
        Objects.requireNonNull(user, "User cannot be null");
        String username = user.username();
        User previous = usersByUsername.putIfAbsent(username, user);
        if (previous != null) {
            throw new IllegalArgumentException("User with username '" + username + "' already exists");
        }
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
        usersByUsername.compute(username, (key, existing) -> {
            if (existing == null) {
                throw new IllegalArgumentException("User with username '" + username + "' not found");
            }
            return User.create(username, newFullName, newEmail);
        });
    }
}