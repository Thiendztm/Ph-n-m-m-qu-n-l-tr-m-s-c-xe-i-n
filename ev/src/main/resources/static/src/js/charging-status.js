/**
 * Real-time Charging Status WebSocket Client
 * 
 * Connects to WebSocket server and receives live charging updates
 * Updates UI in real-time with battery status, cost, time remaining
 */

let stompClient = null;
let sessionId = null;
let isConnected = false;

// Get session ID from URL parameter
function getSessionIdFromUrl() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('sessionId');
}

// Initialize WebSocket connection
function connectWebSocket() {
    sessionId = getSessionIdFromUrl();
    
    if (!sessionId) {
        alert('Không tìm thấy session ID. Vui lòng bắt đầu sạc trước.');
        window.location.href = 'index.html';
        return;
    }

    console.log('Connecting to WebSocket for session:', sessionId);
    
    // Create WebSocket connection
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    
    // Disable debug logs (optional)
    stompClient.debug = null;
    
    // Connect to server
    stompClient.connect({}, onConnected, onError);
}

// Called when WebSocket connection is established
function onConnected() {
    console.log('WebSocket connected successfully');
    isConnected = true;
    
    // Update connection status
    updateConnectionStatus(true);
    
    // Subscribe to charging status updates for this session
    stompClient.subscribe(`/topic/charging/${sessionId}`, onMessageReceived);
    
    // Show charging state
    document.getElementById('loadingState').style.display = 'none';
    document.getElementById('chargingState').style.display = 'block';
    
    // Request initial status
    stompClient.send(`/app/charging/status/${sessionId}`, {}, {});
    
    console.log('Subscribed to /topic/charging/' + sessionId);
}

// Called when WebSocket connection fails
function onError(error) {
    console.error('WebSocket connection error:', error);
    isConnected = false;
    updateConnectionStatus(false);
    
    // Retry after 5 seconds
    setTimeout(() => {
        console.log('Retrying connection...');
        connectWebSocket();
    }, 5000);
}

// Called when receiving message from server
function onMessageReceived(payload) {
    const update = JSON.parse(payload.body);
    console.log('Received update:', update);
    
    // Update UI with new data
    updateUI(update);
}

// Update UI with charging status
function updateUI(update) {
    // Battery percentage
    const soc = update.stateOfCharge || 0;
    document.getElementById('batteryPercentage').textContent = Math.round(soc) + '%';
    
    const batteryFill = document.getElementById('batteryFill');
    batteryFill.style.height = soc + '%';
    
    // Change color based on level
    if (soc < 20) {
        batteryFill.style.background = 'linear-gradient(to top, #ef4444, #fca5a5)';
    } else if (soc < 50) {
        batteryFill.style.background = 'linear-gradient(to top, #f59e0b, #fcd34d)';
    } else if (soc < 80) {
        batteryFill.style.background = 'linear-gradient(to top, #3b82f6, #93c5fd)';
    } else {
        batteryFill.style.background = 'linear-gradient(to top, #22c55e, #86efac)';
    }
    
    // Energy consumed
    document.getElementById('energyConsumed').textContent = 
        (update.energyConsumed || 0).toFixed(2);
    
    // Current cost
    document.getElementById('currentCost').textContent = 
        (update.currentCost || 0).toLocaleString('vi-VN');
    
    // Time remaining
    const timeRemaining = update.timeRemaining || 0;
    if (timeRemaining > 0) {
        document.getElementById('timeRemaining').textContent = timeRemaining;
    } else {
        document.getElementById('timeRemaining').textContent = '0';
    }
    
    // Power output
    document.getElementById('powerOutput').textContent = 
        (update.powerOutput || 0).toFixed(1);
    
    // Station info
    if (update.stationName) {
        document.getElementById('stationName').textContent = update.stationName;
    }
    if (update.chargerName) {
        document.getElementById('chargerName').textContent = update.chargerName;
    }
    if (update.startTime) {
        const startTime = new Date(update.startTime);
        document.getElementById('startTime').textContent = 
            startTime.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
    }
    
    // Status badge
    const statusBadge = document.getElementById('statusBadge');
    const batteryFillElement = document.getElementById('batteryFill');
    
    if (update.status === 'COMPLETED') {
        statusBadge.className = 'status-badge completed';
        statusBadge.innerHTML = '<i class="fa-solid fa-check"></i> Hoàn thành';
        batteryFillElement.classList.remove('charging');
        
        // Show completion notification
        showNotification('Sạc đầy! Vui lòng ngắt kết nối và thanh toán.');
        
        // Play sound (optional)
        playNotificationSound();
        
    } else if (update.status === 'CHARGING') {
        statusBadge.className = 'status-badge charging';
        statusBadge.innerHTML = '<i class="fa-solid fa-bolt"></i> Đang sạc';
        batteryFillElement.classList.add('charging');
    }
    
    // Alert message
    if (update.alertMessage) {
        const alert = document.getElementById('alert');
        document.getElementById('alertMessage').textContent = update.alertMessage;
        alert.classList.add('show');
        
        // Auto hide after 5 seconds
        setTimeout(() => {
            alert.classList.remove('show');
        }, 5000);
    }
}

// Update connection status indicator
function updateConnectionStatus(connected) {
    const statusEl = document.getElementById('connectionStatus');
    if (connected) {
        statusEl.className = 'connection-status connected';
        statusEl.innerHTML = '<i class="fa-solid fa-check-circle"></i> Kết nối thành công';
    } else {
        statusEl.className = 'connection-status disconnected';
        statusEl.innerHTML = '<i class="fa-solid fa-circle-notch fa-spin"></i> Mất kết nối - Đang thử lại...';
    }
}

// Show browser notification
function showNotification(message) {
    // Check if browser supports notifications
    if ('Notification' in window && Notification.permission === 'granted') {
        new Notification('EV Charging', {
            body: message,
            icon: '/src/img/tachnen.png',
            badge: '/src/img/tachnen.png'
        });
    } else if ('Notification' in window && Notification.permission !== 'denied') {
        // Request permission
        Notification.requestPermission().then(permission => {
            if (permission === 'granted') {
                new Notification('EV Charging', {
                    body: message,
                    icon: '/src/img/tachnen.png'
                });
            }
        });
    }
}

// Play notification sound
function playNotificationSound() {
    // Create audio element
    const audio = new Audio('data:audio/wav;base64,UklGRnoGAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQoGAACBhYqFbF1fdJivrJBhNjVgodDbq2EcBj+a2/LDciUFLIHO8tiJNwgZaLvt559NEAxQp+PwtmMcBjiR1/LMeSwFJHfH8N2QQAoUXrTp66hVFApGn+DyvmwhBSuBzvLZiTYIG2m98OScTgwOUKXi77llHAU2jdXwznosBSh+zPLaizsKGGS48OmgTxILTKXh8bllHwU1jtXyz3osBSh+y/HajDgKGGW48OibUR');
    audio.play().catch(err => console.log('Audio play failed:', err));
}

// Stop charging
async function stopCharging() {
    if (!confirm('Bạn có chắc muốn dừng sạc?')) {
        return;
    }
    
    try {
        const response = await fetch(`/api/stations/chargers/${sessionId}/stop-charging`, {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('accessToken'),
                'Content-Type': 'application/json'
            }
        });
        
        if (response.ok) {
            alert('Đã dừng sạc. Đang chuyển đến thanh toán...');
            
            // Disconnect WebSocket
            if (stompClient !== null) {
                stompClient.disconnect();
            }
            
            // Redirect to payment
            window.location.href = `payment.html?sessionId=${sessionId}`;
        } else {
            alert('Không thể dừng sạc. Vui lòng thử lại.');
        }
    } catch (error) {
        console.error('Error stopping charging:', error);
        alert('Có lỗi xảy ra. Vui lòng thử lại.');
    }
}

// Request notification permission on page load
window.addEventListener('load', () => {
    if ('Notification' in window && Notification.permission === 'default') {
        Notification.requestPermission();
    }
});

// Initialize connection when page loads
document.addEventListener('DOMContentLoaded', () => {
    connectWebSocket();
});

// Cleanup on page unload
window.addEventListener('beforeunload', () => {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
});
