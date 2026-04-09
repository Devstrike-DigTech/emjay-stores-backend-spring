# Shift Scheduling System - Testing Guide

## 📋 **Shift Scheduling Endpoints Overview**

### **Shift Templates (9 endpoints)**
```
POST   /api/v1/shifts/templates                  - Create template
GET    /api/v1/shifts/templates                  - List all templates
GET    /api/v1/shifts/templates/active           - List active templates
GET    /api/v1/shifts/templates/{id}             - Get template by ID
GET    /api/v1/shifts/templates/type/{type}      - Get templates by type
PUT    /api/v1/shifts/templates/{id}             - Update template
PATCH  /api/v1/shifts/templates/{id}/deactivate  - Deactivate template
PATCH  /api/v1/shifts/templates/{id}/activate    - Activate template
DELETE /api/v1/shifts/templates/{id}             - Delete template
```

### **Staff Shifts (12 endpoints)**
```
POST   /api/v1/shifts/staff-shifts               - Create shift
POST   /api/v1/shifts/staff-shifts/bulk          - Create multiple shifts
GET    /api/v1/shifts/staff-shifts               - List all shifts
GET    /api/v1/shifts/staff-shifts/{id}          - Get shift by ID
GET    /api/v1/shifts/staff-shifts/staff/{id}    - Get shifts for staff
GET    /api/v1/shifts/staff-shifts/staff/{id}/upcoming - Upcoming shifts
GET    /api/v1/shifts/staff-shifts/date/{date}   - Shifts by date
GET    /api/v1/shifts/staff-shifts/date-range    - Shifts by date range
POST   /api/v1/shifts/staff-shifts/weekly-roster - Get weekly roster
PUT    /api/v1/shifts/staff-shifts/{id}          - Update shift
PATCH  /api/v1/shifts/staff-shifts/{id}/cancel   - Cancel shift
DELETE /api/v1/shifts/staff-shifts/{id}          - Delete shift
```

### **Shift Swap Requests (9 endpoints)**
```
POST   /api/v1/shifts/swap-requests              - Create swap request
GET    /api/v1/shifts/swap-requests              - List all requests
GET    /api/v1/shifts/swap-requests/pending      - List pending requests
GET    /api/v1/shifts/swap-requests/{id}         - Get request by ID
GET    /api/v1/shifts/swap-requests/status/{status} - Requests by status
PATCH  /api/v1/shifts/swap-requests/{id}/approve - Approve request
PATCH  /api/v1/shifts/swap-requests/{id}/reject  - Reject request
PATCH  /api/v1/shifts/swap-requests/{id}/cancel  - Cancel request
DELETE /api/v1/shifts/swap-requests/{id}         - Delete request
```

**Total: 30 endpoints**

---

## 🧪 **Testing Workflow**

### **Phase 1: Setup Shift Templates**

#### **Test 1: Create Morning Shift Template**
```json
POST /api/v1/shifts/templates
{
  "name": "Morning Shift",
  "shiftType": "MORNING",
  "startTime": "08:00:00",
  "endTime": "16:00:00",
  "description": "Standard morning shift",
  "colorCode": "#3B82F6"
}
```

**Expected Response (201):**
```json
{
  "id": "uuid",
  "name": "Morning Shift",
  "shiftType": "MORNING",
  "startTime": "08:00:00",
  "endTime": "16:00:00",
  "description": "Standard morning shift",
  "colorCode": "#3B82F6",
  "isActive": true,
  "durationHours": 8.0,
  "isOvernight": false,
  "createdAt": "...",
  "updatedAt": "..."
}
```

#### **Test 2: Create More Shift Templates**

**Afternoon Shift:**
```json
POST /api/v1/shifts/templates
{
  "name": "Afternoon Shift",
  "shiftType": "AFTERNOON",
  "startTime": "14:00:00",
  "endTime": "22:00:00",
  "colorCode": "#10B981"
}
```

**Night Shift:**
```json
POST /api/v1/shifts/templates
{
  "name": "Night Shift",
  "shiftType": "NIGHT",
  "startTime": "22:00:00",
  "endTime": "06:00:00",
  "description": "Overnight shift",
  "colorCode": "#6366F1"
}
```

**Custom Part-Time:**
```json
POST /api/v1/shifts/templates
{
  "name": "Part-Time Shift",
  "shiftType": "CUSTOM",
  "startTime": "09:00:00",
  "endTime": "14:00:00",
  "colorCode": "#F59E0B"
}
```

#### **Test 3: View All Templates**
```
GET /api/v1/shifts/templates
```

#### **Test 4: View Templates by Type**
```
GET /api/v1/shifts/templates/type/MORNING
```

---

### **Phase 2: Create Staff Shifts**

#### **Test 5: Assign Single Shift**

First, get a staff profile ID (from staff profiles endpoint).

```json
POST /api/v1/shifts/staff-shifts
{
  "staffProfileId": "<staff-uuid>",
  "shiftTemplateId": "<morning-template-uuid>",
  "shiftDate": "2026-02-10",
  "startTime": "08:00:00",
  "endTime": "16:00:00",
  "breakDurationMinutes": 30,
  "notes": "Regular Monday shift"
}
```

**Expected Response (201):**
```json
{
  "id": "uuid",
  "staffProfileId": "...",
  "staffName": "John Doe",
  "shiftTemplateId": "...",
  "shiftTemplateName": "Morning Shift",
  "shiftDate": "2026-02-10",
  "startTime": "08:00:00",
  "endTime": "16:00:00",
  "breakDurationMinutes": 30,
  "totalDurationMinutes": 480,
  "workDurationMinutes": 450,
  "workDurationHours": 7.5,
  "status": "SCHEDULED",
  "notes": "Regular Monday shift",
  "assignedBy": "...",
  "isOvernight": false,
  "createdAt": "...",
  "updatedAt": "..."
}
```

#### **Test 6: Bulk Assign Shifts (Create Weekly Schedule)**

```json
POST /api/v1/shifts/staff-shifts/bulk
{
  "staffProfileIds": [
    "<staff-uuid-1>",
    "<staff-uuid-2>",
    "<staff-uuid-3>"
  ],
  "shiftTemplateId": "<morning-template-uuid>",
  "shiftDates": [
    "2026-02-10",
    "2026-02-11",
    "2026-02-12",
    "2026-02-13",
    "2026-02-14"
  ],
  "startTime": "08:00:00",
  "endTime": "16:00:00",
  "breakDurationMinutes": 30
}
```

**Result:** Creates 15 shifts (3 staff × 5 days)

---

### **Phase 3: View and Manage Schedules**

#### **Test 7: View Today's Schedule**
```
GET /api/v1/shifts/staff-shifts/date/2026-02-10
```

#### **Test 8: View Staff Member's Shifts**
```
GET /api/v1/shifts/staff-shifts/staff/{staff-uuid}
```

#### **Test 9: View Upcoming Shifts**
```
GET /api/v1/shifts/staff-shifts/staff/{staff-uuid}/upcoming
```

#### **Test 10: Get Weekly Roster**
```json
POST /api/v1/shifts/staff-shifts/weekly-roster
{
  "startDate": "2026-02-10",
  "staffProfileIds": [
    "<staff-uuid-1>",
    "<staff-uuid-2>"
  ]
}
```

**Response includes:**
- All shifts for the week
- Staff summaries with total hours
- Shifts grouped by day

---

### **Phase 4: Shift Modifications**

#### **Test 11: Update Shift**
```json
PUT /api/v1/shifts/staff-shifts/{shift-uuid}
{
  "startTime": "09:00:00",
  "endTime": "17:00:00",
  "notes": "Start time changed due to request"
}
```

#### **Test 12: Cancel Shift**
```
PATCH /api/v1/shifts/staff-shifts/{shift-uuid}/cancel
```

**Status changes:** SCHEDULED → CANCELLED

---

### **Phase 5: Shift Swap Requests**

#### **Test 13: Create Swap Request**

Scenario: Staff member wants to swap their Monday shift.

```json
POST /api/v1/shifts/swap-requests
{
  "requesterShiftId": "<monday-shift-uuid>",
  "targetShiftId": "<other-monday-shift-uuid>",
  "reason": "Doctor's appointment on Monday morning"
}
```

**Response:**
```json
{
  "id": "uuid",
  "requesterShiftId": "...",
  "requesterShiftDetails": { /* full shift details */ },
  "targetShiftId": "...",
  "targetShiftDetails": { /* full shift details */ },
  "reason": "Doctor's appointment on Monday morning",
  "status": "PENDING",
  "createdAt": "...",
  "updatedAt": "..."
}
```

#### **Test 14: View Pending Swap Requests**
```
GET /api/v1/shifts/swap-requests/pending
```

#### **Test 15: Approve Swap Request**
```json
PATCH /api/v1/shifts/swap-requests/{swap-uuid}/approve
{
  "approvedBy": "<manager-uuid>"
}
```

**What happens:**
- The two shifts swap staff assignments
- Status changes to APPROVED

#### **Test 16: Reject Swap Request**
```json
PATCH /api/v1/shifts/swap-requests/{swap-uuid}/reject
{
  "approvedBy": "<manager-uuid>",
  "rejectionReason": "Insufficient coverage for that day"
}
```

#### **Test 17: Cancel Own Swap Request**
```
PATCH /api/v1/shifts/swap-requests/{swap-uuid}/cancel
```

---

## 💡 **Advanced Scenarios**

### **Scenario 1: Create Monthly Schedule**

**Step 1:** Create shift templates for different times

**Step 2:** Bulk create shifts for entire month
```json
POST /api/v1/shifts/staff-shifts/bulk
{
  "staffProfileIds": ["staff1", "staff2", "staff3"],
  "shiftDates": ["2026-02-10", "2026-02-11", ...all dates],
  "startTime": "08:00:00",
  "endTime": "16:00:00"
}
```

**Step 3:** View date range
```
GET /api/v1/shifts/staff-shifts/date-range?startDate=2026-02-01&endDate=2026-02-28
```

---

### **Scenario 2: Handle Shift Coverage**

**Check today's coverage:**
```
GET /api/v1/shifts/staff-shifts/date/2026-02-10
```

**If understaffed, quickly assign:**
```json
POST /api/v1/shifts/staff-shifts
{
  "staffProfileId": "<available-staff-uuid>",
  "shiftDate": "2026-02-10",
  "startTime": "08:00:00",
  "endTime": "16:00:00"
}
```

---

### **Scenario 3: Overnight Shifts**

```json
POST /api/v1/shifts/staff-shifts
{
  "staffProfileId": "<staff-uuid>",
  "shiftDate": "2026-02-10",
  "startTime": "22:00:00",
  "endTime": "06:00:00",
  "breakDurationMinutes": 30,
  "notes": "Security night shift"
}
```

System recognizes `isOvernight: true` when endTime < startTime.

---

## 📊 **Validation Tests**

### **Test: Duplicate Shift Template Name**
```json
POST /api/v1/shifts/templates
{
  "name": "Morning Shift",  // Already exists
  "shiftType": "MORNING",
  "startTime": "08:00:00",
  "endTime": "16:00:00"
}
```
**Expected:** 409 Conflict

### **Test: Invalid Time Range**
```json
POST /api/v1/shifts/staff-shifts
{
  "startTime": "16:00:00",
  "endTime": "08:00:00"  // Without overnight context
}
```
**System allows this for overnight shifts**

### **Test: Swap Already Swapped Shift**
```json
POST /api/v1/shifts/swap-requests
{
  "requesterShiftId": "<shift-with-pending-swap>"
}
```
**Expected:** 400 Bad Request - "A pending swap request already exists"

---

## 🎯 **Use Cases by Role**

### **Manager Use Cases:**

1. **Create Weekly Schedule:**
   - Create shift templates
   - Bulk assign shifts to team
   - View weekly roster

2. **Handle Staff Requests:**
   - View pending swap requests
   - Approve/reject swaps
   - Adjust shifts as needed

3. **Monitor Coverage:**
   - View daily schedules
   - Check upcoming shifts
   - Fill gaps quickly

### **Staff Use Cases:**

1. **View Own Schedule:**
   - Get upcoming shifts
   - Check weekly assignments

2. **Request Shift Changes:**
   - Create swap request
   - Cancel own request
   - View request status

---

## 📈 **Data Insights**

### **Weekly Roster Response Example:**
```json
{
  "weekStartDate": "2026-02-10",
  "weekEndDate": "2026-02-16",
  "shifts": [ /* all shifts */ ],
  "staffSummaries": [
    {
      "staffProfileId": "...",
      "staffName": "John Doe",
      "totalShifts": 5,
      "totalHours": 37.5,
      "shiftsByDay": {
        "MONDAY": 1,
        "TUESDAY": 1,
        "WEDNESDAY": 1,
        "THURSDAY": 1,
        "FRIDAY": 1
      }
    }
  ]
}
```

---

## 🎉 **Shift Scheduling Complete!**

You now have a complete shift scheduling system with:
- ✅ Reusable shift templates
- ✅ Staff shift assignments
- ✅ Bulk scheduling
- ✅ Weekly roster view
- ✅ Shift swapping
- ✅ Approval workflow
- ✅ Overnight shift support
- ✅ Coverage tracking

**Next:** Build Clock-In/Out attendance tracking! 🚀
