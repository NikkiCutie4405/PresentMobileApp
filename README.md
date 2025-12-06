# Attendance Management System - Documentation

## Overview
This Android application provides a comprehensive attendance tracking system with grade calculation features. The system allows students to view, track, and manage their attendance across multiple subjects with an integrated grading system.

---

## Table of Contents
1. [Features](#features)
2. [Architecture](#architecture)
3. [Components](#components)
4. [Grading System](#grading-system)
5. [Installation](#installation)
6. [Usage](#usage)
7. [Technical Details](#technical-details)

---

## Features

### Core Features
- **QR Code Scanning**: Mark attendance by scanning QR codes
- **Multi-Subject Tracking**: Monitor attendance across all enrolled subjects
- **Real-time Grade Calculation**: Automatic grade computation based on attendance status
- **Overview & Detail Views**: Toggle between all subjects overview and individual subject details
- **Filter System**: Filter attendance by semester and year
- **Status Tracking**: Three attendance statuses - Present, Late, and Absent
- **Color-Coded Display**: Visual indicators for quick status identification

### User Interface
- Modern, dark-themed UI with accent colors
- Responsive layouts with ScrollView support
- Tab navigation for Content, Attendance, and Grades
- Bottom navigation bar for main sections
- Progress bars showing attendance performance

---

## Architecture

### Application Structure
```
com.matibag.presentlast/
├── attendance.java          (Main attendance page)
├── AttendanceOverview.java  (Overview page for all subjects)
├── login.java               (Authentication)
├── signup.java              (User registration)
└── Other supporting classes
```

### Layout Files
```
res/layout/
├── attendance.xml           (Attendance detail view)
├── attendance_overview.xml  (All subjects overview)
├── login.xml               (Login screen)
└── signup.xml              (Registration screen)
```

---

## Components

### 1. Login System (`login.java`)
**Purpose**: User authentication

**Key Features**:
- Username and password input
- API-based authentication using Volley
- Progress dialog during login
- Error handling with Toast messages

**API Endpoint**: `https://adu-cs.com/mobile/login_api.php`

**Parameters Sent**:
- `username`: User's username
- `userpass`: User's password
- `token`: Authentication token ("pass")

**Code Example**:
```java
request = new StringRequest(Request.Method.POST, URL, 
    response -> {
        // Handle successful login
    },
    error -> {
        // Handle error
    }
);
```

---

### 2. Sign Up System (`signup.java`)
**Purpose**: New user registration

**Key Features**:
- Input validation for all fields
- Password matching verification
- First name, middle name, last name fields
- Username and password creation
- API-based registration

**Validation Rules**:
- All required fields must be filled
- Passwords must match
- Empty field check before submission

**API Endpoint**: `https://adu-cs.com/mobile/signup_api.php`

**Parameters Sent**:
- `firstname`: User's first name
- `middlename`: User's middle name (optional)
- `lastname`: User's last name
- `username`: Desired username
- `password`: User's password
- `token`: Registration token ("signup")

---

### 3. Attendance Page (`attendance.java`)
**Purpose**: Individual subject attendance tracking and management

#### Features

**Dual Mode Operation**:
1. **Single Subject Mode**: Shows detailed attendance records for one subject
2. **Overview Mode**: Displays all subjects with filtering options

**QR Code Integration**:
- Uses ZXing Barcode Scanner library
- Instant attendance marking via QR code
- Visual and audio feedback on successful scan

**Grade Calculation**:
```java
// Formula: (Present × 100% + Late × 50% + Absent × 0%) / Total Days
double attendanceGrade = ((totalPresent * 100.0) + (totalLate * 50.0)) / totalDays;
```

**Tab Navigation**:
- Content Tab: Links to subject content
- Attendance Tab: Current page (highlighted)
- Grades Tab: Links to grades page

**Filter System**:
- Semester filter: All Semesters, 1st Semester, 2nd Semester, Summer
- Year filter: All Years, 2023, 2024, 2025
- Real-time filtering when selection changes

#### Key Methods

**`setupOverviewMode()`**
- Configures the page to show all subjects
- Initializes filter dropdowns
- Hides single-subject elements
- Loads all subject attendance data

**`setupSingleSubjectMode()`**
- Displays attendance records for one subject
- Shows course name and instructor
- Calculates and displays attendance grade
- Hides overview elements

**`loadSampleAttendance()`**
- Loads attendance records
- Calculates total grade points
- Updates grade display
- Color-codes based on performance

**`addAttendanceRow(String date, String status)`**
- Creates individual attendance record rows
- Applies color coding:
  - Green (#10B981) for Present (100%)
  - Orange (#FFA500) for Late (50%)
  - Red (#EF4444) for Absent (0%)
- Displays grade percentage next to status

**`addSubjectOverviewCard(SubjectAttendanceData data)`**
- Creates subject cards in overview mode
- Displays subject name, code, instructor
- Shows attendance percentage and grade
- Includes present/late/absent breakdown
- Clickable to view subject details

#### Data Structure

**`SubjectAttendanceData` Inner Class**:
```java
class SubjectAttendanceData {
    int courseId;
    String subjectCode;
    String subjectName;
    String overallAttendance;
    int presentCount;
    int absentCount;
    int lateCount;
    String semester;
    String year;
    double attendanceGrade;
}
```

---

### 4. Attendance Overview (`AttendanceOverview.java`)
**Purpose**: Dashboard showing attendance across all subjects

#### Features

**Overall Statistics**:
- Total attendance percentage
- Present days count with 100% label
- Late days count with 50% label
- Absent days count with 0% label
- Overall attendance grade display

**Subject Cards**:
- Course name and code
- Instructor name
- Attendance percentage (large display)
- Attendance grade
- Present/Late/Absent breakdown
- Color-coded progress bar
- Clickable to view details

**Navigation Bar**:
- Home button
- Course button
- Grades button
- Attendance button (highlighted)

#### Key Methods

**`calculateOverallAttendance()`**
- Calculates attendance percentage: `(Present + Late) / Total × 100`
- Calculates attendance grade: `(Present × 100 + Late × 50) / Total`
- Updates all statistical displays
- Sets appropriate colors based on grade

**`loadSubjectsAttendance()`**
- Loads sample data for all subjects
- Creates cards for each subject
- Calculates individual subject grades
- Displays in scrollable container

**`addSubjectAttendanceCard(...)`**
- Creates individual subject cards
- Displays course information
- Shows attendance stats with color coding
- Adds progress bar visualization
- Implements click listener for navigation

**`getGradeColor(double grade)`**
- Returns appropriate color based on grade:
  - Green (#10B981): Grade ≥ 90%
  - Orange (#FFA500): Grade ≥ 75%
  - Red (#EF4444): Grade < 75%

---

## Grading System

### Attendance Status Values

| Status | Grade Value | Color | Icon |
|--------|-------------|-------|------|
| **Present** | 100% | Green (#10B981) | ✓ |
| **Late** | 50% | Orange (#FFA500) | ⚠ |
| **Absent** | 0% | Red (#EF4444) | ✗ |

### Grade Calculation Formula

```
Attendance Grade = (Present Count × 100 + Late Count × 50 + Absent Count × 0) / Total Days
```

**Example**:
- Present: 17 days
- Late: 2 days  
- Absent: 1 day
- Total: 20 days

```
Grade = (17 × 100 + 2 × 50 + 1 × 0) / 20
      = (1700 + 100 + 0) / 20
      = 1800 / 20
      = 90.0%
```

### Color Coding Logic

```java
if (grade >= 90) {
    color = Green;    // Excellent
} else if (grade >= 75) {
    color = Orange;   // Good
} else {
    color = Red;      // Needs Improvement
}
```

---

## Installation

### Prerequisites
1. Android Studio (Latest version recommended)
2. Android SDK API 21+ (Android 5.0 Lollipop or higher)
3. Internet connection for API calls

### Required Dependencies

Add to `build.gradle (Module: app)`:

```gradle
dependencies {
    // Volley for API calls
    implementation 'com.android.volley:volley:1.2.1'
    
    // ZXing for QR code scanning
    implementation 'com.journeyapps:zxing-android-embedded:4.3.0'
    
    // AppCompat for modern UI components
    implementation 'androidx.appcompat:appcompat:1.6.1'
}
```

### Permissions

Add to `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
```

### Setup Steps

1. **Clone or Download** the project
2. **Open** in Android Studio
3. **Sync Gradle** files
4. **Update API endpoints** if necessary in:
   - `login.java` → Line with `String URL`
   - `signup.java` → Line with `String URL`
5. **Build** the project
6. **Run** on emulator or physical device

---

## Usage

### For Students

#### 1. Registration
1. Open the app
2. Click "Sign Up" (if available)
3. Fill in:
   - First Name
   - Middle Name (optional)
   - Last Name
   - Username
   - Password
   - Retype Password
4. Click "Submit"
5. Wait for confirmation

#### 2. Login
1. Enter username
2. Enter password
3. Click "Login"
4. Wait for authentication

#### 3. View Attendance Overview
1. Navigate to Attendance tab (bottom navigation)
2. View overall statistics:
   - Total attendance percentage
   - Present/Late/Absent counts
   - Overall grade
3. Scroll through subject cards
4. Click any subject card for details

#### 4. View Individual Subject Attendance
1. Click on a subject card
2. View detailed attendance records:
   - Date of each class
   - Status (Present/Late/Absent)
   - Grade percentage for each entry
3. See overall attendance grade for the subject
4. Scan QR code to mark attendance (if enabled)

#### 5. Mark Attendance via QR Code
1. In attendance page, click "📷 Scan QR Code"
2. Point camera at QR code
3. Wait for scan confirmation
4. Attendance automatically recorded

#### 6. Filter Attendance (Overview Mode)
1. Open Attendance page in overview mode
2. Select semester from dropdown
3. Select year from dropdown
4. View filtered results

---

## Technical Details

### API Integration

**Using Volley Library**:
- Asynchronous HTTP requests
- Built-in request queue management
- Automatic retry and backoff policy
- Request/response caching

**Example API Call Pattern**:
```java
StringRequest request = new StringRequest(
    Request.Method.POST, 
    URL,
    response -> {
        // Success callback
        dialog.hide();
        Toast.makeText(context, response, Toast.LENGTH_LONG).show();
    },
    error -> {
        // Error callback
        dialog.hide();
        Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
    }
) {
    @Override
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        params.put("key", "value");
        return params;
    }
};
queue.add(request);
```

### QR Code Scanning

**Using ZXing Library**:
```java
private final ActivityResultLauncher<ScanOptions> barcodeLauncher = 
    registerForActivityResult(
        new ScanContract(),
        result -> {
            if(result.getContents() != null) {
                // Process scanned data
                markAttendance(result.getContents());
            }
        }
    );

// Launch scanner
ScanOptions options = new ScanOptions();
options.setPrompt("Scan QR Code to Mark Attendance");
options.setBeepEnabled(true);
options.setBarcodeImageEnabled(true);
barcodeLauncher.launch(options);
```

### Dynamic UI Generation

**Creating Views Programmatically**:
```java
private void addAttendanceRow(String date, String status) {
    LinearLayout row = new LinearLayout(this);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setBackgroundColor(0xFF2D2D2D);
    
    TextView txtDate = new TextView(this);
    txtDate.setText(date);
    txtDate.setTextColor(Color.WHITE);
    
    TextView txtStatus = new TextView(this);
    txtStatus.setText(status + " (" + gradePercentage + "%)");
    txtStatus.setTextColor(getStatusColor(status));
    
    row.addView(txtDate);
    row.addView(txtStatus);
    container.addView(row);
}
```

### Data Flow

```
User Input → Validation → API Request → Server Processing → Response → UI Update
```

**Detailed Flow**:
1. User enters credentials/data
2. App validates input locally
3. Progress dialog shows
4. Volley sends HTTP POST request
5. Server processes and responds
6. App receives response
7. Dialog hides
8. UI updates with result
9. Toast message shown

### Color Scheme

**Application Colors**:
- **Background**: #0F172A (Dark blue-gray)
- **Cards**: #1E293B (Lighter blue-gray)
- **Primary**: #2563EB (Blue)
- **Success/Present**: #10B981 (Green)
- **Warning/Late**: #FFA500 (Orange)
- **Error/Absent**: #EF4444 (Red)
- **Text Primary**: #FFFFFF (White)
- **Text Secondary**: #94A3B8 (Gray)
- **Accent**: #6366F1 (Indigo)

### Layout Patterns

**Responsive Design**:
- ScrollView for content overflow
- LinearLayout with weights for proportional sizing
- Match_parent and wrap_content for flexibility
- DP units for consistent sizing across devices
- Padding and margins for spacing

**Example**:
```xml
<LinearLayout
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:layout_weight="1"
    android:orientation="vertical"
    android:padding="16dp">
```

---

## Future Enhancements

### Potential Features
1. **Database Integration**: Replace sample data with SQLite or Room database
2. **Push Notifications**: Remind students of classes
3. **Attendance Analytics**: Graphs and charts for trends
4. **Export Functionality**: Generate PDF reports
5. **Offline Mode**: Cache data for offline viewing
6. **Biometric Attendance**: Fingerprint or face recognition
7. **Teacher Dashboard**: Separate interface for instructors
8. **Excuse Management**: Submit and track absence excuses
9. **Calendar Integration**: Sync with device calendar
10. **Multi-language Support**: Internationalization

### Technical Improvements
1. **MVVM Architecture**: Separate UI from business logic
2. **Repository Pattern**: Abstract data sources
3. **LiveData & ViewModel**: Better state management
4. **Coroutines**: Replace callbacks with async/await
5. **Dependency Injection**: Use Dagger or Hilt
6. **Unit Testing**: Add JUnit tests
7. **UI Testing**: Add Espresso tests
8. **Error Logging**: Integrate Crashlytics

---

## Troubleshooting

### Common Issues

**1. API Connection Failed**
- Check internet connection
- Verify API endpoint URLs
- Check server status
- Review permissions in AndroidManifest.xml

**2. QR Scanner Not Working**
- Grant camera permission
- Check ZXing dependency version
- Ensure camera hardware available
- Test on physical device (not emulator)

**3. Data Not Displaying**
- Check network response in Logcat
- Verify JSON parsing logic
- Ensure UI updates on main thread
- Check for null pointer exceptions

**4. Gradle Build Errors**
- Sync Gradle files
- Clean and rebuild project
- Update dependencies to compatible versions
- Check for duplicate dependencies

**5. UI Layout Issues**
- Test on different screen sizes
- Use ConstraintLayout for complex layouts
- Check for hardcoded dimensions
- Verify ScrollView implementation

---

## Credits & Libraries

### Third-Party Libraries
- **Volley**: Apache 2.0 License - HTTP library
- **ZXing**: Apache 2.0 License - Barcode scanning
- **AndroidX**: Google - Modern Android components

### Development
- **IDE**: Android Studio
- **Language**: Java
- **Min SDK**: API 21 (Android 5.0)
- **Target SDK**: Latest stable version

---

## License

This project is developed for educational purposes. 

---

## Contact & Support

For issues, questions, or contributions:
- Review the code documentation
- Check logcat for error messages
- Verify all dependencies are properly installed
- Test on multiple devices/emulators

---

## Version History

### Current Version
- Attendance tracking system with grading
- QR code scanning integration
- Overview and detail views
- Filter functionality
- Login/Signup system

### Planned Updates
- Database integration
- Enhanced analytics
- Improved error handling
- Additional features as listed in Future Enhancements

---

**Last Updated**: December 2024
