# 🔧 Build Verification Guide

## 🚀 Quick Build Check

### 1. **Setup Android SDK**
```bash
# Set ANDROID_HOME environment variable
export ANDROID_HOME=/path/to/your/android/sdk

# Or create local.properties file
echo "sdk.dir=/path/to/your/android/sdk" > local.properties
```

### 2. **Clean & Build**
```bash
./gradlew clean
./gradlew assembleDebug --stacktrace
```

### 3. **Install & Run**
```bash
./gradlew installDebug
adb shell am start -n com.productivize/.ui.MainActivity
```

---

## ✅ **Pre-Build Checklist**

### **Required Files Present**
- [ ] `local.properties` with correct SDK path
- [ ] All Kotlin files compile without errors
- [ ] All imports resolved correctly
- [ ] Database migration files present
- [ ] Resource files (strings, themes, etc.) present

### **Dependencies Check**
- [ ] All Hilt dependencies properly configured
- [ ] Room database dependencies present
- [ ] Compose dependencies compatible
- [ ] Biometric library included
- [ ] Notification compatibility libraries included

### **Code Compilation**
- [ ] No syntax errors in any Kotlin files
- [ ] All ViewModels properly annotated with @HiltViewModel
- [ ] All DAOs properly configured
- [ ] Database migrations defined correctly
- [ ] All Composable functions properly structured

---

## 🐛 **Common Build Issues & Fixes**

### **Issue: SDK Location Not Found**
```
SDK location not found. Define a valid SDK location...
```
**Fix:** Create `local.properties` file:
```properties
sdk.dir=C:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
```

### **Issue: Hilt Compilation Errors**
```
[Hilt] Processing did not complete...
```
**Fix:** Ensure all classes are properly annotated:
- `@HiltAndroidApp` on Application class
- `@AndroidEntryPoint` on Activities
- `@HiltViewModel` on ViewModels

### **Issue: Room Migration Errors**
```
Cannot find migration from X to Y
```
**Fix:** Database version and migration properly defined in `ProductiVizeDatabase.kt`

### **Issue: Compose Compilation Errors**
```
Unresolved reference: Composable
```
**Fix:** Check Compose BOM version and imports

---

## 📱 **APK Verification**

### **After Successful Build**
```bash
# Check APK was created
ls -la app/build/outputs/apk/debug/

# Install on device/emulator
adb install app/build/outputs/apk/debug/app-debug.apk

# Launch app
adb shell am start -n com.productivize/.ui.MainActivity
```

### **APK Analysis**
```bash
# Check APK size
du -h app/build/outputs/apk/debug/app-debug.apk

# Analyze APK contents
./gradlew analyzeDebugBundle
```

---

## 🔍 **Code Quality Checks**

### **Static Analysis**
```bash
# Run lint checks
./gradlew lintDebug

# Check for unused resources
./gradlew lintDebug --continue
```

### **Manual Code Review**
- [ ] All TODO comments addressed
- [ ] No hardcoded strings (use string resources)
- [ ] Proper error handling in all ViewModels
- [ ] Database operations properly wrapped in try-catch
- [ ] Coroutines properly scoped to ViewModels

---

## 🎯 **Build Success Criteria**

### ✅ **Must Pass**
- [ ] `./gradlew assembleDebug` completes successfully
- [ ] APK file is generated in correct location
- [ ] APK installs on device without errors
- [ ] App launches without immediate crashes

### ✅ **Should Pass**
- [ ] Lint checks pass with minimal warnings
- [ ] No deprecated API usage warnings
- [ ] Reasonable APK size (< 50MB for this app)
- [ ] All features accessible from main navigation

---

## 🚨 **Emergency Build Fixes**

If build fails, try these in order:

1. **Clean Everything**
```bash
./gradlew clean
rm -rf .gradle
rm -rf build
rm -rf app/build
```

2. **Invalidate Caches** (in Android Studio)
```
File → Invalidate Caches and Restart
```

3. **Check Dependencies**
```bash
./gradlew dependencies --configuration debugCompileClasspath
```

4. **Sync Project** (in Android Studio)
```
File → Sync Project with Gradle Files
```

---

## 📊 **Build Performance**

### **Expected Build Times**
- **Clean Build:** 2-5 minutes
- **Incremental Build:** 30-60 seconds
- **Install:** 10-30 seconds

### **Optimization Tips**
- Use Gradle daemon: `--daemon`
- Enable parallel builds: `--parallel`
- Use build cache: `--build-cache`

---

## ✅ **Final Verification**

Before testing, ensure:
- [ ] Build completes without errors
- [ ] APK installs successfully
- [ ] App launches to main screen
- [ ] No immediate crashes or ANRs
- [ ] All navigation tabs are accessible

**Ready for comprehensive testing!** 🎉 