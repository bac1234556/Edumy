import os
import json
import joblib
import hashlib
import numpy as np
import pandas as pd
import tensorflow as tf
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, confusion_matrix, accuracy_score, f1_score

print("Starting sentiment training pipeline...")

# Paths
raw_path = os.path.join("ml-training", "datasets", "raw", "coursera_reviews.csv")
artifacts_dir = os.path.join("ml-training", "artifacts", "sentiment")
saved_models_dir = os.path.join("saved_models", "sentiment")

os.makedirs(artifacts_dir, exist_ok=True)
os.makedirs(saved_models_dir, exist_ok=True)

# 1. Load Data
df = pd.read_csv(raw_path)
df = df.dropna(subset=["reviewText", "Positive"])

# 2. Text Preprocessing
def clean_text(text):
    if not isinstance(text, str):
        return ""
    text = text.lower()
    # Simple HTML/URL stripping
    import re
    text = re.sub(r"<[^>]*>", "", text)
    text = re.sub(r"http\S+|www\S+|https\S+", "", text, flags=re.MULTILINE)
    text = re.sub(r"\s+", " ", text).strip()
    return text

df["clean_text"] = df["reviewText"].apply(clean_text)
df = df[df["clean_text"] != ""]

# 3. Train/Val/Test Split (70/15/15)
train_df, test_df = train_test_split(df, test_size=0.30, random_state=42, stratify=df["Positive"])
val_df, test_df = train_test_split(test_size_df := test_df, test_size=0.50, random_state=42, stratify=test_size_df["Positive"])

print(f"Train size: {len(train_df)}, Val size: {len(val_df)}, Test size: {len(test_df)}")

# 4. Tokenization & Padding
vocab_size = 10000
max_len = 100
embedding_dim = 64
lstm_units = 64
dropout_rate = 0.2

tokenizer = tf.keras.preprocessing.text.Tokenizer(num_words=vocab_size, oov_token="<OOV>")
tokenizer.fit_on_texts(train_df["clean_text"])

train_seq = tokenizer.texts_to_sequences(train_df["clean_text"])
val_seq = tokenizer.texts_to_sequences(val_df["clean_text"])
test_seq = tokenizer.texts_to_sequences(test_df["clean_text"])

train_pad = tf.keras.preprocessing.sequence.pad_sequences(train_seq, maxlen=max_len, padding="post", truncating="post")
val_pad = tf.keras.preprocessing.sequence.pad_sequences(val_seq, maxlen=max_len, padding="post", truncating="post")
test_pad = tf.keras.preprocessing.sequence.pad_sequences(test_seq, maxlen=max_len, padding="post", truncating="post")

train_labels = train_df["Positive"].values
val_labels = val_df["Positive"].values
test_labels = test_df["Positive"].values

# Save Tokenizer
joblib.dump(tokenizer, os.path.join(saved_models_dir, "tokenizer.joblib"))
joblib.dump(tokenizer, os.path.join(artifacts_dir, "tokenizer.joblib"))

# 5. Build BiLSTM Model
def build_bilstm(vocab_size, embedding_dim, lstm_units, max_len):
    model = tf.keras.Sequential([
        tf.keras.layers.Embedding(vocab_size, embedding_dim, input_length=max_len, name="embedding"),
        tf.keras.layers.Bidirectional(tf.keras.layers.LSTM(lstm_units, dropout=0.2, recurrent_dropout=0.0)),
        tf.keras.layers.Dense(64, activation="relu"),
        tf.keras.layers.Dropout(0.5),
        tf.keras.layers.Dense(1, activation="sigmoid")
    ])
    model.compile(optimizer="adam", loss="binary_crossentropy", metrics=["accuracy"])
    return model

model = build_bilstm(vocab_size, embedding_dim, lstm_units, max_len)
model.summary()

# Train Model
callbacks = [
    tf.keras.callbacks.EarlyStopping(monitor="val_loss", patience=2, restore_best_weights=True),
    tf.keras.callbacks.ReduceLROnPlateau(monitor="val_loss", factor=0.5, patience=1)
]

history = model.fit(
    train_pad, train_labels,
    validation_data=(val_pad, val_labels),
    epochs=5,
    batch_size=128,
    callbacks=callbacks
)

# Export Model
model.save(os.path.join(saved_models_dir, "model.keras"))
model.save(os.path.join(artifacts_dir, "model.keras"))
print("Model saved successfully.")

# 6. Evaluate
preds = model.predict(test_pad).flatten()
pred_labels = (preds >= 0.5).astype(int)

acc = accuracy_score(test_labels, pred_labels)
macro_f1 = f1_score(test_labels, pred_labels, average="macro")
weighted_f1 = f1_score(test_labels, pred_labels, average="weighted")

print(f"Accuracy: {acc:.4f}, Macro F1: {macro_f1:.4f}, Weighted F1: {weighted_f1:.4f}")

report = classification_report(test_labels, pred_labels, output_dict=True)
cm = confusion_matrix(test_labels, pred_labels).tolist()

# Trainable parameters count
trainable_count = int(np.sum([tf.keras.backend.count_params(w) for w in model.trainable_weights]))

# Write metadata
metadata = {
    "model_type": "BiLSTM",
    "vocabulary_size": vocab_size,
    "sequence_length": max_len,
    "embedding_dimension": embedding_dim,
    "bilstm_units": lstm_units,
    "dropout": dropout_rate,
    "output_classes": 2,
    "loss": "binary_crossentropy",
    "optimizer": "adam",
    "epochs": len(history.epoch),
    "best_epoch": int(np.argmin(history.history["val_loss"]) + 1),
    "trainable_parameters": trainable_count
}

with open(os.path.join(artifacts_dir, "metadata.json"), "w") as f:
    json.dump(metadata, f, indent=4)

# Write metrics
metrics = {
    "accuracy": acc,
    "macro_f1": macro_f1,
    "weighted_f1": weighted_f1,
    "classification_report": report,
    "confusion_matrix": cm
}

with open(os.path.join(artifacts_dir, "metrics.json"), "w") as f:
    json.dump(metrics, f, indent=4)

# Config
config = {
    "vocab_size": vocab_size,
    "max_len": max_len,
    "embedding_dim": embedding_dim,
    "lstm_units": lstm_units,
    "dropout_rate": dropout_rate
}
with open(os.path.join(artifacts_dir, "training_config.json"), "w") as f:
    json.dump(config, f, indent=4)

# Calculate Checksums
def get_sha256(filepath):
    sha256_hash = hashlib.sha256()
    with open(filepath, "rb") as f:
        for byte_block in iter(lambda: f.read(4096), b""):
            sha256_hash.update(byte_block)
    return sha256_hash.hexdigest()

checksums = {}
for filename in ["model.keras", "tokenizer.joblib", "metadata.json", "metrics.json", "training_config.json"]:
    checksums[filename] = {
        "sha256": get_sha256(os.path.join(artifacts_dir, filename))
    }
with open(os.path.join(artifacts_dir, "checksums.json"), "w") as f:
    json.dump(checksums, f, indent=4)

print("Sentiment pipeline training and export complete.")
