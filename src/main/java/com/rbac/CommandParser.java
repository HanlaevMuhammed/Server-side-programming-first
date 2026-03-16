package com.rbac;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CommandParser {
    private final Map<String, Command> commands = new HashMap<>();
    private final Map<String, String> descriptions = new HashMap<>();

    public void registerCommand(String name, String description, Command command) {
        commands.put(name, command);
        descriptions.put(name, description);
    }

    public void executeCommand(String commandName, Scanner scanner, RBACSystem system) {
        Command cmd = commands.get(commandName);
        if (cmd == null) {
            System.out.println("Unknown command: " + commandName);
            System.out.println("Type 'help' for available commands.");
            return;
        }
        try {
            cmd.execute(scanner, system);
        } catch (Exception e) {
            System.out.println("Error executing command: " + e.getMessage());
        }
    }

    public void printHelp() {
        System.out.println("Available commands:");
        descriptions.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.out.printf("  %-20s %s\n", e.getKey(), e.getValue()));
    }

    public void parseAndExecute(String input, Scanner scanner, RBACSystem system) {
        String[] parts = input.trim().split("\\s+", 2);
        String cmdName = parts[0].toLowerCase();
        executeCommand(cmdName, scanner, system);
    }
}