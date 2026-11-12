# Error Analysis — Sentiment Analysis

This document analyzes the misclassifications made by the trained BiLSTM model on the test partition, isolating 30 characteristic samples across False Positives (FP), False Negatives (FN), and Neutral Confusions.

---

## 1. Summary of Error Classifications

Errors typically cluster into three main categories:
1. **Negation Ambiguity**: Sentiment reverses over long dependencies, or complex phrasing (e.g., *"not as good as I hoped but not bad"*).
2. **Sarcasm/Indirect Language**: Reviews like *"Thanks for wasting my time"* containing positive words like *"Thanks"*.
3. **Implicit Ratings**: Reviews that describe technical glitches (negative sentiment) but are rated 3 or 4 stars by the user (neutral/positive).

---

## 2. Table of 30 Misclassified Samples

| ID | Sample Text | True Label | Predicted Label | Primary Failure Cause |
|---|---|---|---|---|
| 1 | "The course was not bad, but it wasn't great either." | NEUTRAL | POSITIVE | Negation polarity balancing |
| 2 | "I expected a lot more coding exercises. Very basic." | NEGATIVE | NEUTRAL | Implicit disappointment detection |
| 3 | "Thanks for nothing. Total waste of my money." | NEGATIVE | POSITIVE | Sarcasm ("Thanks") overrides negative keywords |
| 4 | "Good information but the audio quality was horrible." | NEUTRAL | NEGATIVE | Punctuation/audio details override positive |
| 5 | "Not as advanced as advertised, but okay for beginners." | NEUTRAL | POSITIVE | Level descriptor polarity |
| 6 | "I couldn't finish it because the lecturer speaks too slowly." | NEGATIVE | NEUTRAL | Action-based negative sentiment |
| 7 | "Brilliant structure, but the coding assignments are outdated." | NEUTRAL | POSITIVE | Outdated assignment details overlooked |
| 8 | "The first half was amazing, the second half was awful." | NEUTRAL | NEGATIVE | Sentence structure polarity split |
| 9 | "This is not a course for professional software engineers." | NEGATIVE | POSITIVE | Negative dependency scope |
| 10 | "Very helpful, but I don't think it is worth the price." | NEUTRAL | POSITIVE | Price value estimation |
| 11 | "Almost fell asleep during the slides. Very dry." | NEGATIVE | NEUTRAL | Metaphorical negative expression |
| 12 | "Good for what it is, just don't expect deep technical details." | NEUTRAL | POSITIVE | Conditional negative clause |
| 13 | "If you have zero programming experience, this is for you." | POSITIVE | NEGATIVE | "Zero experience" flagged as negative |
| 14 | "The instructor is clearly knowledgeable, but cannot teach." | NEGATIVE | POSITIVE | Conjunction contradiction ("but") |
| 15 | "I wish I had taken this course three years ago." | POSITIVE | NEGATIVE | "Wish" subjunctive clause confusion |
| 16 | "A bit too slow in the beginning but it gets better." | POSITIVE | NEUTRAL | Transition sentence structure |
| 17 | "No support in the Q&A section whatsoever." | NEGATIVE | NEUTRAL | Missing support class classification |
| 18 | "Excellent overview, though lacking deep hands-on labs." | NEUTRAL | POSITIVE | Contrast clause processing |
| 19 | "Great, if you like listening to monotone slides." | NEGATIVE | POSITIVE | Sarcastic conditional phrasing |
| 20 | "The audio makes it impossible to follow the lectures." | NEGATIVE | NEUTRAL | Adjective-noun dependency failure |
| 21 | "It was okay. Nothing special, but not terrible." | NEUTRAL | NEGATIVE | Double negation resolution |
| 22 | "Too much talking and not enough coding." | NEGATIVE | NEUTRAL | Quantitative comparison polarity |
| 23 | "Highly recommended for absolute beginners only." | POSITIVE | NEUTRAL | Contextual scoping ("only") |
| 24 | "Useful information, but very repetitive." | NEUTRAL | POSITIVE | "Repetitive" negative weight |
| 25 | "I have mixed feelings. Some parts are 5 stars, others are 1." | NEUTRAL | POSITIVE | Rating-based sentiment contradiction |
| 26 | "This course is a joke." | NEGATIVE | NEUTRAL | Colloquial metaphor |
| 27 | "Highly detailed, but extremely hard to follow." | NEUTRAL | POSITIVE | Complexity polarity |
| 28 | "I wanted to love this, but I just couldn't." | NEGATIVE | POSITIVE | Wish/reality mismatch |
| 29 | "It teaches things you can easily find on YouTube." | NEGATIVE | NEUTRAL | Implicit value comparison |
| 30 | "The code in the repository does not compile." | NEGATIVE | NEUTRAL | Context-specific technical issue |

---

## 3. General Patterns & Future Improvements
- **Subjunctive & Wish clauses**: Phrasings starting with *"I wish..."* or *"I expected..."* require a model that tracks subjective moods rather than local word presence.
- **Contrastive Conjunctions**: Contradictions separated by *"but"* or *"although"* often place the real sentiment at the end. Attention mechanisms could help the model focus on the resolving clause.
- **Domain Metaphors**: Phrasings like *"a joke"* or *"YouTube content"* require semantic understanding of the online learning domain.
