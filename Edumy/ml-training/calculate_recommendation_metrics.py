import os
import json
import joblib
import numpy as np
import pandas as pd
import tensorflow as tf

print("Calculating non-trivial recommendation metrics using batched prediction...")

# Paths
processed_dir = os.path.join("ml-training", "datasets", "processed")
rec_artifacts_dir = os.path.join("ml-training", "artifacts", "recommendation")
saved_models_dir = os.path.join("saved_models", "recommendation")

# Load model, encoders
model_keras_path = os.path.join(saved_models_dir, "model.keras")
user_enc_path = os.path.join(saved_models_dir, "user_encoder.joblib")
item_enc_path = os.path.join(saved_models_dir, "item_encoder.joblib")

if not os.path.exists(model_keras_path):
    model_keras_path = os.path.join(rec_artifacts_dir, "neumf_model.keras")
    user_enc_path = os.path.join(rec_artifacts_dir, "user_encoder.joblib")
    item_enc_path = os.path.join(rec_artifacts_dir, "item_encoder.joblib")

model = tf.keras.models.load_model(model_keras_path)
user_enc = joblib.load(user_enc_path)
item_enc = joblib.load(item_enc_path)

# Load test data
test_csv_path = os.path.join(processed_dir, "test.csv")
if not os.path.exists(test_csv_path):
    print("test.csv not found, rebuilding split from raw data...")
    raw_dir = os.path.join("ml-training", "datasets", "raw")
    df_reg = pd.read_csv(os.path.join(raw_dir, "studentRegistration.csv"))
    df_reg = df_reg.dropna(subset=["date_registration"])
    df_reg["user_idx"] = user_enc.transform(df_reg["id_student"])
    df_reg["item_idx"] = item_enc.transform(df_reg["code_module"])
    df_reg = df_reg.sort_values(by=["user_idx", "date_registration"])
    df_reg["rank"] = df_reg.groupby("user_idx").cumcount(ascending=False)
    test_df = df_reg[df_reg["rank"] == 0]
else:
    test_df = pd.read_csv(test_csv_path)

num_items = len(item_enc.classes_)

# Build batch inputs for all test users and all candidate items
users = test_df["user_idx"].values
pos_items = test_df["item_idx"].values

num_users = len(users)
# Candidates for all users: shape (num_users * num_items)
batch_users = np.repeat(users, num_items)
batch_items = np.tile(np.arange(num_items), num_users)

print(f"Predicting scores for {num_users} users x {num_items} items = {len(batch_users)} predictions...")
preds = model.predict([batch_users, batch_items], batch_size=4096, verbose=1).flatten()

# Reshape predictions to (num_users, num_items)
preds_matrix = preds.reshape(num_users, num_items)

hits_1 = 0
hits_3 = 0
ndcg_1 = 0.0
ndcg_3 = 0.0
precisions_3 = []
recalls_3 = []
mrrs = []
recommended_items = set()

for u_idx in range(num_users):
    pos_i = pos_items[u_idx]
    user_preds = preds_matrix[u_idx]
    
    # Get top items
    top_indices = user_preds.argsort()[::-1]
    
    # Rel is 1 if positive item is in top-3
    for item in top_indices[:3]:
        recommended_items.add(item)
        
    if pos_i == top_indices[0]:
        hits_1 += 1
    if pos_i in top_indices[:3]:
        hits_3 += 1
        
    if pos_i == top_indices[0]:
        ndcg_1 += 1.0
    if pos_i in top_indices[:3]:
        rank = list(top_indices).index(pos_i)
        ndcg_3 += 1.0 / np.log2(rank + 2)
        
    rel = 1 if pos_i in top_indices[:3] else 0
    precisions_3.append(rel / 3.0)
    recalls_3.append(rel / 1.0)
    
    if pos_i in top_indices:
        rank = list(top_indices).index(pos_i)
        mrrs.append(1.0 / (rank + 1))
    else:
        mrrs.append(0.0)

# Calculate final metrics
hr_1 = hits_1 / num_users
hr_3 = hits_3 / num_users
ndcg_1_val = ndcg_1 / num_users
ndcg_3_val = ndcg_3 / num_users
precision_3_val = np.mean(precisions_3)
recall_3_val = np.mean(recalls_3)
mrr_val = np.mean(mrrs)
coverage_val = len(recommended_items) / num_items

print(f"HR@1: {hr_1:.4f}")
print(f"HR@3: {hr_3:.4f}")
print(f"NDCG@1: {ndcg_1_val:.4f}")
print(f"NDCG@3: {ndcg_3_val:.4f}")
print(f"Precision@3: {precision_3_val:.4f}")
print(f"Recall@3: {recall_3_val:.4f}")
print(f"MRR: {mrr_val:.4f}")
print(f"Coverage: {coverage_val:.4f}")

# Save to recommendation_metrics_non_trivial.json
non_trivial_metrics = {
    "HitRate@1": hr_1,
    "HitRate@3": hr_3,
    "NDCG@1": ndcg_1_val,
    "NDCG@3": ndcg_3_val,
    "Precision@3": precision_3_val,
    "Recall@3": recall_3_val,
    "MRR": mrr_val,
    "Coverage": coverage_val,
    "total_users": num_users,
    "catalog_size": num_items
}

with open(os.path.join(rec_artifacts_dir, "recommendation_metrics_non_trivial.json"), "w") as f:
    json.dump(non_trivial_metrics, f, indent=4)

print("Non-trivial recommendation metrics calculation complete.")
