package uth.edu.vn.service;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import uth.edu.vn.entity.*;
import uth.edu.vn.enums.*;
import uth.edu.vn.util.HibernateUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service class for EV Driver functionalities
 * 1. Registration & Account Management
 * 2. Booking & Starting charging sessions
 * 3. Payment & E-wallet
 * 4. History & Personal analytics
 */
public class EVDriverService {
    
    // 1. Registration & Account Management
    public User registerDriver(String email, String password, String fullName, String phoneNumber) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();
            
            // Split fullName into firstName and lastName
            String[] nameParts = fullName.split(" ", 2);
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : "";
            
            User driver = new User(email, password, firstName, lastName, UserRole.EV_DRIVER);
            driver.setPhone(phoneNumber);
            
            session.save(driver);
            transaction.commit();
            
            System.out.println("EV Driver registered successfully: " + email);
            return driver;
            
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }
    
    public User findUserByEmail(String email) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        try {
            Query<User> query = session.createQuery("FROM User WHERE email = :email", User.class);
            query.setParameter("email", email);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }
    
    public User login(String email, String password) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        try {
            Query<User> query = session.createQuery(
                "FROM User WHERE email = :email AND password = :password AND role = :role", User.class);
            query.setParameter("email", email);
            query.setParameter("password", password);
            query.setParameter("role", UserRole.EV_DRIVER);
            
            User user = query.uniqueResult();
            
            if (user != null) {
                System.out.println("Login successful for: " + email);
            } else {
                System.out.println("Invalid credentials for: " + email);
            }
            
            return user;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }
    
    public boolean updateProfile(Long userId, String vehicleModel, String vehiclePlate, Double batteryCapacity) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();
            
            User user = session.get(User.class, userId);
            if (user != null && user.getRole() == UserRole.EV_DRIVER) {
                // Create or update vehicle info
                Xe vehicle = new Xe(userId, vehicleModel.split(" ")[0], vehicleModel, vehiclePlate, "UNKNOWN");
                session.saveOrUpdate(vehicle);
                
                session.update(user);
                transaction.commit();
                
                System.out.println("Profile updated for user: " + user.getEmail());
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }
    
    // 2. Booking & Starting charging sessions
    public List<TramSac> findNearbyStations(Double latitude, Double longitude, Double radiusKm) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        try {
            // Simple proximity search (in real app, use spatial queries)
            Query<TramSac> query = session.createQuery(
                "FROM TramSac WHERE status = :status", TramSac.class);
            query.setParameter("status", StationStatus.ONLINE);
            
            List<TramSac> stations = query.list();
            System.out.println("Found " + stations.size() + " nearby charging stations");
            
            return stations;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }
    
    public List<Charger> getAvailablePoints(Long stationId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        try {
            Query<Charger> query = session.createQuery(
                "FROM Charger WHERE chargingStation.stationId = :stationId AND status = :status", 
                Charger.class);
            query.setParameter("stationId", stationId);
            query.setParameter("status", PointStatus.AVAILABLE);
            
            List<Charger> points = query.list();
            System.out.println("Found " + points.size() + " available charging points");
            
            return points;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }
    
    public DatCho createBooking(Long userId, Long pointId, LocalDateTime startTime, LocalDateTime endTime) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();
            
            User user = session.get(User.class, userId);
            Charger point = session.get(Charger.class, pointId);
            
            if (user != null && point != null && point.getStatus() == PointStatus.AVAILABLE) {
                DatCho booking = new DatCho(user, point, startTime, endTime);
                
                // Reserve the charging point
                point.setStatus(PointStatus.RESERVED);
                
                session.save(booking);
                session.update(point);
                transaction.commit();
                
                System.out.println("Booking created successfully for user: " + user.getEmail());
                return booking;
            }
            
            return null;
            
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }
    
    public PhienSac startChargingSession(Long userId, Long pointId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();
            
            User user = session.get(User.class, userId);
            Charger point = session.get(Charger.class, pointId);
            
            if (user != null && point != null) {
                String qrCode = UUID.randomUUID().toString();
                PhienSac chargingSession = new PhienSac(user, point, qrCode);
                
                // Update charging point status
                point.setStatus(PointStatus.OCCUPIED);
                
                session.save(chargingSession);
                session.update(point);
                transaction.commit();
                
                System.out.println("Charging session started with QR Code: " + qrCode);
                return chargingSession;
            }
            
            return null;
            
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }
    
    // 3. Payment & E-wallet
    public boolean addFundsToWallet(Long userId, Double amount) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();
            
            User user = session.get(User.class, userId);
            if (user != null) {
                user.addFunds(java.math.BigDecimal.valueOf(amount));
                session.update(user);
                transaction.commit();
                
                System.out.println("Added $" + amount + " to wallet. New balance: $" + user.getWalletBalance());
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }
    
    public ThanhToan processPayment(Long sessionId, PaymentMethod paymentMethod) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();
            
            PhienSac chargingSession = session.get(PhienSac.class, sessionId);
            if (chargingSession != null && chargingSession.getTotalCost() != null) {
                
                User user = chargingSession.getUser();
                Double amount = chargingSession.getTotalCost();
                
                // Check wallet balance if using wallet
                if (paymentMethod == PaymentMethod.WALLET) {
                    if (user.getWalletBalance().compareTo(java.math.BigDecimal.valueOf(amount)) < 0) {
                        System.out.println("Insufficient wallet balance");
                        return null;
                    }
                    user.setWalletBalance(user.getWalletBalance().subtract(java.math.BigDecimal.valueOf(amount)));
                    session.update(user);
                }
                
                ThanhToan payment = new ThanhToan(chargingSession.getSessionId(), java.math.BigDecimal.valueOf(amount), paymentMethod.name());
                payment.setStatus("COMPLETED");
                
                session.save(payment);
                transaction.commit();
                
                System.out.println("Payment processed successfully: $" + amount);
                return payment;
            }
            
            return null;
            
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }
    
    // 4. History & Personal analytics
    public List<PhienSac> getChargingHistory(Long userId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        try {
            Query<PhienSac> query = session.createQuery(
                "FROM PhienSac WHERE user.userId = :userId ORDER BY startTime DESC", 
                PhienSac.class);
            query.setParameter("userId", userId);
            
            List<PhienSac> sessions = query.list();
            System.out.println("Found " + sessions.size() + " charging sessions in history");
            
            return sessions;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }
    
    public Double getMonthlyChargingCost(Long userId, int year, int month) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        try {
            Query<Double> query = session.createQuery(
                "SELECT SUM(totalCost) FROM PhienSac " +
                "WHERE user.userId = :userId " +
                "AND YEAR(startTime) = :year " +
                "AND MONTH(startTime) = :month", 
                Double.class);
            query.setParameter("userId", userId);
            query.setParameter("year", year);
            query.setParameter("month", month);
            
            Double totalCost = query.uniqueResult();
            totalCost = totalCost != null ? totalCost : 0.0;
            
            System.out.println("Monthly charging cost for " + month + "/" + year + ": $" + totalCost);
            return totalCost;
            
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        } finally {
            session.close();
        }
    }
}