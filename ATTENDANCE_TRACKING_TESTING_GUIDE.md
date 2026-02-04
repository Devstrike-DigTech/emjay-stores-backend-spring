# Clock-In/Out & Attendance Tracking - Testing Guide

## 📋 **Attendance Endpoints Overview**

### **Clock-In/Out (2 endpoints)**
```
POST   /api/v1/attendance/clock-in                    - Clock in
PATCH  /api/v1/attendance/{id}/clock-out               - Clock out
```

### **Break Management (2 endpoints)**
```
POST   /api/v1/attendance/{id}/start-break            - Start break
PATCH  /api/v1/attendance/breaks/{id}/end             - End break
```

### **Attendance Records (5 endpoints)**
```
GET    /api/v1/attendance/{id}                        - Get by ID
GET    /api/v1/attendance/staff/{id}/active           - Get active attendance
GET    /api/v1/attendance/staff/{id}                  - Get staff records
GET    /api/v1/attendance/date/{date}                 - Get by date
GET    /api/v1/attendance/date-range                  - Get by date range
```

### **Reports (3 endpoints)**
```
POST   /api/v1/attendance/reports/daily               - Daily report
POST   /api/v1/attendance/reports/staff               - Staff report
POST   /api/v1/attendance/reports/timesheet           - Timesheet
```

**Total: 12 endpoints**

---

## 🧪 **Testing Workflow**

### **Scenario 1: Basic Clock-In/Out**

#### **Test 1: Clock In (Without Shift)**
```json
POST /api/v1/attendance/clock-in
{
  "staffProfileId": "<staff-uuid>",
  "location": "Main Office",
  "latitude": 9.0579,
  "longitude": 7.4951,
  "notes": "Arrived on time"
}
```

**Expected Response (201):**
```json
{
  "id": "uuid",
  "staffProfileId": "...",
  "staffName": "John Doe",
  "staffShiftId": null,
  "clockInTime": "2026-02-10T08:15:00",
  "clockInLocation": "Main Office",
  "clockInLatitude": 9.0579,
  "clockInLongitude": 7.4951,
  "clockInNotes": "Arrived on time",
  "clockOutTime": null,
  "totalBreakMinutes": 0,
  "scheduledStartTime": null,
  "scheduledEndTime": null,
  "actualWorkMinutes": null,
  "actualWorkHours": null,
  "isLate": false,
  "lateMinutes": 0,
  "isEarlyDeparture": false,
  "earlyDepartureMinutes": 0,
  "status": "PRESENT",
  "isActive": true,
  "isClockedOut": false,
  "createdAt": "...",
  "updatedAt": "..."
}
```

#### **Test 2: Clock In (With Scheduled Shift)**

First, create a shift for today:
```json
POST /api/v1/shifts/staff-shifts
{
  "staffProfileId": "<staff-uuid>",
  "shiftDate": "2026-02-10",
  "startTime": "08:00:00",
  "endTime": "16:00:00",
  "breakDurationMinutes": 30
}
```

Then clock in after shift start (testing late arrival):
```json
POST /api/v1/attendance/clock-in
{
  "staffProfileId": "<staff-uuid>",
  "shiftId": "<shift-uuid>",
  "location": "Main Office"
}
```

**If clocked in at 08:15 AM when shift starts at 08:00 AM:**
```json
{
  "isLate": true,
  "lateMinutes": 15,
  "status": "LATE",
  "scheduledStartTime": "2026-02-10T08:00:00",
  "scheduledEndTime": "2026-02-10T16:00:00"
}
```

#### **Test 3: Clock Out**
```json
PATCH /api/v1/attendance/{attendance-uuid}/clock-out
{
  "location": "Main Office",
  "notes": "Completed all tasks"
}
```

**Expected Response:**
```json
{
  "clockOutTime": "2026-02-10T16:30:00",
  "actualWorkMinutes": 495,
  "actualWorkHours": 8.25,
  "totalBreakMinutes": 0,
  "isActive": false,
  "isClockedOut": true
}
```

---

### **Scenario 2: Clock-In/Out with Breaks**

#### **Test 4: Clock In**
```json
POST /api/v1/attendance/clock-in
{
  "staffProfileId": "<staff-uuid>",
  "location": "Main Office"
}
```

#### **Test 5: Start Break (Lunch)**
```json
POST /api/v1/attendance/{attendance-uuid}/start-break
{
  "breakType": "LUNCH",
  "notes": "Lunch break"
}
```

**Response:**
```json
{
  "id": "break-uuid",
  "attendanceRecordId": "attendance-uuid",
  "breakStartTime": "2026-02-10T12:00:00",
  "breakEndTime": null,
  "breakDurationMinutes": null,
  "breakType": "LUNCH",
  "isActive": true
}
```

**Attendance status changes to:** `ON_BREAK`

#### **Test 6: End Break**
```json
PATCH /api/v1/attendance/breaks/{break-uuid}/end
{
  "notes": "Back from lunch"
}
```

**Response:**
```json
{
  "breakEndTime": "2026-02-10T12:30:00",
  "breakDurationMinutes": 30,
  "isActive": false
}
```

**Attendance status changes back to:** `PRESENT` (or `LATE` if was late)

#### **Test 7: Clock Out**
```json
PATCH /api/v1/attendance/{attendance-uuid}/clock-out
{
  "location": "Main Office"
}
```

**Expected:**
```json
{
  "totalBreakMinutes": 30,
  "actualWorkMinutes": 450,  // Total time minus break
  "actualWorkHours": 7.5
}
```

---

### **Scenario 3: Late Arrival Detection**

#### **Test 8: Schedule Morning Shift**
```json
POST /api/v1/shifts/staff-shifts
{
  "staffProfileId": "<staff-uuid>",
  "shiftDate": "2026-02-10",
  "startTime": "08:00:00",
  "endTime": "16:00:00"
}
```

#### **Test 9: Clock In Late (8:25 AM)**
```json
POST /api/v1/attendance/clock-in
{
  "staffProfileId": "<staff-uuid>",
  "shiftId": "<shift-uuid>"
}
```

**System automatically calculates:**
```json
{
  "isLate": true,
  "lateMinutes": 25,
  "status": "LATE",
  "scheduledStartTime": "2026-02-10T08:00:00"
}
```

---

### **Scenario 4: Early Departure Detection**

#### **Test 10: Clock Out Early (3:30 PM instead of 4:00 PM)**
```json
PATCH /api/v1/attendance/{attendance-uuid}/clock-out
{}
```

**If scheduled end is 4:00 PM but clocked out at 3:30 PM:**
```json
{
  "isEarlyDeparture": true,
  "earlyDepartureMinutes": 30,
  "status": "EARLY_DEPARTURE",
  "scheduledEndTime": "2026-02-10T16:00:00",
  "clockOutTime": "2026-02-10T15:30:00"
}
```

---

### **Scenario 5: View Attendance Records**

#### **Test 11: Get Active Attendance**
```
GET /api/v1/attendance/staff/{staff-uuid}/active
```

Returns current active clock-in (if any).

#### **Test 12: Get Staff Attendance History**
```
GET /api/v1/attendance/staff/{staff-uuid}?page=0&size=20
```

Returns paginated attendance records for the staff member.

#### **Test 13: Get Today's Attendance**
```
GET /api/v1/attendance/date/2026-02-10
```

**Response shows all staff who clocked in today:**
```json
{
  "content": [
    {
      "staffName": "John Doe",
      "clockInTime": "2026-02-10T08:00:00",
      "status": "PRESENT",
      "isLate": false
    },
    {
      "staffName": "Jane Smith",
      "clockInTime": "2026-02-10T08:15:00",
      "status": "LATE",
      "isLate": true,
      "lateMinutes": 15
    }
  ],
  "totalElements": 2
}
```

#### **Test 14: Get Attendance for Date Range**
```
GET /api/v1/attendance/date-range?startDate=2026-02-01&endDate=2026-02-10
```

---

### **Scenario 6: Attendance Reports**

#### **Test 15: Daily Attendance Report**
```json
POST /api/v1/attendance/reports/daily
{
  "date": "2026-02-10"
}
```

**Response:**
```json
{
  "date": "2026-02-10",
  "totalStaff": 10,
  "present": 8,
  "late": 2,
  "absent": 0,
  "onBreak": 1,
  "earlyDepartures": 0,
  "attendanceRecords": [ /* full records */ ]
}
```

#### **Test 16: Staff Attendance Report (Weekly)**
```json
POST /api/v1/attendance/reports/staff
{
  "staffProfileId": "<staff-uuid>",
  "startDate": "2026-02-03",
  "endDate": "2026-02-09"
}
```

**Response:**
```json
{
  "staffProfileId": "...",
  "staffName": "John Doe",
  "startDate": "2026-02-03",
  "endDate": "2026-02-09",
  "totalDays": 7,
  "daysPresent": 5,
  "daysLate": 1,
  "daysAbsent": 2,
  "totalHoursWorked": 37.5,
  "averageHoursPerDay": 7.5,
  "attendanceRate": 71.43,  // 5 out of 7 days
  "punctualityRate": 80.0,   // 4 on-time out of 5 present
  "attendanceRecords": [ /* detailed records */ ]
}
```

#### **Test 17: Timesheet Report**
```json
POST /api/v1/attendance/reports/timesheet
{
  "staffProfileId": "<staff-uuid>",
  "startDate": "2026-02-01",
  "endDate": "2026-02-15"
}
```

**Response:**
```json
{
  "staffProfileId": "...",
  "staffName": "John Doe",
  "period": "2026-02-01 to 2026-02-15",
  "entries": [
    {
      "date": "2026-02-03",
      "clockIn": "2026-02-03T08:00:00",
      "clockOut": "2026-02-03T16:30:00",
      "breakMinutes": 30,
      "hoursWorked": 8.0,
      "status": "PRESENT",
      "isLate": false
    },
    {
      "date": "2026-02-04",
      "clockIn": "2026-02-04T08:15:00",
      "clockOut": "2026-02-04T16:00:00",
      "breakMinutes": 30,
      "hoursWorked": 7.25,
      "status": "LATE",
      "isLate": true
    }
  ],
  "totalHours": 75.5,
  "totalRegularHours": 75.5,
  "totalOvertimeHours": 0.0
}
```

---

## 💡 **Advanced Use Cases**

### **Use Case 1: Multiple Breaks in One Day**

```bash
# Clock in
POST /api/v1/attendance/clock-in

# Morning break
POST /api/v1/attendance/{id}/start-break {"breakType": "REGULAR"}
PATCH /api/v1/attendance/breaks/{id}/end

# Lunch break
POST /api/v1/attendance/{id}/start-break {"breakType": "LUNCH"}
PATCH /api/v1/attendance/breaks/{id}/end

# Afternoon break
POST /api/v1/attendance/{id}/start-break {"breakType": "REGULAR"}
PATCH /api/v1/attendance/breaks/{id}/end

# Clock out
PATCH /api/v1/attendance/{id}/clock-out
```

**Result:** `totalBreakMinutes` automatically sums all breaks.

---

### **Use Case 2: GPS Location Tracking**

```json
POST /api/v1/attendance/clock-in
{
  "staffProfileId": "<staff-uuid>",
  "location": "Branch Office - Ikeja",
  "latitude": 6.5244,
  "longitude": 3.3792,
  "notes": "Working from Lagos branch today"
}
```

**Clock out with different location:**
```json
PATCH /api/v1/attendance/{id}/clock-out
{
  "location": "Branch Office - Victoria Island",
  "latitude": 6.4281,
  "longitude": 3.4219,
  "notes": "Ended shift at VI branch"
}
```

---

### **Use Case 3: Forgot to Clock Out**

**Manager can view and correct:**
```
GET /api/v1/attendance/staff/{staff-uuid}/active
```

**If staff forgot, manually clock out:**
```json
PATCH /api/v1/attendance/{id}/clock-out
{
  "notes": "Manually clocked out by manager - staff forgot"
}
```

---

## 🎯 **Validation Tests**

### **Test: Cannot Clock In Twice**
```json
POST /api/v1/attendance/clock-in
{
  "staffProfileId": "<staff-uuid>"
}
```

**If already clocked in:**
```
400 Bad Request: "Staff is already clocked in. Please clock out first."
```

### **Test: Cannot Start Break While Not Clocked In**
```json
POST /api/v1/attendance/{id}/start-break
```

**If not clocked in:**
```
400 Bad Request: "Cannot start break. Not clocked in."
```

### **Test: Cannot Start Break While Already on Break**
```json
POST /api/v1/attendance/{id}/start-break
```

**If already on break:**
```
400 Bad Request: "Already on break. Please end current break first."
```

---

## 📊 **Report Examples**

### **Daily Attendance Summary:**
- Who's present today
- Who's late
- Who's on break
- Who left early
- Who hasn't clocked in (absent)

### **Staff Performance Report:**
- Attendance rate (days present / total days)
- Punctuality rate (on-time arrivals / total days)
- Average hours worked per day
- Total hours in period

### **Timesheet for Payroll:**
- Detailed day-by-day breakdown
- Exact clock-in/out times
- Break time deductions
- Total billable hours

---

## 🎉 **Clock-In/Out System Complete!**

You now have:
- ✅ Clock-in/out tracking
- ✅ GPS location capture
- ✅ Break management
- ✅ Late/early detection
- ✅ Work hours calculation
- ✅ Attendance reports
- ✅ Timesheet generation

**Next:** Build Leave Management! 🚀
