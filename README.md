<p align="center">
  <img src="news-pulse-logo.png" alt="News Pulse Logo" width="200"/>
</p>

<h1 align="center">News Pulse</h1>
<p align="center">Your Smart Companion for Personalized, Sentiment-Aware News</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green?logo=android"/>
  <img src="https://img.shields.io/badge/Code-Java-blue?logo=java"/>
  <img src="https://img.shields.io/badge/Layout-XML-orange?logo=xml"/>
  <img src="https://img.shields.io/badge/Backend-Python-yellow?logo=python"/>
  <img src="https://img.shields.io/badge/Backend-Firebase-ffca28?logo=firebase"/>
  <img src="https://img.shields.io/badge/Database-Firestore-blue?logo=firebase"/>
  <img src="https://img.shields.io/badge/CI-GitHub_Actions-blue?logo=githubactions"/>
  <img src="https://img.shields.io/badge/API-NewsAPI-lightgrey"/>
  <img src="https://img.shields.io/badge/NLP-HuggingFace-yellow?logo=huggingface"/>
  <img src="https://img.shields.io/badge/License-GPLv3-blue.svg"/>
  <img src="https://img.shields.io/github/last-commit/jeevandeepsaini/News-Pulse"/>
</p>

## 🚀 Overview

**NewsPulse** is a modern Android application designed to deliver personalized and real-time news updates directly to your fingertips. Built with **Java** and **XML**, and powered by **Firebase**, NewsPulse offers an interactive platform where users can explore news articles by category, engage through likes and bookmarks, and gain emotional insights via sentiment analysis.

What sets NewsPulse apart is its integration of **Python-based sentiment analysis**, adding an intelligent layer to every article by evaluating its overall sentiment. This helps users not only read the news but understand the sentiment behind it — positive, neutral, or negative. Whether you're staying informed about current affairs or curating your own personalized feed, NewsPulse transforms traditional news reading into a smarter, more insightful experience.

The project includes two core components:
- **News-Pulse-App:** The Android frontend for user interaction and personalized news consumption.
- **News-Pulse-Backend:** The backend service that aggregates, categorizes, analyzes sentiment, and stores news articles.

## ✨ Key Features

- 🔐 **Secure Authentication:** Sign in securely using Firebase Authentication, including Google Sign-In support.
- 📰 **Aggregated News Feed:** Curated articles from diverse and trusted sources for comprehensive coverage.
- 🔍 **Category-Based Filtering:** Explore news by interest—politics, tech, sports, health, and more.
- 👤 **Personalized Experience:** Follow topics, receive tailored recommendations, and build your own news feed.
- 📖 **In-App News Reading:** Read full articles in-app with an integrated WebView for a smooth, uninterrupted experience.
- 📊 **Sentiment Analysis:** Understand the overall sentiment of each article—positive, neutral, or negative.
- ❤️ **Like & Bookmark:** Engage with articles by liking or saving them for later reading.
- 📤 **Easy Sharing:** Instantly share articles with friends across platforms.
- ☁️ **Real-Time Backend:** Powered by Firestore for seamless data syncing and real-time updates.
- ✨ **Modern UI/UX:** Clean, intuitive design optimized for various screen sizes and devices.
- 🛡️ **Secure API Layer:** Robust backend infrastructure for reliable data handling and user protection.

## 🗂️ Project Structure

```
News-Pulse/
│
├── News-Pulse-App/       # Android Application
├── News-Pulse-Backend/   # Backend service for news processing
└── README.md             # Project overview (this file)
```

- For detailed setup and usage, refer to:
  - [News-Pulse-App README](./News-Pulse-App/README.md)
  - [News-Pulse-Backend README](./News-Pulse-Backend/README.md)

## 📌 How It Works

1. The **backend** collects news articles via [News API](https://newsapi.org/), then performs sentiment analysis and categorizes the content. It runs locally when executed and is currently deployed on GitHub Actions to run automatically once a day.
2. The **frontend** (Android app) displays curated news feeds, allowing users to search, filter, like, bookmark, and follow topics.
3. User preferences are stored in Firestore and used to deliver personalized news recommendations.
4. Real-time updates ensure users receive the latest stories and see their interactions reflected instantly.

## 🖥️ Quick Start

1. **Clone the repository:**
   ```sh
   git clone https://github.com/jeevandeepsaini/News-Pulse.git
   cd News-Pulse
   ```

2. **Set up the frontend and backend:**
   - Refer to the individual README files for installation and configuration:
     - [`News-Pulse-App`](./News-Pulse-App/README.md)
     - [`News-Pulse-Backend`](./News-Pulse-Backend/README.md)

3. **Run both services:**  
   - Setup instructions, environment variables, and run commands are provided in each subproject's README.

## 🛠️ Tech Stack

### Android App
- **Java** & **XML** – Core technologies for building the native Android UI.
- **Firebase Authentication** – Secure user sign-in with email/Google login.
- **Cloud Firestore** – Real-time NoSQL database to store user preferences and bookmarks.
- **Material Design** – Modern UI components for Android.

### Backend
- **Python** – Used for backend automation and data processing.
- **Hugging Face Transformers (RoBERTa)** – For performing sentiment analysis on news articles.
- **NewsAPI.org** – Source of aggregated news data.
- **Cloud Firestore** – Stores processed news articles with metadata.

### Tools & Platforms
- **Git & GitHub** – Version control and collaboration.
- **Android Studio** – Primary IDE for Android development.
- **GitHub Actions** – Workflow automation for backend news processing.

## 🛡️ Security & Privacy

- **Authentication:** All user accounts are securely managed via Firebase Authentication.
- **Stored Data:** Only minimal user data is stored in Firestore:
  - Name and email (from authentication)
  - User preferences and bookmarks
- **Account Deletion:** Users can permanently delete their accounts after confirming their email.
- **Environment Variables:** 
  - Local development uses a `.env` file (excluded from version control).
  - On GitHub Actions, secrets are managed securely using [GitHub Secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets).
- **Privacy-First Design:** No data is shared with third parties; all processing happens within the app's secure environment.

## ⚙️ API Usage & Deployment Notes

- The project uses [NewsAPI](https://newsapi.org/) to fetch news articles.
- You must obtain your own API key and configure it securely.
- **Secret Management:**
  - `.env` file for local development
  - [GitHub Secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets) for GitHub Actions
- Ensure you comply with each API provider’s **terms of use** and **rate limits** before deploying publicly.
- This repository provides demo configurations only; production deployments may require additional setup.

## 🧹 Firestore Data Cleanup

- To stay within **Firebase’s free tier**, the backend includes an automated cleanup mechanism.
- This function:
  - Deletes news articles that are **7 days or older** from Firestore.
  - Runs automatically via **GitHub Actions** on a regular schedule.
- This helps minimize Firestore read/write operations and keeps the database optimized.

## 📝 License

This project is licensed under the **GNU General Public License v3.0 (GPL-3.0)**.
See the [LICENSE](./LICENSE) file for more details.

## 🙋‍♂️ Author

Built with ❤️ by [Jeevandeep Saini](https://github.com/jeevandeepsaini)  
For questions, suggestions, or collaborations — feel free to connect!
