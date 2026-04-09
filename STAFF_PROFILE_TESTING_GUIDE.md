# Staff Profile Testing Guide

## 📋 **Staff Profile Endpoints:**

```
POST   /api/v1/staff/profiles                    - Create staff profile
GET    /api/v1/staff/profiles                    - List all staff
GET    /api/v1/staff/profiles/{id}               - Get by ID
GET    /api/v1/staff/profiles/user/{userId}      - Get by user ID
GET    /api/v1/staff/profiles/employee/{empId}   - Get by employee ID
GET    /api/v1/staff/profiles/department/{dept}  - Get by department
GET    /api/v1/staff/profiles/status/{status}    - Get by status
GET    /api/v1/staff/profiles/employment-type/{type} - Get by employment type
GET    /api/v1/staff/profiles/statistics         - Get statistics
PUT    /api/v1/staff/profiles/{id}               - Update profile
DELETE /api/v1/staff/profiles/{id}               - Delete profile
```

---

## 🧪 **Testing Scenarios:**

### **1. Create Staff Profile**

First, create a user (or use existing):

```json
POST /api/v1/admin/users
{
  "username": "john.doe",
  "email": "john.doe@emjay.com",
  "password": "Password123!",
  "fullName": "John Doe",
  "role": "STAFF"
}
```

Then create staff profile:

```json
POST /api/v1/staff/profiles
{
  "userId": "<user-id-from-above>",
  "employeeId": "EMP001",
  "firstName": "John",
  "lastName": "Doe",
  "dateOfBirth": "1990-05-15",
  "gender": "Male",
  "phoneNumber": "+234-803-123-4567",
  "personalEmail": "john.personal@gmail.com",
  "address": "123 Main Street, Lekki",
  "city": "Lagos",
  "state": "Lagos",
  "country": "Nigeria",
  "nationality": "Nigerian",
  "position": "Sales Associate",
  "department": "Sales",
  "employmentType": "FULL_TIME",
  "hireDate": "2024-01-15",
  "salary": 150000,
  "bankName": "GTBank",
  "accountNumber": "0123456789",
  "accountName": "John Doe",
  "emergencyContactName": "Jane Doe",
  "emergencyContactPhone": "+234-803-987-6543",
  "emergencyContactRelationship": "Spouse"
}
```

**Expected Response (201 Created):**
```json
{
  "id": "...",
  "userId": "...",
  "employeeId": "EMP001",
  "firstName": "John",
  "lastName": "Doe",
  "fullName": "John Doe",
  "position": "Sales Associate",
  "department": "Sales",
  "employmentType": "FULL_TIME",
  "hireDate": "2024-01-15",
  "yearsOfService": 2,
  "salary": 150000,
  "hasEmergencyContact": true,
  "status": "ACTIVE",
  "createdAt": "2026-02-02T...",
  "updatedAt": "2026-02-02T..."
}
```

---

### **2. Create Multiple Staff Members**

**Manager:**
```json
POST /api/v1/staff/profiles
{
  "userId": "<manager-user-id>",
  "employeeId": "EMP002",
  "firstName": "Sarah",
  "lastName": "Smith",
  "position": "Store Manager",
  "department": "Management",
  "employmentType": "FULL_TIME",
  "hireDate": "2023-06-01",
  "salary": 300000
}
```

**Part-Time Staff:**
```json
POST /api/v1/staff/profiles
{
  "userId": "<user-id>",
  "employeeId": "EMP003",
  "firstName": "Mike",
  "lastName": "Johnson",
  "position": "Sales Assistant",
  "department": "Sales",
  "employmentType": "PART_TIME",
  "hireDate": "2025-01-10",
  "salary": 80000
}
```

**Intern:**
```json
POST /api/v1/staff/profiles
{
  "userId": "<user-id>",
  "employeeId": "INTERN001",
  "firstName": "Ada",
  "lastName": "Okafor",
  "position": "Marketing Intern",
  "department": "Marketing",
  "employmentType": "INTERN",
  "hireDate": "2026-01-01",
  "contractEndDate": "2026-06-30",
  "salary": 50000
}
```

---

### **3. Get All Staff Profiles**

```
GET /api/v1/staff/profiles?page=0&size=20
```

**Returns:** Paginated list of all staff profiles.

---

### **4. Get Staff by Department**

```
GET /api/v1/staff/profiles/department/Sales?page=0&size=20
```

**Returns:** All staff in Sales department.

---

### **5. Get Staff by Status**

```
GET /api/v1/staff/profiles/status/ACTIVE
```

**Returns:** All active staff members.

Available statuses:
- `ACTIVE`
- `ON_LEAVE`
- `SUSPENDED`
- `TERMINATED`

---

### **6. Get Staff by Employment Type**

```
GET /api/v1/staff/profiles/employment-type/FULL_TIME
```

**Returns:** All full-time employees.

Available types:
- `FULL_TIME`
- `PART_TIME`
- `CONTRACT`
- `INTERN`

---

### **7. Get Staff Statistics**

```
GET /api/v1/staff/profiles/statistics
```

**Response:**
```json
{
  "totalStaff": 15,
  "activeStaff": 12,
  "onLeave": 2,
  "suspended": 0,
  "terminated": 1,
  "fullTimeStaff": 10,
  "partTimeStaff": 3,
  "contractStaff": 1,
  "internStaff": 1
}
```

---

### **8. Update Staff Profile**

```json
PUT /api/v1/staff/profiles/{id}
{
  "position": "Senior Sales Associate",
  "salary": 180000,
  "department": "Sales",
  "phoneNumber": "+234-803-111-2222"
}
```

Only provided fields will be updated (partial update).

---

### **9. Update Staff Status**

Put staff on leave:
```json
PUT /api/v1/staff/profiles/{id}
{
  "status": "ON_LEAVE"
}
```

Terminate employment:
```json
PUT /api/v1/staff/profiles/{id}
{
  "status": "TERMINATED"
}
```

---

### **10. Get Staff by Employee ID**

```
GET /api/v1/staff/profiles/employee/EMP001
```

**Use case:** Quick lookup by employee ID (e.g., clock-in systems).

---

### **11. Get Staff by User ID**

```
GET /api/v1/staff/profiles/user/{user-id}
```

**Use case:** Link staff profile to user account.

---

## 🎯 **Complete Workflow Example:**

### **Scenario: Onboarding New Employee**

**Step 1: Create User Account**
```json
POST /api/v1/admin/users
{
  "username": "alice.brown",
  "email": "alice.brown@emjay.com",
  "password": "Welcome123!",
  "fullName": "Alice Brown",
  "role": "STAFF"
}
```

**Step 2: Create Staff Profile**
```json
POST /api/v1/staff/profiles
{
  "userId": "<user-id>",
  "employeeId": "EMP004",
  "firstName": "Alice",
  "lastName": "Brown",
  "dateOfBirth": "1995-03-20",
  "phoneNumber": "+234-803-555-1234",
  "position": "Customer Service Representative",
  "department": "Customer Service",
  "employmentType": "FULL_TIME",
  "hireDate": "2026-02-03",
  "salary": 120000,
  "bankName": "Access Bank",
  "accountNumber": "0987654321",
  "accountName": "Alice Brown"
}
```

**Step 3: Verify Creation**
```
GET /api/v1/staff/profiles/employee/EMP004
```

**Step 4: View Department Team**
```
GET /api/v1/staff/profiles/department/Customer%20Service
```

---

## 🔍 **Validation Tests:**

### **Test 1: Duplicate Employee ID**
```json
POST /api/v1/staff/profiles
{
  "employeeId": "EMP001",  // Already exists
  "..."
}
```

**Expected:** 409 Conflict - "Employee ID 'EMP001' already exists"

---

### **Test 2: User Already Has Profile**
```json
POST /api/v1/staff/profiles
{
  "userId": "<existing-staff-user-id>",
  "..."
}
```

**Expected:** 409 Conflict - "Staff profile already exists for this user"

---

### **Test 3: Invalid User ID**
```json
POST /api/v1/staff/profiles
{
  "userId": "00000000-0000-0000-0000-000000000000",
  "..."
}
```

**Expected:** 404 Not Found - "User not found"

---

### **Test 4: Invalid Email Format**
```json
POST /api/v1/staff/profiles
{
  "personalEmail": "invalid-email",
  "..."
}
```

**Expected:** 400 Bad Request - "Invalid email format"

---

## 💡 **Use Cases:**

### **HR Dashboard:**
```
GET /api/v1/staff/profiles/statistics
```
Shows overview of workforce composition.

### **Department Head View:**
```
GET /api/v1/staff/profiles/department/Sales
```
View all team members.

### **Payroll Processing:**
```
GET /api/v1/staff/profiles/status/ACTIVE
```
Get all active staff for salary processing.

### **Contract Tracking:**
```
GET /api/v1/staff/profiles/employment-type/CONTRACT
```
Monitor contract employees.

---

## 📊 **Data Points Tracked:**

✅ Personal Information (name, DOB, contact)  
✅ Employment Details (position, department, type)  
✅ Compensation (salary, bank details)  
✅ Emergency Contacts  
✅ Employment History (hire date, years of service)  
✅ Status Tracking (active, leave, terminated)  

---

## 🎉 **Phase 2 Complete!**

You now have a complete Staff Management System with:
- ✅ Staff profile creation
- ✅ Department organization
- ✅ Employment type tracking
- ✅ Status management
- ✅ Statistics dashboard
- ✅ Complete CRUD operations

**Next:** Build Shift Scheduling, Clock-In/Out, or Leave Management! 🚀
