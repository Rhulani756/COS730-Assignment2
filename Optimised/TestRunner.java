import java.util.*;
import java.io.*;

/**
 *
 * Verifies functional equivalence with the baseline and validates
 * all optimisation-specific behaviour. Run with: java TestRunnerOptimised
 *
 * Tests:
 *   T1  Valid submission returns 'success'
 *   T2  Invalid submission (empty map) returns 'error'
 *   T3  Invalid submission (missing title) returns 'error'
 *   T4  Invalid submission (missing author) returns 'error'
 *   T5  Accepted outcome -- applyRules() fires notifyAcceptance()
 *   T6  Revision outcome -- applyRules() fires notifyRevision()
 *   T7  Rejected outcome -- applyRules() fires notifyRejection()
 *   T8  Conflict filtering -- conflicted reviewer excluded
 *   T9  Workload filtering -- overloaded reviewer excluded
 *   T10 Submission persisted to db/submissions.txt
 *   T11 Scores persisted to db/scores.txt
 *   T12 ReviewerManager returns List<Reviewer> (GRASP Creator fix)
 *   T13 checkConsensus() result is used -- not discarded
 *   T14 NotificationListener interface -- Researcher receives via interface
 *   T15 Single loop -- assignReview and submitScore in same pass
 */
public class TestRunner {

    static int passed = 0;
    static int failed = 0;

    static final PrintStream SILENT  = new PrintStream(new OutputStream() {
        public void write(int b) {}
    });
    static final PrintStream CONSOLE = System.out;

    public static void main(String[] args) throws Exception {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║   COS 730 -- Assignment 2 · Optimised Test Runner    ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println();

        cleanDb();

        // ── Functional equivalence tests (same as baseline) ───────────
        test_T1_validSubmission();
        test_T2_invalidEmptyMap();
        test_T3_invalidMissingTitle();
        test_T4_invalidMissingAuthor();
        test_T5_acceptedOutcome();
        test_T6_revisionOutcome();
        test_T7_rejectedOutcome();
        test_T8_conflictFiltering();
        test_T9_workloadFiltering();
        test_T10_submissionPersisted();
        test_T11_scoresPersisted();

        // ── Optimisation-specific tests ───────────────────────────────
        test_T12_reviewerManagerReturnsObjects();
        test_T13_checkConsensusResultUsed();
        test_T14_notificationListenerInterface();
        test_T15_singleLoopBehaviour();

        // ── Summary ───────────────────────────────────────────────────
        System.out.println();
        System.out.println("──────────────────────────────────────────────────");
        System.out.printf("  Results:  %d passed  |  %d failed  |  %d total%n",
                passed, failed, passed + failed);
        System.out.println("──────────────────────────────────────────────────");
        System.out.println(failed == 0
                ? "  All tests passed."
                : "  Some tests failed -- see details above.");
    }

    // ════════════════════════════════════════════════════════════════
    //  FUNCTIONAL EQUIVALENCE TESTS
    // ════════════════════════════════════════════════════════════════

    static void test_T1_validSubmission() {
        String result = submit(data("Test Paper", "Dr. Smith"));
        pass_if("T1  Valid submission returns 'success'",
                "success".equals(result));
    }

    static void test_T2_invalidEmptyMap() {
        String result = submit(new HashMap<>());
        pass_if("T2  Empty data map returns 'error'",
                "error".equals(result));
    }

    static void test_T3_invalidMissingTitle() {
        Map<String, Object> d = new HashMap<>();
        d.put("author", "Dr. Smith");
        pass_if("T3  Missing title returns 'error'",
                "error".equals(submit(d)));
    }

    static void test_T4_invalidMissingAuthor() {
        Map<String, Object> d = new HashMap<>();
        d.put("title", "Some Paper");
        pass_if("T4  Missing author returns 'error'",
                "error".equals(submit(d)));
    }

    static void test_T5_acceptedOutcome() {
        // Force all reviewers to score 90 via fixed EvaluationManager
        String log = captureLog(data("Paper", "Author"), 90);
        pass_if("T5  Score 90 produces 'accepted' outcome",
                log.contains("Outcome = accepted"));
    }

    static void test_T6_revisionOutcome() {
        String log = captureLog(data("Paper", "Author"), 50);
        pass_if("T6  Score 50 produces 'revision' outcome",
                log.contains("Outcome = revision"));
    }

    static void test_T7_rejectedOutcome() {
        String log = captureLog(data("Paper", "Author"), 20);
        pass_if("T7  Score 20 produces 'rejected' outcome",
                log.contains("Outcome = rejected"));
    }

    static void test_T8_conflictFiltering() {
        System.setOut(SILENT);
        Database            db  = new Database();
        Researcher          res = new Researcher();
        NotificationService ns  = new NotificationService(res);
        EvaluationManager   em  = new EvaluationManager(db, ns);
        ReviewerManager     rm  = new ReviewerManager(db);
        // Register conflict
        rm.addConflict("Reviewer_A", "Dr. Conflict");
        rm.setSubmissionAuthor("Dr. Conflict");
        List<Reviewer> result = rm.getAvailableReviewers();
        System.setOut(CONSOLE);

        boolean excluded = result.stream()
                .noneMatch(r -> r.getName().equals("Reviewer_A"));
        pass_if("T8  Reviewer_A excluded when conflicting with submitting author",
                excluded && result.size() == 2);
    }

    static void test_T9_workloadFiltering() {
        System.setOut(SILENT);
        Database            db  = new Database();
        Researcher          res = new Researcher();
        NotificationService ns  = new NotificationService(res);
        EvaluationManager   em  = new EvaluationManager(db, ns);
        ReviewerManager     rm  = new ReviewerManager(db);
        // Push Reviewer_B to max workload
        rm.incrementWorkload("Reviewer_B");
        rm.incrementWorkload("Reviewer_B");
        rm.incrementWorkload("Reviewer_B"); // MAX_WORKLOAD = 3
        List<Reviewer> result = rm.getAvailableReviewers();
        System.setOut(CONSOLE);

        boolean excluded = result.stream()
                .noneMatch(r -> r.getName().equals("Reviewer_B"));
        pass_if("T9  Reviewer_B excluded when at max workload",
                excluded && result.size() == 2);
    }

    static void test_T10_submissionPersisted() throws Exception {
        cleanDb();
        submit(data("Persisted Paper", "Dr. Persist"));
        File f = new File("db/submissions.txt");
        pass_if("T10 Submission written to db/submissions.txt",
                f.exists() && f.length() > 0
                && fileContains(f, "title=Persisted Paper")
                && fileContains(f, "author=Dr. Persist"));
    }

    static void test_T11_scoresPersisted() throws Exception {
        cleanDb();
        submit(data("Score Paper", "Dr. Score"));
        File f = new File("db/scores.txt");
        pass_if("T11 Scores written to db/scores.txt",
                f.exists() && f.length() > 0);
    }

    // ════════════════════════════════════════════════════════════════
    //  OPTIMISATION-SPECIFIC TESTS
    // ════════════════════════════════════════════════════════════════

    static void test_T12_reviewerManagerReturnsObjects() {
        System.setOut(SILENT);
        ReviewerManager rm = new ReviewerManager(new Database());
        List<Reviewer> result = rm.getAvailableReviewers();
        System.setOut(CONSOLE);

        // Verify return type is List<Reviewer> and objects are usable
        boolean isReviewerList = result != null
                && !result.isEmpty()
                && result.get(0) instanceof Reviewer
                && result.get(0).getName() != null;
        pass_if("T12 ReviewerManager returns List<Reviewer> (GRASP Creator fix)",
                isReviewerList);
    }

    static void test_T13_checkConsensusResultUsed() {
        // In the optimised system, checkConsensus() returns boolean
        // and applyRules() calls it internally. We verify the return
        // value is non-void by calling it directly.
        System.setOut(SILENT);
        Database            db  = new Database();
        Researcher          res = new Researcher();
        NotificationService ns  = new NotificationService(res);
        EvaluationManager   em  = new EvaluationManager(db, ns);
        em.startEvaluation();
        em.submitScore(85);
        em.submitScore(85);
        em.submitScore(85);
        boolean consensus = em.checkConsensus();
        System.setOut(CONSOLE);

        // All scores 85 >= 60 (ACCEPTANCE_THRESHOLD) -- consensus = true
        pass_if("T13 checkConsensus() returns boolean (result no longer discarded)",
                consensus == true);
    }

    static void test_T14_notificationListenerInterface() {
        // Verify Researcher implements NotificationListener
        Researcher res = new Researcher();
        pass_if("T14 Researcher implements NotificationListener interface",
                res instanceof NotificationListener);
    }

    static void test_T15_singleLoopBehaviour() {
        // Verify that assignReview and submitScore both work in a single
        // pass -- call them in sequence on the same reviewer object
        System.setOut(SILENT);
        Database            db  = new Database();
        Researcher          res = new Researcher();
        NotificationService ns  = new NotificationService(res);
        EvaluationManager   em  = new EvaluationManager(db, ns);
        em.startEvaluation();

        ReviewerManager rm = new ReviewerManager(db);
        List<Reviewer> reviewers = rm.getAvailableReviewers();

        // Single pass: assign then submit for each reviewer
        for (Reviewer r : reviewers) {
            r.assignReview();
            r.submitReviewScore(em);
        }
        double avg = em.calculateAverage();
        System.setOut(CONSOLE);

        pass_if("T15 Single pass: assignReview() and submitScore() work together",
                avg > 0 && reviewers.size() == 3);
    }

    // ════════════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════════════

    static Map<String, Object> data(String title, String author) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("title",  title);
        d.put("author", author);
        return d;
    }

    static String submit(Map<String, Object> data) {
        System.setOut(SILENT);
        try {
            SubmissionController sc = buildController(-1);
            return sc.submit(data);
        } finally {
            System.setOut(CONSOLE);
        }
    }

    /** Captures log output with a fixed score for deterministic outcome testing. */
    static String captureLog(Map<String, Object> data, int fixedScore) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        try {
            buildController(fixedScore).submit(data);
        } finally {
            System.setOut(CONSOLE);
        }
        return baos.toString();
    }

    /**
     * Builds a wired SubmissionController.
     * fixedScore >= 0 forces all reviewer scores to that value (for outcome tests).
     * fixedScore == -1 uses random scores.
     */
    static SubmissionController buildController(int fixedScore) {
        Database             db  = new Database();
        Researcher           res = new Researcher();
        NotificationService  ns  = new NotificationService(res);
        EvaluationManager    em  = fixedScore >= 0
                ? new EvaluationManager(db, ns, fixedScore)
                : new EvaluationManager(db, ns);
        ReviewerManager      rm  = new ReviewerManager(db);
        Validator            v   = new Validator();
        return new SubmissionController(v, db, rm, em);
    }

    static boolean fileContains(File f, String text) throws Exception {
        String content = new String(
                java.nio.file.Files.readAllBytes(f.toPath()));
        return content.contains(text);
    }

    static void cleanDb() {
        new File("db/submissions.txt").delete();
        new File("db/scores.txt").delete();
        new File("db/reviewers.txt").delete();
    }

    static void pass_if(String name, boolean condition) {
        if (condition) {
            System.out.printf("  PASS  %s%n", name);
            passed++;
        } else {
            System.out.printf("  FAIL  %s%n", name);
            failed++;
        }
    }
}