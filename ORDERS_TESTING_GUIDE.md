# Orders & Checkout System - Testing Guide

## 📋 **Orders Endpoints Overview**

### **Order Operations (5 endpoints)**
```
POST   /api/v1/orders/checkout                     - Checkout from cart
GET    /api/v1/orders/{id}                         - Get order by ID
GET    /api/v1/orders/my-orders                    - Get customer orders
PATCH  /api/v1/orders/{id}/status                  - Update status (Admin)
POST   /api/v1/orders/{id}/cancel                  - Cancel order
```

**Total: 5 endpoints**

---

## 🧪 **Complete E-commerce Flow**

### **Step 1: Customer Registration**
```json
POST /api/v1/customers/register
{
  "email": "buyer@example.com",
  "password": "SecurePass123!",
  "firstName": "Jane",
  "lastName": "Buyer"
}
```

### **Step 2: Add Address**
```json
POST /api/v1/customers/me/addresses
Authorization: Bearer {token}
{
  "addressLabel": "Home",
  "recipientName": "Jane Buyer",
  "phone": "+2348012345678",
  "addressLine1": "15 Admiralty Way",
  "city": "Lagos",
  "country": "Nigeria",
  "isDefault": true
}
```

**Save the `address ID`!**

### **Step 3: Add Products to Cart**
```json
POST /api/v1/cart/items
Authorization: Bearer {token}
{
  "productId": "<product-uuid-1>",
  "quantity": 2
}
```

```json
POST /api/v1/cart/items
{
  "productId": "<product-uuid-2>",
  "quantity": 1
}
```

**Cart Summary:**
```
Product A: ₦50,000 × 2 = ₦100,000
Product B: ₦30,000 × 1 = ₦30,000
---
Subtotal:     ₦130,000
Tax (7.5%):   ₦9,750
---
Total:        ₦139,750
```

### **Step 4: Validate Cart (Optional)**
```
GET /api/v1/cart/{cart-id}/validate
```

Ensures:
- Products in stock
- Prices haven't changed
- Products still active

### **Step 5: Checkout!**
```json
POST /api/v1/orders/checkout
Authorization: Bearer {token}
{
  "shippingAddressId": "<address-uuid>",
  "paymentMethod": "PAYSTACK",
  "customerNotes": "Please call before delivery"
}
```

**Expected Response (201):**
```json
{
  "order": {
    "id": "order-uuid",
    "orderNumber": "ORD-2026-00001",
    "customerId": "customer-uuid",
    "status": "PENDING_PAYMENT",
    "subtotal": 130000.00,
    "discountAmount": 0.00,
    "taxAmount": 9750.00,
    "shippingCost": 0.00,
    "totalAmount": 139750.00,
    "itemCount": 2,
    "orderedAt": "2026-02-11T10:30:00",
    "items": [
      {
        "productName": "Wireless Headphones",
        "quantity": 2,
        "unitPrice": 50000.00,
        "subtotal": 100000.00
      },
      {
        "productName": "Smart Watch",
        "quantity": 1,
        "unitPrice": 30000.00,
        "subtotal": 30000.00
      }
    ]
  },
  "payment": {
    "id": "payment-uuid",
    "orderId": "order-uuid",
    "paymentMethod": "PAYSTACK",
    "paymentStatus": "PENDING",
    "amount": 139750.00
  },
  "message": "Order created successfully"
}
```

**What happened:**
1. ✅ Cart validated
2. ✅ Order created with number ORD-2026-00001
3. ✅ Address snapshot captured
4. ✅ Product prices frozen
5. ✅ Cart marked as CONVERTED
6. ✅ Payment record created
7. ✅ Status: PENDING_PAYMENT

---

## 💳 **Payment Flow**

### **Step 6: Process Payment**

**In real implementation:**
- Customer redirected to Paystack/Flutterwave
- Payment processed
- Webhook updates order

**For testing (Manual Status Update):**
```json
PATCH /api/v1/orders/{order-uuid}/status
Authorization: Bearer {admin-token}
{
  "status": "PAID",
  "reason": "Payment confirmed via Paystack"
}
```

**Order Status Changes:**
```
PENDING_PAYMENT → PAID
```

**Response:**
```json
{
  "id": "order-uuid",
  "orderNumber": "ORD-2026-00001",
  "status": "PAID",
  "paidAt": "2026-02-11T10:35:00"
}
```

---

## 📦 **Order Fulfillment**

### **Step 7: Process Order**
```json
PATCH /api/v1/orders/{order-uuid}/status
{
  "status": "PROCESSING",
  "reason": "Order being prepared"
}
```

### **Step 8: Ship Order**
```json
PATCH /api/v1/orders/{order-uuid}/status
{
  "status": "SHIPPED",
  "reason": "Shipped via DHL - Tracking: 1234567890"
}
```

**Response:**
```json
{
  "status": "SHIPPED",
  "shippedAt": "2026-02-12T14:00:00"
}
```

### **Step 9: Deliver Order**
```json
PATCH /api/v1/orders/{order-uuid}/status
{
  "status": "DELIVERED"
}
```

**Complete Order Lifecycle:**
```
PENDING_PAYMENT (Feb 11, 10:30)
      ↓
PAID (Feb 11, 10:35)
      ↓
PROCESSING (Feb 11, 15:00)
      ↓
SHIPPED (Feb 12, 14:00)
      ↓
DELIVERED (Feb 13, 16:30)
```

---

## 📱 **Customer Views Orders**

### **Get Order Details**
```
GET /api/v1/orders/{order-uuid}
Authorization: Bearer {token}
```

**Response:**
```json
{
  "id": "order-uuid",
  "orderNumber": "ORD-2026-00001",
  "status": "DELIVERED",
  "totalAmount": 139750.00,
  "orderedAt": "2026-02-11T10:30:00",
  "paidAt": "2026-02-11T10:35:00",
  "shippedAt": "2026-02-12T14:00:00",
  "deliveredAt": "2026-02-13T16:30:00",
  "items": [...]
}
```

### **Get All Customer Orders**
```
GET /api/v1/orders/my-orders
Authorization: Bearer {token}
```

**Response:**
```json
[
  {
    "orderNumber": "ORD-2026-00003",
    "status": "PROCESSING",
    "totalAmount": 89000.00,
    "orderedAt": "2026-02-15T..."
  },
  {
    "orderNumber": "ORD-2026-00002",
    "status": "DELIVERED",
    "totalAmount": 125000.00,
    "orderedAt": "2026-02-13T..."
  },
  {
    "orderNumber": "ORD-2026-00001",
    "status": "DELIVERED",
    "totalAmount": 139750.00,
    "orderedAt": "2026-02-11T..."
  }
]
```

---

## ❌ **Order Cancellation**

### **Cancel Before Shipping**
```
POST /api/v1/orders/{order-uuid}/cancel?reason=Changed my mind
Authorization: Bearer {token}
```

**Can cancel if status is:**
- PENDING_PAYMENT
- PAID
- PROCESSING

**Cannot cancel if:**
- SHIPPED
- DELIVERED
- Already CANCELLED

**Response:**
```json
{
  "status": "CANCELLED",
  "cancelledAt": "2026-02-11T11:00:00",
  "message": "Order cancelled successfully"
}
```

**What happens:**
- Order marked as CANCELLED
- Stock restored to inventory
- Refund initiated (if paid)

---

## 🎯 **Order Status Workflow**

```mermaid
PENDING_PAYMENT
    ↓ (Payment captured)
PAID
    ↓ (Staff prepares)
PROCESSING
    ↓ (Shipped out)
SHIPPED
    ↓ (Customer receives)
DELIVERED

Side branches:
- PAYMENT_FAILED (from PENDING_PAYMENT)
- CANCELLED (from PENDING_PAYMENT, PAID, PROCESSING)
- REFUNDED (from PAID onwards)
```

---

## 💡 **Advanced Scenarios**

### **Scenario 1: Payment Failed**
```json
{
  "status": "PAYMENT_FAILED",
  "message": "Payment declined by bank"
}
```

Customer can retry payment.

### **Scenario 2: Out of Stock During Checkout**
```
400 Bad Request
{
  "error": "Insufficient stock for Wireless Headphones. Available: 1, Requested: 2"
}
```

### **Scenario 3: Address Snapshot**
Even if customer deletes address later, order still has snapshot:
```json
{
  "shippingAddress": {
    "addressLine1": "15 Admiralty Way",
    "city": "Lagos",
    "recipientName": "Jane Buyer",
    "recipientPhone": "+2348012345678"
  }
}
```

### **Scenario 4: Price Protection**
Product prices captured at checkout:
```json
{
  "items": [
    {
      "unitPrice": 50000.00  // ← Frozen at checkout
    }
  ]
}
```

Even if price changes to ₦60,000 later, customer pays ₦50,000!

---

## 🎉 **Orders & Checkout Complete!**

You now have:
- ✅ Complete checkout flow
- ✅ Order creation from cart
- ✅ Address snapshots
- ✅ Price snapshots
- ✅ Order number generation (ORD-YYYY-NNNNN)
- ✅ Payment integration structure
- ✅ Order status workflow
- ✅ Order history
- ✅ Order cancellation
- ✅ Stock management
- ✅ Status history audit trail

---

## 📈 **Phase 3 Complete Summary**

| Module | Endpoints | Status |
|--------|-----------|--------|
| **Customer Management** | 26 | ✅ Complete |
| **Shopping Cart** | 9 | ✅ Complete |
| **Orders & Checkout** | 5 | ✅ Complete |

**Phase 3 Total: 40 endpoints!**
**Grand Total: 160 endpoints!** 🚀

---

**Your e-commerce platform is now fully functional for:**
- Customer registration & login
- Guest checkout
- Product browsing (from Phase 1)
- Cart management
- Checkout & payment
- Order tracking
- Order fulfillment

**Ready for production testing with your QA engineer!** 🎊
