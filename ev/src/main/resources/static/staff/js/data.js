/**
 * Data management for Staff Dashboard
 */

export const API_BASE_URL = 'http://localhost:8080/api';

export const state = {
  currentPage: 'monitoring',
  stations: [],
  currentStation: null,
  sessions: [],
  token: localStorage.getItem('jwt_token') || ''
};

export function setCurrentStation(stationId) {
  state.currentStation = stationId;
}

export function setStations(stations) {
  state.stations = stations;
}

export function setSessions(sessions) {
  state.sessions = sessions;
}
