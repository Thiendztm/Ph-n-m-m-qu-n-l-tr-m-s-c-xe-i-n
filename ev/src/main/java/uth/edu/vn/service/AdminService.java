package uth.edu.vn.service;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import uth.edu.vn.entity.*;
import uth.edu.vn.enums.*;
import uth.edu.vn.util.HibernateUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Service class for Admin functionalities
 * 1. Station & Charging Point Management
 * 2. User & Service Package Management  
 * 3. Reports & Analytics
 */
public class AdminService {
    
    // 1. Station & Charging Point Management
    public TramSac createChargingStation(String stationName, String address, Double latitude, Double longitude, String operatingHours) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();
            
            TramSac station = new TramSac(stationName, address, latitude, longitude);
            
            session.save(station);
            transaction.commit();
            
            System.out.println("Charging station created: " + stationName);
            return station;
            
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
    
    public Charger addChargingPoint(Long stationId, String pointName, ConnectorType connectorType, Double powerCapacity, Double pricePerKwh) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();
            
            TramSac station = session.get(TramSac.class, stationId);
            
            if (station != null) {
                Charger point = new Charger(pointName, connectorType, powerCapacity, pricePerKwh, station);
                
                session.save(point);
                transaction.commit();
                
                System.out.println("Charging point added: " + pointName + " to station " + station.getName());
                return point;
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
    
    public List<TramSac> getAllStations() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        try {
            Query<TramSac> query = session.createQuery("FROM TramSac", TramSac.class);
            List<TramSac> stations = query.list();
            
            System.out.println("Total charging stations: " + stations.size());
            return stations;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }
    
    public boolean updateStationStatus(Long stationId, StationStatus status) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();
            
            TramSac station = session.get(TramSac.class, stationId);
            
            if (station != null) {
                station.setStatus(status.name());
                session.update(station);
                transaction.commit();
                
                System.out.println("Station " + station.getName() + " status updated to: " + status);
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
    
    public boolean updatePointStatus(Long pointId, PointStatus status) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();
            
            Charger point = session.get(Charger.class, pointId);
            
            if (point != null) {
                point.setStatus(status);
                session.update(point);
                transaction.commit();
                
                System.out.println("Charging point " + point.getPointName() + " status updated to: " + status);
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
    
    // 2. User & Service Subscription Management
    public GoiDichVu createUserSubscription(User user, String planName, Double monthlyFee) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();
            
            GoiDichVu subscription = new GoiDichVu(user, planName, new java.math.BigDecimal(monthlyFee.toString()));
            
            session.save(subscription);
            transaction.commit();
            
            System.out.println("User subscription created: " + planName + " for user: " + user.getEmail());
            return subscription;
            
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
    
    public User createStaffAccount(String email, String password, String fullName, String phoneNumber) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();
            
            // Split fullName into firstName and lastName
            String[] nameParts = fullName.split(" ", 2);
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : "";
            
            User staff = new User(email, password, firstName, lastName, UserRole.CS_STAFF);
            
            session.save(staff);
            transaction.commit();
            
            System.out.println("Staff account created: " + email);
            return staff;
            
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
    
    public List<User> getAllUsers() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        try {
            Query<User> query = session.createQuery("FROM User ORDER BY createdAt DESC", User.class);
            List<User> users = query.list();
            
            System.out.println("Total users in system: " + users.size());
            return users;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }
    
    public List<User> getUsersByRole(UserRole role) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        try {
            Query<User> query = session.createQuery("FROM User WHERE role = :role", User.class);
            query.setParameter("role", role);
            List<User> users = query.list();
            
            System.out.println("Total " + role + " users: " + users.size());
            return users;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }
    
    // 3. Reports & Analytics
    public Map<String, Object> generateSystemOverview() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Map<String, Object> overview = new HashMap<>();
        
        try {
            // Total stations
            Query<Long> stationQuery = session.createQuery("SELECT COUNT(*) FROM TramSac", Long.class);
            Long totalStations = stationQuery.uniqueResult();
            
            // Total charging points
            Query<Long> pointQuery = session.createQuery("SELECT COUNT(*) FROM Charger", Long.class);
            Long totalPoints = pointQuery.uniqueResult();
            
            // Available points
            Query<Long> availableQuery = session.createQuery(
                "SELECT COUNT(*) FROM Charger WHERE status = :status", Long.class);
            availableQuery.setParameter("status", PointStatus.AVAILABLE);
            Long availablePoints = availableQuery.uniqueResult();
            
            // Total users
            Query<Long> userQuery = session.createQuery("SELECT COUNT(*) FROM User", Long.class);
            Long totalUsers = userQuery.uniqueResult();
            
            // Active sessions
            Query<Long> sessionQuery = session.createQuery(
                "SELECT COUNT(*) FROM PhienSac WHERE status = :status", Long.class);
            sessionQuery.setParameter("status", SessionStatus.ACTIVE);
            Long activeSessions = sessionQuery.uniqueResult();
            
            overview.put("totalStations", totalStations);
            overview.put("totalPoints", totalPoints);
            overview.put("availablePoints", availablePoints);
            overview.put("totalUsers", totalUsers);
            overview.put("activeSessions", activeSessions);
            
            System.out.println("=== SYSTEM OVERVIEW ===");
            System.out.println("Total Stations: " + totalStations);
            System.out.println("Total Charging Points: " + totalPoints);
            System.out.println("Available Points: " + availablePoints);
            System.out.println("Total Users: " + totalUsers);
            System.out.println("Active Sessions: " + activeSessions);
            System.out.println("=======================");
            
            return overview;
            
        } catch (Exception e) {
            e.printStackTrace();
            return overview;
        } finally {
            session.close();
        }
    }
    
    public Map<Long, Double> getRevenueByStation() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Map<Long, Double> revenueByStation = new HashMap<>();
        
        try {
            Query<Object[]> query = session.createQuery(
                "SELECT cs.chargingPoint.chargingStation.stationId, SUM(cs.totalCost) " +
                "FROM PhienSac cs WHERE cs.totalCost IS NOT NULL " +
                "GROUP BY cs.chargingPoint.chargingStation.stationId", Object[].class);
            
            List<Object[]> results = query.list();
            
            System.out.println("=== REVENUE BY STATION ===");
            for (Object[] result : results) {
                Long stationId = (Long) result[0];
                Double revenue = (Double) result[1];
                revenueByStation.put(stationId, revenue);
                
                System.out.println("Station ID " + stationId + ": $" + revenue);
            }
            System.out.println("===========================");
            
            return revenueByStation;
            
        } catch (Exception e) {
            e.printStackTrace();
            return revenueByStation;
        } finally {
            session.close();
        }
    }
    
    public Map<String, Long> getUsageStatistics() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Map<String, Long> stats = new HashMap<>();
        
        try {
            // Sessions by status
            Query<Object[]> statusQuery = session.createQuery(
                "SELECT status, COUNT(*) FROM PhienSac GROUP BY status", Object[].class);
            List<Object[]> statusResults = statusQuery.list();
            
            // Peak hours analysis (simplified - count sessions by hour)
            Query<Object[]> hourQuery = session.createQuery(
                "SELECT HOUR(startTime), COUNT(*) FROM PhienSac " +
                "WHERE startTime >= :startDate GROUP BY HOUR(startTime) ORDER BY COUNT(*) DESC", 
                Object[].class);
            hourQuery.setParameter("startDate", LocalDateTime.now().minusDays(30));
            hourQuery.setMaxResults(3);
            List<Object[]> hourResults = hourQuery.list();
            
            System.out.println("=== USAGE STATISTICS ===");
            System.out.println("Sessions by Status:");
            for (Object[] result : statusResults) {
                SessionStatus status = (SessionStatus) result[0];
                Long count = (Long) result[1];
                stats.put("sessions_" + status.toString().toLowerCase(), count);
                System.out.println("  " + status + ": " + count);
            }
            
            System.out.println("Peak Hours (last 30 days):");
            for (Object[] result : hourResults) {
                Integer hour = (Integer) result[0];
                Long count = (Long) result[1];
                System.out.println("  " + hour + ":00 - " + count + " sessions");
            }
            System.out.println("=========================");
            
            return stats;
            
        } catch (Exception e) {
            e.printStackTrace();
            return stats;
        } finally {
            session.close();
        }
    }
    
    public void generateMonthlyReport(int year, int month) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        try {
            Query<Object[]> query = session.createQuery(
                "SELECT " +
                "COUNT(*) as totalSessions, " +
                "SUM(energyConsumed) as totalEnergy, " +
                "SUM(totalCost) as totalRevenue, " +
                "AVG(totalCost) as avgSessionCost " +
                "FROM PhienSac " +
                "WHERE YEAR(startTime) = :year AND MONTH(startTime) = :month", 
                Object[].class);
            query.setParameter("year", year);
            query.setParameter("month", month);
            
            Object[] result = query.uniqueResult();
            
            if (result != null) {
                Long totalSessions = (Long) result[0];
                Double totalEnergy = (Double) result[1];
                Double totalRevenue = (Double) result[2];
                Double avgSessionCost = (Double) result[3];
                
                System.out.println("=== MONTHLY REPORT " + month + "/" + year + " ===");
                System.out.println("Total Sessions: " + (totalSessions != null ? totalSessions : 0));
                System.out.println("Total Energy Dispensed: " + (totalEnergy != null ? totalEnergy : 0) + " kWh");
                System.out.println("Total Revenue: $" + (totalRevenue != null ? totalRevenue : 0));
                System.out.println("Average Session Cost: $" + (avgSessionCost != null ? avgSessionCost : 0));
                System.out.println("===============================================");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
}