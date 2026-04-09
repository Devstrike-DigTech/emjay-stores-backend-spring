# Category Management Testing Guide

## 📋 **Category Endpoints:**

```
POST   /api/v1/categories                      - Create category
GET    /api/v1/categories                      - List all categories
GET    /api/v1/categories/{id}                 - Get category by ID
GET    /api/v1/categories/root                 - Get root categories
GET    /api/v1/categories/{id}/subcategories   - Get subcategories
GET    /api/v1/categories/tree                 - Get hierarchical tree
PUT    /api/v1/categories/{id}                 - Update category
DELETE /api/v1/categories/{id}                 - Delete category
```

---

## 🧪 **Testing Scenarios:**

### **1. View Existing Categories (Already Seeded)**

```
GET /api/v1/categories
```

**Expected Response:**
```json
{
  "categories": [
    {
      "id": "...",
      "name": "Skincare",
      "description": "Skin care products and treatments",
      "parentId": null,
      "isActive": true,
      "isRootCategory": true,
      "hasSubcategories": true,
      "productCount": 0
    },
    {
      "id": "...",
      "name": "Cleansers",
      "description": "Face cleansers and washing products",
      "parentId": "<skincare-id>",
      "isActive": true,
      "isRootCategory": false,
      "hasSubcategories": false,
      "productCount": 0
    }
  ],
  "totalCount": 7
}
```

---

### **2. Get Category Tree (Hierarchical)**

```
GET /api/v1/categories/tree
```

**Expected Response:**
```json
[
  {
    "id": "...",
    "name": "Skincare",
    "description": "Skin care products and treatments",
    "isActive": true,
    "productCount": 0,
    "subcategories": [
      {
        "id": "...",
        "name": "Cleansers",
        "description": "Face cleansers and washing products",
        "isActive": true,
        "productCount": 0,
        "subcategories": []
      },
      {
        "id": "...",
        "name": "Moisturizers",
        "description": "Face and body moisturizers",
        "isActive": true,
        "productCount": 0,
        "subcategories": []
      }
    ]
  }
]
```

---

### **3. Create Root Category**

```json
POST /api/v1/categories
{
  "name": "Fragrances",
  "description": "Perfumes and body sprays"
}
```

**Note:** `parentId` is `null` or omitted for root categories.

---

### **4. Create Subcategory**

First, get a parent category ID (e.g., Skincare), then:

```json
POST /api/v1/categories
{
  "name": "Sunscreens",
  "description": "Sun protection products",
  "parentId": "<skincare-category-id>"
}
```

---

### **5. Get Root Categories Only**

```
GET /api/v1/categories/root
```

Returns only categories with `parentId = null`.

---

### **6. Get Subcategories**

```
GET /api/v1/categories/{skincare-id}/subcategories
```

Returns: Cleansers, Moisturizers, Serums, Sunscreens

---

### **7. Update Category**

```json
PUT /api/v1/categories/{category-id}
{
  "name": "Skincare & Beauty",
  "description": "Complete skin care solutions",
  "isActive": true
}
```

---

### **8. Try to Delete Category with Subcategories**

```
DELETE /api/v1/categories/{skincare-id}
```

**Expected Response (400 Bad Request):**
```json
{
  "message": "Cannot delete category with subcategories. Delete or reassign subcategories first."
}
```

---

### **9. Try to Delete Category with Products**

First, assign a product to a category, then try to delete:

```
DELETE /api/v1/categories/{category-with-products}
```

**Expected Response (400 Bad Request):**
```json
{
  "message": "Cannot delete category with products. Delete or reassign products first."
}
```

---

### **10. Delete Empty Category**

```
DELETE /api/v1/categories/{empty-category-id}
```

**Expected Response (200 OK):**
```json
{
  "message": "Category deleted successfully"
}
```

---

## 🎨 **Creating a Complete Category Structure:**

### **Beauty Store Example:**

```json
// 1. Create "Nail Care" root category
POST /api/v1/categories
{
  "name": "Nail Care",
  "description": "Nail polish and nail care products"
}
// Copy the ID from response

// 2. Create "Nail Polish" subcategory
POST /api/v1/categories
{
  "name": "Nail Polish",
  "description": "Color nail polish",
  "parentId": "<nail-care-id>"
}

// 3. Create "Nail Tools" subcategory
POST /api/v1/categories
{
  "name": "Nail Tools",
  "description": "Nail clippers, files, and tools",
  "parentId": "<nail-care-id>"
}
```

---

## 📊 **Understanding the Response Fields:**

### **CategoryResponse:**
- `isRootCategory`: `true` if `parentId` is `null`
- `hasSubcategories`: `true` if this category has children
- `productCount`: Number of products in this category

### **CategoryTreeResponse:**
- Nested structure showing parent-child relationships
- `subcategories`: Array of child categories (recursive)

---

## 🔍 **Use Cases:**

### **Frontend Navigation Menu:**
```
GET /api/v1/categories/tree
```
Use this to build a hierarchical navigation menu.

### **Product Filter Dropdown:**
```
GET /api/v1/categories
```
Use this for a flat list of all categories.

### **Breadcrumb Navigation:**
```
GET /api/v1/categories/{id}
```
Then follow `parentId` chain to build breadcrumbs.

---

## 🚫 **Validation Rules:**

1. **Name must be unique** across all categories
2. **Cannot be its own parent** (circular reference check)
3. **Cannot create circular references** (A → B → C → A)
4. **Cannot delete if has subcategories**
5. **Cannot delete if has products**

---

## 💡 **Best Practices:**

1. **Keep hierarchy shallow** (2-3 levels max)
2. **Use descriptive names** for better UX
3. **Set inactive instead of deleting** to preserve history
4. **Reassign products** before deleting categories

---

## 🧩 **Integration with Products:**

When creating a product, use a category ID:

```json
POST /api/v1/products
{
  "name": "Vitamin C Serum",
  "categoryId": "<serums-category-id>",
  ...
}
```

Then filter products by category:
```
GET /api/v1/products/category/{category-id}
```

---

Happy organizing! 🏷️
