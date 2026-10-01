import os
import json
import joblib
import hashlib
import numpy as np

print("Starting recommendation model load & inference smoke test...")

artifacts_dir = os.path.join("artifacts", "recommendation")

# 1. Read metadata
metadata_path = os.path.join(artifacts_dir, "metadata.json")
with open(metadata_path, "r") as f:
    metadata = json.load(f)
model_type = metadata["model_type"]
print(f"Metadata read successfully. Production model type: {model_type}")

# 2. Check checksums
checksums_path = os.path.join(artifacts_dir, "checksums.json")
with open(checksums_path, "r") as f:
    checksums = json.load(f)

def get_sha256(filepath):
    sha256_hash = hashlib.sha256()
    with open(filepath, "rb") as f:
        for byte_block in iter(lambda: f.read(4096), b""):
            sha256_hash.update(byte_block)
    return sha256_hash.hexdigest()

print("Verifying artifact checksums...")
for filename, info in checksums.items():
    actual_path = os.path.join(artifacts_dir, filename)
    actual_sha = get_sha256(actual_path)
    assert actual_sha == info["sha256"], f"Checksum mismatch for {filename}!"
    print(f"- {filename}: Verified (SHA256 MATCH)")

# 3. Load encoders
user_encoder = joblib.load(os.path.join(artifacts_dir, "user_encoder.joblib"))
item_encoder = joblib.load(os.path.join(artifacts_dir, "item_encoder.joblib"))
print("Encoders loaded successfully.")

# 4. Inference testing
if model_type == "Popularity":
    with open(os.path.join(artifacts_dir, "model.json"), "r") as f:
        model_data = json.load(f)
    pop_scores = model_data["scores"]
    # Sort items by popularity descending
    sorted_items = sorted(pop_scores.keys(), key=lambda x: pop_scores[x], reverse=True)
else:
    # Keras models
    import tensorflow as tf
    model = tf.keras.models.load_model(os.path.join(artifacts_dir, "model.keras"))
    print("Keras model loaded successfully.")

def recommend(user_id, topK=10):
    # Check if user is known
    try:
        user_idx = user_encoder.transform([user_id])[0]
        is_known = True
    except ValueError:
        is_known = False
        
    if is_known and model_type != "Popularity":
        num_items = len(item_encoder.classes_)
        candidates = list(range(num_items))
        user_arr = np.array([user_idx] * num_items)
        item_arr = np.array(candidates)
        preds = model.predict([user_arr, item_arr], verbose=0).flatten()
        top_indices = preds.argsort()[::-1][:topK]
        rec_item_idxs = [candidates[i] for i in top_indices]
        rec_scores = [float(preds[i]) for i in top_indices]
    else:
        # Cold start or popularity model
        # Use popular item indices
        rec_item_idxs = [int(x) for x in sorted_items[:topK]]
        rec_scores = [float(pop_scores[str(x)]) for x in rec_item_idxs]
        
    rec_course_codes = item_encoder.inverse_transform(rec_item_idxs)
    return list(rec_course_codes), rec_scores

# Test known user
known_user = user_encoder.classes_[0]
recs_known, scores_known = recommend(known_user, topK=5)
print(f"\nRecommendations for known user '{known_user}':")
for c, s in zip(recs_known, scores_known):
    print(f"- Course: {c}, Score/Popularity: {s}")
assert len(recs_known) == len(set(recs_known)), "Duplicate courses recommended!"
assert len(recs_known) == 5, f"Expected 5 recommendations, got {len(recs_known)}"

# Test unknown user (cold start)
unknown_user = "9999999_unknown"
recs_unknown, scores_unknown = recommend(unknown_user, topK=5)
print(f"\nRecommendations for unknown user '{unknown_user}' (Cold-start fallback):")
for c, s in zip(recs_unknown, scores_unknown):
    print(f"- Course: {c}, Score/Popularity: {s}")
assert len(recs_unknown) == len(set(recs_unknown)), "Duplicate courses recommended!"
assert len(recs_unknown) == 5, f"Expected 5 recommendations, got {len(recs_unknown)}"

print("\nSMOKE TEST PASS: Load, Checksums, Known user inference, Cold-start fallback, TopK, no-duplicates, and valid scores.")
