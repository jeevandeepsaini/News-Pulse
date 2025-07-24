import os

if os.getenv("GITHUB_ACTIONS") == "true":
    print("Running in GitHub Actions. Environment variables are injected by GitHub.")
else:
    try:
        import dotenv
        dotenv.load_dotenv()
        print("Loaded environment variables from .env for local development.")
    except ImportError:
        print("python-dotenv not installed. Make sure to install it for local development.")

os.environ["HF_HUB_DISABLE_SYMLINKS_WARNING"] = "1"
os.environ["GRPC_VERBOSITY"] = "ERROR"
os.environ["GRPC_POLL_STRATEGY"] = "poll"

from datetime import datetime
import pytz
import torch
import requests
import firebase_admin
from firebase_admin import credentials, firestore
from transformers import AutoModelForSequenceClassification, AutoTokenizer, AutoConfig

# API Key
NEWS_API_KEY = os.getenv("NEWS_API_KEY")
firebase_key_path = os.getenv("FIREBASE_CREDENTIALS_PATH") or "Python-Backend/serviceAccountKey.json"

# Initialize Firebase
cred = credentials.Certificate(firebase_key_path)
firebase_admin.initialize_app(cred)

# Initialize Firebase Firestore
db = firestore.client()
print("Firestore connection successful!")

# Load model config, tokenizer, and model
MODEL_NAME = "cardiffnlp/twitter-roberta-base-sentiment-latest"
config = AutoConfig.from_pretrained(MODEL_NAME)
tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)
model = AutoModelForSequenceClassification.from_pretrained(MODEL_NAME)

# Auto-load labels from config
SENTIMENT_LABELS = [config.id2label[i] for i in range(len(config.id2label))]

NEWS_SOURCES = ",".join([
    "hindustan-times", "the-indian-express", "bbc-news", "cnn", "al-jazeera-english",
    "nbc-news", "abc-news", "the-guardian", "the-jerusalem-post", "news-com-au",
    "bloomberg", "cnbc", "wired", "engadget", "espn", "bbc-sport", "fox-sports",
    "medical-news-today", "science-daily"
])

# NewsAPI endpoint (fetching top news)
NEWS_API_URL = f"https://newsapi.org/v2/top-headlines"

# Define category mapping based on keywords
CATEGORY_KEYWORDS = {
    "Technology": [
        "tech", "software", "AI", "gadget", "computer", "innovation", "smartphone",
        "robotics", "cloud", "5G", "cybersecurity", "blockchain", "machine learning",
        "virtual reality", "augmented reality", "semiconductor", "quantum computing",
        "metaverse", "automation", "programming", "IoT", "autonomous", "wearable tech"
    ],
    "Business": [
        "market", "finance", "economy", "stock", "investment", "company", "startup",
        "banking", "cryptocurrency", "real estate", "trade", "entrepreneur",
        "merger", "acquisition", "corporate", "GDP", "inflation", "recession",
        "taxes", "unemployment", "venture capital", "interest rates", "Wall Street"
    ],
    "Sports": [
        "football", "soccer", "basketball", "tennis", "cricket", "olympics",
        "golf", "Formula 1", "NASCAR", "MMA", "boxing", "athletics", "badminton",
        "cycling", "baseball", "hockey", "rugby", "esports", "NBA", "NFL",
        "UEFA", "FIFA", "World Cup", "Premier League", "Wimbledon", "Super Bowl"
    ],
    "Health": [
        "medicine", "doctor", "virus", "covid", "vaccine", "fitness", "nutrition",
        "mental health", "yoga", "exercise", "healthcare", "hospital", "wellness",
        "diet", "therapy", "diabetes", "cancer", "depression", "stroke", "cardiology",
        "pharmaceutical", "public health", "pandemic", "genetics", "alternative medicine"
    ],
    "Entertainment": [
        "movie", "film", "celebrity", "music", "Hollywood", "Bollywood", "series",
        "Netflix", "HBO", "Disney", "Marvel", "DC", "Grammy", "Oscar", "theater",
        "Broadway", "K-pop", "anime", "manga", "TV show", "streaming", "concert",
        "festival", "YouTube", "influencer", "pop culture", "gaming", "comedy"
    ],
    "Science": [
        "physics", "biology", "chemistry", "research", "NASA", "discovery",
        "astronomy", "space", "genetics", "AI ethics", "climate", "ecology",
        "environment", "evolution", "neuroscience", "geology", "paleontology",
        "quantum physics", "black hole", "cosmology", "exoplanet", "DNA",
        "biotechnology", "meteorology", "scientific breakthrough"
    ],
    "Politics": [
        "government", "election", "policy", "law", "president", "congress",
        "parliament", "diplomacy", "sanctions", "treaty", "war", "foreign policy",
        "senate", "legislation", "democracy", "dictatorship", "coup", "protest",
        "national security", "immigration", "tax policy", "trade policy",
        "executive order", "human rights", "political party", "campaign"
    ],
    "Travel": [
        "tourism", "vacation", "airlines", "hotels", "beaches", "cruise", "adventure",
        "road trip", "backpacking", "sightseeing", "travel restrictions", "visa",
        "passport", "airfare", "luxury travel", "budget travel", "travel hacks",
        "destinations", "mountains", "national parks", "cultural heritage"
    ],
    "Education": [
        "school", "college", "university", "scholarship", "student", "teacher",
        "online learning", "e-learning", "degree", "curriculum", "STEM", "classroom",
        "homework", "exam", "higher education", "distance learning", "MOOC",
        "academic research", "internship", "career guidance", "tuition", "AI in education"
    ],
    "Automotive": [
        "car", "motorcycle", "electric vehicle", "EV", "self-driving", "hybrid",
        "fuel efficiency", "Tesla", "Ford", "BMW", "Mercedes", "road safety",
        "traffic", "auto industry", "car review", "racing", "supercar", "automobile",
        "truck", "SUV", "fuel prices", "car maintenance", "automotive technology"
    ],
    "General": []  # Default category
}

def get_category(title, description):
    text = (title or "") + " " + (description or "")
    text = text.lower()
    for category, keywords in CATEGORY_KEYWORDS.items():
        if any(keyword in text for keyword in keywords):
            return category
    return "General"

# -----------------------Sentiment Analysis Using RoBERTa-----------------------
def get_sentiment(text):
    if not text:
        return 0, "neutral"
    inputs = tokenizer(text, return_tensors="pt", max_length=512, truncation=True, padding=True)
    with torch.no_grad():
        output = model(**inputs)
    scores = torch.nn.functional.softmax(output.logits, dim=1)[0]
    sentiment_score = float(scores[2] - scores[0])  # Positive - Negative
    sentiment_label = SENTIMENT_LABELS[scores.argmax().item()]
    return sentiment_score, sentiment_label

def format_publication_date(iso_date):
    try:
        dt = datetime.fromisoformat(iso_date.replace("Z", ""))  # Convert to datetime object
        dt = dt.astimezone(pytz.timezone("Asia/Kolkata"))  # Convert to local timezone (IST)
        return dt.strftime("%d-%m-%Y, %I:%M %p")
    except Exception as e:
        print(f"Date formatting error: {e}")
        return iso_date  # Return original if error

def fetch_news():
    all_articles = []
    page = 1
    max_pages = 5

    while page <= max_pages:
        params = {
            "sources": NEWS_SOURCES,
            "apiKey": NEWS_API_KEY,
            "language": "en",
            "pageSize": 100,  # Increased from default (20) to 100
            "page": page
        }

        response = requests.get(NEWS_API_URL, params=params)
        if response.status_code == 200:
            data = response.json()
            if data["status"] == "ok":
                fetched_articles = data.get("articles", [])
                if not fetched_articles:
                    break  # Stop if there are no more articles

                all_articles.extend(fetched_articles)
                page += 1  # Go to the next page
            else:
                print("API Error:", data)
                break  # Stop fetching if API response is not OK
        else:
            print("Failed to fetch news! HTTP Status Code:", response.status_code)
            break  # Stop on request failure
    return all_articles

def store_articles_in_firestore(fetched_articles):
    collection_ref = db.collection("news")  # Firestore collection

    if not fetched_articles:
        print("No articles to store!")
        return

    for article in fetched_articles:
        title = article.get("title", "No Title")

        # Skip articles with the title "Google News"
        if title.lower() == "google news":
            print(f"Skipping unwanted article: {title}")
            continue

        summary = article.get("description", "")
        article_url = article.get("url", "")

        if not article_url:
            continue  # Skip articles without a URL

        # Use URL as the document ID to avoid duplicates
        doc_ref = collection_ref.document(article_url.replace("/", "_"))  # Replace slashes to prevent Firestore errors

        if doc_ref.get().exists:  # Corrected method call
            print(f"Skipping duplicate: {title}")
            continue  # Skip duplicate articles

        # Perform sentiment analysis
        sentiment_score, sentiment_label = get_sentiment(summary)

        # Assign category dynamically
        category = get_category(title, summary)

        # Format publication date
        formatted_date = format_publication_date(article.get("publishedAt", ""))

        # Prepare Firestore document
        doc_data = {
            "title": title,
            "publication_date": formatted_date,
            "summary": summary,
            "url": article_url,
            "imageurl": article.get("urlToImage", ""),
            "category": category,
            "sentiment_score": sentiment_score,
            "sentiment_label": sentiment_label
        }

        # Store in Firestore
        doc_ref.set(doc_data)

    print(f"{len(fetched_articles)} articles stored in Firestore!")


if __name__ == "__main__":
    print("Fetching news...")
    articles = fetch_news()

    if articles:
        print(f"{len(articles)} articles fetched successfully!")
        store_articles_in_firestore(articles)
    else:
        print("No articles fetched.")