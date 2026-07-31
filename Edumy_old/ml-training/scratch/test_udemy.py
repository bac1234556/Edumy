import pandas as pd
df = pd.read_csv("ml-training/datasets/raw/udemy_courses.csv")
print("Columns:", df.columns)
print("Subjects:", df["subject"].value_counts())
