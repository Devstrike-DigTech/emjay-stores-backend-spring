# Customer Management System - Testing Guide

## 📋 **Customer Management Endpoints Overview**

### **Authentication & Registration (5 endpoints)**
```
POST   /api/v1/customers/register                  - Register account
POST   /api/v1/customers/login                     - Login
POST   /api/v1/customers/auth/google               - Google OAuth
POST   /api/v1/customers/guest/session             - Create guest session
GET    /api/v1/customers/guest/session/{id}        - Get guest by session
```

### **Customer Profile (6 endpoints)**
```
GET    /api/v1/customers/me                        - Get current profile
GET    /api/v1/customers/{id}                      - Get by ID (Admin)
GET    /api/v1/customers                           - List all (Admin)
PUT    /api/v1/customers/me                        - Update profile
POST   /api/v1/customers/me/change-password        - Change password
```

### **Customer Addresses (5 endpoints)**
```
POST   /api/v1/customers/me/addresses              - Add address
GET    /api/v1/customers/me/addresses              - List addresses
GET    /api/v1/customers/me/addresses/default      - Get default
PUT    /api/v1/customers/addresses/{id}            - Update address
DELETE /api/v1/customers/addresses/{id}            - Delete address
```

### **Wishlist (6 endpoints)**
```
POST   /api/v1/customers/wishlist                  - Add to wishlist
GET    /api/v1/customers/wishlist                  - Get wishlist
GET    /api/v1/customers/wishlist/count            - Get count
PUT    /api/v1/customers/wishlist/{id}             - Update item
DELETE /api/v1/customers/wishlist/{id}             - Remove item
DELETE /api/v1/customers/wishlist/product/{id}     - Remove by product
```

### **Analytics & Budget (4 endpoints)**
```
GET    /api/v1/customers/analytics/me              - Get analytics
GET    /api/v1/customers/analytics/dashboard       - Get dashboard
POST   /api/v1/customers/analytics/budget          - Set budget cap
DELETE /api/v1/customers/analytics/budget          - Remove budget
```

**Total: 26 endpoints**

---

## 🧪 **Testing Workflow**

### **Scenario 1: Customer Registration & Login**

#### **Test 1: Register New Customer**

```json
POST /api/v1/customers/register
{
  "email": "john.doe@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+2348012345678",
  "newsletterSubscribed": true
}
```

**Expected Response (201):**
```json
{
  "id": "customer-uuid",
  "customerType": "REGISTERED",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "fullName": "John Doe",
  "phone": "+2348012345678",
  "authProvider": "LOCAL",
  "status": "ACTIVE",
  "emailVerified": false,
  "phoneVerified": false,
  "newsletterSubscribed": true,
  "isGuest": false,
  "isActive": true,
  "createdAt": "2026-02-10T..."
}
```

**What happens:**
- Password is hashed and stored
- Customer analytics record auto-created
- Email verification token generated (TODO)

#### **Test 2: Login**

```json
POST /api/v1/customers/login
{
  "email": "john.doe@example.com",
  "password": "SecurePass123!"
}
```

**Expected Response (200):**
```json
{
  "customerId": "customer-uuid",
  "email": "john.doe@example.com",
  "fullName": "John Doe",
  "customerType": "REGISTERED",
  "accessToken": "jwt-access-token...",
  "refreshToken": "jwt-refresh-token..."
}
```

**Use the `accessToken` in subsequent requests:**
```
Authorization: Bearer {accessToken}
```

---

### **Scenario 2: Google OAuth Registration/Login**

#### **Test 3: Login with Google**

```json
POST /api/v1/customers/auth/google
{
  "idToken": "google-id-token-from-frontend"
}
```

**Expected Response (200):**
```json
{
  "customerId": "customer-uuid",
  "email": "john@gmail.com",
  "firstName": "John",
  "lastName": "Doe",
  "profileImageUrl": "https://lh3.googleusercontent.com/...",
  "isNewCustomer": true,
  "accessToken": "jwt-token..."
}
```

**What happens:**
- If `isNewCustomer: true`: New account created
- If `isNewCustomer: false`: Existing account logged in
- Email is pre-verified for Google accounts
- No password needed

---

### **Scenario 3: Guest Checkout**

#### **Test 4: Create Guest Session**

```json
POST /api/v1/customers/guest/session
{
  "email": "guest@example.com",
  "phone": "+2348012345678"
}
```

**Expected Response (200):**
```json
{
  "sessionId": "guest-session-uuid",
  "customerId": "guest-customer-uuid",
  "expiresAt": "2026-02-11T10:00:00"  // 24 hours
}
```

**Use case:**
- Customer can checkout without creating account
- Session valid for 24 hours
- Can later convert to registered account

#### **Test 5: Get Guest Customer**

```
GET /api/v1/customers/guest/session/{sessionId}
```

**Response:**
```json
{
  "id": "guest-customer-uuid",
  "customerType": "GUEST",
  "email": "guest@example.com",
  "phone": "+2348012345678",
  "isGuest": true,
  "fullName": "Guest Customer"
}
```

---

### **Scenario 4: Customer Profile Management**

#### **Test 6: Get Current Profile**

```
GET /api/v1/customers/me
Authorization: Bearer {token}
```

**Response:**
```json
{
  "id": "customer-uuid",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "fullName": "John Doe",
  "phone": "+2348012345678",
  "dateOfBirth": null,
  "gender": null,
  "authProvider": "LOCAL",
  "emailVerified": false,
  "newsletterSubscribed": true,
  "isActive": true
}
```

#### **Test 7: Update Profile**

```json
PUT /api/v1/customers/me
Authorization: Bearer {token}
{
  "firstName": "Jonathan",
  "dateOfBirth": "1990-05-15",
  "gender": "Male",
  "smsNotifications": true
}
```

**Response:** Updated profile

#### **Test 8: Change Password**

```json
POST /api/v1/customers/me/change-password
Authorization: Bearer {token}
{
  "currentPassword": "SecurePass123!",
  "newPassword": "NewSecurePass456!"
}
```

**Response:** 204 No Content

---

### **Scenario 5: Address Management**

#### **Test 9: Add Address**

```json
POST /api/v1/customers/me/addresses
Authorization: Bearer {token}
{
  "addressLabel": "Home",
  "recipientName": "John Doe",
  "phone": "+2348012345678",
  "addressLine1": "15 Admiralty Way",
  "addressLine2": "Lekki Phase 1",
  "city": "Lagos",
  "stateProvince": "Lagos State",
  "postalCode": "101245",
  "country": "Nigeria",
  "isDefault": true,
  "isShippingAddress": true,
  "isBillingAddress": true,
  "deliveryInstructions": "Call when you arrive at the gate"
}
```

**Expected Response (201):**
```json
{
  "id": "address-uuid",
  "customerId": "customer-uuid",
  "addressLabel": "Home",
  "recipientName": "John Doe",
  "addressLine1": "15 Admiralty Way",
  "city": "Lagos",
  "country": "Nigeria",
  "isDefault": true,
  "fullAddress": "15 Admiralty Way, Lekki Phase 1, Lagos, Lagos State, 101245, Nigeria",
  "shortAddress": "15 Admiralty Way, Lagos",
  "displayLabel": "Home",
  "hasCoordinates": false
}
```

#### **Test 10: Add Address with GPS Coordinates**

```json
POST /api/v1/customers/me/addresses
{
  "addressLabel": "Office",
  "addressLine1": "Plot 1440, Sanusi Fafunwa Street",
  "city": "Lagos",
  "stateProvince": "Lagos State",
  "country": "Nigeria",
  "latitude": 6.4281,
  "longitude": 3.4219,
  "isShippingAddress": true
}
```

**For delivery optimization!**

#### **Test 11: Get All Addresses**

```
GET /api/v1/customers/me/addresses
Authorization: Bearer {token}
```

**Response:**
```json
{
  "content": [
    {
      "id": "address-1",
      "addressLabel": "Home",
      "isDefault": true,
      "fullAddress": "..."
    },
    {
      "id": "address-2",
      "addressLabel": "Office",
      "isDefault": false,
      "fullAddress": "..."
    }
  ],
  "totalElements": 2
}
```

#### **Test 12: Get Default Address**

```
GET /api/v1/customers/me/addresses/default
Authorization: Bearer {token}
```

Returns the default shipping address.

#### **Test 13: Update Address**

```json
PUT /api/v1/customers/addresses/{address-uuid}
Authorization: Bearer {token}
{
  "phone": "+2348087654321",
  "deliveryInstructions": "Updated: Ring the doorbell twice"
}
```

#### **Test 14: Delete Address**

```
DELETE /api/v1/customers/addresses/{address-uuid}
Authorization: Bearer {token}
```

**Response:** 204 No Content

---

### **Scenario 6: Wishlist Management**

#### **Test 15: Add Product to Wishlist**

```json
POST /api/v1/customers/wishlist
Authorization: Bearer {token}
{
  "productId": "<product-uuid>",
  "priority": 1,
  "notes": "Want this for my birthday",
  "notifyOnPriceDrop": true,
  "targetPrice": 25000.00
}
```

**Expected Response (201):**
```json
{
  "id": "wishlist-item-uuid",
  "customerId": "customer-uuid",
  "productId": "product-uuid",
  "productName": "Wireless Headphones",
  "productImageUrl": "https://...",
  "currentPrice": 35000.00,
  "priceWhenAdded": 35000.00,
  "targetPrice": 25000.00,
  "priority": 1,
  "notifyOnPriceDrop": true,
  "priceDropPercentage": 0.0,
  "isPriceAtTarget": false,
  "addedAt": "2026-02-10T..."
}
```

**What happens:**
- Product price captured when added
- System can notify when price drops
- Can set target price for alerts

#### **Test 16: Get Wishlist**

```
GET /api/v1/customers/wishlist
Authorization: Bearer {token}
```

**Response:**
```json
{
  "content": [
    {
      "productName": "Wireless Headphones",
      "currentPrice": 32000.00,
      "priceWhenAdded": 35000.00,
      "priceDropPercentage": 8.57,  // Price dropped!
      "isPriceAtTarget": false,
      "targetPrice": 25000.00
    }
  ],
  "totalElements": 1
}
```

#### **Test 17: Get Wishlist Count**

```
GET /api/v1/customers/wishlist/count
Authorization: Bearer {token}
```

**Response:**
```json
{
  "count": 5
}
```

**Great for showing badge count in UI!**

#### **Test 18: Remove from Wishlist**

```
DELETE /api/v1/customers/wishlist/{wishlist-item-uuid}
Authorization: Bearer {token}
```

---

### **Scenario 7: Budget Management & Analytics**

#### **Test 19: Get Customer Analytics**

```
GET /api/v1/customers/analytics/me
Authorization: Bearer {token}
```

**Response:**
```json
{
  "id": "analytics-uuid",
  "customerId": "customer-uuid",
  "totalOrders": 0,
  "totalSpent": 0.00,
  "averageOrderValue": 0.00,
  "monthlyBudgetCap": null,
  "currentMonthSpent": 0.00,
  "budgetUtilizationPercentage": null,
  "remainingBudget": null,
  "isOverBudget": false,
  "isNearBudgetLimit": false,
  "lifetimeValue": 0.00,
  "isNewCustomer": true,
  "isActiveCustomer": false,
  "isVIPCustomer": false
}
```

#### **Test 20: Set Monthly Budget Cap**

```json
POST /api/v1/customers/analytics/budget
Authorization: Bearer {token}
{
  "monthlyBudgetCap": 100000.00,
  "budgetAlertThreshold": 80.0
}
```

**Expected Response:**
```json
{
  "monthlyBudgetCap": 100000.00,
  "currentMonthSpent": 0.00,
  "budgetAlertThreshold": 80.0,
  "budgetUtilizationPercentage": 0.0,
  "remainingBudget": 100000.00,
  "isOverBudget": false,
  "isNearBudgetLimit": false
}
```

**After some purchases:**
```json
{
  "monthlyBudgetCap": 100000.00,
  "currentMonthSpent": 85000.00,
  "budgetUtilizationPercentage": 85.0,
  "remainingBudget": 15000.00,
  "isOverBudget": false,
  "isNearBudgetLimit": true  // Alert! Over 80%
}
```

#### **Test 21: Get Customer Dashboard**

```
GET /api/v1/customers/analytics/dashboard
Authorization: Bearer {token}
```

**Response:**
```json
{
  "profile": { /* customer profile */ },
  "analytics": {
    "totalOrders": 15,
    "totalSpent": 450000.00,
    "averageOrderValue": 30000.00,
    "monthlyBudgetCap": 100000.00,
    "currentMonthSpent": 85000.00,
    "remainingBudget": 15000.00
  },
  "recentOrders": [
    {
      "orderId": "order-1",
      "orderNumber": "ORD-2026-001",
      "orderDate": "2026-02-08T...",
      "totalAmount": 45000.00,
      "status": "DELIVERED",
      "itemCount": 3
    }
  ],
  "wishlistCount": 5,
  "addressCount": 2
}
```

---

## 💡 **Advanced Features**

### **Price Drop Notifications**

When product price drops:
```json
{
  "productName": "Wireless Headphones",
  "priceWhenAdded": 35000.00,
  "currentPrice": 24000.00,
  "priceDropPercentage": 31.43,
  "targetPrice": 25000.00,
  "isPriceAtTarget": true,  // Target reached!
  "notifyOnPriceDrop": true
}
```

**System can send:**
- Email notification
- Push notification
- SMS alert

### **Customer Segmentation**

```json
{
  "isNewCustomer": true,      // 0-1 orders
  "isActiveCustomer": false,   // Purchased in last 90 days
  "isVIPCustomer": false       // Lifetime value > ₦100,000
}
```

**Use for:**
- Targeted marketing
- Special discounts
- Priority support

---

## 🎯 **Validation Tests**

### **Test: Duplicate Email**
```json
POST /api/v1/customers/register
{
  "email": "john.doe@example.com"  // Already exists
}
```
**Expected:** 400 Bad Request - "Email already registered"

### **Test: Weak Password**
```json
POST /api/v1/customers/register
{
  "password": "weak"  // Too short
}
```
**Expected:** 400 Bad Request - "Password must be at least 8 characters"

### **Test: Invalid Login**
```json
POST /api/v1/customers/login
{
  "email": "john@example.com",
  "password": "WrongPassword"
}
```
**Expected:** 401 Unauthorized - "Invalid email or password"

### **Test: Expired Guest Session**
```
GET /api/v1/customers/guest/session/{expired-session-id}
```
**Expected:** 400 Bad Request - "Guest session expired"

---

## 🎉 **Customer Management Complete!**

You now have:
- ✅ Customer registration (email/password)
- ✅ Google OAuth integration (ready)
- ✅ Guest checkout support
- ✅ Profile management
- ✅ Address book with GPS
- ✅ Wishlist with price tracking
- ✅ Budget management
- ✅ Customer analytics
- ✅ Customer dashboard

**Next: Shopping Cart & Orders!** 🛒
