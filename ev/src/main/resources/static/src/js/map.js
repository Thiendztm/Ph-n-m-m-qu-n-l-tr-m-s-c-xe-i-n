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

// Fallback stations data (used if API fails)
const FALLBACK_STATIONS = [
    new tram("Trạm sạc Bình Thạnh", 10.8231, 106.6297, "CCS", "available", 50, 3500, "123 Nguyễn Văn Cừ, Bình Thạnh, TP.HCM", "1.2km"),
    new tram("Trạm sạc Quận 1", 10.7769, 106.7009, "AC", "available", 75, 4000, "789 Nguyễn Huệ, Q.1, TP.HCM", "2.1km"),
    new tram("Trạm sạc Thủ Đức", 10.8700, 106.8030, "CHAdeMO", "available", 100, 3000, "Đại học Quốc gia, Thủ Đức, TP.HCM", "5km"),
    new tram("Trạm sạc Quận 3", 10.7860, 106.6917, "CCS", "available", 60, 3500, "456 Võ Văn Tần, Q.3, TP.HCM", "1.5km"),
    new tram("Trạm sạc Phú Nhuận", 10.7995, 106.6758, "AC", "occupied", 50, 3800, "789 Phan Xích Long, Phú Nhuận, TP.HCM", "2km"),
    new tram("Trạm sạc Tân Bình", 10.8008, 106.6527, "CCS", "available", 75, 3500, "Sân bay Tân Sơn Nhất, TP.HCM", "3km"),
    new tram("Trạm sạc Gò Vấp", 10.8376, 106.6666, "CHAdeMO", "available", 80, 3200, "Ngã tư Quang Trung, Gò Vấp, TP.HCM", "4km"),
    new tram("Trạm sạc Quận 7", 10.7329, 106.7218, "CCS", "available", 100, 3600, "Phú Mỹ Hưng, Q.7, TP.HCM", "6km"),
    new tram("Trạm sạc Bình Tân", 10.7398, 106.6164, "AC", "available", 50, 3400, "Aeon Mall Bình Tân, TP.HCM", "8km"),
    new tram("Trạm sạc Quận 2", 10.7883, 106.7554, "CCS", "occupied", 75, 3700, "Thảo Điền, Q.2, TP.HCM", "3.5km"),
    new tram("Trạm sạc Quận 10", 10.7731, 106.6701, "AC", "available", 60, 3500, "368 Trần Hưng Đạo, Q.10, TP.HCM", "1.8km"),
    new tram("Trạm sạc Quận 5", 10.7557, 106.6761, "CHAdeMO", "available", 90, 3300, "Chợ Lớn, Q.5, TP.HCM", "2.3km")
];

// Fetch stations from local API only
async function fetchStationsFromAPI() {
    console.log('=== Fetching stations from local API ===');
    
    try {
        const response = await fetch('/api/stations');
        
        if (!response.ok) {
            console.warn(`⚠ Local API returned HTTP ${response.status}`);
            throw new Error('Failed to fetch from local API');
        }
        
        const data = await response.json();
        const stations = data.stations || [];
        
        if (stations.length === 0) {
            console.warn('⚠ No stations in database, using fallback data');
            return FALLBACK_STATIONS;
        }
        
        console.log(`✓ Loaded ${stations.length} stations from local API`);
        
        // Convert API data to tram objects
        const localStations = stations.map(s => {
            const status = s.availableChargers > 0 ? 'available' : 'occupied';
            const connector = s.connectorTypes ? Object.keys(s.connectorTypes)[0] : 'CCS';
            
            return new tram(
                s.name,
                s.latitude,
                s.longitude,
                connector,
                status,
                50,  // Default power
                3500, // Default price
                s.address,
                '--'
            );
        });
        
        // Merge with fallback stations to ensure map has data
        const allStations = [...localStations, ...FALLBACK_STATIONS];
        console.log(`📍 Total stations: ${allStations.length}`);
        
        return allStations;
        
    } catch (error) {
        console.error('❌ Error fetching stations:', error.message);
        console.log('Using fallback data...');
        return FALLBACK_STATIONS;
    }
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

        // Fetch stations from API
        console.log('Fetching stations from API...');
        const stations = await fetchStationsFromAPI();
        console.log(`Loaded ${stations.length} stations from API`);

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
    try {
        // Get JWT token
        const token = localStorage.getItem('accessToken') || localStorage.getItem('jwt_token');
        if (!token) {
            alert('Vui lòng đăng nhập để đặt chỗ!');
            window.location.href = '/login.html';
            return;
        }

        const entry = markers.find(m => m.station.id === stationId);
        if (!entry || entry.station.status === 'busy') {
            alert("Trạm đang bận hoặc không tồn tại!");
            return;
        }

        const s = entry.station;
        const bookingInfo = {
            id: s.id,
            name: s.name,
            address: s.address,
            connector: s.connector,
            power: s.power,
            price: s.price,
            distance: s.distance,
            connectorDisplay: `${s.connector} - ${s.power}kW`,
            priceDisplay: `${s.price.toLocaleString()}đ/kWh`
        };

        localStorage.setItem('bookingStation', JSON.stringify(bookingInfo));
        localStorage.setItem('bookingStatus', 'pending');
        window.location.href = 'payment.html';
    } catch (error) {
        console.error('Booking error:', error);
        alert('Có lỗi xảy ra: ' + error.message);
    }
}

// Áp dụng đặt chỗ thành công khi quay lại
function applyBookingFromStorage() {
    const status = localStorage.getItem('bookingStatus');
    const saved = localStorage.getItem('bookingStation');
    if (status === 'success' && saved) {
        try {
            const station = JSON.parse(saved);
            const entry = markers.find(m => m.station.id === station.id);
            if (entry && entry.station.status === 'available') {
                entry.station.capNhatTrangThai('busy');
                updateStationList();
            }
        } catch (e) { 
            console.error(e); 
        }
    }
    // Dọn dẹp
    localStorage.removeItem('bookingStatus');
    localStorage.removeItem('bookingStation');
}

// Expose global functions
if (typeof window !== 'undefined') {
    window.initMap = initMap;
    window.startBooking = startBooking;
    console.log('Map functions exposed globally');
}