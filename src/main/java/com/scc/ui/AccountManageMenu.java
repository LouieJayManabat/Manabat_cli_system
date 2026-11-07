package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.Userdao;
import com.scc.model.ActivityLog;
import com.scc.model.user;

import java.util.List;
import java.util.Scanner;

public class AccountManageMenu {

    private final Userdao userdao;
    private final ActivityLogDao logDao;
    private final Scanner scanner;

    public AccountManageMenu(Userdao userdao, Scanner scanner) {
        this.userdao = userdao;
        this.logDao = new ActivityLogDao();
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
            System.out.println("4. Update User identity / Status");
            System.out.println("5. View System Audit Logs");
            System.out.println("6. Logout");
            System.out.print("Choose an option (1-6): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewAllUser();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser(admin);
                case "4" -> updateUserProcess(admin);
                case "5" -> auditLogsMenu();
                case "6" -> {
                    logDao.logAction(admin.id(), "USER_LOGOUT", "Admin " + admin.username() + " (ID: " + admin.id() + ") logged out.");
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

    private void approveUser(user admin) {
        List<user> pendingUsers = userdao.getPendingUsers();
        displayUserTable(pendingUsers, "PENDING APPROVALS");

        if (pendingUsers.isEmpty()) {
            return;
        }

        int userId = readIntInput("Enter user ID to approve (or 0 to cancel): ");
        if (userId == 0) {
            System.out.println("Operation canceled by the user.");
            return;
        }

        boolean exists = pendingUsers.stream().anyMatch(u -> u.id() == userId);
        if (!exists) {
            System.out.println("User ID not found in pending list.");
            return;
        }

        if (userdao.approveUser(userId)) {
            logDao.logAction(admin.id(), "USER_APPROVED", "Admin " + admin.id() + " approved account for User ID " + userId);
        }
    }

    private void updateUserProcess(user admin) {
        List<user> users = userdao.getAllUser();
        displayUserTable(users, "SELECT USER TO UPDATE");

        if (users.isEmpty()) {
            return;
        }

        int userId = readIntInput("Enter selected ID to update (or 0 to cancel): ");
        if (userId == 0) {
            System.out.println("Operation cancelled.");
            return;
        }

        if (userId == admin.id()) {
            System.out.println("-> [WARNING] Modifying current active admin is restricted. You cannot update your own account.");
            return;
        }

        boolean exists = users.stream().anyMatch(u -> u.id() == userId);
        if (!exists) {
            System.out.println("User ID not found.");
            return;
        }

        String newRole = readValidRole();
        if (newRole == null) {
            System.out.println("Operation cancelled.");
            return;
        }

        String newStatus = readValidStatus();
        if (newStatus == null) {
            System.out.println("Operation cancelled.");
            return;
        }

        if (userdao.updateUser(userId, newRole, newStatus)) {
            logDao.logAction(admin.id(), "USER_UPDATED", "Admin " + admin.id() + " changed role of User " + userId + " to " + newRole);
        }
    }

    private void auditLogsMenu() {
        boolean inAuditMenu = true;
        while (inAuditMenu) {
            System.out.println("\n=================================");
            System.out.println("       SYSTEM AUDIT LOGS         ");
            System.out.println("=================================");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Clear Historical Logs");
            System.out.println("4. Back to Admin Dashboard");
            System.out.print("Choose an option (1-4): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> displayLogs(logDao.getAllLogs(), "ALL SYSTEM AUDIT LOGS");
                case "2" -> {
                    int userId = readIntInput("Enter User ID to filter: ");
                    displayLogs(logDao.getLogsByUserId(userId), "LOGS FOR USER ID: " + userId);
                }
                case "3" -> {
                    System.out.print("Are you sure you want to clear all audit logs? (y/n): ");
                    System.out.println();
                    String confirm = scanner.nextLine().trim();
                    if (confirm.equalsIgnoreCase("y")) {
                        if (logDao.clearLogs()) {
                            System.out.println("-> All activity logs cleared successfully.");
                        } else {
                            System.out.println("-> Failed to clear logs.");
                        }
                    } else {
                        System.out.println("-> Operation cancelled.");
                    }
                }
                case "4" -> inAuditMenu = false;
                default -> System.out.println("-> [ERROR] Invalid Option. Please choose between 1 and 4.");
            }
        }
    }

    private void displayLogs(List<ActivityLog> logs, String title) {
        System.out.println("\n==================================================================================================");
        System.out.println("                                " + title);
        System.out.println("==================================================================================================");
        if (logs.isEmpty()) {
            System.out.println("-> No Activity Logs Found.");
            return;
        }
        System.out.printf("%-5s | %-8s | %-18s | %-35s | %-20s%n", "ID", "User ID", "Action Type", "Description", "Timestamp");
        System.out.println("--------------------------------------------------------------------------------------------------");
        for (ActivityLog log : logs) {
            System.out.printf("%-5s | %-8s | %-18s | %-35s | %-20s%n",
                    log.id(), log.userId(), log.actionType(), log.description(), log.timestamp());
        }
        System.out.println("==================================================================================================");
    }

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid numeric ID.");
            }
        }
    }

    private String readValidRole() {
        while (true) {
            System.out.print("Enter new role [ADMIN, EMPLOYEE, STAFF] (or 'cancel' to abort): ");
            String role = scanner.nextLine().trim().toUpperCase();

            if (role.equals("0") || role.equalsIgnoreCase("CANCEL")) {
                return null;
            }
            if (role.equals("ADMIN") || role.equals("EMPLOYEE") || role.equals("STAFF")) {
                return role;
            }
            System.out.println("-> [ERROR] Invalid role. Please choose ADMIN, EMPLOYEE, or STAFF.");
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("Enter new status [APPROVED, PENDING] (or 'cancel' to abort): ");
            String status = scanner.nextLine().trim().toUpperCase();

            if (status.equals("0") || status.equalsIgnoreCase("CANCEL")) {
                return null;
            }
            if (status.equals("APPROVED") || status.equals("PENDING")) {
                return status;
            }
            System.out.println("-> [ERROR] Invalid status. Please choose APPROVED or PENDING.");
        }
    }
}