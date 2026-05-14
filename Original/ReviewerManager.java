import java.util.*;

public class ReviewerManager {

    private Database database;

    // Maximum concurrent assignments a reviewer may hold
    private static final int MAX_WORKLOAD = 3;

    private String submissionAuthor = ""; // used by filterConflicts
    // reviewer → current assignment count
    private final Map<String, Integer> workloadMap;

    // reviewer → set of authors they conflict with
    private final Map<String, Set<String>> conflictMap;

    public ReviewerManager(Database database) {
        this.database    = database;
        this.workloadMap = new HashMap<>();
        this.conflictMap = new HashMap<>();
    }

    // ── Test hooks (package-private) ─────────────────────────────────
    static final int TEST_MAX_WORKLOAD = 3; // expose MAX_WORKLOAD for tests
    void testAddConflict(String reviewer, String author) {
        conflictMap.computeIfAbsent(reviewer, k -> new HashSet<>()).add(author);
    }
    void testSetAuthor(String author) { this.submissionAuthor = author; }
    void testSetWorkload(String reviewer, int count) { workloadMap.put(reviewer, count); }

    public List<String> getAvailableReviewers() {
        List<String> reviewerList = new ArrayList<>(database.fetchReviewers());

        System.out.println("ReviewerManager: " + reviewerList.size()
                + " reviewer(s) fetched before filtering.");

        filterConflicts(reviewerList);
        checkWorkload(reviewerList);

        System.out.println("ReviewerManager: " + reviewerList.size()
                + " reviewer(s) available after filtering.");

        return reviewerList;
    }

    // Removes reviewers who have a declared conflict of interest.
    private void filterConflicts(List<String> reviewerList) {
        if (conflictMap.isEmpty()) {
            System.out.println("ReviewerManager: no conflicts declared — all pass conflict check.");
            return;
        }
        Iterator<String> it = reviewerList.iterator();
        while (it.hasNext()) {
            String reviewer = it.next();
            Set<String> conflicts = conflictMap.getOrDefault(reviewer, Collections.emptySet());
            boolean hasConflict = conflicts.stream()
                    .anyMatch(a -> a.equalsIgnoreCase(submissionAuthor));
            if (hasConflict) {
                it.remove();
                System.out.println("ReviewerManager: " + reviewer
                        + " removed — conflict of interest with '"
                        + submissionAuthor + "'.");
            }
        }
    }

    // Removes reviewers whose assignment count is at or above MAX_WORKLOAD.
    private void checkWorkload(List<String> reviewerList) {
        Iterator<String> it = reviewerList.iterator();
        while (it.hasNext()) {
            String reviewer = it.next();
            int load = workloadMap.getOrDefault(reviewer, 0);
            if (load >= MAX_WORKLOAD) {
                it.remove();
                System.out.println("ReviewerManager: " + reviewer
                        + " removed — at capacity (" + load + "/" + MAX_WORKLOAD + ").");
            } else {
                System.out.println("ReviewerManager: " + reviewer
                        + " eligible — workload " + load + "/" + MAX_WORKLOAD + ".");
            }
        }
    }
}