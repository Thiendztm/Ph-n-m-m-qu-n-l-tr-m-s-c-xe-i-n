// js/data.js
export const state = {
  currentPage: 'stations',
  stations: [],
  users: [],
  accounts: [],
  reports: {}
};

export function initializeData() {
  state.stations = [
    {
      id: 'FC-BTH-01-1', name: 'Trạm sạc Bình Thạnh 1', lat: 10.8231, lng: 106.6297,
      connector: 'CCS', status: 'busy', power: 50, price: 3500,
      address: '123 Nguyễn Văn Cừ, Bình Thạnh, TP.HCM', distance: '1.2km',
      kwh: '25.3', temp: '32', kw: '45', amp: '85', soc: '75', volt: '400'
    },
    {
      id: 'FC-BTH-01-2', name: 'Trạm sạc Bình Thạnh 2', lat: 10.8245, lng: 106.6312,
      connector: 'AC', status: 'available', power: 22, price: 3200,
      address: '456 Điện Biên Phủ, Bình Thạnh, TP.HCM', distance: '0.8km',
      kwh: '--', temp: '--', kw: '--', amp: '--', soc: '--', volt: '--'
    },
    {
      id: 'FC-Q1-01-1', name: 'Trạm sạc Quận 1', lat: 10.7769, lng: 106.7009,
      connector: 'CCS', status: 'busy', power: 75, price: 4000,
      address: '789 Nguyễn Huệ, Quận 1, TP.HCM', distance: '2.1km',
      kwh: '18.7', temp: '28', kw: '70', amp: '120', soc: '60', volt: '380'
    }
  ];

  // DỮ LIỆU MỚI: THÊM vehicleId + vehicleType
  state.users = [
    {
      id: 'U001',
      name: 'Nguyễn Văn A',
      email: 'nguyenvana@email.com',
      phone: '0901234567',
      status: 'active',
      joinDate: '2024-01-15',
      totalCharges: 15,
      totalSpent: 1250000,
      vehicleId: '51H-12345',
      vehicleType: 'VinFast VF e34'
    },
    {
      id: 'U002',
      name: 'Trần Thị B',
      email: 'tranthib@email.com',
      phone: '0907654321',
      status: 'active',
      joinDate: '2024-02-20',
      totalCharges: 8,
      totalSpent: 680000,
      vehicleId: '52H-67890',
      vehicleType: 'Tesla Model 3'
    },
    // {
    //   id: 'U003',
    //   name: 'Lê Văn C',
    //   email: 'levanc@email.com',
    //   phone: '0909876543',
    //   status: 'inactive',
    //   joinDate: '2024-01-05',
    //   totalCharges: 3,
    //   totalSpent: 240000,
    //   vehicleId: '53H-24680',
    //   vehicleType: 'Hyundai Kona Electric'
    // },
    // // DỮ LIỆU ẢO MỚI
    // {
    //   id: 'U004',
    //   name: 'Phạm Minh D',
    //   email: 'phamminhd@email.com',
    //   phone: '0912345678',
    //   status: 'active',
    //   joinDate: '2024-03-10',
    //   totalCharges: 22,
    //   totalSpent: 1980000,
    //   vehicleId: '54H-13579',
    //   vehicleType: 'Kia EV6'
    // },
    // {
    //   id: 'U005',
    //   name: 'Vũ Thị E',
    //   email: 'vuthie@email.com',
    //   phone: '0923456789',
    //   status: 'active',
    //   joinDate: '2024-04-01',
    //   totalCharges: 12,
    //   totalSpent: 960000,
    //   vehicleId: '55H-86420',
    //   vehicleType: 'Mercedes EQS'
    // },
    // {
    //   id: 'U006',
    //   name: 'Hoàng Văn F',
    //   email: 'hoangvanf@email.com',
    //   phone: '0934567890',
    //   status: 'inactive',
    //   joinDate: '2024-02-28',
    //   totalCharges: 5,
    //   totalSpent: 400000,
    //   vehicleId: '56H-97531',
    //   vehicleType: 'BMW i4'
    // }
  ];

  state.accounts = [
    { id: 'ACC001', userId: 'U001', userName: 'Nguyễn Văn A', balance: 250000, status: 'active', lastTransaction: '2024-03-15' },
    { id: 'ACC002', userId: 'U002', userName: 'Trần Thị B', balance: 180000, status: 'active', lastTransaction: '2024-03-14' },
    { id: 'ACC003', userId: 'U003', userName: 'Lê Văn C', balance: 50000, status: 'frozen', lastTransaction: '2024-02-28' },
    { id: 'ACC004', userId: 'U004', userName: 'Phạm Minh D', balance: 320000, status: 'active', lastTransaction: '2024-04-05' },
    { id: 'ACC005', userId: 'U005', userName: 'Vũ Thị E', balance: 150000, status: 'active', lastTransaction: '2024-04-03' },
    { id: 'ACC006', userId: 'U006', userName: 'Hoàng Văn F', balance: 80000, status: 'frozen', lastTransaction: '2024-03-20' }
  ];

  state.reports = {
    dailyRevenue: 3200000,   // tăng lên do thêm user
    monthlyRevenue: 92000000,
    totalUsers: 6,
    activeStations: 8,
    totalCharges: 1570,
    averageSessionTime: '48 phút'
  };
}