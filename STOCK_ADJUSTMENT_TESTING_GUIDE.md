# Stock Adjustment Testing Guide

## 📊 **Stock Adjustment Endpoints:**

```
POST   /api/v1/stock-adjustments                    - Create adjustment
GET    /api/v1/stock-adjustments                    - List all (paginated)
GET    /api/v1/stock-adjustments/{id}               - Get by ID
GET    /api/v1/stock-adjustments/product/{id}       - Product history
GET    /api/v1/stock-adjustments/product/{id}/summary - Product summary
GET    /api/v1/stock-adjustments/user/{id}          - User's adjustments
GET    /api/v1/stock-adjustments/type/{type}        - By type
```

---

## 📦 **Adjustment Types:**

- **ADDITION** - Receiving new stock from suppliers
- **DEDUCTION** - Damaged, expired, or lost items
- **SALE** - Products sold to customers
- **RETURN** - Customer returns

---

## 🧪 **Testing Scenarios:**

### **1. Add Stock (Receiving Inventory)**

```json
POST /api/v1/stock-adjustments
{
  "productId": "<your-product-id>",
  "adjustmentType": "ADDITION",
  "quantity": 50,
  "reason": "Received shipment from Beauty Supplies Co."
}
```

**Expected:**
- Product stock increases by 50
- Adjustment record created
- Returns 201 Created

---

### **2. Deduct Stock (Damaged Items)**

```json
POST /api/v1/stock-adjustments
{
  "productId": "<product-id>",
  "adjustmentType": "DEDUCTION",
  "quantity": 5,
  "reason": "Damaged during shipping"
}
```

**Expected:**
- Product stock decreases by 5
- Previous and new quantities tracked

---

### **3. Record Sale**

```json
POST /api/v1/stock-adjustments
{
  "productId": "<product-id>",
  "adjustmentType": "SALE",
  "quantity": 10,
  "reason": "Sold to customer #12345"
}
```

**Expected:**
- Stock decreases by 10
- Sale tracked for reporting

---

### **4. Process Return**

```json
POST /api/v1/stock-adjustments
{
  "productId": "<product-id>",
  "adjustmentType": "RETURN",
  "quantity": 2,
  "reason": "Customer return - unopened"
}
```

**Expected:**
- Stock increases by 2
- Return recorded

---

### **5. Try to Deduct More Than Available**

```json
POST /api/v1/stock-adjustments
{
  "productId": "<product-with-10-stock>",
  "adjustmentType": "SALE",
  "quantity": 15
}
```

**Expected Response (400 Bad Request):**
```json
{
  "message": "Insufficient stock. Available: 10, Requested: 15"
}
```

---

### **6. View All Adjustments**

```
GET /api/v1/stock-adjustments?page=0&size=20
```

Returns paginated list of all stock adjustments, newest first.

---

### **7. View Product's Stock History**

```
GET /api/v1/stock-adjustments/product/{product-id}?page=0&size=20
```

Shows all stock changes for a specific product.

**Response:**
```json
{
  "content": [
    {
      "id": "...",
      "productId": "...",
      "productName": "Vitamin C Serum",
      "productSku": "SER-VIT-001",
      "userId": "...",
      "userName": "admin",
      "adjustmentType": "ADDITION",
      "quantity": 50,
      "previousQuantity": 0,
      "newQuantity": 50,
      "reason": "Initial stock",
      "createdAt": "2026-02-02T10:00:00"
    }
  ],
  "totalElements": 5,
  "totalPages": 1,
  "currentPage": 0,
  "pageSize": 20
}
```

---

### **8. Get Product Stock Summary**

```
GET /api/v1/stock-adjustments/product/{product-id}/summary
```

**Response:**
```json
{
  "productId": "...",
  "productName": "Vitamin C Serum",
  "currentStock": 35,
  "totalAdditions": 50,
  "totalDeductions": 5,
  "totalSales": 10,
  "totalReturns": 0,
  "adjustments": [...]
}
```

Shows complete stock movement summary.

---

### **9. View User's Adjustments**

```
GET /api/v1/stock-adjustments/user/{user-id}
```

See all stock adjustments made by a specific user (audit trail).

---

### **10. Filter by Adjustment Type**

```
GET /api/v1/stock-adjustments/type/SALE?page=0&size=20
```

Get only sales, or any specific type: `ADDITION`, `DEDUCTION`, `SALE`, `RETURN`.

---

## 🎯 **Complete Workflow Example:**

### **Scenario: New Product Launch**

**Step 1: Create Product**
```json
POST /api/v1/products
{
  "sku": "SERUM-NEW-001",
  "name": "Hyaluronic Acid Serum",
  "categoryId": "<serums-category>",
  "supplierId": "<supplier-id>",
  "retailPrice": 18000,
  "costPrice": 12000,
  "stockQuantity": 0,
  "minStockThreshold": 10
}
```
Product created with 0 stock.

**Step 2: Receive Initial Stock**
```json
POST /api/v1/stock-adjustments
{
  "productId": "<product-id>",
  "adjustmentType": "ADDITION",
  "quantity": 100,
  "reason": "Initial inventory - received from supplier"
}
```
Stock now: 100

**Step 3: Record Sales (Multiple)**
```json
POST /api/v1/stock-adjustments
{
  "productId": "<product-id>",
  "adjustmentType": "SALE",
  "quantity": 15,
  "reason": "Walk-in customer purchase"
}
```
Stock now: 85

**Step 4: Handle Damaged Items**
```json
POST /api/v1/stock-adjustments
{
  "productId": "<product-id>",
  "adjustmentType": "DEDUCTION",
  "quantity": 3,
  "reason": "Damaged packaging - cannot sell"
}
```
Stock now: 82

**Step 5: Process Return**
```json
POST /api/v1/stock-adjustments
{
  "productId": "<product-id>",
  "adjustmentType": "RETURN",
  "quantity": 1,
  "reason": "Customer return - sealed product"
}
```
Stock now: 83

**Step 6: Review History**
```
GET /api/v1/stock-adjustments/product/{product-id}/summary
```

See complete audit trail!

---

## 📊 **Understanding the Response:**

### **Stock Adjustment Response:**
```json
{
  "id": "adjustment-uuid",
  "productId": "product-uuid",
  "productName": "Hyaluronic Acid Serum",
  "productSku": "SERUM-NEW-001",
  "userId": "user-uuid",
  "userName": "admin",
  "adjustmentType": "ADDITION",
  "quantity": 100,
  "previousQuantity": 0,
  "newQuantity": 100,
  "reason": "Initial inventory",
  "createdAt": "2026-02-02T10:30:00"
}
```

**Key Fields:**
- `previousQuantity` - Stock before adjustment
- `newQuantity` - Stock after adjustment
- `quantity` - Amount adjusted
- `reason` - Why the adjustment was made
- `userName` - Who made the adjustment (audit trail)

---

## 🔍 **Audit & Reporting Use Cases:**

### **Daily Sales Report:**
```
GET /api/v1/stock-adjustments/type/SALE?page=0&size=100
```

### **Stock Losses (Damaged/Expired):**
```
GET /api/v1/stock-adjustments/type/DEDUCTION?page=0&size=100
```

### **User Activity:**
```
GET /api/v1/stock-adjustments/user/{user-id}
```

### **Product Movement:**
```
GET /api/v1/stock-adjustments/product/{product-id}/summary
```

---

## 💡 **Best Practices:**

1. **Always provide reason** for audit trail
2. **Record sales immediately** for accurate inventory
3. **Regular stock checks** - compare physical vs system
4. **Review deductions** to identify patterns (theft, damage)
5. **Track returns** separately from additions
6. **Monitor low stock** products (use product low-stock endpoint)

---

## 🚨 **Important Notes:**

- ✅ **Automatic Stock Update** - Product quantity updates automatically
- ✅ **Cannot Oversell** - Validates sufficient stock before deduction
- ✅ **Audit Trail** - Every change tracked with user and timestamp
- ✅ **Immutable** - Adjustments cannot be edited/deleted (data integrity)
- ✅ **All Users Can Adjust** - Admin, Manager, and Staff roles

---

## 🎉 **System Complete!**

You now have a complete inventory management system with:
- ✅ Product Management
- ✅ Category Hierarchy
- ✅ Supplier Tracking
- ✅ Image Management
- ✅ **Stock Adjustments with Full Audit Trail**

Happy tracking! 📊
