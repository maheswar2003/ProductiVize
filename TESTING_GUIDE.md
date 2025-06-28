# 🧪 ProductiVize - Comprehensive Testing Guide

## 🚀 Pre-Testing Setup

### Prerequisites
1. **Android Studio** with latest SDK
2. **Android device or emulator** (API 24+)
3. **Enable Developer Options** on physical device
4. **Grant notification permissions** when prompted

### Build & Install
```bash
./gradlew clean
./gradlew assembleDebug
./gradlew installDebug
```

---

## 📋 **COMPLETE TESTING CHECKLIST**

### 🎯 **1. App Launch & Navigation**
- [ ] App launches without crashes
- [ ] Bottom navigation works (Tracker, Insights, Journal, More)
- [ ] Dark mode toggle in settings works immediately
- [ ] App remembers dark mode preference after restart
- [ ] Navigation between screens is smooth
- [ ] Back button works correctly

---

### ⭐ **2. Tracker Screen - Core Functionality**

#### **Hour Rating System**
- [ ] Can rate hours 1-5 stars by tapping
- [ ] Star colors change correctly (Gold for selected)
- [ ] Haptic feedback works when vibration is enabled
- [ ] No haptic feedback when vibration is disabled
- [ ] Rating persists after navigating away and back
- [ ] Achievement percentage updates immediately after rating

#### **Date Navigation**
- [ ] Calendar icon opens date picker
- [ ] Date picker shows current date selected
- [ ] "OK" button in date picker works
- [ ] "Cancel" button dismisses date picker
- [ ] Left/right arrows navigate days
- [ ] Cannot navigate to future dates
- [ ] Cannot navigate more than 1 year back
- [ ] Date display updates correctly

#### **Achievement Ring**
- [ ] Shows correct percentage based on user's threshold setting
- [ ] Ring fills proportionally to achievement percentage
- [ ] Glow effect appears for 80%+ achievement
- [ ] Stats show correct values (Rated Hours, Avg Rating, Peak Hours)

#### **Insights Display**
- [ ] Shows personalized insights when available
- [ ] Insights reflect user's daily goal and threshold settings
- [ ] No insights shown for days with no ratings

---

### 📊 **3. Insights Screen**

#### **Empty State**
- [ ] Shows "Start tracking to see insights" when no data
- [ ] Emoji and text display correctly

#### **With Data**
- [ ] Weekly chart displays correctly
- [ ] Week average calculates correctly
- [ ] Best day shows highest achievement percentage
- [ ] Recent insights from last 3 days display
- [ ] No crashes when data is empty

---

### 📝 **4. Journal Screen**

#### **Basic Functionality**
- [ ] Mood selector works (emoji selection)
- [ ] Text fields accept input (Wins, Challenges, Goals)
- [ ] Auto-generated achievements display
- [ ] Auto-generated patterns display
- [ ] Save button works without crashes

#### **Biometric Lock (if enabled in settings)**
- [ ] Lock toggle appears when biometric lock is enabled
- [ ] Lock toggle hidden when biometric lock is disabled
- [ ] Biometric prompt appears when locking/unlocking
- [ ] Proper fallback for devices without biometric support

#### **Auto-Lock Timer (if enabled in settings)**
- [ ] Journal auto-locks after 5 minutes of inactivity
- [ ] Timer resets when user interacts with journal
- [ ] Timer cancels when journal is manually locked
- [ ] No memory leaks or crashes from timer

---

### ⚙️ **5. Settings Screen - All Sections**

#### **🎨 Appearance**
- [ ] Dark Mode toggle works immediately
- [ ] Theme persists across app restarts

#### **🔔 Notifications**
- [ ] Enable Notifications shows test notification
- [ ] Hourly Reminders shows test notification
- [ ] Journal Reminders shows test notification
- [ ] Notification time picker works (18:00-22:00 options)
- [ ] Selected time displays correctly
- [ ] Notifications respect enabled/disabled state

#### **🔒 Privacy & Security**
- [ ] Biometric Lock toggle works
- [ ] Shows/hides journal lock toggle accordingly
- [ ] Auto-Lock Journal toggle works
- [ ] Auto-lock timer respects this setting

#### **💾 Data & Backup**
- [ ] Auto Backup shows "backup enabled" notification
- [ ] Export Data creates shareable file
- [ ] Export format (CSV/JSON) selection works
- [ ] Clear All Data shows confirmation dialog
- [ ] Clear All Data actually deletes all data

#### **🎯 Goals & Productivity**
- [ ] Daily Goal dialog opens with grid (1-16 hours)
- [ ] Selected goal displays in settings
- [ ] Achievement Threshold dialog opens with stars (1-5)
- [ ] Selected threshold displays in settings
- [ ] Vibration Feedback toggle affects haptic feedback
- [ ] Changes affect achievement calculations immediately

#### **ℹ️ About**
- [ ] App Version opens app info screen
- [ ] Help & Support opens help URL
- [ ] Rate App opens Play Store (or shows error gracefully)
- [ ] Share App opens share dialog

---

### 🔧 **6. Advanced Feature Testing**

#### **Settings Integration**
- [ ] Change daily goal to 4 hours, rate 3 hours at 4+ stars → Should show "Almost there!" insight
- [ ] Change achievement threshold to 4 stars → Only 4-5 star hours count as achievements
- [ ] Change achievement threshold to 2 stars → 2-5 star hours count as achievements
- [ ] Export format setting affects actual export file type

#### **Data Persistence**
- [ ] Rate some hours, close app, reopen → Ratings preserved
- [ ] Change settings, close app, reopen → Settings preserved
- [ ] Write journal entry, close app, reopen → Entry preserved
- [ ] Achievement calculations persist across app restarts

#### **Edge Cases**
- [ ] Rate all 24 hours → Achievement percentage calculates correctly
- [ ] Rate no hours → Shows 0% achievement, no crashes
- [ ] Very long journal entries → No crashes, text scrolls properly
- [ ] Rapid navigation between screens → No crashes
- [ ] Change date while rating hours → Ratings save to correct date

---

### 📱 **7. Device-Specific Testing**

#### **Permissions**
- [ ] App requests notification permission appropriately
- [ ] Graceful handling when notifications denied
- [ ] Biometric permission handling
- [ ] File access for export functionality

#### **Different Screen Sizes**
- [ ] UI looks good on small phones
- [ ] UI looks good on large phones/tablets
- [ ] Text remains readable at different font sizes
- [ ] Touch targets are appropriately sized

#### **Performance**
- [ ] Smooth scrolling in all screens
- [ ] No lag when rating hours
- [ ] Quick navigation between screens
- [ ] No memory leaks during extended use

---

### 🐛 **8. Error Handling Testing**

#### **Network/Storage Issues**
- [ ] App works without internet connection
- [ ] Graceful handling of storage full scenarios
- [ ] Database corruption recovery

#### **Invalid Input**
- [ ] No crashes with extremely long text input
- [ ] Proper handling of invalid dates
- [ ] Graceful handling of corrupted settings

---

### ✅ **9. User Experience Flows**

#### **New User Flow**
1. [ ] Launch app for first time
2. [ ] Navigate to settings and customize (goal: 6 hours, threshold: 3 stars)
3. [ ] Rate a few hours on tracker
4. [ ] Check insights (should show goal progress)
5. [ ] Write journal entry
6. [ ] Export data

#### **Daily Usage Flow**
1. [ ] Open app in morning
2. [ ] Rate previous day's remaining hours
3. [ ] Check achievement and insights
4. [ ] Write journal reflection
5. [ ] Set goals for today

#### **Settings Customization Flow**
1. [ ] Enable dark mode → Immediate theme change
2. [ ] Set daily goal to 8 hours
3. [ ] Set achievement threshold to 4 stars
4. [ ] Enable notifications and biometric lock
5. [ ] Test all notification types
6. [ ] Verify biometric lock works in journal

---

## 🎯 **Success Criteria**

### ✅ **Must Pass (Critical)**
- [ ] No crashes during any normal usage
- [ ] All settings actually affect app behavior
- [ ] Data persists correctly across app restarts
- [ ] Core rating and achievement system works
- [ ] Dark mode toggle works immediately

### ✅ **Should Pass (Important)**
- [ ] All notifications work when permissions granted
- [ ] Biometric lock works on supported devices
- [ ] Export functionality creates valid files
- [ ] Insights show personalized content
- [ ] Auto-lock timer works correctly

### ✅ **Nice to Have (Enhancement)**
- [ ] Smooth animations and transitions
- [ ] Proper empty states and loading indicators
- [ ] Graceful error messages for edge cases
- [ ] Optimal performance on older devices

---

## 🚨 **Known Issues to Verify Fixed**

- [x] Date picker OK button now works
- [x] Settings actually change app behavior
- [x] Dark mode toggle works immediately
- [x] No crashes from null settings
- [x] Achievement calculations use user thresholds
- [x] Vibration setting affects haptic feedback
- [x] Biometric lock shows/hides correctly
- [x] Auto-lock timer doesn't cause memory leaks
- [x] Export includes proper intent flags
- [x] Notification permission handling

---

## 📝 **Test Report Template**

```
## Test Results - [Date]

### Environment
- Device: [Device Name]
- Android Version: [Version]
- App Version: [Version]

### Summary
- ✅ Passed: [X]/[Total] tests
- ❌ Failed: [X]/[Total] tests
- ⚠️ Issues: [List any issues found]

### Critical Issues Found
[List any crashes or major bugs]

### Minor Issues Found
[List any UI glitches or minor problems]

### Performance Notes
[Any performance observations]

### Recommendations
[Suggestions for improvements]
```

---

## 🎉 **Final Validation**

The app is ready for release when:
- [ ] All critical tests pass
- [ ] No crashes during normal usage
- [ ] All settings are functional
- [ ] Data persistence works correctly
- [ ] User experience flows are smooth

**Happy Testing! 🚀** 