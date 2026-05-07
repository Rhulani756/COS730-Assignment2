# COS 730 – Assignment 2: Intelligent Submission and Review System

**Student:** [Rhulani Matiane u23875616]  
**Due:** 14 May 2026  
**Module:** COS 730 – Software Engineering

---

## Repository Structure

```
/
├── Original/        ← Task 1: Baseline implementation (this folder)
│   ├── *.java       ← All source files
│   ├── db/          ← Auto-generated on first run (persistence files)
│   └── README.md    ← You are here
│
└── Optimised/       ← Tasks 4 & 5: Optimised design and implementation
    ├── *.java
    └── README.md
```

---

## Task 1 – Baseline Implementation

This folder contains an implementation of the provided sequence diagram for the **Intelligent Submission and Review System**. No optimisations have been introduced and all classes, interactions, and responsibilities match the diagram exactly.

### Class Responsibilities

| Class | Sequence Diagram Lifeline | Responsibility |
|---|---|---|
| `UI` | UI | Swing GUI — entry point for the Researcher; delegates to SubmissionController |
| `SubmissionController` | SubmissionController | Orchestrates the full submission pipeline |
| `Validator` | Validator | Validates submission data format |
| `Database` | Database | File-based persistence for submissions, scores, and reviewers |
| `ReviewerManager` | ReviewerManager | Fetches and filters the reviewer list |
| `Reviewer` | Reviewer | Represents an individual reviewer; submits scores |
| `EvaluationManager` | EvaluationManager | Aggregates scores, checks consensus, applies outcome rules |
| `NotificationService` | NotificationService | Sends acceptance/rejection/revision notifications |
| `Researcher` | Researcher | Receives final notification of outcome |

### Persistence

The `Database` class provides **real file-based persistence** using Java built-in I/O only (no external libraries). A `db/` directory is created automatically on first run containing:

| File | Contents |
|---|---|
| `db/reviewers.txt` | Reviewer names, one per line — edit to add/remove reviewers |
| `db/submissions.txt` | Full record of every submission with timestamp and generated ID |
| `db/scores.txt` | Timestamped score entry for every review submitted |

All files are plain text and human-readable — open them directly to inspect persisted data.

---

## How to Run

### Prerequisites

- **Java 11 or later** — check with `java -version`
- If not installed:
  - **Windows:** `winget install EclipseAdoptium.Temurin.21.JDK`
  - **macOS:** `brew install openjdk`
  - **Ubuntu/Debian:** `sudo apt install default-jdk`

### Option A — Run the JAR directly (recommended, no compilation needed)

```bash
# 1. Clone the repository
git clone <your-repo-url>

# 2. Navigate to the Original folder
cd Original

# 3. Run
java -jar Original.jar
```

### Option B — Compile from source

```bash
# 1. Clone the repository
git clone <your-repo-url>

# 2. Navigate to the Original folder
cd Original

# 3. Compile all source files
javac *.java

# 4. Run
java UI
```

### Using the GUI

1. Fill in **Paper Title** and **Primary Author** (both required)
2. Optionally select a research category, add an abstract and keywords
3. Click **Submit Artefact**
4. Watch the **System Activity Log** panel on the right — every step of the sequence diagram pipeline is logged in real time
5. A dialog confirms the outcome (accepted / rejected / revision)
6. Check the `db/` folder to see the persisted submission and scores

**To test the invalid path:** leave Title and Author blank and click Submit — the validation error path fires and no data is saved.

**To add/remove reviewers:** edit `db/reviewers.txt` (one name per line) and restart.

---

## Sequence Diagram Traceability

Every interaction in the provided sequence diagram maps directly to a method call in the code. Comments in each file reference the corresponding diagram element.

| Diagram Interaction | Code Location |
|---|---|
| `submitResearchOutput(data)` | `UI.handleSubmit()` |
| `submit(data)` | `SubmissionController.submit()` |
| `validateFormat(data)` | `Validator.validateFormat()` |
| `[alt: invalid] return error` | `UI.returnError()` |
| `saveSubmission(data) → confirmation` | `Database.saveSubmission()` |
| `getAvailableReviewers()` | `ReviewerManager.getAvailableReviewers()` |
| `fetchReviewers()` | `Database.fetchReviewers()` |
| `filterConflicts(reviewerList)` | `ReviewerManager.filterConflicts()` *(stub — per diagram spec)* |
| `checkWorkload(reviewerList)` | `ReviewerManager.checkWorkload()` *(stub — per diagram spec)* |
| `[loop] assignReview()` | `Reviewer.assignReview()` in loop |
| `startEvaluation()` | `EvaluationManager.startEvaluation()` |
| `[loop] submitReviewScore(score)` | `Reviewer.submitReviewScore()` in loop |
| `saveScore(score)` | `Database.saveScore()` |
| `calculateAverage()` | `EvaluationManager.calculateAverage()` |
| `checkConsensus()` | `EvaluationManager.checkConsensus()` |
| `applyRules()` | `EvaluationManager.applyRules()` |
| `[alt: accepted] notifyAcceptance()` | `NotificationService.notifyAcceptance()` |
| `[alt: rejected] notifyRejection()` | `NotificationService.notifyRejection()` |
| `[alt: revision] notifyRevision()` | `NotificationService.notifyRevision()` |
| `sendNotification()` | `Researcher.receiveNotification()` |

---

## Design Notes (for Task 2 analysis)

The baseline intentionally preserves the following inefficiencies from the original diagram — these are documented and addressed in the **Optimised** implementation:

- `filterConflicts()` and `checkWorkload()` are stubs with no implemented logic
- `calculateAverage()` is called twice, once inside `applyRules()` and once explicitly before it
- `SubmissionController` directly instantiates `Reviewer` objects (tight coupling)
- Decision logic (`applyRules`) is scattered across the sequence rather than centralised
- `ReviewerManager` only calls `Database` for `fetchReviewers()`, conflict and workload data sources are unspecified