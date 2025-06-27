import { mockUsers, mockStores, mockBooks, mockOrders, mockAdvertisements, mockAdvertisementApplications, mockCart } from '../mock/data'

// 模拟延迟
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

// 用户相关API
export const userApi = {
  login: async (email: string, password: string) => {
    await delay(500)
    const user = mockUsers.find(u => u.email === email)
    if (user) {
      return { user, token: 'mock-token' }
    }
    throw new Error('Invalid credentials')
  },

  register: async (userData: any) => {
    await delay(500)
    return { ...userData, id: mockUsers.length + 1 }
  },

  getProfile: async () => {
    await delay(300)
    return mockUsers[0]
  },

  updateProfile: async (data: any) => {
    await delay(300)
    return { ...mockUsers[0], ...data }
  }
}

// 书店相关API
export const storeApi = {
  getAll: async () => {
    await delay(300)
    return mockStores
  },

  getById: async (id: number) => {
    await delay(300)
    return mockStores.find(s => s.id === id)
  },

  create: async (storeData: any) => {
    await delay(500)
    return { ...storeData, id: mockStores.length + 1 }
  },

  update: async (id: number, data: any) => {
    await delay(300)
    return { ...mockStores.find(s => s.id === id), ...data }
  }
}

// 书籍相关API
export const bookApi = {
  getAll: async () => {
    await delay(300)
    return mockBooks
  },

  getById: async (id: number) => {
    await delay(300)
    return mockBooks.find(b => b.id === id)
  },

  create: async (bookData: any) => {
    await delay(500)
    return { ...bookData, id: mockBooks.length + 1 }
  },

  update: async (id: number, data: any) => {
    await delay(300)
    return { ...mockBooks.find(b => b.id === id), ...data }
  },

  search: async (query: string) => {
    await delay(300)
    return mockBooks.filter(b => 
      b.title.toLowerCase().includes(query.toLowerCase()) ||
      b.author.toLowerCase().includes(query.toLowerCase())
    )
  }
}

// 订单相关API
export const orderApi = {
  getAll: async () => {
    await delay(300)
    return mockOrders
  },

  getById: async (id: number) => {
    await delay(300)
    return mockOrders.find(o => o.id === id)
  },

  create: async (orderData: any) => {
    await delay(500)
    return { ...orderData, id: mockOrders.length + 1 }
  },

  updateStatus: async (id: number, status: string) => {
    await delay(300)
    return { ...mockOrders.find(o => o.id === id), status }
  }
}

// 广告相关API
export const advertisementApi = {
  getAll: async () => {
    await delay(300)
    return mockAdvertisements
  },

  getById: async (id: number) => {
    await delay(300)
    return mockAdvertisements.find(a => a.id === id)
  },

  create: async (adData: any) => {
    await delay(500)
    return { ...adData, id: mockAdvertisements.length + 1 }
  },

  update: async (id: number, data: any) => {
    await delay(300)
    return { ...mockAdvertisements.find(a => a.id === id), ...data }
  }
}

// 广告申请相关API
export const advertisementApplicationApi = {
  getAll: async () => {
    await delay(300)
    return mockAdvertisementApplications
  },

  getById: async (id: number) => {
    await delay(300)
    return mockAdvertisementApplications.find(a => a.id === id)
  },

  create: async (applicationData: any) => {
    await delay(500)
    return { ...applicationData, id: mockAdvertisementApplications.length + 1 }
  },

  updateStatus: async (id: number, status: string) => {
    await delay(300)
    return { ...mockAdvertisementApplications.find(a => a.id === id), status }
  }
}

// 购物车相关API
export const cartApi = {
  get: async () => {
    await delay(300)
    return mockCart
  },

  addItem: async (bookId: number, quantity: number) => {
    await delay(300)
    const book = mockBooks.find(b => b.id === bookId)
    if (!book) throw new Error('Book not found')
    return {
      ...mockCart,
      items: [...mockCart.items, { bookId, quantity, price: book.price, title: book.title, cover: book.cover }]
    }
  },

  updateItem: async (bookId: number, quantity: number) => {
    await delay(300)
    return {
      ...mockCart,
      items: mockCart.items.map(item => 
        item.bookId === bookId ? { ...item, quantity } : item
      )
    }
  },

  removeItem: async (bookId: number) => {
    await delay(300)
    return {
      ...mockCart,
      items: mockCart.items.filter(item => item.bookId !== bookId)
    }
  },

  clear: async () => {
    await delay(300)
    return { items: [], totalAmount: 0 }
  }
} 