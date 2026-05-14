import java.util.*;
import java.io.*;

public class TestRunner {

    private static int passed = 0;
    private static int failed = 0;

    private static final PrintStream SILENT = new PrintStream(new OutputStream() {
        public void write(int b) {}
    });
    private static final PrintStream CONSOLE = System.out;

    public static void main(String[] args) throws Exception {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   COS 730 – Assignment 2 · Baseline Test Runner  ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();

        cleanDb();

        test_T1_validSubmissionPipelineRuns();
        test_T2_invalidSubmissionEmptyMap();
        test_T3_invalidSubmissionMissingTitle();
        test_T4_invalidSubmissionMissingAuthor();
        test_T5_acceptedOutcome();
        test_T6_revisionOutcome();
        test_T7_rejectedOutcome();
        test_T8_conflictFiltering();
        test_T9_workloadFiltering();
        test_T10_submissionPersisted();
        test_T11_scoresPersisted();

        System.out.println();
        System.out.println("──────────────────────────────────────────────────");
        System.out.printf("  Results:  %d passed  |  %d failed  |  %d total%n",
                passed, failed, passed + failed);
        System.out.println("──────────────────────────────────────────────────");
        if (failed == 0) {
            System.out.println("  All tests passed.");
        } else {
            System.out.println("  Some tests failed — see details above.");
        }
    }

    static void test_T1_validSubmissionPipelineRuns() {
        String result = submit(buildData("AI in Healthcare", "Dr. Smith"), 85);
        pass_if("T1  Valid submission returns 'success'",
                "success".equals(result));
    }

    static void test_T2_invalidSubmissionEmptyMap() {
        String result = submit(new HashMap<>(), 85);
        pass_if("T2  Empty data map returns 'error'",
                "error".equals(result));
    }

    static void test_T3_invalidSubmissionMissingTitle() {
        Map<String, Object> data = new HashMap<>();
        data.put("author", "Dr. Smith");
        String result = submit(data, 85);
        pass_if("T3  Missing title returns 'error'",
                "error".equals(result));
    }

    static void test_T4_invalidSubmissionMissingAuthor() {
        Map<String, Object> data = new HashMap<>();
        data.put("title", "Some Paper");
        String result = submit(data, 85);
        pass_if("T4  Missing author returns 'error'",
                "error".equals(result));
    }

    static void test_T5_acceptedOutcome() {
        // Score of 90 > ACCEPTANCE_THRESHOLD (60) → accepted
        String log = captureLog(buildData("Paper A", "Author A"), 90);
        pass_if("T5  Score 90 produces 'accepted' outcome",
                log.contains("Outcome = accepted"));
    }

    static void test_T6_revisionOutcome() {
        // Score of 50 — between REVISION_THRESHOLD (40) and ACCEPTANCE_THRESHOLD (60) → revision
        String log = captureLog(buildData("Paper B", "Author B"), 50);
        pass_if("T6  Score 50 produces 'revision' outcome",
                log.contains("Outcome = revision"));
    }

    static void test_T7_rejectedOutcome() {
        // Score of 20 < REVISION_THRESHOLD (40) → rejected
        String log = captureLog(buildData("Paper C", "Author C"), 20);
        pass_if("T7  Score 20 produces 'rejected' outcome",
                log.contains("Outcome = rejected"));
    }

    static void test_T8_conflictFiltering() {
        ReviewerManager rm = buildReviewerManager();
        rm.testAddConflict("Reviewer_A", "Dr. Conflict");
        rm.testSetAuthor("Dr. Conflict");
        List<String> result = rm.getAvailableReviewers();
        pass_if("T8  Reviewer_A excluded when conflicting with submitting author",
                !result.contains("Reviewer_A") && result.size() == 2);
    }

    static void test_T9_workloadFiltering() {
        ReviewerManager rm = buildReviewerManager();
        rm.testSetWorkload("Reviewer_B", ReviewerManager.TEST_MAX_WORKLOAD);
        List<String> result = rm.getAvailableReviewers();
        pass_if("T9  Reviewer_B excluded when at max workload",
                !result.contains("Reviewer_B") && result.size() == 2);
    }

    static void test_T10_submissionPersisted() throws Exception {
        cleanDb();
        submit(buildData("Persisted Paper", "Dr. Persist"), 85);
        File f = new File("db/submissions.txt");
        boolean exists    = f.exists() && f.length() > 0;
        boolean hasTitle  = fileContains(f, "title=Persisted Paper");
        boolean hasAuthor = fileContains(f, "author=Dr. Persist");
        pass_if("T10 Submission written to db/submissions.txt with correct fields",
                exists && hasTitle && hasAuthor);
    }

    static void test_T11_scoresPersisted() throws Exception {
        cleanDb();
        submit(buildData("Score Paper", "Dr. Score"), 85);
        File f = new File("db/scores.txt");
        pass_if("T11 Scores written to db/scores.txt",
                f.exists() && f.length() > 0);
    }

    static Map<String, Object> buildData(String title, String author) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("title",  title);
        data.put("author", author);
        return data;
    }

    static String submit(Map<String, Object> data, int score) {
        System.setOut(SILENT);
        try {
            SubmissionController sc = buildController(score);
            return sc.submit(data);
        } finally {
            System.setOut(CONSOLE);
        }
    }

    static String captureLog(Map<String, Object> data, int score) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        try {
            buildController(score).submit(data);
        } finally {
            System.setOut(CONSOLE);
        }
        return baos.toString();
    }

    static SubmissionController buildController(int score) {
        Database             db  = new Database();
        Researcher           res = new Researcher();
        NotificationService  ns  = new NotificationService(res);
        EvaluationManager    em  = new EvaluationManager(db, ns, score);
        ReviewerManager      rm  = new ReviewerManager(db);
        Validator            v   = new Validator();
        return new SubmissionController(v, db, rm, em);
    }

    static ReviewerManager buildReviewerManager() {
        System.setOut(SILENT);
        ReviewerManager rm = new ReviewerManager(new Database());
        System.setOut(CONSOLE);
        return rm;
    }

    static boolean fileContains(File f, String text) throws Exception {
        String content = new String(java.nio.file.Files.readAllBytes(f.toPath()));
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