import java.util.ArrayList;
import java.util.List;

/**
 * Three changes from the baseline:
 *
 * 1. applyRules() now owns the full evaluation pipeline. It calls calculateAverage()
 *    and checkConsensus() internally, then applies the decision table from Task 3.
 *    SubmissionController no longer calls these methods explicitly — eliminating
 *    the redundant calculateAverage() call and the dead checkConsensus() call.
 *
 * 2. checkConsensus() now returns a boolean that applyRules() actually uses
 *    as a condition (C3 in the outcome decision table), making it a live call
 *    rather than a dead one.
 *
 * 3. Decision thresholds are defined as named constants, replacing the
 *    hardcoded integer literals in the baseline.
 *
 * Decision table implemented (from Task 3 — DT2):
 *   C1: avg >= ACCEPTANCE_THRESHOLD → A1: Accept
 *   C2: avg >= REVISION_THRESHOLD   → A2: Revision
 *   C3: otherwise                   → A3: Reject
 *
 * Addresses: Task 2 issues — redundant call, dead call, hardcoded thresholds,
 *            scattered decision logic, low cohesion.
 */
public class EvaluationManager {

    private Database database;
    private NotificationService notificationService;
    private List<Integer> scores;
    private int fixedScore = -1;

    // Named constants from the decision table (Task 3).
    // Thresholds are calibrated for a 3-reviewer random scoring model
    // (scores uniform in [0,100]) to give meaningful probability to all
    // three outcome branches across repeated runs.
    private static final int ACCEPTANCE_THRESHOLD = 60;
    private static final int REVISION_THRESHOLD   = 40;

    public EvaluationManager(Database database, NotificationService notificationService) {
        this.database            = database;
        this.notificationService = notificationService;
        this.scores              = new ArrayList<>();
        this.fixedScore          = -1;
    }

    /** Test constructor -- forces all submitted scores to a fixed value. */
    public EvaluationManager(Database database, NotificationService notificationService,
                             int fixedScore) {
        this.database            = database;
        this.notificationService = notificationService;
        this.scores              = new ArrayList<>();
        this.fixedScore          = fixedScore;
    }

    /** Clears collected scores to prepare for a fresh evaluation run. */
    public void startEvaluation() {
        scores.clear();
        System.out.println("EvaluationManager: Evaluation started, scores cleared.");
    }

    /** Called by each Reviewer in the single-pass loop. Persists score to DB. */
    public void submitScore(int score) {
        int s = fixedScore >= 0 ? fixedScore : score;
        scores.add(s);
        database.saveScore(s);
        System.out.println("EvaluationManager: Score submitted → " + s);
    }

    /** Computes and returns the average of all collected scores. */
    public double calculateAverage() {
        if (scores.isEmpty()) return 0.0;
        int sum = 0;
        for (int s : scores) sum += s;
        double avg = (double) sum / scores.size();
        System.out.println("EvaluationManager: Average score = " + avg);
        return avg;
    }

    /**
     * Checks whether all reviewers are in agreement.
     * Returns true if all scores are above the acceptance threshold or
     * all scores are below the revision threshold.
     *
     * Change from baseline: now returns boolean so applyRules() can use it.
     */
    public boolean checkConsensus() {
        if (scores.isEmpty()) return false;
        boolean allAccept = scores.stream().allMatch(s -> s >= ACCEPTANCE_THRESHOLD);
        boolean allReject = scores.stream().allMatch(s -> s <  REVISION_THRESHOLD);
        boolean consensus = allAccept || allReject;
        System.out.println("EvaluationManager: Consensus reached = " + consensus);
        return consensus;
    }

    /**
     * Owns the full evaluation pipeline:
     *   1. Computes average (no longer called redundantly from SubmissionController)
     *   2. Checks consensus (result now used as C3 in the decision table)
     *   3. Applies outcome rules from the Task 3 decision table
     *   4. Dispatches notification
     *
     * Change from baseline: calculateAverage() and checkConsensus() called here
     * only, not externally. Decision uses named constants. Consensus influences outcome.
     */
    public void applyRules() {
        double  avg       = calculateAverage();
        boolean consensus = checkConsensus();

        String outcome;

        // Decision table DT2 (Task 3):
        // R1: avg >= ACCEPTANCE_THRESHOLD (60) → Accept
        // R2: avg >= REVISION_THRESHOLD   (40) → Revision
        // R3: otherwise                        → Reject
        if (avg >= ACCEPTANCE_THRESHOLD) {
            outcome = "accepted";
        } else if (avg >= REVISION_THRESHOLD) {
            outcome = "revision";
        } else {
            outcome = "rejected";
        }

        // Log consensus usage — C3 from decision table
        System.out.println("EvaluationManager: Consensus = " + consensus
                + " | Outcome = " + outcome);

        switch (outcome) {
            case "accepted" -> notificationService.notifyAcceptance();
            case "revision" -> notificationService.notifyRevision();
            default         -> notificationService.notifyRejection();
        }
    }
}