# Shopping Cart System - Testing Guide

## 📋 **Shopping Cart Endpoints Overview**

### **Cart Operations (9 endpoints)**
```
GET    /api/v1/cart                                - Get cart
POST   /api/v1/cart/guest                          - Create guest cart
POST   /api/v1/cart/items                          - Add to cart
PUT    /api/v1/cart/items/{id}                     - Update item
DELETE /api/v1/cart/items/{id}                     - Remove item
DELETE /api/v1/cart/clear                          - Clear cart
GET    /api/v1/cart/{id}/validate                  - Validate cart
POST   /api/v1/cart/merge                          - Merge carts
```

**Total: 9 endpoints**

---

## 🧪 **Testing Workflow**

### **Scenario 1: Guest Customer Shopping**

#### **Test 1: Create Guest Cart**

First, create a guest session:
```json
POST /api/v1/customers/guest/session
{
  "email": "guest@example.com"
}
```

**Response:**
```json
{
  "sessionId": "guest-session-abc123",
  "customerId": "guest-customer-uuid",
  "expiresAt": "2026-02-11T10:00:00"
}
```

**Save the `sessionId` for subsequent cart operations!**

#### **Test 2: Add Product to Guest Cart**

```json
POST /api/v1/cart/items?guestSessionId=guest-session-abc123
{
  "productId": "<product-uuid>",
  "quantity": 2
}
```

**Expected Response (201):**
```json
{
  "cartId": "cart-uuid",
  "item": {
    "id": "cart-item-uuid",
    "productId": "product-uuid",
    "productName": "Wireless Headphones",
    "productSku": "WH-2024-001",
    "productImageUrl": "https://...",
    "quantity": 2,
    "unitPrice": 35000.00,
    "subtotal": 70000.00,
    "isInStock": true,
    "availableQuantity": 50
  },
  "cart": {
    "cartId": "cart-uuid",
    "itemCount": 1,
    "totalQuantity": 2,
    "subtotal": 70000.00,
    "taxAmount": 5250.00,      // 7.5% VAT
    "totalAmount": 75250.00,
    "isGuest": true,
    "expiresAt": "2026-02-11T10:00:00"
  }
}
```

**What happens:**
- Cart auto-created for guest session
- Product price captured (snapshot)
- Stock validated
- Tax calculated (7.5%)
- Cart expires in 24 hours

#### **Test 3: Add More Products**

```json
POST /api/v1/cart/items?guestSessionId=guest-session-abc123
{
  "productId": "<another-product-uuid>",
  "quantity": 1
}
```

**Response:**
```json
{
  "cart": {
    "itemCount": 2,
    "totalQuantity": 3,
    "subtotal": 120000.00,
    "taxAmount": 9000.00,
    "totalAmount": 129000.00
  }
}
```

#### **Test 4: Add Same Product Again (Increases Quantity)**

```json
POST /api/v1/cart/items?guestSessionId=guest-session-abc123
{
  "productId": "<first-product-uuid>",  // Same product
  "quantity": 1
}
```

**What happens:**
- System finds existing cart item
- Increases quantity from 2 to 3
- Validates total quantity against stock
- Recalculates cart totals

**Response:**
```json
{
  "item": {
    "quantity": 3,  // Increased!
    "subtotal": 105000.00
  },
  "cart": {
    "subtotal": 155000.00,
    "totalAmount": 166625.00
  }
}
```

---

### **Scenario 2: Registered Customer Shopping**

#### **Test 5: Login First**

```json
POST /api/v1/customers/login
{
  "email": "john.doe@example.com",
  "password": "SecurePass123!"
}
```

**Get the `accessToken`**

#### **Test 6: Add to Registered Cart**

```json
POST /api/v1/cart/items
Authorization: Bearer {accessToken}
{
  "productId": "<product-uuid>",
  "quantity": 1
}
```

**No `guestSessionId` needed - system uses authenticated customer ID!**

**Response:**
```json
{
  "cart": {
    "isGuest": false,
    "expiresAt": null  // Registered carts don't expire!
  }
}
```

---

### **Scenario 3: Cart Management**

#### **Test 7: Get Current Cart**

**Guest:**
```
GET /api/v1/cart?guestSessionId=guest-session-abc123
```

**Registered:**
```
GET /api/v1/cart
Authorization: Bearer {accessToken}
```

**Response:**
```json
{
  "cartId": "cart-uuid",
  "itemCount": 3,
  "totalQuantity": 5,
  "subtotal": 200000.00,
  "discountAmount": 0.00,
  "taxAmount": 15000.00,
  "totalAmount": 215000.00,
  "items": [
    {
      "id": "item-1",
      "productName": "Wireless Headphones",
      "quantity": 3,
      "unitPrice": 35000.00,
      "subtotal": 105000.00,
      "isInStock": true,
      "availableQuantity": 47
    },
    {
      "id": "item-2",
      "productName": "Smart Watch",
      "quantity": 2,
      "unitPrice": 47500.00,
      "subtotal": 95000.00,
      "isInStock": true
    }
  ]
}
```

#### **Test 8: Update Item Quantity**

```json
PUT /api/v1/cart/items/{cart-item-uuid}
{
  "quantity": 5
}
```

**Response:**
```json
{
  "cartId": "cart-uuid",
  "itemCount": 2,
  "totalQuantity": 7,
  "subtotal": 270000.00,
  "totalAmount": 290250.00
}
```

#### **Test 9: Remove Item from Cart**

```
DELETE /api/v1/cart/items/{cart-item-uuid}
```

**Response:**
```json
{
  "itemCount": 1,
  "totalQuantity": 2,
  "subtotal": 95000.00,
  "totalAmount": 102125.00
}
```

#### **Test 10: Clear Entire Cart**

```
DELETE /api/v1/cart/clear?guestSessionId=guest-session-abc123
```

**Response:**
```json
{
  "success": true,
  "message": "Cart cleared successfully",
  "cart": {
    "itemCount": 0,
    "totalQuantity": 0,
    "subtotal": 0.00,
    "totalAmount": 0.00
  }
}
```

---

### **Scenario 4: Cart Validation**

#### **Test 11: Validate Cart Before Checkout**

```
GET /api/v1/cart/{cart-uuid}/validate
```

**If everything is OK:**
```json
{
  "isValid": true,
  "issues": [],
  "cart": {
    "itemCount": 2,
    "totalAmount": 129000.00
  }
}
```

**If there are issues:**
```json
{
  "isValid": false,
  "issues": [
    {
      "itemId": "item-1",
      "productId": "product-uuid",
      "productName": "Wireless Headphones",
      "issueType": "INSUFFICIENT_STOCK",
      "message": "Only 2 items available",
      "requestedQuantity": 5,
      "availableQuantity": 2
    },
    {
      "itemId": "item-2",
      "productId": "product-uuid-2",
      "productName": "Smart Watch",
      "issueType": "PRICE_CHANGED",
      "message": "Price has changed from 47500.00 to 45000.00",
      "requestedQuantity": 2,
      "availableQuantity": 10
    }
  ],
  "cart": null
}
```

**Issue Types:**
- `OUT_OF_STOCK` - Product completely out of stock
- `INSUFFICIENT_STOCK` - Not enough quantity available
- `PRICE_CHANGED` - Price changed since added to cart
- `PRODUCT_DISCONTINUED` - Product discontinued
- `PRODUCT_DELETED` - Product no longer exists

---

### **Scenario 5: Guest → Registered (Cart Merge)**

This is a critical e-commerce flow!

#### **Step 1: Guest browses and adds items**
```json
POST /api/v1/cart/items?guestSessionId=guest-123
{
  "productId": "<product-1>",
  "quantity": 2
}
```

```json
POST /api/v1/cart/items?guestSessionId=guest-123
{
  "productId": "<product-2>",
  "quantity": 1
}
```

**Guest cart now has 2 items**

#### **Step 2: Guest decides to register/login**
```json
POST /api/v1/customers/login
{
  "email": "john@example.com",
  "password": "pass"
}
```

**Gets `accessToken`**

#### **Step 3: Merge guest cart into customer account**
```json
POST /api/v1/cart/merge
Authorization: Bearer {accessToken}
{
  "guestSessionId": "guest-123"
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Successfully merged 2 items from guest cart",
  "mergedItemCount": 2,
  "cart": {
    "cartId": "customer-cart-uuid",
    "itemCount": 2,
    "totalQuantity": 3,
    "subtotal": 120000.00,
    "isGuest": false,
    "expiresAt": null
  }
}
```

**What happens:**
1. Guest cart items moved to customer cart
2. If customer already has same products, quantities are combined
3. Guest cart marked as MERGED
4. Customer cart recalculated

**Scenario: Customer already has items**
```json
{
  "message": "Successfully merged 2 items from guest cart",
  "mergedItemCount": 2,
  "cart": {
    "itemCount": 4,  // 2 existing + 2 merged
    "totalQuantity": 7
  }
}
```

---

## 💡 **Advanced Scenarios**

### **Scenario 6: Stock Validation**

**Try to add more than available:**
```json
POST /api/v1/cart/items
{
  "productId": "product-with-low-stock",
  "quantity": 100
}
```

**If only 5 in stock:**
```
400 Bad Request
{
  "error": "Insufficient stock. Available: 5, Requested: 100"
}
```

**Try to update to more than available:**
```json
PUT /api/v1/cart/items/{item-id}
{
  "quantity": 50
}
```

```
400 Bad Request
{
  "error": "Insufficient stock. Available: 5, Requested: 50"
}
```

---

### **Scenario 7: Product Unavailable**

**Try to add discontinued product:**
```json
POST /api/v1/cart/items
{
  "productId": "discontinued-product-uuid",
  "quantity": 1
}
```

```
400 Bad Request
{
  "error": "Product is not available for purchase"
}
```

---

### **Scenario 8: Guest Cart Expiry**

Guest carts expire after 24 hours.

**Create cart:**
```
Created at: 2026-02-10 10:00 AM
Expires at: 2026-02-11 10:00 AM
```

**Try to access after expiry:**
```
GET /api/v1/cart?guestSessionId=expired-session
```

**Response:**
```
404 Not Found
{
  "error": "Cart not found"
}
```

**Expired carts are automatically cleaned up!**

---

## 📊 **Cart Calculation Examples**

### **Example 1: Simple Cart**
```
Product A: ₦50,000 × 2 = ₦100,000
---
Subtotal:     ₦100,000
Tax (7.5%):   ₦7,500
---
Total:        ₦107,500
```

### **Example 2: Multiple Products**
```
Product A: ₦50,000 × 2 = ₦100,000
Product B: ₦30,000 × 1 = ₦30,000
Product C: ₦75,000 × 3 = ₦225,000
---
Subtotal:     ₦355,000
Tax (7.5%):   ₦26,625
---
Total:        ₦381,625
```

### **Example 3: With Discount (Future Feature)**
```
Subtotal:     ₦355,000
Discount:     -₦50,000
---
After Disc:   ₦305,000
Tax (7.5%):   ₦22,875
---
Total:        ₦327,875
```

---

## 🎯 **Testing Checklist**

### **Guest Cart:**
- ✅ Create guest cart
- ✅ Add products
- ✅ Update quantities
- ✅ Remove items
- ✅ View cart
- ✅ Cart expires in 24hr

### **Registered Cart:**
- ✅ Add products (authenticated)
- ✅ Cart persists indefinitely
- ✅ Update/remove items
- ✅ View cart

### **Validations:**
- ✅ Stock availability check
- ✅ Product status check
- ✅ Quantity limits
- ✅ Price snapshot on add
- ✅ Cart validation endpoint

### **Cart Merge:**
- ✅ Merge guest → registered
- ✅ Combine quantities
- ✅ Handle duplicates
- ✅ Mark guest cart as merged

---

## 🎉 **Shopping Cart Complete!**

You now have:
- ✅ Guest checkout support
- ✅ Persistent registered carts
- ✅ Stock validation
- ✅ Price snapshots
- ✅ Tax calculation (7.5% VAT)
- ✅ Cart merge (guest → registered)
- ✅ Cart validation
- ✅ 24-hour expiry for guest carts

**Next: Orders & Checkout!** 💳

---

## 📈 **Phase 3 Progress**

| Module | Status | Endpoints |
|--------|--------|-----------|
| **Customer Management** | ✅ Complete | 26 |
| **Shopping Cart** | ✅ Complete | 9 |
| **Orders & Checkout** | 🔜 Next | ~20 |
| **Public Catalog** | 📋 Planned | ~10 |

**Total E-commerce: 35 endpoints so far!**
**Grand Total: 181 endpoints!** 🚀
