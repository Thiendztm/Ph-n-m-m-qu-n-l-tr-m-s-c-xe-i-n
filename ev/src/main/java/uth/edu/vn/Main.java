package uth.edu.vn;

import uth.edu.vn.entity.*;
import uth.edu.vn.enums.*;
import uth.edu.vn.service.*;
import uth.edu.vn.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;
import java.util.Scanner;

/**
 * Main class for EV Charging Station Management System
 * This class initializes the database schema and provides sample data and functionality demonstrations
 */
public class Main {
    
    private static AdminService adminService = new AdminService();
    private static EVDriverService driverService = new EVDriverService();
    private static CSStaffService staffService = new CSStaffService();
    private static Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        System.out.println("=== EV CHARGING STATION MANAGEMENT SYSTEM ===");
        System.out.println("Initializing database and creating sample data...");
        
        try {
            // Initialize Hibernate - this will create all tables automatically
            HibernateUtil.getSessionFactory();
            System.out.println("✓ Database schema created successfully!");
            
            // Create sample data
            createSampleData();
            
            // Show main menu
            showMainMenu();
            
        } catch (Exception e) {
            System.err.println("❌ Error initializing application: " + e.getMessage());
            e.printStackTrace();
        } finally {
            HibernateUtil.shutdown();
            scanner.close();
        }
    }
    
    private static void createSampleData() {
        System.out.println("\n=== CREATING SAMPLE DATA ===");
        
        // 1. Create Admin account (for demo purposes - in production, use proper registration)
        User admin = null;
        try {
            admin = driverService.registerDriver("admin@evms.com", "admin123", "System Admin", "0123456789");
            if (admin != null) {
                admin.setRole(UserRole.ADMIN); // Change role to admin after creation
                admin = driverService.saveUser(admin); // IMPORTANT: Save the updated role!
                System.out.println("✓ Created admin account: " + admin.getEmail() + " with role: " + admin.getRole());
            }
        } catch (Exception e) {
            System.out.println("ℹ Admin user already exists, skipping creation");
            // Try to find existing admin user
            admin = driverService.findUserByEmail("admin@evms.com");
            if (admin != null && admin.getRole() != UserRole.ADMIN) {
                // Update role if needed
                admin.setRole(UserRole.ADMIN);
                admin = driverService.saveUser(admin);
                System.out.println("✓ Updated admin role for: " + admin.getEmail());
            }
        }
        
        // 2. Create sample subscriptions for admin
        // TODO: Restore subscription creation after GoiDichVu repository is implemented
        // GoiDichVu adminSubscription = adminService.createUserSubscription(admin, "Admin Premium Plan", 500000.0);
        // if (adminSubscription != null) {
        //     System.out.println("✓ Created admin subscription: " + adminSubscription.getPlanName());
        // }
        
        // 3. Create charging stations
        TramSac station1 = adminService.createChargingStation(
            "Downtown EV Hub", 
            "123 Main Street, Ho Chi Minh City", 
            10.7769, 106.7009, 
            "24/7"
        );
        
        TramSac station2 = adminService.createChargingStation(
            "Shopping Mall Charging", 
            "456 Nguyen Hue Street, Ho Chi Minh City", 
            10.7760, 106.7041, 
            "6:00 AM - 11:00 PM"
        );
        
        if (station1 != null && station2 != null) {
            // 3. Add charging points to stations
            adminService.addChargingPoint(station1.getId(), "DC Fast 1", ConnectorType.CCS, 150.0, 0.25);
            adminService.addChargingPoint(station1.getId(), "DC Fast 2", ConnectorType.CHADEMO, 100.0, 0.23);
            adminService.addChargingPoint(station1.getId(), "AC Slow 1", ConnectorType.AC_TYPE2, 22.0, 0.15);
            
            adminService.addChargingPoint(station2.getId(), "AC Type2 1", ConnectorType.AC_TYPE2, 11.0, 0.18);
            adminService.addChargingPoint(station2.getId(), "AC Type2 2", ConnectorType.AC_TYPE2, 11.0, 0.18);
        }
        
        // 4. Create staff accounts (with error handling)
        try {
            adminService.createStaffAccount("staff1@gmail.com", "staff123", "Do mixi ", "0901234567");
        } catch (Exception e) { System.out.println("ℹ Staff1 already exists"); }
        
        try {
            adminService.createStaffAccount("staff2@gmail.com", "staff123", "be xuanmai", "0907654321");
        } catch (Exception e) { System.out.println("ℹ Staff2 already exists"); }
        
        // 5. Create sample EV drivers
        User driver1 = null;
        User driver2 = null;
        
        try {
            driver1 = driverService.registerDriver("nekohimeken@gmail.com", "pass123", "Tran Minh Thien", "0909111222");
        } catch (Exception e) {
            System.out.println("ℹ Driver1 already exists, using existing user");
            driver1 = driverService.findUserByEmail("nekohimeken@gmail.com");
        }
        
        try {
            driver2 = driverService.registerDriver("driver2@gmail.com", "pass456", "Phung Thanh Do", "0909333444");
        } catch (Exception e) {
            System.out.println("ℹ Driver2 already exists, using existing user");
            driver2 = driverService.findUserByEmail("driver2@gmail.com");
        }
        
        if (driver1 != null && driver2 != null) {
            // Update driver profiles with vehicle information
            driverService.updateProfile(driver1.getId(), "Tesla Model 3", "30A-12345", 75.0);
            driverService.updateProfile(driver2.getId(), "VinFast VF8", "51F-67890", 87.7);
            
            // Add funds to wallets
            driverService.addFundsToWallet(driver1.getId(), 100.0);
            driverService.addFundsToWallet(driver2.getId(), 150.0);
        }
        
        // 6. Create sample subscriptions for drivers
        // TODO: Restore subscription creation after GoiDichVu repository is implemented
        // if (driver1 != null) {
        //     adminService.createUserSubscription(driver1, "Basic Monthly Plan", 100000.0);
        // }
        // if (driver2 != null) {
        //     adminService.createUserSubscription(driver2, "Premium VIP Plan", 300000.0);
        // }
        
        // 7. Create sample vehicles for existing drivers
        if (driver1 != null && driver2 != null) {
            createSampleVehicles(driver1, driver2);
        }
        
        // 8. Create sample notifications
        createSampleNotifications(admin, driver1);
        
        // 9. Create sample invoices and reports
        createSampleInvoicesAndReports(admin, driver1);
        
        System.out.println("✓ Sample data created successfully!");
    }
    
    private static void createSampleVehicles(User driver1, User driver2) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();
            
            // Vehicle for driver1
            Xe vehicle1 = new Xe();
            vehicle1.setUserId(driver1.getId());
            vehicle1.setMake("Tesla");
            vehicle1.setModel("Model 3");
            vehicle1.setPlateNumber("30A-12345");
            vehicle1.setPlugType("CCS");
            session.persist(vehicle1);
            
            // Vehicle for driver2
            Xe vehicle2 = new Xe();
            vehicle2.setUserId(driver2.getId());
            vehicle2.setMake("VinFast");
            vehicle2.setModel("VF8");
            vehicle2.setPlateNumber("51F-67890");
            vehicle2.setPlugType("AC_TYPE2");
            session.persist(vehicle2);
            
            transaction.commit();
            System.out.println("✓ Created sample vehicles");
            
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("Error creating sample vehicles: " + e.getMessage());
        } finally {
            session.close();
        }
    }
    
    private static void createSampleNotifications(User admin, User driver1) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();
            
            // Welcome notification for driver
            ThongBao notification1 = new ThongBao();
            notification1.setUser(driver1);
            notification1.setType(NotificationType.SYSTEM_ALERT);
            notification1.setTitle("Welcome to EV Charging System!");
            notification1.setMessage("Thank you for registering. Your account is now active.");
            notification1.setStatus(NotificationStatus.UNREAD);
            session.persist(notification1);
            
            // System maintenance notification
            ThongBao notification2 = new ThongBao();
            notification2.setUser(driver1);
            notification2.setType(NotificationType.MAINTENANCE_SCHEDULED);
            notification2.setTitle("Scheduled Maintenance Notice");
            notification2.setMessage("Downtown EV Hub will undergo maintenance on Sunday 2-4 AM.");
            notification2.setStatus(NotificationStatus.UNREAD);
            session.persist(notification2);
            
            transaction.commit();
            System.out.println("✓ Created sample notifications");
            
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("Error creating sample notifications: " + e.getMessage());
        } finally {
            session.close();
        }
    }
    
    private static void createSampleInvoicesAndReports(User admin, User driver1) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();
            
            // Sample invoice (simplified - normally created from payment)
            HoaDon invoice = new HoaDon();
            invoice.setInvoiceNumber("INV-2024-001");
            invoice.setTotalAmount(new java.math.BigDecimal("45.50"));
            invoice.setDescription("Charging session at Downtown EV Hub");
            invoice.setIssueDate(java.time.LocalDateTime.now());
            session.persist(invoice);
            
            // Sample report
            BaoCao report = new BaoCao();
            report.setTitle("Monthly Usage Report");
            report.setType(ReportType.MONTHLY_REVENUE);
            report.setGeneratedBy(admin);
            report.setData("{\"totalSessions\": 150, \"totalRevenue\": 12500.00, \"avgSessionDuration\": 45}");
            session.persist(report);
            
            transaction.commit();
            System.out.println("✓ Created sample invoices and reports");
            
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("Error creating sample invoices and reports: " + e.getMessage());
        } finally {
            session.close();
        }
    }
    
    private static void showMainMenu() {
        while (true) {
            System.out.println("\n=== MAIN MENU ===");
            System.out.println("1. EV Driver Functions");
            System.out.println("2. Charging Station Staff Functions");
            System.out.println("3. Admin Functions");
            System.out.println("4. View System Overview");
            System.out.println("0. Exit");
            System.out.print("Select option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            
            switch (choice) {
                case 1:
                    showDriverMenu();
                    break;
                case 2:
                    showStaffMenu();
                    break;
                case 3:
                    showAdminMenu();
                    break;
                case 4:
                    adminService.generateSystemOverview();
                    break;
                case 0:
                    System.out.println("Thank you for using EV Charging Station Management System!");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
    
    private static void showDriverMenu() {
        System.out.println("\n=== EV DRIVER MENU ===");
        System.out.println("1. Login");
        System.out.println("2. Find nearby charging stations");
        System.out.println("3. View charging history (Login required)");
        System.out.println("4. Add funds to wallet (Login required)");
        System.out.println("0. Back to main menu");
        System.out.print("Select option: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                System.out.print("Enter email: ");
                String email = scanner.nextLine();
                System.out.print("Enter password: ");
                String password = scanner.nextLine();
                
                User driver = driverService.login(email, password);
                if (driver != null) {
                    System.out.println("✓ Login successful! Welcome " + driver.getFirstName() + " " + driver.getLastName());
                    System.out.println("Wallet Balance: $" + driver.getWalletBalance());
                } else {
                    System.out.println("❌ Login failed. Invalid credentials.");
                }
                break;
                
            case 2:
                List<TramSac> stations = driverService.findNearbyStations(10.7769, 106.7009, 10.0);
                System.out.println("\n=== NEARBY CHARGING STATIONS ===");
                for (TramSac station : stations) {
                    System.out.println("📍 " + station.getName() + " - " + station.getAddress());
                    System.out.println("   Status: " + station.getStatus());
                    
                    List<Charger> points = driverService.getAvailablePoints(station.getId());
                    System.out.println("   Available Points: " + points.size());
                    for (Charger point : points) {
                        System.out.println("     - " + point.getPointName() + " (" + point.getConnectorType() + 
                                         ", " + point.getPowerCapacity() + "kW, $" + point.getPricePerKwh() + "/kWh)");
                    }
                    System.out.println();
                }
                break;
                
            case 3:
                System.out.print("Enter your user ID: ");
                Long userId = scanner.nextLong();
                List<PhienSac> history = driverService.getChargingHistory(userId);
                
                System.out.println("\n=== CHARGING HISTORY ===");
                for (PhienSac session : history) {
                    System.out.println("Session: " + session.getStartTime() + 
                                     " | Energy: " + session.getEnergyConsumed() + "kWh" +
                                     " | Cost: $" + session.getTotalCost());
                }
                break;
                
            case 4:
                System.out.print("Enter your user ID: ");
                Long walletUserId = scanner.nextLong();
                System.out.print("Enter amount to add: $");
                Double amount = scanner.nextDouble();
                
                if (driverService.addFundsToWallet(walletUserId, amount)) {
                    System.out.println("✓ Funds added successfully!");
                } else {
                    System.out.println("❌ Failed to add funds. Please check user ID.");
                }
                break;
                
            case 0:
                return;
                
            default:
                System.out.println("Invalid option.");
        }
    }
    
    private static void showStaffMenu() {
        System.out.println("\n=== STAFF MENU ===");
        System.out.println("1. View station status");
        System.out.println("2. Start charging session for walk-in customer");
        System.out.println("3. Stop charging session");
        System.out.println("4. Process cash payment");
        System.out.println("5. Generate daily report");
        System.out.println("0. Back to main menu");
        System.out.print("Select option: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                System.out.print("Enter station ID: ");
                Long stationId = scanner.nextLong();
                staffService.getStationStatus(stationId);
                break;
                
            case 2:
                System.out.print("Enter charging point ID: ");
                Long pointId = scanner.nextLong();
                scanner.nextLine();
                System.out.print("Enter vehicle plate number: ");
                String vehiclePlate = scanner.nextLine();
                
                PhienSac session = staffService.startSessionByStaff(pointId, vehiclePlate);
                if (session != null) {
                    System.out.println("✓ Charging session started. Session ID: " + session.getSessionId());
                }
                break;
                
            case 3:
                System.out.print("Enter session ID: ");
                Long sessionId = scanner.nextLong();
                System.out.print("Enter energy consumed (kWh): ");
                Double energy = scanner.nextDouble();
                System.out.print("Enter end SOC (%): ");
                Integer endSoc = scanner.nextInt();
                
                if (staffService.stopChargingSession(sessionId, energy, endSoc)) {
                    System.out.println("✓ Charging session stopped successfully!");
                }
                break;
                
            case 4:
                System.out.print("Enter session ID for cash payment: ");
                Long paymentSessionId = scanner.nextLong();
                
                ThanhToan payment = staffService.processCashPayment(paymentSessionId);
                if (payment != null) {
                    System.out.println("✓ Cash payment processed. Payment ID: " + payment.getId());
                }
                break;
                
            case 5:
                System.out.print("Enter station ID for daily report: ");
                Long reportStationId = scanner.nextLong();
                staffService.generateDailyReport(reportStationId);
                break;
                
            case 0:
                return;
                
            default:
                System.out.println("Invalid option.");
        }
    }
    
    private static void showAdminMenu() {
        System.out.println("\n=== ADMIN MENU ===");
        System.out.println("1. View all charging stations");
        System.out.println("2. Create new charging station");
        System.out.println("3. Add charging point to station");
        System.out.println("4. View all users");
        System.out.println("5. Generate revenue report");
        System.out.println("6. Generate monthly report");
        System.out.println("7. View usage statistics");
        System.out.println("0. Back to main menu");
        System.out.print("Select option: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                List<TramSac> stations = adminService.getAllStations();
                System.out.println("\n=== ALL CHARGING STATIONS ===");
                for (TramSac station : stations) {
                    System.out.println("ID: " + station.getId() + " | " + station.getName() + 
                                     " | " + station.getAddress() + " | Status: " + station.getStatus());
                }
                break;
                
            case 2:
                System.out.print("Enter station name: ");
                String name = scanner.nextLine();
                System.out.print("Enter address: ");
                String address = scanner.nextLine();
                System.out.print("Enter latitude: ");
                Double lat = scanner.nextDouble();
                System.out.print("Enter longitude: ");
                Double lon = scanner.nextDouble();
                scanner.nextLine();
                System.out.print("Enter operating hours: ");
                String hours = scanner.nextLine();
                
                TramSac newStation = adminService.createChargingStation(name, address, lat, lon, hours);
                if (newStation != null) {
                    System.out.println("✓ Station created with ID: " + newStation.getId());
                }
                break;
                
            case 3:
                System.out.print("Enter station ID: ");
                Long stationId = scanner.nextLong();
                scanner.nextLine();
                System.out.print("Enter point name: ");
                String pointName = scanner.nextLine();
                System.out.println("Connector types: CCS, CHADEMO, AC_TYPE2, AC_TYPE1");
                System.out.print("Enter connector type: ");
                String connectorStr = scanner.nextLine();
                ConnectorType connector = ConnectorType.valueOf(connectorStr);
                System.out.print("Enter power capacity (kW): ");
                Double power = scanner.nextDouble();
                System.out.print("Enter price per kWh: $");
                Double price = scanner.nextDouble();
                
                Charger newPoint = adminService.addChargingPoint(stationId, pointName, connector, power, price);
                if (newPoint != null) {
                    System.out.println("✓ Charging point added with ID: " + newPoint.getPointId());
                }
                break;
                
            case 4:
                List<User> users = adminService.getAllUsers();
                System.out.println("\n=== ALL USERS ===");
                for (User user : users) {
                    System.out.println("ID: " + user.getId() + " | " + user.getFirstName() + " " + user.getLastName() + 
                                     " | " + user.getEmail() + " | Role: " + user.getRole());
                }
                break;
                
            case 5:
                adminService.getRevenueByStation();
                break;
                
            case 6:
                System.out.print("Enter year (YYYY): ");
                int year = scanner.nextInt();
                System.out.print("Enter month (1-12): ");
                int month = scanner.nextInt();
                adminService.generateMonthlyReport(year, month);
                break;
                
            case 7:
                adminService.getUsageStatistics();
                break;
                
            case 0:
                return;
                
            default:
                System.out.println("Invalid option.");
        }
    }
}