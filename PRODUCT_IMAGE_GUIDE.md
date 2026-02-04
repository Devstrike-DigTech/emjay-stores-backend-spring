# Product Image Upload Guide

## 📸 **Overview**

The product image system allows you to:
- Upload multiple images per product
- Set a primary/featured image
- Specify display order
- Auto-validate image format and size
- Store images locally with URL access

---

## ✅ **Supported Features:**

- **Image Formats**: JPEG, JPG, PNG, WEBP
- **Max File Size**: 5MB
- **Storage**: Local filesystem (`uploads/products/`)
- **Multiple Images**: Yes, unlimited per product
- **Primary Image**: One per product
- **Display Order**: Sortable

---

## 🔌 **API Endpoints:**

### **1. Upload Product Image**
```
POST /api/v1/products/{productId}/images
Authorization: Bearer <token>
Content-Type: multipart/form-data

Parameters:
- file (required): Image file
- isPrimary (optional): true/false (default: false)
- displayOrder (optional): integer (default: 0)
```

### **2. Get All Product Images**
```
GET /api/v1/products/{productId}/images
Authorization: Bearer <token>
```

### **3. Get Primary Image**
```
GET /api/v1/products/{productId}/images/primary
Authorization: Bearer <token>
```

### **4. Set Image as Primary**
```
PATCH /api/v1/products/{productId}/images/{imageId}/set-primary
Authorization: Bearer <token>
```

### **5. Delete Image**
```
DELETE /api/v1/products/{productId}/images/{imageId}
Authorization: Bearer <token>
```

---

## 🧪 **Testing in Swagger:**

### **Step 1: Create a Product**
```json
POST /api/v1/products
{
  "sku": "IMG-TEST-001",
  "name": "Product with Images",
  "categoryId": "<your-category-id>",
  "retailPrice": 5000,
  "costPrice": 3000,
  "stockQuantity": 50,
  "status": "ACTIVE"
}
```
Copy the product ID from the response.

### **Step 2: Upload an Image**
```
POST /api/v1/products/{productId}/images

In Swagger:
1. Click "Try it out"
2. Enter productId
3. Set isPrimary = true
4. Click "Choose File" and select an image
5. Click "Execute"
```

### **Step 3: View Product with Images**
```
GET /api/v1/products/{productId}
```

Response will now include:
```json
{
  "id": "...",
  "name": "Product with Images",
  "images": [
    {
      "id": "image-id",
      "imageUrl": "/uploads/products/uuid_timestamp.jpg",
      "isPrimary": true,
      "displayOrder": 0
    }
  ]
}
```

### **Step 4: Access the Image**
Open in browser:
```
http://localhost:8080/uploads/products/uuid_timestamp.jpg
```

---

## 📦 **Using Postman/cURL:**

### **Upload with cURL:**
```bash
curl -X POST "http://localhost:8080/api/v1/products/{productId}/images?isPrimary=true" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@/path/to/image.jpg"
```

### **Upload with Postman:**
1. Method: POST
2. URL: `http://localhost:8080/api/v1/products/{productId}/images`
3. Headers: `Authorization: Bearer YOUR_TOKEN`
4. Body → form-data:
   - Key: `file` (type: File)
   - Value: Select image file
   - Key: `isPrimary` (type: Text)
   - Value: `true`

---

## 🎨 **Frontend Integration Example:**

### **React/Next.js:**
```javascript
const uploadProductImage = async (productId, imageFile, isPrimary = false) => {
  const formData = new FormData();
  formData.append('file', imageFile);
  
  const response = await fetch(
    `http://localhost:8080/api/v1/products/${productId}/images?isPrimary=${isPrimary}`,
    {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${accessToken}`
      },
      body: formData
    }
  );
  
  return await response.json();
};

// Usage:
<input 
  type="file" 
  accept="image/jpeg,image/jpg,image/png,image/webp"
  onChange={(e) => uploadProductImage(productId, e.target.files[0], true)}
/>
```

### **Display Images:**
```jsx
<div>
  {product.images.map(image => (
    <img 
      key={image.id}
      src={`http://localhost:8080${image.imageUrl}`}
      alt={product.name}
      className={image.isPrimary ? 'primary' : ''}
    />
  ))}
</div>
```

---

## 📁 **File Structure:**

After uploading, files are stored:
```
project-root/
└── uploads/
    └── products/
        ├── uuid1_timestamp1.jpg
        ├── uuid2_timestamp2.png
        └── uuid3_timestamp3.webp
```

**Database stores:**
- `image_url`: `/uploads/products/uuid_timestamp.jpg`
- `file_name`: `uuid_timestamp.jpg`
- `file_size`: bytes
- `mime_type`: `image/jpeg`

---

## 🔒 **Security & Validation:**

✅ **Automatically validates:**
- File type (only images)
- File size (max 5MB)
- Filename (prevents path traversal)

❌ **Blocked:**
- Non-image files
- Files > 5MB
- Invalid filenames with `..`

---

## ☁️ **Upgrading to Cloud Storage (AWS S3):**

To use AWS S3 instead of local storage:

1. **Add dependency to build.gradle.kts:**
```kotlin
implementation("software.amazon.awssdk:s3:2.20.0")
```

2. **Update FileStorageService to use S3:**
```kotlin
@Service
class S3FileStorageService(
    private val s3Client: S3Client
) {
    fun storeFile(file: MultipartFile): FileUploadResult {
        val key = "products/${UUID.randomUUID()}_${file.originalFilename}"
        
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket("your-bucket-name")
                .key(key)
                .contentType(file.contentType)
                .build(),
            RequestBody.fromInputStream(file.inputStream, file.size)
        )
        
        return FileUploadResult(
            imageUrl = "https://your-bucket.s3.amazonaws.com/$key",
            ...
        )
    }
}
```

3. **Configure AWS credentials in application.yml**

---

## 🎯 **Best Practices:**

1. **Always set one primary image** for product listings
2. **Use display order** to control image sequence
3. **Optimize images** before upload (compress, resize)
4. **Use WebP format** for better compression
5. **Consider CDN** for production (CloudFlare, AWS CloudFront)

---

## 🐛 **Troubleshooting:**

**Issue**: Images not showing
- Check `uploads/products/` directory exists
- Verify file permissions
- Check URL: `http://localhost:8080/uploads/products/filename.jpg`

**Issue**: "File too large"
- Max size is 5MB
- Compress image before upload

**Issue**: "Invalid file type"
- Only JPEG, JPG, PNG, WEBP allowed
- Check file extension

---

Happy uploading! 📸
