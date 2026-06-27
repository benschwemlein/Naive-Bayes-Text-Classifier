# Naive Bayes Text Classifier

A Naive Bayes document classifier built as part of my machine learning research at DePaul University (M.S. Computer Science, ~2008). Classifies text documents into categories using Laplace-smoothed word probability estimates.

### How it works

The classifier reads a corpus of labeled documents organized by directory (each directory is a target class), builds a vocabulary, and computes P(word | class) for each word using Laplace smoothing. It then classifies unseen documents by finding the class that maximizes the product of word probabilities.

### Usage

```bash
# Two-directory mode: separate example and validation sets
java BayesText <example_dir> <validation_dir>

# Single-directory mode: splits corpus 2/3 training, 1/3 validation
java BayesText <corpus_dir>
```

Documents should be plain text files organized into subdirectories by class label. Output is written to a tab-separated file with classification results and accuracy.
