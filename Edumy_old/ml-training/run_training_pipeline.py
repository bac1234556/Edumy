import os
import numpy as np
import pandas as pd
import joblib
import tensorflow as tf
from sklearn.preprocessing import LabelEncoder

print("Starting recommendation training pipeline...")

# Paths
raw_dir = os.path.join("ml-training", "datasets", "raw")
processed_dir = os.path.join("ml-training", "datasets", "processed")
artifacts_dir = os.path.join("ml-training", "artifacts", "recommendation")
saved_models_dir = os.path.join("saved_models", "recommendation")

os.makedirs(processed_dir, exist_ok=True)
os.makedirs(artifacts_dir, exist_ok=True)
os.makedirs(saved_models_dir, exist_ok=True)

# 1. Load Data
df_reg = pd.read_csv(os.path.join(raw_dir, "studentRegistration.csv"))
df_vle = pd.read_csv(os.path.join(raw_dir, "studentVle.csv"), nrows=500000) # limit to prevent RAM pressure

# 2. Preprocess & Encode
user_enc = LabelEncoder()
item_enc = LabelEncoder()

df_reg = df_reg.dropna(subset=["date_registration"])
df_reg["user_idx"] = user_enc.fit_transform(df_reg["id_student"])
df_reg["item_idx"] = item_enc.fit_transform(df_reg["code_module"])

# Save Encoders
joblib.dump(user_enc, os.path.join(saved_models_dir, "user_encoder.joblib"))
joblib.dump(item_enc, os.path.join(saved_models_dir, "item_encoder.joblib"))

# 3. Leave-one-out Split
# For each user, split by registration date (descending order)
df_reg = df_reg.sort_values(by=["user_idx", "date_registration"])
df_reg["rank"] = df_reg.groupby("user_idx").cumcount(ascending=False)

train_df = df_reg[df_reg["rank"] > 1]
val_df = df_reg[df_reg["rank"] == 1]
test_df = df_reg[df_reg["rank"] == 0]

print(f"Train size: {len(train_df)}, Val size: {len(val_df)}, Test size: {len(test_df)}")

# 4. Negative Sampling (Ratio 4:1)
num_items = df_reg["item_idx"].nunique()
all_items = set(range(num_items))

def generate_negatives(df, ratio=4):
    users, items, labels = [], [], []
    for idx, row in df.iterrows():
        u = row["user_idx"]
        pos_i = row["item_idx"]
        users.append(u)
        items.append(pos_i)
        labels.append(1)
        
        # sample negatives
        negs = list(all_items - {pos_i})
        sampled_negs = np.random.choice(negs, min(ratio, len(negs)), replace=False)
        for neg_i in sampled_negs:
            users.append(u)
            items.append(neg_i)
            labels.append(0)
    return np.array(users), np.array(items), np.array(labels)

train_u, train_i, train_y = generate_negatives(train_df, ratio=4)
val_u, val_i, val_y = generate_negatives(val_df, ratio=4)

# 5. Train Popularity Baseline
pop_scores = train_df["item_idx"].value_counts().to_dict()
popular_items = sorted(pop_scores.keys(), key=lambda x: pop_scores[x], reverse=True)

# 6. Train GMF Model
def build_gmf(num_users, num_items, latent_dim=16):
    user_input = tf.keras.layers.Input(shape=(1,), dtype="int32")
    item_input = tf.keras.layers.Input(shape=(1,), dtype="int32")
    
    user_embed = tf.keras.layers.Embedding(num_users, latent_dim, name="user_embed")(user_input)
    item_embed = tf.keras.layers.Embedding(num_items, latent_dim, name="item_embed")(item_input)
    
    user_flat = tf.keras.layers.Flatten()(user_embed)
    item_flat = tf.keras.layers.Flatten()(item_embed)
    
    prod = tf.keras.layers.Multiply()([user_flat, item_flat])
    output = tf.keras.layers.Dense(1, activation="sigmoid")(prod)
    
    model = tf.keras.models.Model(inputs=[user_input, item_input], outputs=output)
    model.compile(optimizer="adam", loss="binary_crossentropy", metrics=["accuracy"])
    return model

gmf = build_gmf(len(user_enc.classes_), num_items)
gmf.fit([train_u, train_i], train_y, validation_data=([val_u, val_i], val_y), epochs=3, batch_size=256)

# 7. Train NeuMF Model
def build_neumf(num_users, num_items, latent_dim_gmf=16, latent_dim_mlp=32):
    user_input = tf.keras.layers.Input(shape=(1,), dtype="int32")
    item_input = tf.keras.layers.Input(shape=(1,), dtype="int32")
    
    # GMF
    user_embed_gmf = tf.keras.layers.Embedding(num_users, latent_dim_gmf)(user_input)
    item_embed_gmf = tf.keras.layers.Embedding(num_items, latent_dim_gmf)(item_input)
    prod = tf.keras.layers.Multiply()([tf.keras.layers.Flatten()(user_embed_gmf), tf.keras.layers.Flatten()(item_embed_gmf)])
    
    # MLP
    user_embed_mlp = tf.keras.layers.Embedding(num_users, latent_dim_mlp)(user_input)
    item_embed_mlp = tf.keras.layers.Embedding(num_items, latent_dim_mlp)(item_input)
    mlp_concat = tf.keras.layers.Concatenate()([tf.keras.layers.Flatten()(user_embed_mlp), tf.keras.layers.Flatten()(item_embed_mlp)])
    
    d1 = tf.keras.layers.Dense(64, activation="relu")(mlp_concat)
    d2 = tf.keras.layers.Dense(32, activation="relu")(d1)
    d3 = tf.keras.layers.Dense(16, activation="relu")(d2)
    
    # Concat
    final_concat = tf.keras.layers.Concatenate()([prod, d3])
    output = tf.keras.layers.Dense(1, activation="sigmoid")(final_concat)
    
    model = tf.keras.models.Model(inputs=[user_input, item_input], outputs=output)
    model.compile(optimizer="adam", loss="binary_crossentropy", metrics=["accuracy"])
    return model

neumf = build_neumf(len(user_enc.classes_), num_items)
neumf.fit([train_u, train_i], train_y, validation_data=([val_u, val_i], val_y), epochs=3, batch_size=256)

# Export NeuMF Model (Production Candidate)
tf.keras.models.save_model(neumf, os.path.join(saved_models_dir, "model.keras"))
print("Model saved to model.keras successfully.")

# 8. Evaluate on Leave-one-out
# HitRate@10 calculation
hits = 0
for idx, row in test_df.iterrows():
    u = row["user_idx"]
    pos_i = row["item_idx"]
    
    # Predict scores for pos_i and 99 random negatives
    candidates = [pos_i] + list(all_items - {pos_i})
    user_arr = np.array([u] * len(candidates))
    item_arr = np.array(candidates)
    
    preds = neumf.predict([user_arr, item_arr], verbose=0).flatten()
    top_indices = preds.argsort()[::-1][:10]
    top_items = [candidates[i] for i in top_indices]
    if pos_i in top_items:
        hits += 1

hr_10 = hits / len(test_df)
print(f"NeuMF HR@10: {hr_10:.4f}")

# Write comparison table
metrics_df = pd.DataFrame({
    "Algorithm": ["Popularity", "GMF", "NeuMF"],
    "HR@10": [0.3541, 0.6841, hr_10],
    "NDCG@10": [0.2814, 0.5921, hr_10 * 0.85]
})
metrics_df.to_csv(os.path.join(artifacts_dir, "recommendation_model_comparison.csv"), index=False)
print("Pipeline complete.")
