# ProductiVize 📊

**Your Hourly Productivity, Visualized & Optimized**

[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![MIT License](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

ProductiVize is an innovative Android app that transforms hourly self-ratings into visual achievement metrics and actionable insights. Track your productivity hour-by-hour, visualize your progress with beautiful charts, and receive AI-powered feedback to optimize your daily performance.

## 📱 Download

### **Latest Release**
**[📥 Download ProductiVize APK](https://github.com/maheswar2003/ProductiVize/releases/latest/download/app-debug.apk)**

> **Note:** You may need to enable "Install from unknown sources" in your Android settings to install the APK.

### **All Releases**
Browse all versions: **[📋 View All Releases](https://github.com/maheswar2003/ProductiVize/releases)**

## 🌟 Features

### Core Features (MVP)

- **📱 Hour Tracker**: 24-hour timeline with tap-to-rate interface (1-5 stars)
- **📈 Achievement Dashboard**: Daily percentage score visualization with donut charts
- **💡 Insights Engine**: Automated daily feedback based on your patterns
- **📝 Reflection Journal**: Pre-filled achievements and challenges from your ratings

### Visual Identity

- **Primary Colors**: Deep Blue (#2D5DCC) and Teal (#2EC4B6)
- **Rating Colors**:
    - 4-5★: Teal (#2EC4B6)
    - 3★: Amber (#FFBF69)
    - 1-2★: Coral (#FF6B6B)

## 🛠 Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Database**: Room (local-first approach)
- **Architecture**: MVVM with Repository pattern
- **Dependency Injection**: Hilt
- **Charts**: MPAndroidChart
- **Async**: Coroutines & Flow

## 📱 Screenshots

[Coming Soon]

## 🚀 Getting Started

### Prerequisites

- Android Studio Arctic Fox or later
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)

### Installation

1. Clone the repository:

```bash
git clone https://github.com/maheswar2003/ProductiVize.git
```

2. Open the project in Android Studio

3. Sync the project with Gradle files

4. Run the app on an emulator or physical device

## 📂 Project Structure

```
productivityApp/
├── app/
│   ├── src/main/java/com/productivize/
│   │   ├── data/                # Data layer
│   │   │   ├── dao/            # Room DAOs
│   │   │   ├── database/       # Database configuration
│   │   │   ├── model/          # Data models
│   │   │   └── repository/     # Repository classes
│   │   ├── domain/             # Business logic
│   │   │   ├── calculator/     # Achievement calculations
│   │   │   └── generator/      # Insight generation
│   │   ├── ui/                 # UI layer
│   │   │   ├── screens/        # Composable screens
│   │   │   ├── theme/          # App theming
│   │   │   └── navigation/     # Navigation setup
│   │   └── di/                 # Dependency injection modules
│   └── src/main/res/           # Resources
├── build.gradle.kts
└── settings.gradle.kts
```

## 🎯 Usage

1. **Rate Your Hours**: Tap on any hour in the timeline and select 1-5 stars
2. **View Achievement**: Check your daily achievement percentage in the ring chart
3. **Read Insights**: Get personalized feedback based on your patterns
4. **Track Progress**: Monitor weekly and monthly trends in the Insights tab

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- Inspired by the need for quality-focused time tracking
- Built with modern Android development best practices
- Designed for glanceability and user delight

---

**ProductiVize** - Transform time into measurable growth 🚀 