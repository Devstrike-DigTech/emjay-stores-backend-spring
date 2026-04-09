# Supplier Management Testing Guide

## 📦 **Supplier Endpoints:**

```
POST   /api/v1/suppliers                   - Create supplier
GET    /api/v1/suppliers                   - List all suppliers
GET    /api/v1/suppliers/{id}              - Get supplier by ID
GET    /api/v1/suppliers/active            - Get active suppliers only
GET    /api/v1/suppliers/{id}/products     - Get supplier's products
PUT    /api/v1/suppliers/{id}              - Update supplier
DELETE /api/v1/suppliers/{id}              - Delete supplier
```

---

## 🧪 **Testing Scenarios:**

### **1. View Existing Supplier (Already Seeded)**

```
GET /api/v1/suppliers
```

**Expected Response:**
```json
{
  "suppliers": [
    {
      "id": "...",
      "name": "Beauty Supplies Co.",
      "contactPerson": "Jane Smith",
      "email": "contact@beautysupplies.com",
      "phone": "+234-800-123-4567",
      "address": "123 Beauty Lane, Lagos, Nigeria",
      "paymentTerms": null,
      "isActive": true,
      "productCount": 0
    }
  ],
  "totalCount": 1
}
```

---

### **2. Create New Supplier**

```json
POST /api/v1/suppliers
{
  "name": "Premium Cosmetics Ltd",
  "contactPerson": "John Doe",
  "email": "sales@premiumcosmetics.com",
  "phone": "+234-800-555-0001",
  "address": "456 Commerce Street, Abuja, Nigeria",
  "paymentTerms": "Net 30 days, 2% discount for early payment"
}
```

**Expected Response (201 Created):**
```json
{
  "id": "uuid-here",
  "name": "Premium Cosmetics Ltd",
  "contactPerson": "John Doe",
  "email": "sales@premiumcosmetics.com",
  "phone": "+234-800-555-0001",
  "address": "456 Commerce Street, Abuja, Nigeria",
  "paymentTerms": "Net 30 days, 2% discount for early payment",
  "isActive": true,
  "productCount": 0
}
```

---

### **3. Get Supplier by ID**

```
GET /api/v1/suppliers/{supplier-id}
```

Returns detailed supplier information.

---

### **4. Get Active Suppliers Only**

```
GET /api/v1/suppliers/active
```

Returns only suppliers where `isActive = true`.

---

### **5. Update Supplier**

```json
PUT /api/v1/suppliers/{supplier-id}
{
  "contactPerson": "Jane Doe",
  "phone": "+234-800-555-9999",
  "paymentTerms": "Net 45 days"
}
```

**Note:** Only provided fields are updated. Omitted fields remain unchanged.

---

### **6. Deactivate Supplier**

```json
PUT /api/v1/suppliers/{supplier-id}
{
  "isActive": false
}
```

Sets supplier as inactive (won't appear in active suppliers list).

---

### **7. Get Supplier's Products**

First, create a product with a supplier, then:

```
GET /api/v1/suppliers/{supplier-id}/products?page=0&size=20
```

Returns paginated list of products from this supplier.

---

### **8. Try to Delete Supplier with Products**

```
DELETE /api/v1/suppliers/{supplier-with-products}
```

**Expected Response (400 Bad Request):**
```json
{
  "message": "Cannot delete supplier with products. Delete or reassign products first."
}
```

---

### **9. Delete Empty Supplier**

```
DELETE /api/v1/suppliers/{supplier-without-products}
```

**Expected Response (200 OK):**
```json
{
  "message": "Supplier deleted successfully"
}
```

---

## 💼 **Sample Suppliers for Testing:**

### **Local Supplier:**
```json
{
  "name": "Lagos Beauty Distributors",
  "contactPerson": "Amina Hassan",
  "email": "info@lagosbeauty.ng",
  "phone": "+234-803-555-1234",
  "address": "78 Market Road, Lagos Island, Lagos",
  "paymentTerms": "Cash on delivery or Net 15 days"
}
```

### **International Supplier:**
```json
{
  "name": "Global Skincare Imports",
  "contactPerson": "Michael Chen",
  "email": "orders@globalskincare.com",
  "phone": "+1-555-123-4567",
  "address": "1234 Import Plaza, New York, NY 10001, USA",
  "paymentTerms": "Wire transfer, Net 60 days, FOB shipping point"
}
```

### **Wholesale Supplier:**
```json
{
  "name": "Wholesale Beauty Warehouse",
  "contactPerson": "Sarah Johnson",
  "email": "wholesale@beautywarehouse.com",
  "phone": "+234-809-777-8888",
  "address": "Industrial Estate, Plot 45, Ikeja, Lagos",
  "paymentTerms": "Minimum order $5000, Net 30 days, 5% bulk discount"
}
```

---

## 🔗 **Integration with Products:**

When creating a product, link it to a supplier:

```json
POST /api/v1/products
{
  "name": "Luxury Face Cream",
  "sku": "CREAM-LUX-001",
  "categoryId": "<category-id>",
  "supplierId": "<supplier-id>",
  "retailPrice": 25000,
  "costPrice": 15000,
  ...
}
```

Then view supplier's products:
```
GET /api/v1/suppliers/{supplier-id}/products
```

---

## 📊 **Understanding Response Fields:**

- `productCount`: Number of products from this supplier
- `isActive`: Whether supplier is currently active
- `paymentTerms`: Custom payment terms negotiated with supplier

---

## 🚫 **Validation Rules:**

1. **Name must be unique** across all suppliers
2. **Email must be valid format** (if provided)
3. **Cannot delete if has products** assigned
4. **Name is required**, all other fields optional

---

## 💡 **Best Practices:**

1. **Set inactive instead of deleting** to preserve history
2. **Document payment terms** clearly for accounting
3. **Keep contact info updated** for smooth operations
4. **Use descriptive names** (e.g., "Premium Skincare Imports" not "PSI")
5. **Track products per supplier** to manage inventory sources

---

## 🎯 **Common Workflows:**

### **Adding New Product from Existing Supplier:**
1. `GET /api/v1/suppliers/active` - Get active suppliers
2. Select supplier ID
3. `POST /api/v1/products` with `supplierId`

### **Changing Supplier for Product:**
1. `GET /api/v1/suppliers` - Find new supplier
2. `PUT /api/v1/products/{product-id}` - Update `supplierId`

### **Reviewing Supplier Performance:**
1. `GET /api/v1/suppliers/{id}` - Get supplier details
2. `GET /api/v1/suppliers/{id}/products` - View their products
3. Analyze product count and sales data

---

Happy managing! 📦
