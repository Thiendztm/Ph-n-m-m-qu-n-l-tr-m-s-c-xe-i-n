// js/data.js - Real-time data from backend API
import { api } from './api-client.js';

export const state = {
  currentPage: 'stations',
  stations: [],
  users: [],
  accounts: [],
  reports: {},
  loading: false,
  error: null
};

// Fetch stations from API
export async function fetchStations() {
  try {
    state.loading = true;
    const response = await api.get('/admin/stations');
    const stations = response.stations || response; // Handle both {stations: []} and []
    state.stations = (Array.isArray(stations) ? stations : []).map(s => ({
      id: s.id,
      name: s.name,
      lat: s.latitude,
      lng: s.longitude,
      connector: s.connectorType || 'CCS',
      status: s.status?.toLowerCase() || 'available',
      power: s.powerCapacity || 50,
      price: s.pricePerKwh || 3500,
      address: s.address,
      distance: '--',
      kwh: '--',
      temp: '--',
      kw: '--',
      amp: '--',
      soc: '--',
      volt: '--'
    }));
    state.loading = false;
    return state.stations;
  } catch (error) {
    console.error('Failed to fetch stations:', error);
    state.error = error.message;
    state.loading = false;
    return [];
  }
}

// Fetch users from API
export async function fetchUsers() {
  try {
    state.loading = true;
    const response = await api.get('/admin/users?role=EV_DRIVER');
    const users = response.users || response; // Handle both formats
    state.users = (Array.isArray(users) ? users : []).map(u => ({
      id: u.id,
      name: (u.firstName && u.lastName) ? `${u.firstName} ${u.lastName}` : u.email,
      email: u.email,
      phone: u.phone || '--',
      status: 'active', // Backend doesn't have active field, assume all are active
      joinDate: u.createdAt ? new Date(u.createdAt).toISOString().split('T')[0] : '--',
      totalCharges: 0, // TODO: Get from charging history
      totalSpent: 0, // TODO: Get from payment history
      vehicleId: '--',
      vehicleType: '--',
      role: u.role
    }));
    state.loading = false;
    return state.users;
  } catch (error) {
    console.error('Failed to fetch users:', error);
    state.error = error.message;
    state.loading = false;
    return [];
  }
}

// Fetch accounts/wallets from API
export async function fetchAccounts() {
  try {
    state.loading = true;
    // Get all users and their wallet info
    const response = await api.get('/admin/users?role=EV_DRIVER');
    const users = response.users || response;
    state.accounts = (Array.isArray(users) ? users : []).map(u => ({
      id: `ACC${u.id}`,
      userId: u.id,
      userName: (u.firstName && u.lastName) ? `${u.firstName} ${u.lastName}` : u.email,
      balance: u.walletBalance || 0,
      status: 'active', // Backend doesn't have active field
      lastTransaction: '--' // TODO: Get from payment history
    }));
    state.loading = false;
    return state.accounts;
  } catch (error) {
    console.error('Failed to fetch accounts:', error);
    state.error = error.message;
    state.loading = false;
    return [];
  }
}

// Fetch reports/statistics from API
export async function fetchReports() {
  try {
    state.loading = true;
    const overview = await api.get('/admin/overview');
    state.reports = {
      dailyRevenue: overview.dailyRevenue || 0,
      monthlyRevenue: overview.monthlyRevenue || 0,
      totalUsers: overview.totalUsers || 0,
      activeStations: overview.activeStations || 0,
      totalCharges: overview.totalChargingSessions || 0,
      averageSessionTime: overview.averageSessionTime || '--'
    };
    state.loading = false;
    return state.reports;
  } catch (error) {
    console.error('Failed to fetch reports:', error);
    state.error = error.message;
    state.loading = false;
    // Return default values on error
    state.reports = {
      dailyRevenue: 0,
      monthlyRevenue: 0,
      totalUsers: 0,
      activeStations: 0,
      totalCharges: 0,
      averageSessionTime: '--'
    };
    return state.reports;
  }
}

// Initialize data - fetch all data from API
export async function initializeData() {
  const token = localStorage.getItem('accessToken');
  if (!token) {
    console.warn('No access token found, skipping data initialization');
    return;
  }
  
  await Promise.all([
    fetchStations(),
    fetchUsers(),
    fetchAccounts(),
    fetchReports()
  ]);
}