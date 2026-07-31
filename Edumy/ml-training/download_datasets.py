import os
import urllib.request

print("Downloading datasets...")

raw_dir = os.path.join("ml-training", "datasets", "raw")
os.makedirs(raw_dir, exist_ok=True)

# 1. Download Udemy Courses Dataset for classification
udemy_url = "https://raw.githubusercontent.com/nnaemeka-git/payment/main/udemy_courses.csv"
udemy_path = os.path.join(raw_dir, "udemy_courses.csv")
print(f"Downloading Udemy courses from {udemy_url}...")
try:
    urllib.request.urlretrieve(udemy_url, udemy_path)
    print(f"Saved Udemy courses to {udemy_path} (size: {os.path.getsize(udemy_path)} bytes)")
except Exception as e:
    print(f"Error downloading Udemy courses: {e}")

# 2. Download a smaller reviews dataset for Sentiment Analysis
# We can download a public review dataset with columns 'Review' and 'Label' or similar,
# or a cleaned Coursera reviews dataset sample.
# Let's download a standard review dataset from a public repo:
sentiment_url = "https://raw.githubusercontent.com/sharmaroshan/Coursera-Reviews-Analysis/master/Coursera_reviews.csv"
# Since Coursera_reviews.csv can be large, if it fails, we fall back to a smaller one or handle it.
sentiment_path = os.path.join(raw_dir, "coursera_reviews.csv")
print(f"Downloading Coursera reviews from {sentiment_url}...")
try:
    urllib.request.urlretrieve(sentiment_url, sentiment_path)
    print(f"Saved Coursera reviews to {sentiment_path} (size: {os.path.getsize(sentiment_path)} bytes)")
except Exception as e:
    print(f"Error downloading Coursera reviews: {e}")
    # Fallback to a smaller dataset from another repo
    fallback_url = "https://raw.githubusercontent.com/anujvyas/Movie-Review-Sentiment-Analysis/master/movie_reviews.csv"
    print(f"Trying fallback movie reviews dataset from {fallback_url}...")
    try:
        urllib.request.urlretrieve(fallback_url, sentiment_path)
        print(f"Saved fallback reviews to {sentiment_path} (size: {os.path.getsize(sentiment_path)} bytes)")
    except Exception as ex:
        print(f"Error downloading fallback reviews: {ex}")

print("Dataset download process finished.")
