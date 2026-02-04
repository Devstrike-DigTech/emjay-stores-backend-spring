# Staff Profile Image Upload Guide

## 📸 **Profile Image Endpoints:**

```
POST   /api/v1/staff/profiles/{id}/profile-image    - Upload profile image
DELETE /api/v1/staff/profiles/{id}/profile-image    - Delete profile image
```

---

## 🧪 **Testing Image Upload:**

### **1. Upload Profile Image**

**In Swagger UI:**
1. Navigate to `POST /api/v1/staff/profiles/{id}/profile-image`
2. Click "Try it out"
3. Enter the staff profile ID
4. Click "Choose File" and select an image
5. Click "Execute"

**Using cURL:**
```bash
curl -X POST "http://localhost:8080/api/v1/staff/profiles/{staff-id}/profile-image" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/profile-photo.jpg"
```

**Expected Response (200 OK):**
```json
{
  "id": "...",
  "employeeId": "EMP001",
  "firstName": "John",
  "lastName": "Doe",
  "fullName": "John Doe",
  "profileImageUrl": "staff-profiles/abc123-456def.jpg",
  "position": "Sales Associate",
  "..."
}
```

---

### **2. Verify Image Upload**

**Check the response includes:**
- ✅ `profileImageUrl` is populated
- ✅ Path starts with `staff-profiles/`

**Image is stored in:**
```
uploads/staff-profiles/{unique-filename}.jpg
```

**Access the image:**
```
http://localhost:8080/uploads/staff-profiles/{filename}
```

---

### **3. Update Profile Image (Replace)**

Simply upload again - the old image will be automatically deleted:

```bash
curl -X POST "http://localhost:8080/api/v1/staff/profiles/{staff-id}/profile-image" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/new-photo.jpg"
```

**What happens:**
1. Old image is deleted
2. New image is uploaded
3. Profile is updated with new path

---

### **4. Delete Profile Image**

```bash
curl -X DELETE "http://localhost:8080/api/v1/staff/profiles/{staff-id}/profile-image" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response (200 OK):**
```json
{
  "id": "...",
  "profileImageUrl": null,
  "..."
}
```

---

### **5. View Profile with Image**

```
GET /api/v1/staff/profiles/{id}
```

**Response includes:**
```json
{
  "profileImageUrl": "staff-profiles/abc123.jpg"
}
```

**Full image URL:**
```
http://localhost:8080/uploads/staff-profiles/abc123.jpg
```

---

## 📋 **Complete Workflow:**

### **Scenario: Add Employee Photo**

**Step 1: Create Staff Profile**
```json
POST /api/v1/staff/profiles
{
  "userId": "...",
  "employeeId": "EMP005",
  "firstName": "Sarah",
  "lastName": "Johnson",
  "position": "HR Manager",
  "..."
}
```

**Step 2: Upload Profile Photo**
```
POST /api/v1/staff/profiles/{id}/profile-image
[Select photo file]
```

**Step 3: Verify**
```
GET /api/v1/staff/profiles/{id}
```

Check response includes `profileImageUrl`.

**Step 4: View Image**
Open in browser:
```
http://localhost:8080/uploads/staff-profiles/[filename]
```

---

## ✅ **Validation:**

### **Supported Formats:**
- ✅ JPG/JPEG
- ✅ PNG
- ✅ GIF

### **File Size:**
- ✅ Max: 5MB (configurable in application.yml)

### **Security:**
- ✅ Only Admin/Manager can upload
- ✅ File validation (type, size, content)
- ✅ Unique filenames (prevents collisions)
- ✅ Stored in dedicated `staff-profiles` directory

---

## 🔒 **Security Features:**

1. **Role-Based Access:**
   - Upload: ADMIN, MANAGER
   - View: All authenticated users
   - Delete: ADMIN, MANAGER

2. **File Validation:**
   - Type checking
   - Size limits
   - Content verification

3. **Automatic Cleanup:**
   - Old images deleted on update
   - Images deleted when profile is deleted

---

## 💡 **Use Cases:**

### **Employee ID Cards:**
```
GET /api/v1/staff/profiles/employee/EMP001
```
Returns profile with photo for ID card printing.

### **HR Dashboard:**
```
GET /api/v1/staff/profiles/department/Sales
```
Display team with profile photos.

### **Staff Directory:**
```
GET /api/v1/staff/profiles
```
List all staff with photos.

---

## 🎨 **Frontend Integration:**

### **Display Profile Image:**

```html
<img 
  src="http://localhost:8080/uploads/staff-profiles/abc123.jpg" 
  alt="John Doe"
  class="profile-photo"
/>
```

### **Handle Missing Image:**

```javascript
const profileImageUrl = staff.profileImageUrl 
  ? `http://localhost:8080/uploads/${staff.profileImageUrl}`
  : '/default-avatar.png';
```

### **Upload Form:**

```html
<form>
  <input 
    type="file" 
    accept="image/jpeg,image/png,image/gif"
    onChange={handleFileSelect}
  />
  <button onClick={uploadProfileImage}>Upload</button>
</form>
```

---

## 📊 **Updated Response Structure:**

```json
{
  "id": "uuid",
  "userId": "uuid",
  "employeeId": "EMP001",
  "firstName": "John",
  "lastName": "Doe",
  "fullName": "John Doe",
  "profileImageUrl": "staff-profiles/abc123.jpg",  ← NEW
  "dateOfBirth": "1990-05-15",
  "gender": "Male",
  "phoneNumber": "+234-803-123-4567",
  "position": "Sales Associate",
  "department": "Sales",
  "employmentType": "FULL_TIME",
  "hireDate": "2024-01-15",
  "yearsOfService": 2,
  "status": "ACTIVE",
  "hasEmergencyContact": true,
  "createdAt": "2026-02-02T...",
  "updatedAt": "2026-02-02T..."
}
```

---

## 🎉 **Profile Images Complete!**

Staff profiles now support:
- ✅ Photo upload
- ✅ Photo replacement
- ✅ Photo deletion
- ✅ Automatic cleanup
- ✅ Secure storage

**Next:** Continue with Shift Scheduling, Clock-In/Out, or Leave Management! 🚀
