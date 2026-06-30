"""
Naive Bayes Text Classifier

Usage:
  python naive_bayes.py <corpus_dir>
      Splits files 2/3 training, 1/3 validation (every 3rd file is validation).

  python naive_bayes.py <example_dir> <validation_dir>
      Uses separate directories for training and validation.

Each subdirectory under the corpus/example directory is treated as a class label.
"""

import sys
import os
import re
import math
from collections import defaultdict
from datetime import datetime


def get_files(directory):
    result = []
    for root, dirs, files in os.walk(directory):
        for f in files:
            result.append(os.path.join(root, f))
    return result


def tokenize(line):
    tokens = []
    for tok in line.lower().split():
        tok = re.sub(r'[^a-z]', '', tok)
        if tok:
            tokens.append(tok)
    return tokens


def train(example_files):
    # Map label -> file count and total word count
    label_file_count = defaultdict(int)
    label_word_count = defaultdict(int)

    # vocabulary: word -> {label -> count}
    vocab = defaultdict(lambda: defaultdict(int))
    word_total = defaultdict(int)

    for path in example_files:
        label = os.path.basename(os.path.dirname(path))
        label_file_count[label] += 1

        try:
            with open(path, encoding='utf-8', errors='replace') as fh:
                for line in fh:
                    for token in tokenize(line):
                        vocab[token][label] += 1
                        word_total[token] += 1
                        label_word_count[label] += 1
        except OSError:
            pass

    # Filter vocabulary when large: drop words with count < 3 and top 100 most frequent
    if len(vocab) > 1000:
        to_remove = [w for w, c in word_total.items() if c < 3]
        for w in to_remove:
            del vocab[w]
            del word_total[w]

        top100 = sorted(word_total, key=word_total.get, reverse=True)[:100]
        for w in top100:
            vocab.pop(w, None)
            word_total.pop(w, None)

    return label_file_count, label_word_count, vocab


def classify(doc_path, label_file_count, label_word_count, vocab, num_examples):
    vocab_size = len(vocab)
    scores = {}

    for label in label_file_count:
        p_v = label_file_count[label] / num_examples
        n = label_word_count[label]
        scores[label] = p_v  # start with prior; accumulate in log space below

    log_scores = {label: math.log(p_v) for label, p_v in scores.items()}

    try:
        with open(doc_path, encoding='utf-8', errors='replace') as fh:
            for line in fh:
                for token in tokenize(line):
                    if token not in vocab:
                        continue
                    for label in label_file_count:
                        n = label_word_count[label]
                        nk = vocab[token].get(label, 0)
                        p_wk_v = (nk + 1.0) / (n + vocab_size)
                        log_scores[label] += math.log(p_wk_v)
    except OSError:
        pass

    return max(log_scores, key=log_scores.get)


def main():
    if len(sys.argv) == 3:
        example_files = get_files(sys.argv[1])
        validation_files = get_files(sys.argv[2])
    elif len(sys.argv) == 2:
        all_files = get_files(sys.argv[1])
        example_files = [f for i, f in enumerate(all_files) if (i + 1) % 3 != 0]
        validation_files = [f for i, f in enumerate(all_files) if (i + 1) % 3 == 0]
    else:
        print("Usage: naive_bayes.py <corpus_dir>  OR  naive_bayes.py <example_dir> <validation_dir>")
        sys.exit(1)

    print(f"\nExample Set Size = {len(example_files)}")
    print(f"Validation Set Size = {len(validation_files)}")

    print("\nTraining...")
    label_file_count, label_word_count, vocab = train(example_files)
    num_examples = len(example_files)

    print(f"Labels: {sorted(label_file_count)}")
    print(f"Vocabulary size = {len(vocab)}")

    print("\nClassifying validation set...")
    correct = 0
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    outfile = f"bayestext_out_{timestamp}.txt"

    with open(outfile, 'w') as out:
        out.write("Document_Path\tClassification\tCorrect\n\n")
        for i, path in enumerate(validation_files):
            label = os.path.basename(os.path.dirname(path))
            predicted = classify(path, label_file_count, label_word_count, vocab, num_examples)

            if label not in label_file_count:
                result = 'n/a'
            elif predicted == label:
                result = 'T'
                correct += 1
            else:
                result = 'F'

            out.write(f"{path}\t{predicted}\t{result}\n")

            if len(validation_files) < 20 or (i + 1) % max(1, len(validation_files) // 20) == 0:
                print(f"\t{i + 1} of {len(validation_files)} documents classified.")

    accuracy = correct / len(validation_files) if validation_files else 0
    print(f"\nNumber correctly classified = {correct}")
    print(f"Accuracy = {round(accuracy * 100, 1)}%")
    print(f"Results written to {outfile}")


if __name__ == '__main__':
    main()
