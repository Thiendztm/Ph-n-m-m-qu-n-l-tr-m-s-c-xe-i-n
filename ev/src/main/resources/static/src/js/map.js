/**
 * Hàm khởi tạo (constructor) cho đối tượng trạm sạc.
 */
function tram(name, lat, lng, connector, status, power, price, address, distance) {
    this.name = name;
    this.lat = lat;
    this.lng = lng;
    this.id = `${lat},${lng}`;
    this.connector = connector;
    this.status = status;
    this.power = power;
    this.price = price;
    this.address = address;
    this.distance = distance;

    this.hienThiThongTin = function() {
        console.log(`--- Thông tin trạm sạc ---`);
        console.log(`Tên: ${this.name}`);
        console.log(`Địa chỉ: ${this.address}`);
        console.log(`Trạng thái: ${this.status === 'available' ? 'Còn trống' : 'Đang bận'}`);
        console.log(`Loại sạc: ${this.connector} (${this.power}kW)`);
        console.log(`Giá: ${this.price}đ/kWh`);
    };

    this.conTrong = function() {
        return this.status === 'available';
    };

    this.capNhatTrangThai = function(newStatus) {
        this.status = newStatus;
        console.log(`Trạm ${this.name} đã được cập nhật trạng thái thành: ${newStatus}`);
    };
}

let map;
let markers = [];
let isMapLoaded = false;

// API Configuration
const API_BASE_URL = 'http://localhost:8080/api';

// Load stations from backend API
async function loadStationsFromAPI() {
    try {
        const response = await fetch(`${API_BASE_URL}/stations`);
        const data = await response.json();
        
        if (data.success && data.stations) {
            // Convert API response to tram objects
            return data.stations.map(station => {
                // Find first available charger for display
                const firstCharger = station.chargers && station.chargers.length > 0 
                    ? station.chargers[0] 
                    : null;
                
                return new tram(
                    station.name,
                    station.latitude,
                    station.longitude,
                    firstCharger?.connectorType || 'Type 2',
                    station.availableChargers > 0 ? 'available' : 'occupied',
                    firstCharger?.powerCapacity || 50,
                    firstCharger?.pricePerKwh || 3500,
                    station.address,
                    '0km' // Distance will be calculated if user shares location
                );
            });
        } else {
            console.warn('No stations found, using fallback data');
            return getFallbackStations();
        }
    } catch (error) {
        console.error('Error loading stations from API:', error);
        console.warn('Using fallback station data');
        return getFallbackStations();
    }
}

// Fallback stations if API fails
function getFallbackStations() {
    return [
        new tram("Trạm sạc Bình Thạnh 1", 10.8231, 106.6297, "CCS", "available", 50, 3500, "123 Nguyễn Văn Cừ, Bình Thạnh, TP.HCM", "1.2km"),
        new tram("Trạm sạc Quận 1", 10.7769, 106.7009, "AC", "available", 75, 4000, "789 Nguyễn Huệ, Q.1, TP.HCM", "2.1km"),
        new tram("Trạm Sạc Sài Gòn 3", 10.770, 106.690, "CHAdeMO", "available", 100, 2500, "3 Pasteur, Q.1, TP.HCM", "0.8km")
    ];
}

// Main initialization function
async function initMap() {
    console.log('initMap called');
    
    try {
        // Kiểm tra Google Maps API
        if (typeof google === 'undefined' || !google.maps) {
            throw new Error("Google Maps API not loaded. Check your API key and billing.");
        }

        // Import thư viện marker
        const { AdvancedMarkerElement, PinElement } = await google.maps.importLibrary("marker");
        
        console.log('Google Maps libraries loaded successfully');
        isMapLoaded = true;
        
        // Tạo bản đồ với Map ID cho AdvancedMarkerElement
        map = new google.maps.Map(document.getElementById("map"), {
            center: { lat: 10.7769, lng: 106.7009 },
            zoom: 12,
            mapId: "ev_charging_map", // Required for AdvancedMarkerElement
            styles: [
                { elementType: 'geometry', stylers: [{ color: '#242f3e' }] },
                { elementType: 'labels.text.stroke', stylers: [{ color: '#242f3e' }] },
                { elementType: 'labels.text.fill', stylers: [{ color: '#746855' }] },
                { featureType: 'administrative.locality', elementType: 'labels.text.fill', stylers: [{ color: '#d59563' }] }
            ]
        });

        // Load stations from API
        const stations = await loadStationsFromAPI();
        
        const infowindow = new google.maps.InfoWindow({ content: "", maxWidth: 320 });
        
        stations.forEach((station) => {
            let marker;
            
            try {
                // Thử sử dụng AdvancedMarkerElement với PinElement
                const pinColor = station.status === "available" ? "#00FF00" : "#FF0000";
                const pin = new PinElement({
                    background: pinColor,
                    borderColor: "#FFFFFF",
                    glyphColor: "#FFFFFF",
                    glyph: station.status === "available" ? "⚡" : "🔌"
                });
                
                marker = new AdvancedMarkerElement({
                    position: { lat: station.lat, lng: station.lng },
                    map: map,
                    title: station.name,
                    content: pin.element,
                    gmpClickable: true
                });
            } catch (error) {
                console.warn('AdvancedMarkerElement failed, using fallback Marker:', error.message);
                // Fallback to regular Marker for billing issues
                const iconColor = station.status === "available" ? "green" : "red";
                marker = new google.maps.Marker({
                    position: { lat: station.lat, lng: station.lng },
                    map: map,
                    title: station.name,
                    icon: `http://maps.google.com/mapfiles/ms/icons/${iconColor}-dot.png`
                });
            }

            marker.addListener("click", () => {
                const content = `
                    <div class="info-window">
                        <h3>${station.name}</h3>
                        <div class="info-details">
                            <p><strong>Loại:</strong> ${station.connector}</p>
                            <p><strong>Trạng thái:</strong> <span class="status ${station.status.toLowerCase()}">${station.status === 'available' ? 'Trống' : 'Đang dùng'}</span></p>
                            <p><strong>Công suất:</strong> ${station.power}kW</p>
                            <p><strong>Giá:</strong> ${station.price}đ/kWh</p>
                            <p><strong>Địa chỉ:</strong> ${station.address}</p>
                        </div>
                        <div class="action-row">
                            ${station.status === 'available' 
                                ? `<button onclick="startBooking('${station.id}')">Đặt chỗ</button>`
                                : `<button disabled>Đã đặt chỗ</button>`}
                        </div>
                    </div>
                `;
                infowindow.setContent(content);
                infowindow.open(map, marker);
            });

            markers.push({ marker, station });
        });

        updateStationList();
        initSearchFunctionality(stations);
        
        // Gán sự kiện cho nút vị trí hiện tại
        const locBtn = document.getElementById("currentLocation");
        if (locBtn) locBtn.addEventListener("click", getCurrentLocation);
        
        // Áp dụng kết quả thanh toán (nếu có)
        applyBookingFromStorage();
        
    } catch (error) {
        console.error("Google Maps API Error:", error.message);
        
        // Kiểm tra nếu là lỗi billing
        if (error.message.includes('BillingNotEnabledMapError')) {
            showBillingError();
        } else {
            initFallbackUI();
        }
    }
}

// Khởi tạo UI fallback khi không có Google Maps
function initFallbackUI() {
    const mapContainer = document.getElementById("map");
    if (mapContainer) {
        mapContainer.innerHTML = `
            <div style="display: flex; align-items: center; justify-content: center; height: 100%; background: #f5f5f5; color: #666; flex-direction: column;">
                <h3>Bản đồ không khả dụng</h3>
                <p>Vui lòng kiểm tra:</p>
                <ul style="text-align: left;">
                    <li>Kết nối mạng</li>
                    <li>Google Maps API key</li>
                    <li>Cài đặt billing trong Google Cloud Console</li>
                </ul>
            </div>
        `;
    }
}

// Hiển thị lỗi billing cụ thể
function showBillingError() {
    const mapContainer = document.getElementById("map");
    if (mapContainer) {
        mapContainer.innerHTML = `
            <div style="display: flex; align-items: center; justify-content: center; height: 100%; background: #fff3cd; color: #856404; flex-direction: column; border: 1px solid #ffeaa7; border-radius: 8px; padding: 20px;">
                <h3 style="color: #856404; margin-bottom: 15px;">⚠️ Google Maps Billing Issue</h3>
                <p style="text-align: center; margin-bottom: 15px;">
                    <strong>Bản đồ cần cài đặt billing để hoạt động đầy đủ.</strong>
                </p>
                <div style="background: white; padding: 15px; border-radius: 6px; margin-bottom: 15px;">
                    <p style="margin: 5px 0;"><strong>Cách khắc phục:</strong></p>
                    <ol style="text-align: left; margin: 10px 0; padding-left: 20px;">
                        <li>Vào <a href="https://console.cloud.google.com/apis/credentials" target="_blank" style="color: #007bff;">Google Cloud Console</a></li>
                        <li>Chọn project và enable billing</li>
                        <li>Enable "Maps JavaScript API" và "Places API"</li>
                        <li>Refresh trang này</li>
                    </ol>
                </div>
                <p style="font-size: 14px; color: #6c757d;">
                    Bản đồ vẫn có thể hoạt động với chức năng cơ bản, nhưng một số tính năng nâng cao có thể bị hạn chế.
                </p>
            </div>
        `;
    }
}

// Lấy vị trí hiện tại
async function getCurrentLocation() {
    if (!navigator.geolocation || !isMapLoaded) {
        alert("Không thể lấy vị trí hiện tại.");
        return;
    }
    
    try {
        const position = await new Promise((resolve, reject) => {
            navigator.geolocation.getCurrentPosition(resolve, reject);
        });
        
        const userLocation = { lat: position.coords.latitude, lng: position.coords.longitude };
        map.setCenter(userLocation);
        map.setZoom(14);
        
        updateStationList();
    } catch (error) {
        alert("Không thể lấy vị trí hiện tại: " + error.message);
    }
}

// Khởi tạo tính năng tìm kiếm
function initSearchFunctionality(stations) {
    const searchInput = document.getElementById("searchInput");
    if (!searchInput) return;
    
    searchInput.addEventListener("input", () => {
        filterMarkers(stations);
        updateStationList();
    });
}

// Cập nhật danh sách trạm
function updateStationList() {
    const listContent = document.getElementById("listContent");
    if (!listContent) return;
    
    let html = '';
    const visibleStations = markers.filter(item => item.marker.map);

    visibleStations.forEach((item) => {
        const station = item.station;
        const isAvailable = station.status === 'available';

        html += `
            <div class="station-card">
                <div class="station-header">
                    <h4>${station.name}</h4>
                    <span class="status ${station.status.toLowerCase()}">
                        ${isAvailable ? 'Trống' : 'Đang dùng'}
                    </span>
                </div>
                <p class="address">${station.address}</p>
                <p class="details">
                    <i class="fa-solid fa-bolt"></i> ${station.connector} | ${station.power}kW | ${station.price}đ/kWh
                </p>
                <p class="distance">${station.distance}</p>
                <div class="action-row">
                    ${isAvailable 
                        ? `<button data-id="${station.id}" class="btn-book" onclick="startBooking('${station.id}')">Đặt chỗ</button>`
                        : `<button class="btn-busy" disabled>Đã đặt chỗ</button>`
                    }
                </div>
            </div>
        `;
    });

    listContent.innerHTML = html;
    const countEl = document.getElementById("stationCount");
    if (countEl) countEl.textContent = `${visibleStations.length} trạm`;
}

// Hàm lọc marker
function filterMarkers(stations) {
    const searchQuery = (document.getElementById('searchInput')?.value || '').trim().toLowerCase();

    markers.forEach(({ marker, station }) => {
        const matchesSearch = !searchQuery || 
            station.name.toLowerCase().includes(searchQuery) || 
            station.address.toLowerCase().includes(searchQuery);
        
        if (matchesSearch) {
            marker.map = map;
        } else {
            marker.map = null;
        }
    });
}

// Bắt đầu đặt chỗ
async function startBooking(stationId) {
    const token = localStorage.getItem('jwt_token');
    
    if (!token) {
        if (confirm('Bạn cần đăng nhập để đặt chỗ. Chuyển đến trang đăng nhập?')) {
            window.location.href = 'login.html?redirect=index.html';
        }
        return;
    }

    const entry = markers.find(m => m.station.id === stationId);
    if (!entry || entry.station.status === 'busy') {
        alert("Trạm đang bận hoặc không tồn tại!");
        return;
    }

    // Show booking modal
    showBookingModal(entry.station);
}

// Show booking modal
function showBookingModal(station) {
    const modal = document.createElement('div');
    modal.id = 'bookingModal';
    modal.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0,0,0,0.5);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 10000;
    `;

    const now = new Date();
    const minTime = new Date(now.getTime() + 30 * 60000); // 30 minutes from now
    const maxTime = new Date(now.getTime() + 24 * 60 * 60000); // 24 hours from now

    modal.innerHTML = `
        <div style="background: white; padding: 30px; border-radius: 16px; max-width: 500px; width: 90%;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                <h2 style="margin: 0; font-size: 24px; font-weight: 700;">Đặt chỗ</h2>
                <button onclick="closeBookingModal()" style="background: none; border: none; font-size: 24px; cursor: pointer;">&times;</button>
            </div>
            
            <div style="background: #f5f5f5; padding: 15px; border-radius: 8px; margin-bottom: 20px;">
                <h3 style="margin: 0 0 10px 0; font-size: 18px; font-weight: 600;">${station.name}</h3>
                <p style="margin: 5px 0; color: #666;"><i class="fa-solid fa-location-dot"></i> ${station.address}</p>
                <p style="margin: 5px 0; color: #666;"><i class="fa-solid fa-bolt"></i> ${station.connector} - ${station.power}kW</p>
                <p style="margin: 5px 0; color: #666;"><i class="fa-solid fa-coins"></i> ${station.price}đ/kWh</p>
            </div>

            <form id="bookingForm">
                <div style="margin-bottom: 20px;">
                    <label style="display: block; margin-bottom: 8px; font-weight: 600; color: #333;">
                        Thời gian đặt chỗ
                    </label>
                    <input 
                        type="datetime-local" 
                        id="bookingTime" 
                        name="bookingTime"
                        required
                        min="${minTime.toISOString().slice(0, 16)}"
                        max="${maxTime.toISOString().slice(0, 16)}"
                        value="${minTime.toISOString().slice(0, 16)}"
                        style="width: 100%; padding: 12px; border: 2px solid #e0e0e0; border-radius: 8px; font-size: 16px;"
                    />
                    <small style="color: #666; display: block; margin-top: 5px;">
                        Tối thiểu 30 phút từ bây giờ, tối đa 24 giờ
                    </small>
                </div>

                <div style="margin-bottom: 20px;">
                    <label style="display: block; margin-bottom: 8px; font-weight: 600; color: #333;">
                        Thời gian dự kiến sạc (phút)
                    </label>
                    <input 
                        type="number" 
                        id="duration" 
                        name="duration"
                        min="15"
                        max="480"
                        value="60"
                        required
                        style="width: 100%; padding: 12px; border: 2px solid #e0e0e0; border-radius: 8px; font-size: 16px;"
                    />
                    <small style="color: #666; display: block; margin-top: 5px;">
                        Từ 15 phút đến 8 giờ
                    </small>
                </div>

                <div style="margin-bottom: 20px;">
                    <label style="display: block; margin-bottom: 8px; font-weight: 600; color: #333;">
                        Ghi chú (tùy chọn)
                    </label>
                    <textarea 
                        id="notes" 
                        name="notes"
                        rows="3"
                        placeholder="Ví dụ: Tôi sẽ đến muộn 5 phút..."
                        style="width: 100%; padding: 12px; border: 2px solid #e0e0e0; border-radius: 8px; font-size: 16px; resize: vertical;"
                    ></textarea>
                </div>

                <div style="display: flex; gap: 10px;">
                    <button 
                        type="button" 
                        onclick="closeBookingModal()"
                        style="flex: 1; padding: 15px; background: #e0e0e0; color: #333; border: none; border-radius: 8px; font-size: 16px; font-weight: 600; cursor: pointer;"
                    >
                        Hủy
                    </button>
                    <button 
                        type="submit"
                        style="flex: 1; padding: 15px; background: #4CAF50; color: white; border: none; border-radius: 8px; font-size: 16px; font-weight: 600; cursor: pointer;"
                    >
                        Xác nhận đặt chỗ
                    </button>
                </div>
            </form>

            <div id="bookingError" style="display: none; margin-top: 15px; padding: 12px; background: #ffebee; color: #c62828; border-radius: 8px; border-left: 4px solid #f44336;"></div>
        </div>
    `;

    document.body.appendChild(modal);

    // Handle form submission
    document.getElementById('bookingForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        await submitBooking(station.id);
    });
}

// Close booking modal
function closeBookingModal() {
    const modal = document.getElementById('bookingModal');
    if (modal) {
        modal.remove();
    }
}

// Submit booking
async function submitBooking(stationId) {
    const bookingTime = document.getElementById('bookingTime').value;
    const duration = document.getElementById('duration').value;
    const notes = document.getElementById('notes').value;
    const token = localStorage.getItem('jwt_token');

    try {
        const response = await fetch(`${API_BASE_URL}/stations/${stationId}/reserve`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                reservationTime: bookingTime,
                estimatedDuration: parseInt(duration),
                notes: notes
            })
        });

        const data = await response.json();

        if (response.ok && data.success) {
            closeBookingModal();
            alert('Đặt chỗ thành công! Bạn có thể xem chi tiết trong mục Lịch sử.');
            
            // Optionally reload stations to update availability
            if (map) {
                const stations = await loadStationsFromAPI();
                updateStationList();
            }
        } else {
            document.getElementById('bookingError').style.display = 'block';
            document.getElementById('bookingError').textContent = data.message || 'Không thể đặt chỗ. Vui lòng thử lại.';
        }
    } catch (error) {
        console.error('Error booking station:', error);
        document.getElementById('bookingError').style.display = 'block';
        document.getElementById('bookingError').textContent = 'Lỗi kết nối. Vui lòng thử lại.';
    }
}

// Make functions globally available
window.startBooking = startBooking;
window.closeBookingModal = closeBookingModal;

// Expose global functions
if (typeof window !== 'undefined') {
    window.initMap = initMap;
    window.startBooking = startBooking;
    console.log('Map functions exposed globally');
}