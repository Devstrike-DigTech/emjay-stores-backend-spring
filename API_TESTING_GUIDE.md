# API Testing Guide - Emjay Backend

## 🚀 Getting Started

### 1. Start the Application
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 2. Access Swagger UI
Open your browser: **http://localhost:8080/swagger-ui.html**

---

## 👤 Default Admin Account

Use this account for testing:

- **Email**: `admin@emjay.com`
- **Username**: `admin`
- **Password**: `Admin@123`
- **Role**: ADMIN

---

## 📋 Test Scenarios

### Scenario 1: User Registration

**Endpoint**: `POST /api/v1/auth/register`

**Request Body**:
```json
{
  "email": "john.doe@example.com",
  "username": "johndoe",
  "password": "Password@123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+234-800-555-1234"
}
```

**Expected Response** (201 Created):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900000,
  "user": {
    "id": "uuid-here",
    "email": "john.doe@example.com",
    "username": "johndoe",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+234-800-555-1234",
    "role": "STAFF",
    "isActive": true,
    "isVerified": false
  }
}
```

**Copy the `accessToken` from the response!**

---

### Scenario 2: User Login

**Endpoint**: `POST /api/v1/auth/login`

**Request Body** (with email):
```json
{
  "emailOrUsername": "admin@emjay.com",
  "password": "Admin@123"
}
```

**Or with username**:
```json
{
  "emailOrUsername": "admin",
  "password": "Admin@123"
}
```

**Expected Response** (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900000,
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "admin@emjay.com",
    "username": "admin",
    "firstName": "System",
    "lastName": "Administrator",
    "phone": null,
    "role": "ADMIN",
    "isActive": true,
    "isVerified": true
  }
}
```

---

### Scenario 3: Authorize in Swagger

After logging in:

1. Click the **"Authorize"** button (green lock icon) in Swagger UI
2. Enter: `Bearer <your-access-token>`
   - Example: `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
3. Click **"Authorize"**
4. Click **"Close"**

Now you can access protected endpoints! 🎉

---

### Scenario 4: Get Current User

**Endpoint**: `GET /api/v1/auth/me`

**Headers**: 
- `Authorization: Bearer <your-access-token>`

**Expected Response** (200 OK):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "admin@emjay.com",
  "username": "admin",
  "firstName": "System",
  "lastName": "Administrator",
  "phone": null,
  "role": "ADMIN",
  "isActive": true,
  "isVerified": true
}
```

---

### Scenario 5: Change Password

**Endpoint**: `POST /api/v1/auth/change-password`

**Headers**: 
- `Authorization: Bearer <your-access-token>`

**Request Body**:
```json
{
  "currentPassword": "Admin@123",
  "newPassword": "NewPassword@456"
}
```

**Expected Response** (200 OK):
```json
{
  "message": "Password changed successfully"
}
```

**Note**: All refresh tokens will be revoked after password change.

---

### Scenario 6: Refresh Access Token

**Endpoint**: `POST /api/v1/auth/refresh`

**Request Body**:
```json
{
  "refreshToken": "your-refresh-token-here"
}
```

**Expected Response** (200 OK):
```json
{
  "accessToken": "new-access-token",
  "refreshToken": "new-refresh-token",
  "tokenType": "Bearer",
  "expiresIn": 900000,
  "user": { ... }
}
```

---

### Scenario 7: Logout

**Endpoint**: `POST /api/v1/auth/logout`

**Headers**: 
- `Authorization: Bearer <your-access-token>`

**Expected Response** (200 OK):
```json
{
  "message": "Logged out successfully"
}
```

**Note**: This revokes all refresh tokens for the user.

---

## ❌ Error Scenarios

### Invalid Credentials
**Request**:
```json
{
  "emailOrUsername": "admin@emjay.com",
  "password": "WrongPassword"
}
```

**Response** (401 Unauthorized):
```json
{
  "timestamp": "2026-01-29T21:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email/username or password",
  "path": "/api/v1/auth/login"
}
```

### Email Already Exists
**Response** (409 Conflict):
```json
{
  "timestamp": "2026-01-29T21:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Email already registered",
  "path": "/api/v1/auth/register"
}
```

### Validation Errors
**Request** (weak password):
```json
{
  "email": "test@test.com",
  "username": "test",
  "password": "weak",
  "firstName": "Test",
  "lastName": "User"
}
```

**Response** (400 Bad Request):
```json
{
  "timestamp": "2026-01-29T21:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/auth/register",
  "validationErrors": {
    "password": "Password must be at least 8 characters"
  }
}
```

---

## 🔐 JWT Token Details

### Access Token
- **Lifetime**: 15 minutes (900,000 ms)
- **Purpose**: API authentication
- **Claims**: userId, email, role, type=ACCESS

### Refresh Token
- **Lifetime**: 7 days (604,800,000 ms)
- **Purpose**: Get new access tokens
- **Claims**: userId, type=REFRESH

---

## 📊 Database Verification

### Check Registered Users
```sql
SELECT id, email, username, first_name, last_name, role, is_active 
FROM users;
```

### Check Refresh Tokens
```sql
SELECT id, user_id, expires_at, is_revoked, created_at 
FROM refresh_tokens 
WHERE user_id = '<user-uuid>';
```

### Verify Password Hash
```sql
SELECT username, password_hash 
FROM users 
WHERE username = 'admin';
```

---

## 🧪 cURL Examples

### Register
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "username": "testuser",
    "password": "Test@123",
    "firstName": "Test",
    "lastName": "User"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "emailOrUsername": "admin@emjay.com",
    "password": "Admin@123"
  }'
```

### Get Current User (with token)
```bash
curl -X GET http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```

---

## ✅ Checklist

- [ ] Application started successfully
- [ ] Swagger UI accessible at http://localhost:8080/swagger-ui.html
- [ ] Can login with admin account
- [ ] Can register new user
- [ ] Can access protected endpoints with JWT token
- [ ] Can refresh tokens
- [ ] Can change password
- [ ] Can logout
- [ ] Error responses are properly formatted

---

## 🎯 Next Steps

Once authentication is working:
1. Test all endpoints thoroughly
2. Create additional test users with different roles
3. Verify role-based access control
4. Proceed to build inventory management endpoints

Happy Testing! 🚀
