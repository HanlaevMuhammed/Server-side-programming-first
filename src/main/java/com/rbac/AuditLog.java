package com.rbac;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditLog {
    private static final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private static volatile boolean running = true;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        Thread worker = new Thread(() -> {
            while (running || !queue.isEmpty()) {
                try {
                    String entry = queue.take();
                    System.out.println("[AUDIT] " + entry);
                    // можно также записывать в файл
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        worker.setDaemon(true);
        worker.start();
    }

    public static void log(String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        queue.offer(timestamp + " " + message);
    }

    public static void shutdown() {
        running = false;
    }
}