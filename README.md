
# 🪐 Planet Destroyer (Android Game)

**Planet Destroyer** is a fast-paced Android endless runner where the player controls a spaceship, dodging meteors, collecting coins, and surviving as long as possible to reach new high scores. Built in Kotlin, it supports both **touch** and **sensor (tilt)** controls and integrates location tracking and a high score system.

---

## 📱 Features

🎮 **Endless Gameplay**  
  • 5-lane meteor dodging action  
  • Increasing difficulty over time  
  • Real-time lives tracking with heart icons

🎯 **Controls**  
  • Choose between **touch** and **accelerometer (tilt)** control  
  • Smooth, responsive input

🪙 **Coins & Score**  
  • Animated coin spawning logic  
  • Coin pickup sound & explosion effects  
  • Distance-based scoring system (+ extra for coins)

🏁 **High Score System**  
  • Saves top 10 scores locally  
  • Records GPS location of each score  
  • High Score screen with Google Map view

🔊 **Sound & Feedback**  
  • Background music  
  • Explosion sound on collision  
  • Vibration feedback on hit

🗺️ **Google Maps Integration**  
  • Displays location of each high score  
  • Tap scores to zoom in on the map

⚙️ **Extras & Utilities**  
  • Modular architecture with clean code separation  
  • SharedPreferences for data persistence  
  • Glide for animations and visual polish  
  • Planned Firebase support for future cloud syncing

---

## 🛠️ Project Structure

```
PlanetDestroyer/
├── activities/
│   ├── StartActivity.kt
│   ├── MainGameActivity.kt
│   ├── GameOverActivity.kt
│   ├── HighScoresActivity.kt
│   └── MapActivity.kt
├── logic/
│   └── GameLogic.kt
├── manager/
│   ├── GameManager.kt
│   └── ScoreManager.kt
├── utils/
│   ├── SoundUtils.kt
│   └── VibrationUtils.kt
├── res/
│   ├── layout/
│   ├── drawable/
│   └── values/
└── README.md
```

---

## 🧰 Tech Stack

- Kotlin
- Android SDK
- Google Maps SDK
- Fused Location Provider
- Glide (GIF and image handling)
- SharedPreferences
- XML Layouts (GridLayout, RelativeLayout)

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/DanKric/PlanetDestroyer.git
```

### 2. Open in Android Studio

- Use Android Studio Giraffe or newer
- Recommended: real Android device for sensor controls

### 3. Run the App

- On a physical device (best experience)
- Or on an emulator with Google Play services enabled

---

## 📦 Required Permissions

Add these to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.VIBRATE"/>
```

---

## 🙋‍♂️ Author

**Dan Krikli**  
🎓 Afeka Tel Aviv Academic College of Engineering  
🔗 [GitHub Profile](https://github.com/DanKric)

---

## 🔮 Future Plans

- 🦾 Power-ups and shields
- 🌌 Unlockable skins & ship customization
- 🧠 Smarter AI-generated obstacle patterns
- 🕹️ Multiplayer challenge mode (Bluetooth or online)
---

🛸 **Made with Kotlin and caffeine.**  
