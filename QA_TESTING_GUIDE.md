# 🧪 EMJAY BACKEND - COMPLETE QA TESTING GUIDE

## 📋 **OVERVIEW**

This guide provides a comprehensive testing workflow for all backend endpoints in the Emjay platform.

**Total Endpoints:** 245+
**Base URL:** `http://localhost:8080/api/v1`

---

## 🔐 **AUTHENTICATION & SETUP**

Before testing, you'll need to:
1. Create test accounts
2. Obtain authentication tokens
3. Set up test data

---

## 📍 **TESTING SEQUENCE**

The endpoints are organized in a recommended testing order that builds on previous steps.

---

# PHASE 1: AUTHENTICATION & USER MANAGEMENT

## 1.1 Create Admin User

**Endpoint:** `POST /auth/register`

**Description:** Create the first admin user for testing

**Request Body:**
```json
{
  "email": "admin@emjay.com",
  "password": "Admin123!@#",
  "firstName": "Admin",
  "lastName": "User",
  "role": "ADMIN"
}
```

**Expected Result:**
- Status: `201 Created`
- Response contains user ID and token
- User is created with ADMIN role

---

## 1.2 Login as Admin

**Endpoint:** `POST /auth/login`

**Description:** Authenticate and get JWT token

**Request Body:**
```json
{
  "email": "admin@emjay.com",
  "password": "Admin123!@#"
}
```

**Expected Result:**
- Status: `200 OK`
- Response contains `token` field
- **Save this token** - use in `Authorization: Bearer {token}` header for all subsequent requests

---

## 1.3 Create Manager User

**Endpoint:** `POST /auth/register`

**Request Body:**
```json
{
  "email": "manager@emjay.com",
  "password": "Manager123!@#",
  "firstName": "Store",
  "lastName": "Manager",
  "role": "MANAGER"
}
```

**Expected Result:**
- Status: `201 Created`
- Manager account created

---

## 1.4 Create Staff User

**Endpoint:** `POST /auth/register`

**Request Body:**
```json
{
  "email": "staff@emjay.com",
  "password": "Staff123!@#",
  "firstName": "Staff",
  "lastName": "Member",
  "role": "STAFF"
}
```

**Expected Result:**
- Status: `201 Created`
- Staff account created

---

# PHASE 2: INVENTORY MANAGEMENT SYSTEM (IMS)

**Note:** Use Admin token for all IMS operations

## 2.1 Create Categories

### Create Main Category - Hair Care

**Endpoint:** `POST /inventory/categories`

**Request Body:**
```json
{
  "name": "Hair Care",
  "description": "Hair care products and treatments",
  "displayOrder": 1
}
```

**Expected Result:**
- Status: `201 Created`
- Response contains category ID
- **Save category ID** for later use

---

### Create Subcategory - Natural Hair

**Endpoint:** `POST /inventory/categories`

**Request Body:**
```json
{
  "name": "Natural Hair",
  "description": "Products for natural hair",
  "parentId": "{hair-care-category-id}",
  "displayOrder": 1
}
```

---

### Create Category - Skin Care

**Endpoint:** `POST /inventory/categories`

**Request Body:**
```json
{
  "name": "Skin Care",
  "description": "Skin care products",
  "displayOrder": 2
}
```

---

## 2.2 Get All Categories

**Endpoint:** `GET /inventory/categories`

**Expected Result:**
- Status: `200 OK`
- Returns list of all categories
- Should show hierarchical structure

---

## 2.3 Create Brands

### Create Brand - Cantu

**Endpoint:** `POST /inventory/brands`

**Request Body:**
```json
{
  "name": "Cantu",
  "description": "Natural hair care products",
  "website": "https://www.cantubeauty.com",
  "country": "USA"
}
```

**Expected Result:**
- Status: `201 Created`
- **Save brand ID**

---

### Create Brand - Shea Moisture

**Endpoint:** `POST /inventory/brands`

**Request Body:**
```json
{
  "name": "Shea Moisture",
  "description": "Natural and organic beauty products",
  "website": "https://www.sheamoisture.com",
  "country": "USA"
}
```

---

## 2.4 Create Products

### Create Product 1 - Shampoo

**Endpoint:** `POST /inventory/products`

**Request Body:**
```json
{
  "name": "Moisture Lock Shampoo",
  "description": "Deep moisturizing shampoo for natural hair",
  "sku": "CSHMP-001",
  "categoryId": "{natural-hair-category-id}",
  "brandId": "{cantu-brand-id}",
  "basePrice": 3500.00,
  "costPrice": 2000.00,
  "stockQuantity": 100,
  "reorderLevel": 20,
  "unit": "BOTTLE",
  "weight": 400,
  "weightUnit": "GRAMS",
  "isActive": true
}
```

**Expected Result:**
- Status: `201 Created`
- Product created with SKU
- **Save product ID**

---

### Create Product 2 - Conditioner

**Endpoint:** `POST /inventory/products`

**Request Body:**
```json
{
  "name": "Deep Conditioner",
  "description": "Intensive conditioning treatment",
  "sku": "CCOND-001",
  "categoryId": "{natural-hair-category-id}",
  "brandId": "{cantu-brand-id}",
  "basePrice": 3000.00,
  "costPrice": 1800.00,
  "stockQuantity": 80,
  "reorderLevel": 15,
  "unit": "BOTTLE",
  "weight": 400,
  "weightUnit": "GRAMS",
  "isActive": true
}
```

---

### Create Product 3 - Hair Mask

**Endpoint:** `POST /inventory/products`

**Request Body:**
```json
{
  "name": "Coconut Oil Hair Mask",
  "description": "Weekly deep treatment mask",
  "sku": "SMASK-001",
  "categoryId": "{natural-hair-category-id}",
  "brandId": "{shea-moisture-brand-id}",
  "basePrice": 4500.00,
  "costPrice": 2500.00,
  "stockQuantity": 50,
  "reorderLevel": 10,
  "unit": "JAR",
  "weight": 500,
  "weightUnit": "GRAMS",
  "isActive": true
}
```

---

## 2.5 Get Products

**Endpoint:** `GET /inventory/products?page=0&size=20`

**Expected Result:**
- Status: `200 OK`
- Returns paginated list of products
- Should show all 3 products created

---

## 2.6 Search Products

**Endpoint:** `GET /inventory/products/search?query=shampoo`

**Expected Result:**
- Status: `200 OK`
- Returns products matching "shampoo"

---

## 2.7 Get Low Stock Products

**Endpoint:** `GET /inventory/products/low-stock`

**Expected Result:**
- Status: `200 OK`
- Returns products below reorder level

---

## 2.8 Update Product Stock

**Endpoint:** `PATCH /inventory/products/{product-id}/stock`

**Request Body:**
```json
{
  "quantity": 150,
  "reason": "New stock delivery"
}
```

**Expected Result:**
- Status: `200 OK`
- Stock quantity updated
- Stock movement recorded

---

## 2.9 Create Stock Movement

**Endpoint:** `POST /inventory/stock-movements`

**Request Body:**
```json
{
  "productId": "{product-id}",
  "movementType": "IN",
  "quantity": 50,
  "reason": "Supplier delivery",
  "notes": "Batch #12345"
}
```

**Expected Result:**
- Status: `201 Created`
- Stock movement recorded
- Product stock automatically updated

---

## 2.10 Get Stock Movement History

**Endpoint:** `GET /inventory/stock-movements?productId={product-id}`

**Expected Result:**
- Status: `200 OK`
- Returns all stock movements for product

---

# PHASE 3: STAFF MANAGEMENT SYSTEM (SMS)

## 3.1 Create Staff Member

**Endpoint:** `POST /staff/members`

**Request Body:**
```json
{
  "userId": "{staff-user-id}",
  "employeeId": "EMP-001",
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane.doe@emjay.com",
  "phone": "+2348012345678",
  "dateOfBirth": "1995-05-15",
  "gender": "FEMALE",
  "address": "123 Lagos Street, Victoria Island",
  "city": "Lagos",
  "state": "Lagos",
  "country": "Nigeria",
  "hireDate": "2024-01-15",
  "position": "Hair Stylist",
  "department": "BEAUTY_SERVICES",
  "employmentType": "FULL_TIME",
  "monthlySalary": 150000.00
}
```

**Expected Result:**
- Status: `201 Created`
- **Save staff ID** for services booking

---

## 3.2 Get All Staff

**Endpoint:** `GET /staff/members?page=0&size=20`

**Expected Result:**
- Status: `200 OK`
- Returns paginated staff list

---

## 3.3 Create Staff Schedule

**Endpoint:** `POST /staff/schedules`

**Request Body:**
```json
{
  "staffId": "{staff-id}",
  "dayOfWeek": "MONDAY",
  "startTime": "09:00",
  "endTime": "17:00",
  "isAvailable": true
}
```

**Expected Result:**
- Status: `201 Created`
- Schedule created

---

## 3.4 Create Leave Request

**Endpoint:** `POST /staff/leaves`

**Request Body:**
```json
{
  "staffId": "{staff-id}",
  "leaveType": "ANNUAL",
  "startDate": "2026-03-15",
  "endDate": "2026-03-20",
  "reason": "Family vacation",
  "status": "PENDING"
}
```

**Expected Result:**
- Status: `201 Created`
- Leave request created

---

## 3.5 Approve Leave Request

**Endpoint:** `PATCH /staff/leaves/{leave-id}/approve`

**Expected Result:**
- Status: `200 OK`
- Leave status changed to APPROVED

---

# PHASE 4: E-COMMERCE SYSTEM

## 4.1 Create Customer Account

**Endpoint:** `POST /customers/register`

**Request Body:**
```json
{
  "email": "customer@test.com",
  "password": "Customer123!",
  "firstName": "John",
  "lastName": "Customer",
  "phone": "+2348087654321"
}
```

**Expected Result:**
- Status: `201 Created`
- Customer account created
- **Save customer ID and token**

---

## 4.2 Customer Login

**Endpoint:** `POST /customers/login`

**Request Body:**
```json
{
  "email": "customer@test.com",
  "password": "Customer123!"
}
```

**Expected Result:**
- Status: `200 OK`
- Returns customer token
- **Use this token for customer operations**

---

## 4.3 Add Customer Address

**Endpoint:** `POST /customers/addresses`
**Auth:** Customer token

**Request Body:**
```json
{
  "addressLabel": "Home",
  "recipientName": "John Customer",
  "phone": "+2348087654321",
  "addressLine1": "45 Allen Avenue",
  "addressLine2": "Flat 3",
  "city": "Ikeja",
  "stateProvince": "Lagos",
  "postalCode": "100001",
  "country": "Nigeria",
  "isDefault": true,
  "isShippingAddress": true,
  "isBillingAddress": true
}
```

**Expected Result:**
- Status: `201 Created`
- **Save address ID**

---

## 4.4 Add Product to Cart

**Endpoint:** `POST /cart/items`
**Auth:** Customer token

**Request Body:**
```json
{
  "productId": "{shampoo-product-id}",
  "quantity": 2
}
```

**Expected Result:**
- Status: `201 Created`
- Cart item added
- Cart automatically created if doesn't exist

---

## 4.5 Add More Items to Cart

**Endpoint:** `POST /cart/items`
**Auth:** Customer token

**Request Body:**
```json
{
  "productId": "{conditioner-product-id}",
  "quantity": 1
}
```

---

## 4.6 View Cart

**Endpoint:** `GET /cart`
**Auth:** Customer token

**Expected Result:**
- Status: `200 OK`
- Returns cart with all items
- Shows subtotal, tax, total

---

## 4.7 Update Cart Item Quantity

**Endpoint:** `PUT /cart/items/{item-id}`
**Auth:** Customer token

**Request Body:**
```json
{
  "quantity": 3
}
```

**Expected Result:**
- Status: `200 OK`
- Quantity updated
- Totals recalculated

---

## 4.8 Checkout - Create Order

**Endpoint:** `POST /orders/checkout`
**Auth:** Customer token

**Request Body:**
```json
{
  "shippingAddressId": "{address-id}",
  "paymentMethod": "PAYSTACK",
  "customerNotes": "Please deliver between 9am-5pm"
}
```

**Expected Result:**
- Status: `201 Created`
- Order created with status PENDING_PAYMENT
- Payment record created
- Cart marked as CONVERTED
- Returns order details and payment info
- **Save order ID**

---

## 4.9 Get Order Details

**Endpoint:** `GET /orders/{order-id}`
**Auth:** Customer token

**Expected Result:**
- Status: `200 OK`
- Returns full order details

---

## 4.10 Get Customer Orders

**Endpoint:** `GET /orders/my-orders?page=0&size=20`
**Auth:** Customer token

**Expected Result:**
- Status: `200 OK`
- Returns customer's order history

---

## 4.11 Update Order Status (Admin)

**Endpoint:** `PATCH /orders/{order-id}/status`
**Auth:** Admin token

**Request Body:**
```json
{
  "status": "PAID",
  "reason": "Payment confirmed via Paystack webhook"
}
```

**Expected Result:**
- Status: `200 OK`
- Order status updated
- Status history recorded

---

## 4.12 Guest Checkout Flow

### 4.12.1 Create Guest Session

**Endpoint:** `POST /customers/guest`

**Request Body:**
```json
{
  "email": "guest@test.com",
  "firstName": "Guest",
  "lastName": "User",
  "phone": "+2348099887766"
}
```

**Expected Result:**
- Status: `201 Created`
- Returns guest session ID
- **Save session ID**

---

### 4.12.2 Add to Guest Cart

**Endpoint:** `POST /cart/guest/items`

**Request Body:**
```json
{
  "guestSessionId": "{guest-session-id}",
  "productId": "{product-id}",
  "quantity": 1
}
```

---

### 4.12.3 Guest Checkout

**Endpoint:** `POST /orders/guest/checkout`

**Request Body:**
```json
{
  "guestSessionId": "{guest-session-id}",
  "shippingAddress": {
    "recipientName": "Guest User",
    "phone": "+2348099887766",
    "addressLine1": "10 Test Street",
    "city": "Abuja",
    "stateProvince": "FCT",
    "postalCode": "900001",
    "country": "Nigeria"
  },
  "paymentMethod": "PAYSTACK"
}
```

**Expected Result:**
- Status: `201 Created`
- Order created for guest
- Shipping address saved

---

# PHASE 5: BUNDLES & PROMOTIONS

## 5.1 Calculate Bundle Price

**Endpoint:** `POST /bundles/calculate-price`
**Auth:** Admin token

**Request Body:**
```json
{
  "products": [
    {
      "productId": "{shampoo-id}",
      "quantity": 1
    },
    {
      "productId": "{conditioner-id}",
      "quantity": 1
    },
    {
      "productId": "{hair-mask-id}",
      "quantity": 1
    }
  ]
}
```

**Expected Result:**
- Status: `200 OK`
- Returns original total, suggested bundle price (10-20% discount)
- Savings amount and percentage

---

## 5.2 Create Product Bundle

**Endpoint:** `POST /bundles`
**Auth:** Admin token

**Request Body:**
```json
{
  "name": "Complete Hair Care Bundle",
  "slug": "complete-hair-care-bundle",
  "description": "Everything you need for healthy natural hair",
  "bundlePrice": 9000.00,
  "products": [
    {
      "productId": "{shampoo-id}",
      "quantity": 1
    },
    {
      "productId": "{conditioner-id}",
      "quantity": 1
    },
    {
      "productId": "{hair-mask-id}",
      "quantity": 1
    }
  ],
  "minQuantity": 1,
  "maxQuantity": 10,
  "isFeatured": true,
  "startDate": "2026-03-01",
  "endDate": "2026-12-31"
}
```

**Expected Result:**
- Status: `201 Created`
- Bundle created
- Savings automatically calculated
- **Save bundle ID**

---

## 5.3 Get Active Bundles (Public)

**Endpoint:** `GET /bundles/active`
**Auth:** None required

**Expected Result:**
- Status: `200 OK`
- Returns active bundles
- Shows savings amounts

---

## 5.4 Create Promotion

**Endpoint:** `POST /promotions`
**Auth:** Admin token

**Request Body:**
```json
{
  "name": "New Customer 15% Off",
  "code": "WELCOME15",
  "description": "15% off your first order",
  "promotionType": "PERCENTAGE_DISCOUNT",
  "discountValue": 15,
  "minPurchaseAmount": 5000.00,
  "maxDiscountAmount": 5000.00,
  "usageLimit": 100,
  "usagePerCustomer": 1,
  "appliesTo": "ALL",
  "startDate": "2026-03-01",
  "endDate": "2026-12-31"
}
```

**Expected Result:**
- Status: `201 Created`
- **Save promotion ID**

---

## 5.5 Validate Promo Code (Public)

**Endpoint:** `POST /promotions/validate-code`
**Auth:** None required

**Request Body:**
```json
{
  "code": "WELCOME15",
  "orderAmount": 10000.00,
  "customerId": "{customer-id}"
}
```

**Expected Result:**
- Status: `200 OK`
- Returns validation result
- Shows discount amount
- Indicates if customer can use code

---

# PHASE 6: BLOG/CMS

## 6.1 Create Blog Category

**Endpoint:** `POST /blog/categories`
**Auth:** Admin token

**Request Body:**
```json
{
  "name": "Hair Care Tips",
  "slug": "hair-care-tips",
  "description": "Tips and advice for natural hair care"
}
```

**Expected Result:**
- Status: `201 Created`
- **Save category ID**

---

## 6.2 Create Blog Tags

**Endpoint:** `POST /blog/tags`
**Auth:** Admin token

**Request Body:**
```json
{
  "name": "Natural Hair"
}
```

**Expected Result:**
- Status: `201 Created`
- **Save tag ID**
- Repeat for tags: "Product Review", "Tutorial"

---

## 6.3 Create Blog Post

**Endpoint:** `POST /blog/posts`
**Auth:** Admin token

**Request Body:**
```json
{
  "title": "5 Best Products for Natural Hair in 2026",
  "slug": "5-best-products-natural-hair-2026",
  "excerpt": "Discover the top products that will transform your natural hair journey this year.",
  "content": "<h2>Introduction</h2><p>Natural hair requires special care and the right products. Here are our top 5 picks for 2026...</p><h3>1. Moisture Lock Shampoo</h3><p>This amazing shampoo provides deep moisture...</p>",
  "categoryId": "{blog-category-id}",
  "tagIds": ["{natural-hair-tag-id}", "{product-review-tag-id}"],
  "featuredImageUrl": "https://example.com/images/natural-hair.jpg",
  "status": "DRAFT",
  "isFeatured": true
}
```

**Expected Result:**
- Status: `201 Created`
- Blog post created in DRAFT status
- **Save post ID**

---

## 6.4 Add Product Link to Blog Post

**Endpoint:** `POST /blog/posts/{post-id}/links`
**Auth:** Admin token

**Request Body:**
```json
{
  "linkType": "PRODUCT",
  "linkId": "{shampoo-product-id}",
  "displayText": "Moisture Lock Shampoo - Our Top Pick",
  "context": "Product Recommendations"
}
```

**Expected Result:**
- Status: `201 Created`
- Product link added to post

---

## 6.5 Add Video to Blog Post

**Endpoint:** `POST /blog/posts/{post-id}/videos`
**Auth:** Admin token

**Request Body:**
```json
{
  "videoUrl": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
  "videoProvider": "YOUTUBE",
  "title": "How to Use These Products",
  "description": "Step-by-step tutorial"
}
```

**Expected Result:**
- Status: `201 Created`
- Video embedded in post
- YouTube ID auto-extracted

---

## 6.6 Publish Blog Post

**Endpoint:** `PATCH /blog/posts/{post-id}/publish`
**Auth:** Admin token

**Expected Result:**
- Status: `200 OK`
- Post status changed to PUBLISHED
- Published date set

---

## 6.7 Get Published Posts (Public)

**Endpoint:** `GET /blog/posts?page=0&size=20`
**Auth:** None required

**Expected Result:**
- Status: `200 OK`
- Returns published posts
- Includes product links

---

## 6.8 View Blog Post by Slug (Public)

**Endpoint:** `GET /blog/posts/{slug}`
**Auth:** None required

**Expected Result:**
- Status: `200 OK`
- Returns full post with content
- View count incremented

---

# PHASE 7: BEAUTY SERVICES BOOKING

## 7.1 Create Service Category

**Endpoint:** `POST /services/categories`
**Auth:** Admin token

**Request Body:**
```json
{
  "name": "Hair Styling",
  "description": "Professional hair styling services",
  "displayOrder": 1
}
```

**Expected Result:**
- Status: `201 Created`
- **Save category ID**

---

## 7.2 Create Service

**Endpoint:** `POST /services`
**Auth:** Admin token

**Request Body:**
```json
{
  "name": "Box Braids - Medium Length",
  "description": "Professional box braids for medium length hair",
  "categoryId": "{service-category-id}",
  "basePrice": 15000.00,
  "durationMinutes": 240,
  "bufferTimeMinutes": 30,
  "requiresDeposit": true,
  "depositAmount": 5000.00,
  "isActive": true
}
```

**Expected Result:**
- Status: `201 Created`
- **Save service ID**

---

## 7.3 Create Service Add-on

**Endpoint:** `POST /services/{service-id}/addons`
**Auth:** Admin token

**Request Body:**
```json
{
  "name": "Hair Extension (Premium)",
  "description": "High quality hair extension",
  "price": 3000.00,
  "durationMinutes": 30
}
```

**Expected Result:**
- Status: `201 Created`
- **Save addon ID**

---

## 7.4 Get Available Services (Public)

**Endpoint:** `GET /services?page=0&size=20`
**Auth:** None required

**Expected Result:**
- Status: `200 OK`
- Returns active services

---

## 7.5 Check Staff Availability

**Endpoint:** `GET /services/availability?staffId={staff-id}&date=2026-03-15`
**Auth:** Customer token

**Expected Result:**
- Status: `200 OK`
- Returns available time slots

---

## 7.6 Create Booking

**Endpoint:** `POST /bookings`
**Auth:** Customer token

**Request Body:**
```json
{
  "serviceId": "{service-id}",
  "staffId": "{staff-id}",
  "bookingDate": "2026-03-15",
  "startTime": "10:00",
  "addonIds": ["{addon-id}"],
  "notes": "Please use gentle products"
}
```

**Expected Result:**
- Status: `201 Created`
- Booking created with PENDING status
- **Booking confirmation email queued**
- **24hr reminder scheduled automatically**
- **Save booking ID**

---

## 7.7 Get Customer Bookings

**Endpoint:** `GET /bookings/my-bookings?page=0&size=20`
**Auth:** Customer token

**Expected Result:**
- Status: `200 OK`
- Returns customer's bookings

---

## 7.8 Update Booking Status (Admin)

**Endpoint:** `PATCH /bookings/{booking-id}/status`
**Auth:** Admin token

**Request Body:**
```json
{
  "status": "CONFIRMED"
}
```

**Expected Result:**
- Status: `200 OK`
- Booking status updated

---

## 7.9 Cancel Booking

**Endpoint:** `PATCH /bookings/{booking-id}/cancel`
**Auth:** Customer token

**Request Body:**
```json
{
  "reason": "Schedule conflict"
}
```

**Expected Result:**
- Status: `200 OK`
- Booking cancelled
- **Cancellation notification sent**

---

# PHASE 8: NOTIFICATIONS

## 8.1 Queue Manual Notification (Admin)

**Endpoint:** `POST /notifications/queue`
**Auth:** Admin token

**Request Body:**
```json
{
  "notificationType": "PROMO_CODE",
  "channel": "EMAIL",
  "recipientEmail": "customer@test.com",
  "recipientName": "John Customer",
  "subject": "Special Offer Just For You!",
  "htmlContent": "<h1>20% Off This Weekend!</h1><p>Use code WEEKEND20</p>",
  "relatedEntityType": "PROMOTION"
}
```

**Expected Result:**
- Status: `201 Created`
- Notification queued
- Will be sent within 30 seconds by background job

---

## 8.2 Get Notification History (Admin)

**Endpoint:** `GET /notifications/history?page=0&size=20`
**Auth:** Admin token

**Expected Result:**
- Status: `200 OK`
- Returns sent notifications
- Shows delivery status

---

## 8.3 Get Customer Notification Preferences

**Endpoint:** `GET /notifications/preferences`
**Auth:** Customer token

**Expected Result:**
- Status: `200 OK`
- Returns customer's notification settings

---

## 8.4 Update Notification Preferences

**Endpoint:** `PUT /notifications/preferences`
**Auth:** Customer token

**Request Body:**
```json
{
  "emailPromotions": false,
  "smsPromotions": false,
  "emailBookingReminders": true,
  "smsBookingReminders": true
}
```

**Expected Result:**
- Status: `200 OK`
- Preferences updated

---

## 8.5 Create Email Template (Admin)

**Endpoint:** `POST /notifications/templates/email`
**Auth:** Admin token

**Request Body:**
```json
{
  "name": "Order Shipped Template",
  "templateType": "ORDER_CONFIRMATION",
  "subject": "Your Order is On Its Way! {{order_number}}",
  "htmlContent": "<h1>Order Shipped!</h1><p>Hi {{customer_name}},</p><p>Your order {{order_number}} has been shipped.</p>",
  "variables": {
    "customer_name": "Customer's first name",
    "order_number": "Order number"
  }
}
```

**Expected Result:**
- Status: `201 Created`
- Template saved

---

# PHASE 9: ANALYTICS & BUSINESS INTELLIGENCE

**Note:** All analytics endpoints require Admin or Manager role

## 9.1 Get Dashboard Overview

**Endpoint:** `GET /analytics/dashboard`
**Auth:** Admin token

**Expected Result:**
- Status: `200 OK`
- Returns dashboard metrics:
  - Today's revenue
  - Today's orders
  - Today's bookings
  - Active customers
  - Monthly revenue
  - Monthly growth
  - Top products
  - Recent orders

---

## 9.2 Get Sales Summary

**Endpoint:** `GET /analytics/sales/summary?startDate=2026-02-01&endDate=2026-02-28`
**Auth:** Admin token

**Expected Result:**
- Status: `200 OK`
- Returns sales data for date range
- Total revenue, orders, AOV
- Completion rate
- New vs returning customers
- Sales trend data

---

## 9.3 Get Sales Trend

**Endpoint:** `GET /analytics/sales/trend?period=MONTHLY&count=12`
**Auth:** Admin token

**Expected Result:**
- Status: `200 OK`
- Returns 12 months of sales data
- Shows revenue trends

---

## 9.4 Get Top Products

**Endpoint:** `GET /analytics/products/top?period=MONTHLY&limit=10`
**Auth:** Admin token

**Expected Result:**
- Status: `200 OK`
- Returns top 10 products by revenue
- Shows units sold, revenue, rank

---

## 9.5 Get Customer Segmentation

**Endpoint:** `GET /analytics/customers/segmentation`
**Auth:** Admin token

**Expected Result:**
- Status: `200 OK`
- Returns customer breakdown:
  - VIP customers (high value)
  - Regular customers
  - Occasional customers
  - At-risk customers (churn risk)

---

## 9.6 Get Top Customers

**Endpoint:** `GET /analytics/customers/top?limit=10`
**Auth:** Admin token

**Expected Result:**
- Status: `200 OK`
- Returns top 10 customers by lifetime value
- Shows total spent, order count

---

# AUTOMATED FEATURES TO VERIFY

## Automatic Notifications

After completing the flows above, verify these automated notifications were sent:

### ✅ Order Confirmation
- Created when order is placed
- Email + SMS sent to customer
- Check notification history

### ✅ Booking Confirmation  
- Created when booking is made
- Email + SMS sent immediately

### ✅ 24hr Booking Reminder
- Scheduled automatically
- Check notification queue for scheduled notifications
- Should send 24 hours before appointment

### ✅ Payment Received
- Triggered when order status changes to PAID
- Email + SMS confirmation

---

## Background Jobs to Verify

### Daily Sales Analytics (1 AM)
- Check if sales_analytics table has daily records
- Verify aggregation is working

### Customer Analytics Calculation (2 AM)
- Check admin_customer_analytics table
- Verify customer segmentation

### Notification Queue Processing (Every 30 seconds)
- Verify pending notifications are being sent
- Check notification status changes from PENDING to SENT

---

# NEGATIVE TEST CASES

## Authentication

### Invalid Login
**Endpoint:** `POST /auth/login`
```json
{
  "email": "admin@emjay.com",
  "password": "WrongPassword"
}
```
**Expected:** 401 Unauthorized

---

### Access Protected Endpoint Without Token
**Endpoint:** `GET /inventory/products`
**Headers:** None
**Expected:** 401 Unauthorized

---

### Access Admin Endpoint as Customer
**Endpoint:** `POST /inventory/products`
**Auth:** Customer token
**Expected:** 403 Forbidden

---

## Inventory

### Create Product with Duplicate SKU
**Endpoint:** `POST /inventory/products`
**SKU:** Use existing SKU
**Expected:** 400 Bad Request - "SKU already exists"

---

### Insufficient Stock for Order
1. Set product stock to 1
2. Try to add 5 to cart
**Expected:** 400 Bad Request - "Insufficient stock"

---

## Cart & Checkout

### Checkout with Empty Cart
**Endpoint:** `POST /orders/checkout`
**Expected:** 400 Bad Request - "Cart is empty"

---

### Apply Invalid Promo Code
**Endpoint:** `POST /promotions/validate-code`
```json
{
  "code": "INVALID123",
  "orderAmount": 10000.00
}
```
**Expected:** 404 Not Found

---

## Booking

### Book Unavailable Time Slot
**Endpoint:** `POST /bookings`
**Time:** Same as existing booking
**Expected:** 400 Bad Request - "Time slot not available"

---

## Validation Errors

### Create Product with Missing Required Fields
**Endpoint:** `POST /inventory/products`
```json
{
  "name": "Test Product"
}
```
**Expected:** 400 Bad Request with validation errors

---

# TESTING TOOLS RECOMMENDATIONS

## Postman Collection
- Import all endpoints into Postman
- Create environment variables for:
  - `base_url`
  - `admin_token`
  - `customer_token`
  - `product_id`
  - `order_id`
  - etc.

## Database Checks

Verify data in tables:
```sql
-- Check order was created
SELECT * FROM orders WHERE id = '{order-id}';

-- Check notifications were queued
SELECT * FROM notification_queue WHERE status = 'PENDING';

-- Check stock was updated
SELECT stock_quantity FROM products WHERE id = '{product-id}';

-- Check analytics data
SELECT * FROM sales_analytics WHERE period_date = CURRENT_DATE;
```

---

# EXPECTED LOG OUTPUT

When testing, you should see these logs:

```
📧 [MOCK EMAIL] Sending email to: customer@test.com
📧 [MOCK EMAIL] Subject: Order Confirmed - ORD-2026-00123
📱 [MOCK SMS] Sending SMS to: +2348087654321
📱 [MOCK SMS] Message: Hi John! Your order...
```

---

# SUCCESS CRITERIA

## ✅ Phase 1-2: Authentication & Inventory
- [ ] All user roles created
- [ ] Products created successfully
- [ ] Stock movements working
- [ ] Categories hierarchical

## ✅ Phase 3: Staff Management
- [ ] Staff members created
- [ ] Schedules configured
- [ ] Leave requests flow working

## ✅ Phase 4: E-commerce
- [ ] Customer registration working
- [ ] Cart operations functional
- [ ] Checkout process complete
- [ ] Guest checkout working
- [ ] Orders created successfully

## ✅ Phase 5: Bundles & Promotions
- [ ] Bundles created with savings
- [ ] Promo codes validated
- [ ] Discounts calculated correctly

## ✅ Phase 6: Blog/CMS
- [ ] Posts created and published
- [ ] Product links working
- [ ] Videos embedded
- [ ] Public viewing functional

## ✅ Phase 7: Services & Booking
- [ ] Services created
- [ ] Availability checking works
- [ ] Bookings created
- [ ] Status updates working

## ✅ Phase 8: Notifications
- [ ] Email notifications sent
- [ ] SMS notifications sent
- [ ] Scheduled reminders queued
- [ ] Preferences respected

## ✅ Phase 9: Analytics
- [ ] Dashboard loads
- [ ] Reports generated
- [ ] Customer segmentation working
- [ ] Background jobs running

---

# TROUBLESHOOTING

## No Notifications Received
- Check notification_queue table
- Verify background jobs are running
- Check logs for errors

## Stock Not Updating
- Verify stock_movements table has records
- Check product.stock_quantity value
- Ensure transaction completed

## Authentication Errors
- Verify token is not expired
- Check Authorization header format: `Bearer {token}`
- Ensure user has correct role

---

# CONCLUSION

This testing guide covers all 245+ endpoints across 9 major phases. Follow the sequence for best results, as later phases depend on data created in earlier phases.

**Estimated Testing Time:** 8-10 hours for complete coverage

Good luck with testing! 🎉
