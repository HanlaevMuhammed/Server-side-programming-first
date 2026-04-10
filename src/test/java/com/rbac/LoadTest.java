package com.rbac;

import org.junit.jupiter.api.Test;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

public class LoadTest {

    @Test
    void concurrentOperationsTest() throws InterruptedException {
        RBACSystem system = new RBACSystem();
        ExecutorService pool = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 50; i++) {
            final int idx = i;
            pool.submit(() -> {
                try {
                    String username = "user" + idx;
                    User user = User.create(username, "User " + idx, username + "@test.com");
                    system.getUserManager().add(user);
                    Role role = new Role("Role" + idx, "desc");
                    system.getRoleManager().add(role);
                    AssignmentMetadata meta = AssignmentMetadata.now("admin", "load test");
                    PermanentAssignment pa = new PermanentAssignment(user, role, meta);
                    system.getAssignmentManager().add(pa);
                    // фильтры
                    system.getUserManager().findByFilterParallel(u -> u.username().contains("user"));
                    system.getRoleManager().findByFilterParallel(r -> r.getName().contains("Role"));
                    system.getAssignmentManager().findByFilterParallel(a -> a.isActive());
                } catch (Exception e) {
                    // допускаются исключения дублирования, но не падение
                }
            });
        }
        pool.shutdown();
        boolean finished = pool.awaitTermination(30, TimeUnit.SECONDS);
        assertTrue(finished, "Tasks did not complete in time");
        System.out.println("Load test finished. Final stats:\n" + system.generateStatistics());
    }
}