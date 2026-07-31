import os
import time
import json
import joblib
import hashlib
import numpy as np
import pandas as pd
import tensorflow as tf
from sklearn.model_selection import train_test_split
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.svm import LinearSVC
from sklearn.calibration import CalibratedClassifierCV
from sklearn.preprocessing import LabelEncoder
from sklearn.metrics import classification_report, accuracy_score, f1_score

print("Starting course classification training pipeline...")

# Paths
raw_path = os.path.join("ml-training", "datasets", "raw", "udemy_courses.csv")
artifacts_dir = os.path.join("ml-training", "artifacts", "classification")
saved_models_dir = os.path.join("saved_models", "classification")

os.makedirs(artifacts_dir, exist_ok=True)
os.makedirs(saved_models_dir, exist_ok=True)

# 1. Load Data
df = pd.read_csv(raw_path)
# Clean and drop empty subjects or titles
df = df.dropna(subset=["course_title", "subject"])

# Normalize categories to what the backend expects
# The backend seeder has: Development, Business, Design, Marketing, IT & Software, etc.
# Udemy dataset has: Web Development, Business Finance, Musical Instruments, Graphic Design
# Let's map subjects:
# Web Development -> Development
# Business Finance -> Business
# Graphic Design -> Design
# Musical Instruments -> Personal Development
subject_mapping = {
    "Web Development": "Development",
    "Business Finance": "Business",
    "Graphic Design": "Design",
    "Musical Instruments": "Personal Development"
}
df["mapped_subject"] = df["subject"].map(subject_mapping)

# 2. Text Preprocessing
def clean_text(text):
    if not isinstance(text, str):
        return ""
    text = text.lower()
    import re
    text = re.sub(r"\s+", " ", text).strip()
    return text

df["clean_title"] = df["course_title"].apply(clean_text)
df = df[df["clean_title"] != ""]

# 3. Train/Val/Test Split (70/15/15)
train_df, test_df = train_test_split(df, test_size=0.30, random_state=42, stratify=df["mapped_subject"])
val_df, test_df = train_test_split(test_size_df := test_df, test_size=0.50, random_state=42, stratify=test_size_df["mapped_subject"])

print(f"Train size: {len(train_df)}, Val size: {len(val_df)}, Test size: {len(test_df)}")

# Label Encoder
le = LabelEncoder()
train_labels = le.fit_transform(train_df["mapped_subject"])
val_labels = le.transform(val_df["mapped_subject"])
test_labels = le.transform(test_df["mapped_subject"])

num_classes = len(le.classes_)

# 4. TF-IDF Fit strictly on training set
max_features = 5000
tfidf = TfidfVectorizer(ngram_range=(1, 2), max_features=max_features, min_df=2, max_df=0.9, stop_words="english")

train_features = tfidf.fit_transform(train_df["clean_title"]).toarray()
val_features = tfidf.transform(val_df["clean_title"]).toarray()
test_features = tfidf.transform(test_df["clean_title"]).toarray()

# Save Vectorizer and Label Encoder
joblib.dump(tfidf, os.path.join(saved_models_dir, "tfidf_vectorizer.joblib"))
joblib.dump(tfidf, os.path.join(artifacts_dir, "tfidf_vectorizer.joblib"))
joblib.dump(le, os.path.join(saved_models_dir, "label_encoder.joblib"))
joblib.dump(le, os.path.join(artifacts_dir, "label_encoder.joblib"))

# 5. Train Linear SVM
start_time = time.time()
base_svm = LinearSVC(C=1.0, random_state=42, dual=False)
svm = CalibratedClassifierCV(estimator=base_svm, cv=3)
svm.fit(train_features, train_labels)
svm_train_time = time.time() - start_time

# Predict SVM
svm_start_inf = time.time()
svm_preds = svm.predict(test_features)
svm_inf_latency = (time.time() - svm_start_inf) / len(test_features)
svm_acc = accuracy_score(test_labels, svm_preds)
svm_macro_f1 = f1_score(test_labels, svm_preds, average="macro")
svm_weighted_f1 = f1_score(test_labels, svm_preds, average="weighted")

# 6. Train MLP Model
input_dim = train_features.shape[1]

def build_mlp(input_dim, num_classes):
    model = tf.keras.Sequential([
        tf.keras.layers.Input(shape=(input_dim,)),
        tf.keras.layers.Dense(256, activation="relu"),
        tf.keras.layers.Dropout(0.3),
        tf.keras.layers.Dense(64, activation="relu"),
        tf.keras.layers.Dropout(0.3),
        tf.keras.layers.Dense(num_classes, activation="softmax")
    ])
    model.compile(optimizer="adam", loss="sparse_categorical_crossentropy", metrics=["accuracy"])
    return model

mlp = build_mlp(input_dim, num_classes)
mlp.summary()

# Train MLP
start_time = time.time()
callbacks = [
    tf.keras.callbacks.EarlyStopping(monitor="val_loss", patience=3, restore_best_weights=True)
]
history = mlp.fit(
    train_features, train_labels,
    validation_data=(val_features, val_labels),
    epochs=10,
    batch_size=64,
    callbacks=callbacks,
    verbose=1
)
mlp_train_time = time.time() - start_time

# Predict MLP
mlp_start_inf = time.time()
mlp_preds_probs = mlp.predict(test_features)
mlp_preds = np.argmax(mlp_preds_probs, axis=1)
mlp_inf_latency = (time.time() - mlp_start_inf) / len(test_features)
mlp_acc = accuracy_score(test_labels, mlp_preds)
mlp_macro_f1 = f1_score(test_labels, mlp_preds, average="macro")
mlp_weighted_f1 = f1_score(test_labels, mlp_preds, average="weighted")

print(f"SVM Accuracy: {svm_acc:.4f}, MLP Accuracy: {mlp_acc:.4f}")

# Compare Models
svm_report = classification_report(test_labels, svm_preds, target_names=le.classes_, output_dict=True)
mlp_report = classification_report(test_labels, mlp_preds, target_names=le.classes_, output_dict=True)

# Select Production Model (Best F1/Accuracy)
best_model_name = "SVM" if svm_macro_f1 >= mlp_macro_f1 else "MLP"
print(f"Production Model Chosen: {best_model_name}")

if best_model_name == "SVM":
    joblib.dump(svm, os.path.join(saved_models_dir, "model.joblib"))
    joblib.dump(svm, os.path.join(artifacts_dir, "model.joblib"))
    prod_model_path = os.path.join(artifacts_dir, "model.joblib")
else:
    mlp.save(os.path.join(saved_models_dir, "model.keras"))
    mlp.save(os.path.join(artifacts_dir, "model.keras"))
    prod_model_path = os.path.join(artifacts_dir, "model.keras")

# Write comparison CSV
comparison_df = pd.DataFrame({
    "Algorithm": ["SVM", "MLP"],
    "Accuracy": [svm_acc, mlp_acc],
    "Macro F1": [svm_macro_f1, mlp_macro_f1],
    "Weighted F1": [svm_weighted_f1, mlp_weighted_f1],
    "Training Time (s)": [svm_train_time, mlp_train_time],
    "Inference Latency (s)": [svm_inf_latency, mlp_inf_latency]
})
comparison_df.to_csv(os.path.join(artifacts_dir, "model_comparison.csv"), index=False)

# Trainable parameters count for MLP
mlp_param_count = int(np.sum([tf.keras.backend.count_params(w) for w in mlp.trainable_weights]))

# Save metadata
metadata = {
    "production_model_type": best_model_name,
    "num_classes": num_classes,
    "classes": list(le.classes_),
    "tfidf_max_features": max_features,
    "mlp_parameters": mlp_param_count,
    "svm_training_time_s": svm_train_time,
    "mlp_training_time_s": mlp_train_time,
    "training_timestamp": pd.Timestamp.now().isoformat()
}
with open(os.path.join(artifacts_dir, "metadata.json"), "w") as f:
    json.dump(metadata, f, indent=4)

# Save metrics
metrics = {
    "accuracy": svm_acc if best_model_name == "SVM" else mlp_acc,
    "macro_f1": svm_macro_f1 if best_model_name == "SVM" else mlp_macro_f1,
    "weighted_f1": svm_weighted_f1 if best_model_name == "SVM" else mlp_weighted_f1,
    "svm_report": svm_report,
    "mlp_report": mlp_report
}
with open(os.path.join(artifacts_dir, "metrics.json"), "w") as f:
    json.dump(metrics, f, indent=4)

# Calculate Checksums
def get_sha256(filepath):
    sha256_hash = hashlib.sha256()
    with open(filepath, "rb") as f:
        for byte_block in iter(lambda: f.read(4096), b""):
            sha256_hash.update(byte_block)
    return sha256_hash.hexdigest()

checksums = {}
model_file = "model.joblib" if best_model_name == "SVM" else "model.keras"
for filename in [model_file, "tfidf_vectorizer.joblib", "label_encoder.joblib", "metadata.json", "metrics.json"]:
    checksums[filename] = {
        "sha256": get_sha256(os.path.join(artifacts_dir, filename))
    }
with open(os.path.join(artifacts_dir, "checksums.json"), "w") as f:
    json.dump(checksums, f, indent=4)

print("Course classification pipeline training and export complete.")
