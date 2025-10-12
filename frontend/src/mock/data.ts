// 用户相关数据
export const mockUsers = [
  {
    id: 1,
    username: 'user1',
    email: 'user1@example.com',
    role: 'user',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=user1',
    createdAt: '2024-01-01T00:00:00Z'
  },
  {
    id: 2,
    username: 'store1',
    email: 'store1@example.com',
    role: 'store',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=store1',
    createdAt: '2024-01-02T00:00:00Z'
  }
]

// 书店相关数据
export const mockStores = [
  {
    id: 1,
    name: '知识书店',
    description: '专注于文学和艺术类书籍',
    ownerId: 2,
    status: 'active',
    rating: 4.5,
    createdAt: '2024-01-02T00:00:00Z',
    address: '北京市海淀区中关村大街1号',
    phone: '010-12345678'
  }
]

// 书籍相关数据
export const mockBooks = [
  {
    id: 1,
    title: '三体',
    author: '刘慈欣',
    description: '科幻小说代表作',
    price: 59.8,
    stock: 100,
    storeId: 1,
    category: '科幻',
    cover: 'https://picsum.photos/200/300',
    rating: 4.8,
    sales: 1000,
    createdAt: '2024-01-03T00:00:00Z'
  },
  {
    id: 2,
    title: '活着',
    author: '余华',
    description: '经典文学作品',
    price: 39.8,
    stock: 50,
    storeId: 1,
    category: '文学',
    cover: 'https://picsum.photos/200/300',
    rating: 4.9,
    sales: 800,
    createdAt: '2024-01-04T00:00:00Z'
  }
]

// 订单相关数据
export const mockOrders = [
  {
    id: 1,
    userId: 1,
    storeId: 1,
    status: 'pending',
    totalAmount: 99.6,
    items: [
      {
        bookId: 1,
        quantity: 1,
        price: 59.8
      },
      {
        bookId: 2,
        quantity: 1,
        price: 39.8
      }
    ],
    createdAt: '2024-01-05T00:00:00Z',
    address: '北京市朝阳区建国路1号',
    phone: '13800138000'
  }
]

// 广告相关数据
export const mockAdvertisements = [
  {
    id: 1,
    title: '新年特惠',
    content: '新年期间全场图书8折起',
    image: 'https://picsum.photos/800/400',
    startDate: '2024-01-01T00:00:00Z',
    endDate: '2024-02-01T00:00:00Z',
    status: 'active'
  }
]

// 广告申请相关数据
export const mockAdvertisementApplications = [
  {
    id: 1,
    storeId: 1,
    title: '春季促销',
    content: '春季特惠活动',
    image: 'https://picsum.photos/800/400',
    startDate: '2024-03-01T00:00:00Z',
    endDate: '2024-04-01T00:00:00Z',
    status: 'pending',
    createdAt: '2024-01-06T00:00:00Z'
  }
]

// 购物车相关数据
export const mockCart = {
  items: [
    {
      bookId: 1,
      quantity: 1,
      price: 59.8,
      title: '三体',
      cover: 'https://picsum.photos/200/300'
    }
  ],
  totalAmount: 59.8
} 