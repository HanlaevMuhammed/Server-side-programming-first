package com.rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

class CommandParserTest {
    private CommandParser parser;
    private RBACSystem system;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
        system = new RBACSystem();
        System.setOut(new PrintStream(outContent));
    }

    @Test
    void registerAndExecuteCommand() {
        parser.registerCommand("test", "Test command", (scanner, sys) ->
                System.out.println("Executed"));
        parser.executeCommand("test", new Scanner(System.in), system);
        assertTrue(outContent.toString().contains("Executed"));
    }

    @Test
    void unknownCommandShowsMessage() {
        parser.executeCommand("unknown", new Scanner(System.in), system);
        assertTrue(outContent.toString().contains("Unknown command: unknown"));
    }

    @Test
    void printHelpContainsDescriptions() {
        parser.registerCommand("cmd1", "Desc1", (s, sys) -> {});
        parser.registerCommand("cmd2", "Desc2", (s, sys) -> {});
        parser.printHelp();
        String output = outContent.toString();
        assertTrue(output.contains("cmd1") && output.contains("Desc1"));
        assertTrue(output.contains("cmd2") && output.contains("Desc2"));
    }
}