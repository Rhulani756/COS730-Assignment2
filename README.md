# COS 730 -- Assignment 2
## Intelligent Submission and Review System


## Overview

This repository contains the full implementation for COS 730 Assignment 2: *From Behavioural Models to Optimised Implementation*. The project implements a peer-review submission pipeline in two phases:

- **Original/** -- Baseline implementation faithful to the provided sequence diagram (correctness only, no optimisations)
- **Optimised/** -- Refactored implementation addressing all design issues identified in Task 2, incorporating decision table logic from Task 3, and matching the redesigned sequence diagram from Task 4

---

## Repository Structure

```
COS730-Assignment2/
│
├── Original/                        # Task 1 -- Baseline implementation
│   ├── Database.java
│   ├── EvaluationManager.java
│   ├── ReviewerManager.java
│   ├── NotificationService.java
│   ├── Validator.java
│   ├── Reviewer.java
│   ├── Researcher.java
│   ├── SubmissionController.java
│   ├── UI.java
│   ├── Main.java
│   ├── TestRunner.java              # 11 test cases
│   ├── BenchmarkBaseline.java       # Task 6 metrics
│   ├── Original.jar
│   └── db/                         # Runtime persistence files
│       ├── submissions.txt
│       ├── scores.txt
│       └── reviewers.txt
│
├── Optimised/                       # Task 5 -- Optimised implementation
│   ├── Database.java
│   ├── EvaluationManager.java
│   ├── ReviewerManager.java
│   ├── NotificationService.java
│   ├── NotificationListener.java    # NEW -- interface for decoupling
│   ├── Validator.java
│   ├── Reviewer.java
│   ├── Researcher.java
│   ├── SubmissionController.java
│   ├── UI.java
│   ├── Main.java
│   ├── TestRunnerOptimised.java     # 15 test cases (11 + 4 optimisation-specific)
│   ├── BenchmarkOptimised.java      # Task 6 metrics
│   ├── Optimised.jar
│   └── db/
│
├── Optimised_Sequence_Diagram.drawio  # Task 4 -- Optimised sequence diagram
└── README.md
```

---

## Requirements

- Java JDK 21 or later
- No external dependencies

---

## Running the Applications

### Baseline

```bash
cd Original
java -jar Original.jar
```

Or compile and run from source:

```bash
cd Original
javac *.java
java Main
```

### Optimised

```bash
cd Optimised
java -jar Optimised.jar
```

Or compile and run from source:

```bash
cd Optimised
javac *.java
java Main
```

---

## Running the Tests

### Baseline -- 11 tests

```bash
cd Original
javac *.java
java TestRunner
```

Expected output: `11 passed | 0 failed | 11 total`

### Optimised -- 15 tests

```bash
cd Optimised
javac *.java
java TestRunnerOptimised
```

Expected output: `15 passed | 0 failed | 15 total`

Tests T1--T11 verify functional equivalence with the baseline.  
Tests T12--T15 verify the optimisation-specific structural improvements:

| Test | Verifies |
|------|----------|
| T12 | `ReviewerManager` returns `List<Reviewer>` (GRASP Creator fix) |
| T13 | `checkConsensus()` returns a boolean that is used (dead call eliminated) |
| T14 | `Researcher` implements `NotificationListener` (decoupling fix) |
| T15 | `assignReview()` and `submitReviewScore()` work in a single pass (loop merge) |

---

## Running the Benchmarks (Task 6)

### Baseline benchmark

```bash
cd Original
javac *.java
java BenchmarkBaseline
```

### Optimised benchmark

```bash
cd Optimised
javac *.java
java BenchmarkOptimised
```

Each benchmark measures:

1. Execution time (avg ms/submission, 10,000 runs, 200 warm-up iterations)
2. Throughput (submissions/second)
3. Lines of code per class
4. Method count per class
5. Cyclomatic complexity per class (CC = 1 + decision points)
6. Fan-out coupling per class (distinct type dependencies)

---

## Key Design Decisions

### Baseline (Original/)

- **Persistence:** `Database` uses real file I/O -- submissions written to `db/submissions.txt`, scores to `db/scores.txt`, reviewers seeded from `db/reviewers.txt`
- **Filtering:** `filterConflicts()` and `checkWorkload()` implemented as private methods in `ReviewerManager` -- required for correctness, not optimisation
- **Scoring:** each reviewer is assigned a random score in [0, 100]; thresholds set at 60 (accepted) and 40 (revision/rejected) so all three `[alt]` branches are reachable
- **Diagram fidelity:** all interactions from the sequence diagram are implemented exactly, including the redundant `calculateAverage()` call and the dual reviewer loop

### Optimised (Optimised/)

| Change | GRASP Principle | Effect |
|--------|----------------|--------|
| `NotificationListener` interface introduced | Low Coupling | `NotificationService` decoupled from `Researcher` |
| `ReviewerManager` returns `List<Reviewer>` | Creator | Controller no longer instantiates reviewers |
| `applyRules()` owns full evaluation pipeline | Information Expert | Redundant calls removed from controller |
| Score generated inside `Reviewer` | Information Expert | Responsibility placed with correct object |
| Single reviewer loop | Controller | Dual traversal eliminated |
| Named threshold constants | -- | Decision policy explicit and maintainable |

---

## Task 6 Benchmark Results (actual machine)

| Metric | Baseline | Optimised |
|--------|----------|-----------|
| Interactions/submission | 31 | 28 (-9.7%) |
| Avg execution time | 1.2493 ms | 1.2637 ms (~equal) |
| Throughput | 800/sec | 791/sec (~equal) |
| Total LOC | 244 | 264 (+20) |
| Total methods | 28 | 33 (+5) |
| Total cyclomatic complexity | 40 | 38 (-2) |
| Total WMC | 57 | 58 (+1) |
| SubmissionController fan-out | 9 | 7 (-2) |
| Dead interactions | 3 (9.7%) | 0 (0.0%) |
| Test-to-interaction ratio | 35.5% | 53.6% |

> Execution time is equivalent because file I/O dominates runtime and masks the savings from eliminating three in-memory interactions. The structural improvements are measurable in interaction count, complexity, and coupling.

---

## Sequence Diagram

The optimised sequence diagram (`Optimised_Sequence_Diagram.drawio`) can be opened at [app.diagrams.net](https://app.diagrams.net). It matches the style of the original baseline diagram and shows all structural changes from Task 4.
