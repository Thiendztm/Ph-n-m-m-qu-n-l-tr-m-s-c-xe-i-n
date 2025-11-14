package uth.edu.vn.service;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import uth.edu.vn.entity.*;
import uth.edu.vn.enums.*;
import uth.edu.vn.util.HibernateUtil;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for Charging Station Staff functionalities
 * 1. Payment management at charging stations
 * 2. Monitoring and reporting
 */
public class CSStaffService {
    
    // 1. Payment management at charging stations
    public PhienSac startSessionByStaff(Long pointId, String vehiclePlate) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();
            
            Charger point = session.get(Charger.class, pointId);
            
            if (point != null && point.getStatus() == PointStatus.AVAILABLE) {
                // Create temporary user for walk-in customers
                User walkInUser = new User("walkin_" + System.currentTimeMillis() + "@temp.com", 
                                         "temp123", "Walk-in", "Customer", UserRole.EV_DRIVER);
                
                // Create vehicle for walk-in customer
                Xe walkInVehicle = new Xe();
                walkInVehicle.setUserId(walkInUser.getId());
                walkInVehicle.setPlateNumber(vehiclePlate);
                walkInVehicle.setPlugType("Type2"); // Default plug type
                session.persist(walkInVehicle);
                
                session.persist(walkInUser);
                
                PhienSac chargingSession = new PhienSac(walkInUser, point, "STAFF_" + System.currentTimeMillis());
                
                // Update charging point status
                point.setStatus(PointStatus.OCCUPIED);
                
                session.persist(chargingSession);
                session.merge(point);
                transaction.commit();
                
                System.out.println("Charging session started by staff for vehicle: " + vehiclePlate);
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
    
    public boolean stopChargingSession(Long sessionId, Double energyConsumed, Integer endSoc) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();
            
            PhienSac chargingSession = session.get(PhienSac.class, sessionId);
            
            if (chargingSession != null && chargingSession.getStatus() == SessionStatus.ACTIVE) {
                // Update session details
                chargingSession.setEndTime(LocalDateTime.now());
                chargingSession.setEnergyConsumed(energyConsumed);
                chargingSession.setEndSoc(endSoc);
                chargingSession.setStatus(SessionStatus.COMPLETED);
                
                // Calculate total cost
                Double pricePerKwh = chargingSession.getChargingPoint().getPricePerKwh();
                Double totalCost = energyConsumed * pricePerKwh;
                chargingSession.setTotalCost(totalCost);
                
                // Update charging point status
                Charger point = chargingSession.getChargingPoint();
                point.setStatus(PointStatus.AVAILABLE);
                
                session.merge(chargingSession);
                session.merge(point);
                transaction.commit();
                
                System.out.println("Charging session stopped. Energy consumed: " + energyConsumed + " kWh, Cost: $" + totalCost);
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
    
    public ThanhToan processCashPayment(Long sessionId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();
            
            PhienSac chargingSession = session.get(PhienSac.class, sessionId);
            
            if (chargingSession != null && chargingSession.getTotalCost() != null) {
                ThanhToan payment = new ThanhToan(
                    chargingSession.getSessionId(), 
                    java.math.BigDecimal.valueOf(chargingSession.getTotalCost()), 
                    "CASH"
                );
                
                payment.setStatus("COMPLETED");
                
                session.persist(payment);
                transaction.commit();
                
                System.out.println("Cash payment processed: $" + chargingSession.getTotalCost());
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
    
    // 2. Monitoring and reporting
    public List<Charger> getStationStatus(Long stationId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        try {
            Query<Charger> query = session.createQuery(
                "FROM Charger WHERE chargingStation.stationId = :stationId", 
                Charger.class);
            query.setParameter("stationId", stationId);
            
            List<Charger> points = query.list();
            
            System.out.println("Station Status Report:");
            for (Charger point : points) {
                System.out.println("Point " + point.getPointName() + ": " + point.getStatus() + 
                                 " (" + point.getPowerCapacity() + " kW, " + point.getConnectorType() + ")");
            }
            
            return points;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }
    
    public List<PhienSac> getActiveSessionsAtStation(Long stationId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        try {
            Query<PhienSac> query = session.createQuery(
                "FROM PhienSac cs WHERE cs.chargingPoint.chargingStation.stationId = :stationId " +
                "AND cs.status = :status", 
                PhienSac.class);
            query.setParameter("stationId", stationId);
            query.setParameter("status", SessionStatus.ACTIVE);
            
            List<PhienSac> activeSessions = query.list();
            
            System.out.println("Active sessions at station: " + activeSessions.size());
            return activeSessions;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }
    
    public boolean reportIncident(Long stationId, Long pointId, String description) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        
        try {
            transaction = session.beginTransaction();
            
            Charger point = session.get(Charger.class, pointId);
            
            if (point != null && point.getChargingStation().getId().equals(stationId)) {
                // Mark point as out of order
                point.setStatus(PointStatus.OUT_OF_ORDER);
                session.update(point);
                
                transaction.commit();
                
                System.out.println("Incident reported for point " + point.getPointName() + ": " + description);
                System.out.println("Point marked as OUT_OF_ORDER");
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
    
    public void generateDailyReport(Long stationId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        try {
            // Get today's sessions
            Query<PhienSac> query = session.createQuery(
                "FROM PhienSac cs WHERE cs.chargingPoint.chargingStation.stationId = :stationId " +
                "AND DATE(cs.startTime) = CURRENT_DATE", 
                PhienSac.class);
            query.setParameter("stationId", stationId);
            
            List<PhienSac> todaySessions = query.list();
            
            Double totalRevenue = 0.0;
            Double totalEnergy = 0.0;
            
            for (PhienSac cs : todaySessions) {
                if (cs.getTotalCost() != null) {
                    totalRevenue += cs.getTotalCost();
                }
                if (cs.getEnergyConsumed() != null) {
                    totalEnergy += cs.getEnergyConsumed();
                }
            }
            
            System.out.println("=== DAILY REPORT ===");
            System.out.println("Date: " + LocalDateTime.now().toLocalDate());
            System.out.println("Total Sessions: " + todaySessions.size());
            System.out.println("Total Energy Dispensed: " + totalEnergy + " kWh");
            System.out.println("Total Revenue: $" + totalRevenue);
            System.out.println("==================");
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
}