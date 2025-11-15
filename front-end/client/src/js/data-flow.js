/**
 * Data Flow Connector - Ensures proper communication between Driver, Staff, and Admin
 * This module handles cross-actor data synchronization and event broadcasting
 */

import api, { RealtimeConnection } from './api-client.js';

// ============= Event Bus for Cross-Component Communication =============
class EventBus {
    constructor() {
        this.listeners = {};
    }
    
    on(event, callback) {
        if (!this.listeners[event]) {
            this.listeners[event] = [];
        }
        this.listeners[event].push(callback);
    }
    
    off(event, callback) {
        if (!this.listeners[event]) return;
        this.listeners[event] = this.listeners[event].filter(cb => cb !== callback);
    }
    
    emit(event, data) {
        if (!this.listeners[event]) return;
        this.listeners[event].forEach(callback => callback(data));
    }
}

export const eventBus = new EventBus();

// ============= Session State Manager =============
export class SessionManager {
    constructor() {
        this.currentSession = null;
        this.sessionHistory = [];
    }
    
    // Driver: Create and track session
    async startDriverSession(chargerId, stationId) {
        try {
            const session = await api.driver.startSession(chargerId, stationId);
            this.currentSession = session;
            
            // Broadcast session started event
            eventBus.emit('session:started', session);
            
            // Save to localStorage for persistence
            localStorage.setItem('currentSessionId', session.sessionId);
            
            return session;
        } catch (error) {
            eventBus.emit('session:error', { type: 'start', error });
            throw error;
        }
    }
    
    // Driver: Stop session
    async stopDriverSession(sessionId) {
        try {
            const result = await api.driver.stopSession(sessionId);
            
            // Broadcast session stopped event
            eventBus.emit('session:stopped', { sessionId, result });
            
            // Clear current session
            this.currentSession = null;
            localStorage.removeItem('currentSessionId');
            
            return result;
        } catch (error) {
            eventBus.emit('session:error', { type: 'stop', error });
            throw error;
        }
    }
    
    // Staff: Monitor session
    async getSessionForStaff(sessionId) {
        try {
            const session = await api.staff.getSessionDetails(sessionId);
            eventBus.emit('session:updated', session);
            return session;
        } catch (error) {
            console.error('Error fetching staff session:', error);
            throw error;
        }
    }
    
    // Staff: Manually stop session for driver
    async staffStopSession(sessionId, reason = 'Staff intervention') {
        try {
            const result = await api.staff.stopSessionForDriver(sessionId, reason);
            eventBus.emit('session:staff_stopped', { sessionId, reason, result });
            return result;
        } catch (error) {
            eventBus.emit('session:error', { type: 'staff_stop', error });
            throw error;
        }
    }
    
    // Load session history for driver
    async loadDriverHistory() {
        try {
            const history = await api.driver.getChargingHistory();
            this.sessionHistory = history;
            eventBus.emit('history:loaded', history);
            return history;
        } catch (error) {
            console.error('Error loading history:', error);
            throw error;
        }
    }
}

// ============= Payment Flow Manager =============
export class PaymentManager {
    constructor() {
        this.pendingPayments = [];
    }
    
    // Driver: Online payment
    async processOnlinePayment(sessionId, paymentMethod) {
        try {
            const result = await api.driver.paySession(sessionId, paymentMethod);
            eventBus.emit('payment:completed', { sessionId, method: paymentMethod, result });
            return result;
        } catch (error) {
            eventBus.emit('payment:failed', { sessionId, error });
            throw error;
        }
    }
    
    // Staff: Cash payment
    async processCashPayment(sessionId, amount, notes = '') {
        try {
            const result = await api.staff.processCashPayment(sessionId, amount, notes);
            eventBus.emit('payment:cash_completed', { sessionId, amount, result });
            return result;
        } catch (error) {
            eventBus.emit('payment:failed', { sessionId, error });
            throw error;
        }
    }
    
    // Get invoice for both driver and staff
    async getInvoice(sessionId) {
        try {
            const invoice = await api.driver.getInvoice(sessionId);
            eventBus.emit('invoice:loaded', invoice);
            return invoice;
        } catch (error) {
            console.error('Error fetching invoice:', error);
            throw error;
        }
    }
}

// ============= Incident Flow Manager =============
export class IncidentManager {
    constructor() {
        this.activeIncidents = [];
    }
    
    // Staff: Report incident
    async reportIncident(stationId, chargerId, type, description, severity = 'MEDIUM') {
        try {
            const incident = await api.staff.reportIncident(
                stationId, 
                chargerId, 
                type, 
                description, 
                severity
            );
            
            this.activeIncidents.push(incident);
            eventBus.emit('incident:reported', incident);
            
            return incident;
        } catch (error) {
            eventBus.emit('incident:error', { type: 'report', error });
            throw error;
        }
    }
    
    // Admin: Assign incident to staff
    async assignIncident(incidentId, staffId) {
        try {
            const result = await api.admin.assignIncident(incidentId, staffId);
            eventBus.emit('incident:assigned', { incidentId, staffId, result });
            return result;
        } catch (error) {
            console.error('Error assigning incident:', error);
            throw error;
        }
    }
    
    // Admin: Resolve incident
    async resolveIncident(incidentId, solution) {
        try {
            const result = await api.admin.resolveIncident(incidentId, solution);
            
            // Remove from active incidents
            this.activeIncidents = this.activeIncidents.filter(i => i.id !== incidentId);
            
            eventBus.emit('incident:resolved', { incidentId, solution, result });
            return result;
        } catch (error) {
            console.error('Error resolving incident:', error);
            throw error;
        }
    }
    
    // Get incidents for staff view
    async getStaffIncidents(stationId = null) {
        try {
            const incidents = await api.staff.getIncidents(stationId);
            this.activeIncidents = incidents.filter(i => i.status !== 'RESOLVED');
            eventBus.emit('incidents:loaded', incidents);
            return incidents;
        } catch (error) {
            console.error('Error loading incidents:', error);
            throw error;
        }
    }
    
    // Get all incidents for admin
    async getAdminIncidents(status = null, severity = null) {
        try {
            const incidents = await api.admin.getAllIncidents(status, severity);
            eventBus.emit('incidents:admin_loaded', incidents);
            return incidents;
        } catch (error) {
            console.error('Error loading admin incidents:', error);
            throw error;
        }
    }
}

// ============= Station Monitoring Manager =============
export class StationMonitor {
    constructor() {
        this.monitoredStations = new Map();
        this.realtimeConnections = new Map();
    }
    
    // Staff: Start monitoring station
    startMonitoring(stationId, onUpdate) {
        const conn = new RealtimeConnection(
            `/ws/staff/station/${stationId}`,
            (data) => {
                this.monitoredStations.set(stationId, data);
                eventBus.emit('station:updated', data);
                if (onUpdate) onUpdate(data);
            },
            (error) => {
                console.error(`Monitor error for station ${stationId}:`, error);
                eventBus.emit('station:connection_error', { stationId, error });
            }
        );
        
        conn.connect();
        this.realtimeConnections.set(stationId, conn);
        
        return conn;
    }
    
    // Stop monitoring station
    stopMonitoring(stationId) {
        const conn = this.realtimeConnections.get(stationId);
        if (conn) {
            conn.disconnect();
            this.realtimeConnections.delete(stationId);
            this.monitoredStations.delete(stationId);
        }
    }
    
    // Get station status (staff or admin)
    async getStationStatus(stationId, isAdmin = false) {
        try {
            const status = isAdmin 
                ? await api.admin.getAllStations() 
                : await api.staff.getStationStatus(stationId);
            
            eventBus.emit('station:status_loaded', { stationId, status });
            return status;
        } catch (error) {
            console.error('Error fetching station status:', error);
            throw error;
        }
    }
    
    // Staff: Get active sessions at station
    async getActiveSessions(stationId) {
        try {
            const sessions = await api.staff.getActiveSessions(stationId);
            eventBus.emit('sessions:active_loaded', { stationId, sessions });
            return sessions;
        } catch (error) {
            console.error('Error fetching active sessions:', error);
            throw error;
        }
    }
    
    // Cleanup all connections
    cleanup() {
        this.realtimeConnections.forEach((conn, stationId) => {
            this.stopMonitoring(stationId);
        });
    }
}

// ============= Admin Dashboard Manager =============
export class AdminDashboard {
    constructor() {
        this.dashboardConnection = null;
    }
    
    // Start real-time dashboard updates
    startDashboard(onUpdate) {
        this.dashboardConnection = new RealtimeConnection(
            '/ws/admin/dashboard',
            (data) => {
                eventBus.emit('dashboard:updated', data);
                if (onUpdate) onUpdate(data);
            },
            (error) => {
                console.error('Dashboard connection error:', error);
                eventBus.emit('dashboard:error', error);
            }
        );
        
        this.dashboardConnection.connect();
        return this.dashboardConnection;
    }
    
    // Stop dashboard updates
    stopDashboard() {
        if (this.dashboardConnection) {
            this.dashboardConnection.disconnect();
            this.dashboardConnection = null;
        }
    }
    
    // Get reports
    async getRevenueReport(startDate, endDate, groupBy = 'day') {
        try {
            const report = await api.admin.getRevenueReport(startDate, endDate, groupBy);
            eventBus.emit('report:revenue_loaded', report);
            return report;
        } catch (error) {
            console.error('Error fetching revenue report:', error);
            throw error;
        }
    }
    
    async getUsageReport(startDate, endDate, stationId = null) {
        try {
            const report = await api.admin.getUsageReport(startDate, endDate, stationId);
            eventBus.emit('report:usage_loaded', report);
            return report;
        } catch (error) {
            console.error('Error fetching usage report:', error);
            throw error;
        }
    }
}

// ============= Global Instances =============
export const sessionManager = new SessionManager();
export const paymentManager = new PaymentManager();
export const incidentManager = new IncidentManager();
export const stationMonitor = new StationMonitor();
export const adminDashboard = new AdminDashboard();

// ============= Notification Helper =============
export function showNotification(message, type = 'info', duration = 3000) {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        background: ${type === 'success' ? '#4CAF50' : type === 'error' ? '#f44336' : type === 'warning' ? '#ff9800' : '#2196F3'};
        color: white;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.2);
        z-index: 10000;
        animation: slideIn 0.3s ease-out;
        max-width: 400px;
        font-family: 'Inter', sans-serif;
    `;
    
    const icon = type === 'success' ? '✓' : type === 'error' ? '✕' : type === 'warning' ? '⚠' : 'ℹ';
    notification.innerHTML = `<strong>${icon}</strong> ${message}`;
    
    document.body.appendChild(notification);
    
    // Auto remove after duration
    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease-out';
        setTimeout(() => notification.remove(), 300);
    }, duration);
}

// Add CSS animation
if (!document.getElementById('notification-styles')) {
    const style = document.createElement('style');
    style.id = 'notification-styles';
    style.textContent = `
        @keyframes slideIn {
            from { transform: translateX(400px); opacity: 0; }
            to { transform: translateX(0); opacity: 1; }
        }
        @keyframes slideOut {
            from { transform: translateX(0); opacity: 1; }
            to { transform: translateX(400px); opacity: 0; }
        }
    `;
    document.head.appendChild(style);
}

// ============= Setup Event Listeners =============
// Listen to all events and log for debugging
if (localStorage.getItem('debugMode') === 'true') {
    const allEvents = [
        'session:started', 'session:stopped', 'session:updated', 'session:error',
        'payment:completed', 'payment:cash_completed', 'payment:failed',
        'incident:reported', 'incident:assigned', 'incident:resolved',
        'station:updated', 'station:status_loaded',
        'dashboard:updated'
    ];
    
    allEvents.forEach(event => {
        eventBus.on(event, (data) => {
            console.log(`[EventBus] ${event}:`, data);
        });
    });
}

// Global error handler
window.addEventListener('unhandledrejection', (event) => {
    console.error('Unhandled promise rejection:', event.reason);
    showNotification('Có lỗi xảy ra. Vui lòng thử lại.', 'error');
});

// Export default object with all managers
export default {
    sessionManager,
    paymentManager,
    incidentManager,
    stationMonitor,
    adminDashboard,
    eventBus,
    showNotification
};
