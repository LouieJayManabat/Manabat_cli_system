package com.scc.ui;

import com.scc.dao.Userdao;
import com.scc.model.user;

import java.util.List;
import java.util.Scanner;

public class AccountManageMenu {

    private final Userdao userdao;
    private final Scanner scanner;

    public AccountManageMenu(Userdao userdao, Scanner scanner) {
        this.userdao = userdao;
        this.scanner = scanner;
    }

    public void display(user admin) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n=================================");
            System.out.println("          ADMIN DASHBOARD        ");
            System.out.println("=================================");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending User Approvals");
            System.out.println("3. Approve User Registration");
            System.out.println("4. Add New User");
            System.out.println("5. Update User identity / Status");
            System.out.println("6. Delete User Account");
            System.out.println("7. Logout");
            System.out.print("Choose an option (1-7): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewAllUser();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser();
                case "7" -> {
                    System.out.println("Logging out...");
                    loggedIn = false;
                }
                default -> System.out.println("-> [ERROR] invalid Option");
            }
        }
    }

    private void displayUserTable(List<user> users, String title) {
        System.out.println("\n=========================" + title + "=========================");
        if (users.isEmpty()) {
            System.out.println("-> No Records Found");
            return;
        }
        System.out.println("+----+-----------------+------------+------------+");
        System.out.printf("| %-3s | %-15s | %-10s | %-10s |\n", "ID", "Username", "Role", "Status");
        System.out.println("+----+-----------------+------------+------------+");
        for (user u : users) {
            System.out.printf("| %-3s | %-15s | %-10s | %-10s |\n",
                    u.id(), u.username(), u.roles(), u.status());
        }
        System.out.println("+----+-----------------+------------+------------+");
    }

    private void viewAllUser() {
        displayUserTable(userdao.getAllUser(), "ALL REGISTERED USER");
    }

    private void viewPendingUsers() {
        displayUserTable(userdao.getPendingUsers(), "PENDING APPROVALS");
    }

    private void approveUser() {
        List<user> pendingUsers = userdao.getPendingUsers();
        displayUserTable(pendingUsers, "PENDING APPROVALS");

        if (pendingUsers.isEmpty()) {
            return;
        }

        System.out.print("Enter user ID to approve (or 0 to cancel): ");
        try {
            int userId = Integer.parseInt(scanner.nextLine().trim());
            if (userId == 0) {
                System.out.println("Operation canceled by the user.");
                return;
            }

            boolean exists = pendingUsers.stream().anyMatch(u -> u.id() == userId);
            if (!exists) {
                System.out.println("User ID not found in pending list.");
                return;
            }

            userdao.approveUser(userId);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a numerical User ID.");
        }
    }
}