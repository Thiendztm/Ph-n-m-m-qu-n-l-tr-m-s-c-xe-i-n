/**
 * Hàm khởi tạo (constructor) cho đối tượng trạm sạc.
 * @param {string} name - Tên trạm sạc
 * @param {number} lat - Vĩ độ
 * @param {number} lng - Kinh độ
 * @param {string} connector - Loại cổng sạc (CCS, AC, v.v.)
 * @param {string} status - Trạng thái (available, busy)
 * @param {number} power - Công suất (kW)
 * @param {number} price - Đơn giá (đ/kWh)
 * @param {string} address - Địa chỉ
 * @param {string} distance - Khoảng cách (ví dụ: "1.2km")
 */
function tram(name, lat, lng, connector, status, power, price, address, distance) {
    this.name = name;
    this.lat = lat;
    this.lng = lng;
    this.id = `${lat},${lng}`; // id đơn giản, duy nhất theo toạ độ
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

function initMap() {
    if (typeof google === 'undefined' || !google.maps) {
        console.error("Google Maps API not loaded. Check your API key and billing.");
        // Vẫn khởi tạo các sự kiện UI tối thiểu để không bị treo
        const searchInput = document.getElementById("searchInput");
        if (searchInput) {
            searchInput.addEventListener("input", () => updateStationList());
        }
        const locBtn = document.getElementById("currentLocation");
        if (locBtn) locBtn.addEventListener("click", () => alert("Không thể lấy bản đồ do lỗi API key/billing."));
        return;
    }

    map = new google.maps.Map(document.getElementById("map"), {
        center: { lat: 10.7769, lng: 106.7009 },
        zoom: 12,
        styles: [
            { elementType: 'geometry', stylers: [{ color: '#242f3e' }] },
            { elementType: 'labels.text.stroke', stylers: [{ color: '#242f3e' }] },
            { elementType: 'labels.text.fill', stylers: [{ color: '#746855' }] },
            { featureType: 'administrative.locality', elementType: 'labels.text.fill', stylers: [{ color: '#d59563' }] }
        ]
    });

    const tramSaiGon1 = new tram("Trạm sạc Bình Thạnh 1", 10.8231, 106.6297, "CCS", "busy", 50, 3500, "123 Nguyễn Văn Cừ, Bình Thạnh, TP.HCM", "1.2km");
    const tramSaiGon2 = new tram("Trạm sạc Quận 1", 10.7769, 106.7009, "AC", "busy", 75, 4000, "789 Nguyễn Huệ, Q.1, TP.HCM", "2.1km");
    const tramSaiGon3 = new tram("Trạm Sạc Sài Gòn 3", 10.770, 106.690, "CHAdeMO", "available", 100, 2500, "3 Pasteur, Q.1, TP.HCM", "0.8km");
    // const tramSaiGon4 = new tram("Trạm Sạc Sài Gòn 4", 10.780, 106.695, "CCS", "busy", 75, 2200, "4 Lê Duẩn, Q.1, TP.HCM", "1.5km");
    // const tramSaiGon5 = new tram("Trạm Sạc Sài Gòn 5", 10.775, 106.710, "AC", "available", 40, 1800, "5 Tôn Đức Thắng, Q.1, TP.HCM", "2.0km");
    // const tramSaiGon6 = new tram("Trạm Sạc Sài Gòn 6", 10.771, 106.714, "AC", "available", 40, 1800, "6 Tôn Đức Thắng, Q.1, TP.HCM", "2.0km");

    const stations = [tramSaiGon1, tramSaiGon2, tramSaiGon3];

    const infowindow = new google.maps.InfoWindow({ content: "", maxWidth: 320 });
    stations.forEach((station) => {
        // Ưu tiên AdvancedMarkerElement nếu có, fallback sang Marker cổ điển
        const MarkerCtor = google.maps.marker && google.maps.marker.AdvancedMarkerElement
            ? google.maps.marker.AdvancedMarkerElement
            : google.maps.Marker;
        const marker = MarkerCtor === google.maps.Marker
            ? new google.maps.Marker({
                position: { lat: station.lat, lng: station.lng },
                map: map,
                title: station.name,
                icon: station.status === "available" ? "http://maps.google.com/mapfiles/ms/icons/green-dot.png" : "http://maps.google.com/mapfiles/ms/icons/red-dot.png"
            })
            : new google.maps.marker.AdvancedMarkerElement({
                position: { lat: station.lat, lng: station.lng },
                map: map,
                title: station.name,
                gmpClickable: true
            });

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
                            ? `<button onclick="bookStation('${station.name}','${station.id}')">Đặt chỗ</button>`
                            : `<button disabled>Đang dùng</button>`}
                    </div>
                </div>
            `;
            infowindow.setContent(content);
            infowindow.open(map, marker);
        });

        markers.push({ marker, station });
    });

    updateStationList();

    // Khởi tạo Autocomplete (nếu thư viện Places sẵn sàng)
    const searchInput = document.getElementById("searchInput");
    if (searchInput) {
        // Dùng PlaceAutocompleteElement mới nếu khả dụng, fallback sang Autocomplete cũ
        if (google.maps.places && google.maps.places.PlaceAutocompleteElement) {
            const pae = new google.maps.places.PlaceAutocompleteElement();
            pae.addEventListener('gmp-placeselect', (e) => {
                const place = e.place;
                if (place && place.location) {
                    map.setCenter(place.location);
                    map.setZoom(14);
                }
                updateStationList();
            });
        } else if (google.maps.places && google.maps.places.Autocomplete) {
            const autocomplete = new google.maps.places.Autocomplete(searchInput, {
                fields: ["geometry", "name", "formatted_address"],
                types: ["geocode", "establishment"]
            });
            autocomplete.addListener("place_changed", () => {
                const place = autocomplete.getPlace();
                if (place && place.geometry) {
                    map.setCenter(place.geometry.location);
                    map.setZoom(14);
                }
                updateStationList();
            });
        }

        // Tìm kiếm nội bộ theo tên/địa chỉ (fallback và realtime)
        searchInput.addEventListener("input", () => {
            const q = searchInput.value.trim().toLowerCase();
            markers.forEach(({ marker, station }) => {
                const matches =
                    q === "" ||
                    station.name.toLowerCase().includes(q) ||
                    station.address.toLowerCase().includes(q);
                marker.setVisible(matches);
            });
            filterMarkers(); // áp dụng thêm bộ lọc nếu có
            updateStationList();
        });

        // Enter để zoom đến trạm phù hợp đầu tiên
        searchInput.addEventListener("keydown", (e) => {
            if (e.key === "Enter") {
                e.preventDefault();
                const q = searchInput.value.trim().toLowerCase();
                // Ưu tiên các marker đang hiển thị (sau khi lọc theo text)
                let candidates = markers.filter(({ marker }) => marker.getVisible());
                if (candidates.length === 0 && q) {
                    // fallback: tìm theo tên/địa chỉ nếu tất cả đều ẩn
                    candidates = markers.filter(({ station }) =>
                        station.name.toLowerCase().includes(q) || station.address.toLowerCase().includes(q)
                    );
                }
                if (candidates.length > 0) {
                    const { station } = candidates[0];
                    const target = { lat: station.lat, lng: station.lng };
                    if (map) {
                        map.panTo(target);
                        map.setZoom(15);
                    }
                }
            }
        });
    }

    // Gán sự kiện cho nút vị trí hiện tại
    const locBtn = document.getElementById("currentLocation");
    if (locBtn) locBtn.addEventListener("click", getCurrentLocation);

    // Gán sự kiện thay đổi cho các bộ lọc (hỗ trợ cả select và UI tùy biến)
    const filterConnectorEl = document.getElementById("filterConnector");
    const filterStatusEl = document.getElementById("filterStatus");
    if (filterConnectorEl) filterConnectorEl.addEventListener("change", filterMarkers);
    if (filterStatusEl) filterStatusEl.addEventListener("change", filterMarkers);
    // UI tùy biến: lắng nghe click vào .option và cập nhật text trong .select
    document.querySelectorAll('.select-menu .option').forEach(opt => {
        opt.addEventListener('click', () => {
            const menu = opt.closest('.select-menu');
            const span = menu && menu.querySelector('.select span');
            if (span) span.textContent = opt.textContent.trim();
            filterMarkers();
        });
    });
}

// Lấy vị trí hiện tại
function getCurrentLocation() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition((position) => {
            const userLocation = { lat: position.coords.latitude, lng: position.coords.longitude };
            map.setCenter(userLocation);
            map.setZoom(14);
            new google.maps.Marker({
                position: userLocation,
                map: map,
                icon: 'http://maps.google.com/mapfiles/ms/icons/blue-dot.png',
                title: 'Vị trí của bạn'
            });
            updateStationList();
        }, () => alert("Không thể lấy vị trí hiện tại."));
    }
}

// Cập nhật danh sách trạm
function updateStationList() {
    const listContent = document.getElementById("listContent");
    if (!listContent) {
        console.error("listContent element not found.");
        return;
    }
    let html = '';

    const visibleStations = markers.filter(item => item.marker.getVisible());

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
                        ? `<button onclick="bookStation('${station.name}', '${station.id}')" class="btn-book">Đặt chỗ</button>`
                        : `<button class="btn-busy" disabled>Đang dùng</button>`
                    }
                </div>
            </div>
        `;
    });

    listContent.innerHTML = html;
    document.getElementById("stationCount").textContent = `${visibleStations.length} trạm`;
}

// Hàm lọc marker
function filterMarkers() {
    // Thu thập giá trị từ select thật (nếu có) hoặc từ UI tuỳ biến (.select-menu)
    let connector = (document.getElementById("filterConnector")?.value || "").trim();
    let status = (document.getElementById("filterStatus")?.value || "").trim();

    if (!connector && document.querySelector('.select-menu:nth-of-type(1) .select span')) {
        const text = document.querySelector('.select-menu:nth-of-type(1) .select span').textContent.trim();
        connector = text === 'Tất cả' ? '' : text;
    }
    if (!status && document.querySelector('.select-menu:nth-of-type(2) .select span')) {
        const text = document.querySelector('.select-menu:nth-of-type(2) .select span').textContent.trim();
        status = text === 'Trạng thái' ? '' : (text === 'Trống' ? 'available' : (text === 'Đang dùng' ? 'busy' : ''));
    }

    markers.forEach(({ marker, station }) => {
        let show = true;
        if (connector && station.connector !== connector) show = false;
        if (status && station.status !== status) show = false;
        // giữ nguyên trạng thái ẩn/hiện hiện tại do ô tìm kiếm đặt trước đó
        marker.setVisible(show && marker.getVisible());
    });

    updateStationList();
}

// Hàm đặt chỗ
function bookStation(name, id) {
    // Tìm station theo id (ưu tiên) hoặc theo name
    const entry = markers.find(({ station }) => (id && station.id === id) || station.name === name);
    if (!entry) {
        alert('Không tìm thấy trạm để đặt chỗ.');
        return;
    }
    const { station, marker } = entry;
    if (station.status === 'busy') return; // đã bận

    // Cập nhật trạng thái dữ liệu
    station.capNhatTrangThai('busy');

    // Cập nhật marker hiển thị
    if (marker instanceof google.maps.Marker) {
        marker.setIcon('http://maps.google.com/mapfiles/ms/icons/red-dot.png');
    }

    // Làm mới danh sách và bộ lọc
    updateStationList();
    filterMarkers();

    alert(`Đặt chỗ trạm ${station.name} thành công!`);
}

// Đảm bảo callback toàn cục cho script Google Maps
if (typeof window !== 'undefined') {
    window.initMap = initMap;
}