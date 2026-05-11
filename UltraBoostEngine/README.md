# UltraBoost Engine 🚀

**Professional Android Gaming Performance Optimizer**

UltraBoost Engine is a smart adaptive performance engine designed to improve gaming smoothness, frame stability, multitasking performance, and responsiveness during heavy Android gaming scenarios.

## ⚠️ Important Philosophy

This application:
- ✅ Prioritizes **stable frametimes** over fake FPS claims
- ✅ Uses **safe optimization** within Android's security limitations
- ✅ **Never disables thermal safety systems**
- ✅ **Never forces dangerous overclocking**
- ✅ **Never kills important Android system processes**
- ✅ Respects Android security restrictions
- ✅ Uses **adaptive optimization** instead of aggressive unsafe methods

## 🎯 Core Objectives

- Reduce stutters and micro-stutters
- Improve frametime stability
- Improve touch responsiveness
- Reduce recording lag
- Improve multitasking performance
- Improve frame consistency during PvP
- Reduce heavy frame spikes
- Improve smoothness under Discord + screen recording + Minecraft Java scenarios

## 🎮 Features

### Adaptive Optimization Modes
- **Balanced Mode** - Safe optimization with stable thermals
- **Gaming Mode** - Aggressive frame stabilization, lower latency
- **Recording Mode** - Optimized for screen recording + Discord voice chat
- **PvP Mode** - Ultra-low latency for competitive play
- **Extreme Mode** - Maximum safe optimization

### Key Modules
1. **App Detection Engine** - Detects selected app launch and monitors foreground processes
2. **Native Performance Engine (C++/JNI)** - Frame pacing stabilizer, CPU thread balancing
3. **Thermal Monitoring System** - Monitors temperature and reduces intensity when overheating
4. **Recorder & Voice Chat Optimization** - Balances workload during multitasking
5. **Smart Overlay UI** - Real-time FPS, temperature, and stats display
6. **Performance Profiles** - Custom profiles for different scenarios

### Minecraft Java Companion
- Detects PojavLauncher and MojoLauncher
- Suggests adaptive render distance
- Recommends optimized settings for recording

## 🏗️ Architecture

```
app/src/main/
├── java/com/ultraboost/engine/
│   ├── ui/                 # Activities and UI components
│   ├── service/            # Foreground services (Optimization, Overlay)
│   ├── optimization/       # Optimization modes and state
│   ├── jni/                # JNI bridge to native engine
│   ├── monitoring/         # Thermal, Memory, Performance monitors
│   ├── detection/          # App detection engine
│   ├── settings/           # Settings management
│   └── utils/              # Utility classes
├── cpp/                    # Native C++ performance engine
│   ├── native_engine.cpp   # Main native optimization code
│   └── CMakeLists.txt      # Native build configuration
└── res/                    # Resources (layouts, colors, strings)
```

## 🛠️ Technical Stack

### Android Layer
- Kotlin
- Android Studio
- Foreground Services
- Usage Stats API
- Notifications API
- View Binding

### Native Layer
- C++
- Android NDK
- JNI
- CMake

## 📱 Requirements

- **Minimum SDK:** Android 11 (API 30)
- **Target SDK:** Android 14 (API 34)
- **NDK:** Required for native optimization engine

## 🔧 Build Instructions

### Prerequisites
1. Android Studio Arctic Fox or newer
2. Android NDK installed
3. CMake installed

### Building

```bash
# Clone the repository
git clone https://github.com/yourusername/UltraBoostEngine.git
cd UltraBoostEngine

# Build Debug APK
./gradlew assembleDebug

# Build Release APK
./gradlew assembleRelease
```

### Using GitHub Actions
The project includes a workflow file that automatically builds APKs on push:
- Debug APK uploaded as artifact
- Release APK created on version tags

## 📄 Permissions

UltraBoost Engine uses only necessary permissions:
- `FOREGROUND_SERVICE` - For persistent optimization
- `PACKAGE_USAGE_STATS` - For app detection
- `POST_NOTIFICATIONS` - For status updates
- `SYSTEM_ALERT_WINDOW` - For in-game overlay
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` - For reliable background operation

## ⚡ Safety Features

1. **Thermal Protection** - Automatically reduces optimization when device gets hot
2. **Adaptive Intensity** - Adjusts based on temperature and memory pressure
3. **Safe Limits** - Never exceeds safe operating parameters
4. **No Root Required** - Works within Android's standard security model

## 🎨 UI Design

- Dark futuristic theme with neon gaming style
- Smooth transitions and animations
- Circular performance meters
- Live statistics dashboard
- Glassmorphism effects

## 📝 Disclaimer

This application does NOT:
- Claim impossible FPS multiplication
- Force dangerous overclocking
- Disable thermal protection
- Kill critical Android processes
- Damage hardware
- Cause bootloops
- Use fake RAM cleaner logic

## 🔮 Future Expandability

Architecture supports future additions:
- Launcher plugins
- Fabric companion mods
- Vulkan/OpenGL monitoring
- Shizuku integration
- Optional root enhancements
- AI adaptive optimization
- Advanced statistics system

## 📄 License

See LICENSE file for details.

## 🤝 Contributing

Contributions are welcome! Please ensure any changes:
- Follow the safety-first philosophy
- Don't introduce dangerous optimizations
- Maintain code quality standards
- Include appropriate documentation

---

**Made with ❤️ for the Android gaming community**
