// src/constants/roles.ts
export const ROLES = {
  CUSTOMER: 'customer',
  MERCHANT: 'merchant',
  ADMIN: 'admin'
}

export const ROLE_NAMES = {
  [ROLES.CUSTOMER]: '顾客',
  [ROLES.MERCHANT]: '商家',
  [ROLES.ADMIN]: '管理员'
}

export const ROLE_OPTIONS = [
  { value: ROLES.CUSTOMER, label: ROLE_NAMES[ROLES.CUSTOMER] },
  { value: ROLES.MERCHANT, label: ROLE_NAMES[ROLES.MERCHANT] },
  { value: ROLES.ADMIN, label: ROLE_NAMES[ROLES.ADMIN] }
]
