import urllib.request
import pandas as pd

url = "https://raw.githubusercontent.com/pycaret/pycaret/master/datasets/amazon.csv"
try:
    urllib.request.urlretrieve(url, "amazon_test.csv")
    df = pd.read_csv("amazon_test.csv")
    print("Columns:", df.columns)
    print("Shape:", df.shape)
    print(df.head())
except Exception as e:
    print("Error:", e)
