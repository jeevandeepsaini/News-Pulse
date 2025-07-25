# News Pulse Backend

## 🚀 Overview
The **News-Pulse-Backend** is the core backend service powering the News Pulse Android app. Built with Python, it serves as an automated pipeline that aggregates news articles from leading sources, categorizes them using NLP, performs sentiment analysis with transformer models like RoBERTa, and stores the enriched content in Firebase Firestore. Designed for automation and scalability, the backend ensures that users receive real-time, relevant, and sentiment-aware news seamlessly delivered to the mobile frontend.

## ✨ Key Features
- **🌐 Automated News Aggregation:** Fetches top headlines and breaking news from trusted global sources via [NewsAPI](https://newsapi.org/).
- **🏷️ Keyword-Based Categorization:** Classifies articles into predefined categories (e.g., Technology, Business, Health, Sports) using keyword matching logic.
- **📊 Advanced Sentiment Analysis:** Uses pre-trained RoBERTa transformer models from HuggingFace for efficient and accurate sentiment scoring of news content.
- **🗃️ Firestore Integration:** Deduplicates and stores enriched news articles in **Firebase Firestore**, ensuring fast, structured, and scalable access for mobile clients.
- **⚙️ Scalable & Automated Pipeline:** Designed for seamless automation with scheduled jobs, enabling real-time updates without manual intervention.
- **🔐 Environment-Aware Configuration:** Automatically manages environment variables for smooth deployment across local development and CI environments (e.g., GitHub Actions).

## 📌 How It Works

1. **Environment Bootstrap:** Loads env vars (local or CI).
2. **Model Initialization:** Loads RoBERTa model/tokenizer from HuggingFace.
3. **News Fetching:** Uses NewsAPI, paginates results for up to 100 articles.
4. **Categorization:** Uses keyword mapping for dynamic categorization.
5. **Sentiment Analysis:** Applies RoBERTa to articles.
6. **Firestore Storage:** Deduplicates by URL, stores all relevant fields.

## 🖥️ Setup Instructions

1. Clone the Repository
   
   ```sh
   git clone https://github.com/jeevandeepsaini/News-Pulse.git
   cd News-Pulse/News-Pulse-Backend
   ```
2. Create and activate a virtual environment:

   #### **Recommended:** Python 3.8 or higher
   ```sh
   python -m venv .venv
   .venv\Scripts\activate  # On Windows
   # or
   source .venv/bin/activate  # On macOS/Linux
   ```
3. Install Dependencies

   ```sh
   pip install -r requirements.txt
   ```
   #### Core Dependencies
   - `torch`
   - `transformers`
   - `requests`
   - `firebase-admin`
   - `python-dotenv`
   - `pytz`

4. Firestore Setup

   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/).
   - Enable Firestore and download your service account key.

5. Environment Variables

   To manage sensitive configuration securely across **local development** and **GitHub Actions**, follow these steps:

   - #### 🔧 Local Development
      Create a `.env` file in `News-Pulse-Backend` with:

      ```env
      NEWS_API_KEY=your_newsapi_key_here
      FIREBASE_CREDENTIALS_PATH=News-Pulse-Backend/serviceAccountKey.json
      ```
      - Obtain your NewsAPI key from [NEWSAPI](https://newsapi.org/register).
      - Place your Firebase service account JSON at the specified path.
  
   - #### 🤖 GitHub Actions (CI/CD)
      Navigate to GitHub Repository **Settings** → **Secrets and variables** → **Actions** → **New repository secret.**
      Add the following secrets:
       - `NEWS_API_KEY`: Your NewsAPI key
       - `FIREBASE_JSON`: Your Firebase service account file contents, Base64-encoded.

7. Run the `news_pipeline.py`

   ```sh
   python news_pipeline.py
   ```
   - Fetches articles, analyzes, categorizes, and uploads to Firestore.
   - Output logs indicate progress and any skipped/duplicate items.

## 🧹 Firestore Data Cleanup

- To stay within **Firebase’s free tier**, the backend includes an automated cleanup mechanism.
- This function:
  - Deletes news articles that are **7 days or older** from Firestore.
  - Runs automatically via **GitHub Actions** on a regular schedule.
- This helps minimize Firestore read/write operations and keeps the database optimized.

## 🔒 Security & Best Practices

- **Never share secrets:** Keep API keys and Firebase credentials secure.
- **Use `.env` for local, secrets-injected for CI/CD.**
- **Validate NewsAPI responses:** Handle API errors and HTTP failures gracefully.
- **Deduplicate on storage:** Articles are stored with URLs as document IDs.
- **Exception Handling:** All critical I/O and API calls are wrapped with error handling.

## 💡 References

- [NewsAPI Docs](https://newsapi.org/docs)
- [Firebase Admin Python](https://firebase.google.com/docs/firestore/quickstart)
- [HuggingFace Transformers](https://huggingface.co/docs/transformers)
- [CardiffNLP/twitter-roberta-base-sentiment-latest](https://huggingface.co/cardiffnlp/twitter-roberta-base-sentiment-latest) – Pretrained sentiment analysis model used in this project.
