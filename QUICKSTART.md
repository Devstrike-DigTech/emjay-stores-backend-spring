# Quick Start Guide - Emjay Backend

## 🚀 Get Up and Running in 5 Minutes

### Step 1: Fix Database Permissions

The error you're seeing is because PostgreSQL 15+ requires explicit permissions on the `public` schema.

**Option A: Use the setup script (Recommended)**
```bash
cd emjay-backend
chmod +x setup-db.sh
./setup-db.sh
```

**Option B: Manual SQL commands**
```bash
psql -U postgres -d emjay_dev_db -c "
  GRANT ALL ON SCHEMA public TO emjay_dev;
  ALTER SCHEMA public OWNER TO emjay_dev;
"
```

**Option C: Recreate database properly**
```bash
psql -U postgres <<EOF
DROP DATABASE IF EXISTS emjay_dev_db;
CREATE DATABASE emjay_dev_db OWNER emjay_dev;
\c emjay_dev_db
GRANT ALL ON SCHEMA public TO emjay_dev;
ALTER SCHEMA public OWNER TO emjay_dev;
EOF
```

### Step 2: Verify Database Setup

```bash
# Test connection
psql -U emjay_dev -d emjay_dev_db -c "SELECT current_database(), current_user;"

# Should output:
#  current_database | current_user 
# ------------------+--------------
#  emjay_dev_db     | emjay_dev
```

### Step 3: Create Flyway Migration Directory

```bash
cd emjay-backend
mkdir -p src/main/resources/db/migration
```

### Step 4: Start the Application

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

You should see:
```
Started EmjayBackendApplication in X.XXX seconds
```

### Step 5: Access Swagger UI

Open your browser: http://localhost:8080/swagger-ui.html

---

## 📋 What Happened?

### The Error Explained

```
ERROR: permission denied for schema public
```

**Why?** PostgreSQL 15 changed the default permissions for the `public` schema for security reasons. Previously, all users had CREATE privilege by default. Now it must be explicitly granted.

**The Fix:** Grant the user ownership or explicit permissions on the schema.

---

## ✅ Checklist

- [ ] PostgreSQL is running (`pg_isready`)
- [ ] Database `emjay_dev_db` exists
- [ ] User `emjay_dev` exists with password `emjay_dev`
- [ ] User has permissions on schema `public`
- [ ] Flyway migration directory exists
- [ ] Application starts without errors

---

## 🎯 Next Steps After Successful Start

1. **Create first migration** - Add database tables
2. **Test the application** - Run tests
3. **Continue development** - Build Infrastructure layer

---

## 🆘 Still Having Issues?

### Check PostgreSQL Version
```bash
psql -U postgres -c "SELECT version();"
```

### Check Existing Permissions
```bash
psql -U postgres -d emjay_dev_db -c "\dp"
```

### View Schema Owner
```bash
psql -U postgres -d emjay_dev_db -c "\dn+"
```

### Enable Debug Logging
Add to `application-dev.yml`:
```yaml
logging:
  level:
    org.flywaydb: DEBUG
```

---

## 📞 Common Solutions

| Issue | Solution |
|-------|----------|
| User doesn't exist | `CREATE USER emjay_dev WITH PASSWORD 'emjay_dev';` |
| Database doesn't exist | `CREATE DATABASE emjay_dev_db OWNER emjay_dev;` |
| Permission denied | `ALTER SCHEMA public OWNER TO emjay_dev;` |
| Connection refused | Check PostgreSQL is running: `sudo service postgresql start` |
| Port 5432 in use | Change port in connection string or stop conflicting service |

---

Good luck! 🚀
