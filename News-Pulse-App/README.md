# News-Pulse-App

## 🚀 Overview
**News-Pulse-App** is a modern Android application that delivers fresh news from various sources, providing users with a smooth, customizable, and engaging reading experience. The app follows best practices in Android development, including modular architecture, efficient API integration, and robust error handling.

## ✨ Key Features
- 📰 **Latest News Feed**: Stay updated with real-time news articles aggregated from trusted sources via powerful APIs.
- 🧠 **Personalized News Experience**: Discover top headlines, trending stories, and category-based content (e.g., Technology, Sports, Business).
- 🏷️ **Sentiment Tags**: Articles are automatically tagged with sentiments like **positive**, **negative**, or **neutral** for quick emotional context.
- 🔍 **Smart Search**: Search across multiple sources using keywords to find relevant news instantly.
- ❤️ **Bookmarking**: Mark and organize your favorite articles for later access.
- 👍 **Like & Share**: Engage with news stories by liking and sharing them with others.
- 🔄 **Swipe-to-Refresh**: Easily refresh your news feed with a simple swipe gesture.
- 🌐 **WebView Article Viewer**: Enjoy a seamless in-app reading experience using WebView for full-article display.
- 📱 **Responsive UI**: Optimized layouts for phones and tablets, featuring **Material 3 design** with smooth transitions and modern UX.
- 🛡️ **Robust Error Handling**: User-friendly error messages and resilient fallback mechanisms for network or API failures.

## 📸 Screenshots

![Home Screen](screenshots/home.png)
![Article Detail](screenshots/article_detail.png)
![Categories](screenshots/categories.png)

## 🧰 Key Technologies

- Built using native **Java + XML**
- Integrated with **Firebase Authentication** and **Firestore**
- **Glide** for efficient image loading and caching
- Follows **Material** design principles
- Uses **WebView** for seamless in-app article reading

## 🖥️ Setup Instructions

### Download the APK
   - Download the latest version of [News Pulse](https://github.com/jeevandeepsaini/News-Pulse/releases/download/v1.0.0/NewsPulse.apk)
   - Install the APK on your Android device.
   - Open the app and start reading the latest news instantly!

### Build from Source

#### Prerequisites
   - **Android Studio** installed
   - **Java Development Kit (JDK)** installed
   - **Android device/emulator** for testing
  
#### Steps to Install

   1. **Clone the repository:**
       ```sh
       git clone https://github.com/jeevandeepsaini/News-Pulse.git
       cd News-Pulse/News-Pulse-App
       ```
   2. **Open in Android Studio:**
       - Launch Android Studio.
       - Select **File > Open** and choose the `News-Pulse-App` folder.
         
   2. **Firebase Setup**
       - Add your `google-services.json` file to the `app/` directory.
       - Ensure Firebase Authentication and Firestore are enabled in your Firebase Console.
         
   3. **Build & Run:**
       - Connect your Android device or start an emulator.
       - Click the **Run** button or use `Shift + F10`.
