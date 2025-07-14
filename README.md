# 📱 ProductiVize - Productivity Tracking App

A comprehensive Android app for tracking hourly productivity, generating insights, and maintaining a daily journal.

## 🌟 Features

### 📊 **Productivity Tracking**
- **Hourly Rating System** - Rate each hour from 1-5 stars
- **Achievement Calculation** - Personalized based on your threshold
- **Daily Goals** - Set and track your productivity targets
- **Visual Progress Ring** - See your achievement percentage at a glance

### 📈 **Insights & Analytics**
- **Weekly Charts** - Visual representation of your productivity trends
- **Personalized Insights** - AI-generated suggestions based on your patterns
- **Achievement Metrics** - Track your best days and average performance
- **Pattern Detection** - Identify your most productive hours

### 📝 **Daily Journal**
- **Mood Tracking** - Record your daily mood with emojis
- **Reflection Sections** - Wins, challenges, and tomorrow's goals
- **Auto-Generated Content** - Smart suggestions based on your ratings
- **Biometric Lock** - Secure your private thoughts
- **Auto-Lock Timer** - Automatically locks after 5 minutes of inactivity

### ⚙️ **Comprehensive Settings**
- **🎨 Appearance** - Dark/Light mode toggle
- **🔔 Notifications** - Hourly reminders and journal prompts
- **🔒 Privacy & Security** - Biometric lock and auto-lock features
- **💾 Data Management** - Export to CSV/JSON, auto-backup, clear data
- **🎯 Goals & Productivity** - Customize daily goals and achievement thresholds
- **ℹ️ About** - App info, help, rating, and sharing

## 🏗️ Architecture

### **Tech Stack**
- **Kotlin** - 100% Kotlin codebase
- **Jetpack Compose** - Modern declarative UI
- **Room Database** - Local data persistence
- **Hilt** - Dependency injection
- **Coroutines & Flow** - Asynchronous programming
- **MVVM Architecture** - Clean separation of concerns

### **Key Components**
- **MainActivity** - Entry point with theme management
- **ProductiVizeNavigation** - Bottom navigation between screens
- **ViewModels** - State management and business logic
- **Repository Pattern** - Data access abstraction
- **DAOs** - Database access objects
- **Dependency Injection** - Hilt modules for clean architecture

## 🚀 Getting Started

### **Prerequisites**
- Android Studio Arctic Fox or later
- Android SDK 24+ (Android 7.0)
- Kotlin 1.8+
- Gradle 8.0+

### **Setup**
1. **Clone the repository**
```bash
git clone <repository-url>
cd ProductiVize
```

2. **Configure Android SDK**
```bash
# Create local.properties file
echo "sdk.dir=/path/to/your/android/sdk" > local.properties
```

3. **Build the project**
```bash
./gradlew clean
./gradlew assembleDebug
```

4. **Install on device**
```bash
./gradlew installDebug
```

## 🧪 Testing

### **Comprehensive Testing Guide**
See [TESTING_GUIDE.md](TESTING_GUIDE.md) for detailed testing instructions covering:
- 📱 All app features and user flows
- ⚙️ Settings functionality verification
- 🔧 Edge cases and error handling
- 📊 Performance and compatibility testing

### **Build Verification**
See [BUILD_CHECK.md](BUILD_CHECK.md) for build troubleshooting and verification steps.

### **Quick Test Commands**
```bash
# Run unit tests
./gradlew test

# Run lint checks
./gradlew lintDebug

# Generate test coverage report
./gradlew jacocoTestReport
```

## 📱 App Structure

### **Main Screens**
1. **📊 Tracker** - Rate hours and view daily achievement
2. **📈 Insights** - Weekly charts and productivity analytics
3. **📝 Journal** - Daily reflection and mood tracking
4. **⚙️ Settings** - Comprehensive app configuration

### **Key Features Implementation**
- **Personalized Achievement Calculation** - Uses user-defined thresholds
- **Smart Insights Generation** - Adapts to user's goals and patterns
- **Robust Data Export** - CSV and JSON formats with sharing
- **Secure Journal** - Biometric authentication and auto-lock
- **Comprehensive Settings** - 20+ configurable options

## 🔧 Configuration

### **Customizable Settings**
- **Daily Goal**: 1-16 hours (default: 8)
- **Achievement Threshold**: 1-5 stars (default: 3)
- **Notification Times**: 18:00-22:00 (default: 20:00)
- **Auto-Lock Timer**: 5 minutes for journal
- **Export Format**: CSV or JSON
- **Theme**: Light or Dark mode

### **Database Schema**
- **HourLog** - Individual hour ratings and metadata
- **DailySummary** - Aggregated daily statistics and insights
- **JournalEntry** - Daily journal entries with mood and reflections
- **Settings** - User preferences and configuration

## 🎯 Key Improvements Made

### **🚨 Critical Fixes**
- ✅ Fixed database injection dependencies
- ✅ Resolved null pointer exceptions
- ✅ Fixed date picker confirmation button
- ✅ Corrected settings integration throughout app
- ✅ Improved error handling and crash prevention

### **🛡️ Stability Enhancements**
- ✅ Comprehensive exception handling
- ✅ Proper coroutine cancellation
- ✅ Input validation and bounds checking
- ✅ Memory leak prevention
- ✅ Graceful fallbacks for unsupported features

### **🎨 User Experience**
- ✅ Immediate dark mode toggle
- ✅ Personalized insights and calculations
- ✅ Intuitive settings dialogs
- ✅ Smooth navigation and transitions
- ✅ Proper loading states and empty states

## 📊 Performance

### **Optimizations**
- **Efficient Database Queries** - Optimized Room queries with indexes
- **Lazy Loading** - Content loaded only when needed
- **State Management** - Proper StateFlow usage for reactive UI
- **Memory Management** - Proper coroutine scoping and cancellation

### **Metrics**
- **App Size**: ~15-25 MB
- **Memory Usage**: ~50-80 MB during normal use
- **Battery Impact**: Minimal (no background services)
- **Startup Time**: <2 seconds on modern devices

## 🔐 Privacy & Security

### **Data Handling**
- **Local Storage Only** - All data stored locally on device
- **No Network Requests** - Completely offline functionality
- **Biometric Protection** - Optional journal encryption
- **Export Control** - User controls all data sharing

### **Permissions Required**
- **Biometric** - For journal lock feature (optional)
- **Notifications** - For productivity reminders (optional)
- **Storage** - For data export functionality

## 🤝 Contributing

### **Development Setup**
1. Fork the repository
2. Create a feature branch
3. Follow the existing code style and architecture
4. Add comprehensive tests for new features
5. Update documentation as needed

### **Code Standards**
- **Kotlin Coding Conventions** - Follow official Kotlin style guide
- **MVVM Architecture** - Maintain clean separation of concerns
- **Dependency Injection** - Use Hilt for all dependencies
- **Error Handling** - Comprehensive try-catch blocks
- **Documentation** - Clear comments and documentation

## 📄 License

This project is licensed under the Proprietary License - see the [LICENSE](LICENSE) file for details.

## 🎉 Acknowledgments

- **Jetpack Compose** - For the modern UI framework
- **Room Database** - For reliable local data persistence
- **Hilt** - For clean dependency injection
- **Material Design 3** - For beautiful and consistent UI components

---

**ProductiVize - Track your productivity, achieve your goals! 🚀**
