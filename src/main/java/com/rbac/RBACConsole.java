package com.rbac;

import java.util.Scanner;

public class RBACConsole {
    public static void main(String[] args) {
        RBACSystem system = new RBACSystem();
        CommandParser parser = new CommandParser();
        CommandRegistry.registerAll(parser);

        Scanner scanner = new Scanner(System.in);
        System.out.println("RBAC System Console. Type 'help' for commands.");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;
            if (input.equalsIgnoreCase("exit")) {
                System.out.print("Exit? (yes/no): ");
                String confirm = scanner.nextLine().trim();
                if (confirm.equalsIgnoreCase("yes")) {
                    System.out.println("Exiting...");
                    break;
                }
                continue;
            }
            parser.parseAndExecute(input, scanner, system);
        }
        scanner.close();
    }
}