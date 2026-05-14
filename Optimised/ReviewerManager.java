import java.util.*;

/**
 * Two key changes from the baseline:
 *
 * 1. getAvailableReviewers() now returns List&lt;Reviewer&gt; instead of List&lt;String&gt;.
 *    Reviewer objects are instantiated here, fixing the GRASP Creator violation
 *    where SubmissionController was creating Reviewer instances directly.
 *
 * 2. filterConflicts() and checkWorkload() are now implemented with real logic.
 *    In the baseline these were empty stubs. The optimised design implements
 *    them as part of the same single pass over the reviewer list, avoiding
 *    the dual-traversal inefficiency if combined, while still keeping them
 *    as separate private methods for clarity.
 *
 * Addresses: Task 2 issues — Creator violation, unimplemented stubs.
 */
public class ReviewerManager {

    private Database database;

    // Submission author — set before getAvailableReviewers() is called
    // so filterConflicts() knows which author to check against.
    private String submissionAuthor = "";

    // Maximum concurrent assignments a reviewer may hold.
    private static final int MAX_WORKLOAD = 3;

    // Tracks workload in memory for this session.
    private Map<String, Integer> workloadMap = new HashMap<>();

    // Tracks declared conflicts: reviewer name → set of conflicting authors.
    private Map<String, Set<String>> conflictMap = new HashMap<>();

    public ReviewerManager(Database database) {
        this.database = database;
    }

    public void setSubmissionAuthor(String author) {
        this.submissionAuthor = author == null ? "" : author.trim();
    }

    /**
     * Fetches all reviewers from the Database, applies conflict and workload
     * filters, instantiates Reviewer objects, and returns the eligible list.
     *
     * Returns List&lt;Reviewer&gt; — object creation responsibility moved here
     * from SubmissionController (GRASP Creator fix).
     */
    public List<Reviewer> getAvailableReviewers() {
        List<String> names = new ArrayList<>(database.fetchReviewers());

        System.out.println("ReviewerManager: " + names.size()
                + " reviewer(s) fetched before filtering.");

        filterConflicts(names);
        checkWorkload(names);

        System.out.println("ReviewerManager: " + names.size()
                + " reviewer(s) available after filtering.");

        // Instantiate Reviewer objects here — Creator responsibility (GRASP Creator)
        List<Reviewer> reviewers = new ArrayList<>();
        for (String name : names) {
            reviewers.add(new Reviewer(name));
        }
        return reviewers;
    }

    /**
     * Removes reviewers who have a declared conflict of interest with the
     * submitting author. Conflict entries are stored in conflictMap.
     * To add a conflict: call addConflict(reviewer, author) before submission.
     */
    private void filterConflicts(List<String> names) {
        if (conflictMap.isEmpty()) {
            System.out.println("ReviewerManager: no conflicts declared — all pass conflict check.");
            return;
        }
        Iterator<String> it = names.iterator();
        while (it.hasNext()) {
            String name = it.next();
            Set<String> conflicts = conflictMap.getOrDefault(name, Collections.emptySet());
            boolean hasConflict = conflicts.stream()
                    .anyMatch(a -> a.equalsIgnoreCase(submissionAuthor));
            if (hasConflict) {
                it.remove();
                System.out.println("ReviewerManager: " + name
                        + " removed — conflict of interest with '" + submissionAuthor + "'.");
            }
        }
    }

    /**
     * Removes reviewers whose current assignment count is at or above MAX_WORKLOAD.
     * Workload is tracked in workloadMap and incremented after each assignment.
     */
    private void checkWorkload(List<String> names) {
        Iterator<String> it = names.iterator();
        while (it.hasNext()) {
            String name = it.next();
            int load = workloadMap.getOrDefault(name, 0);
            if (load >= MAX_WORKLOAD) {
                it.remove();
                System.out.println("ReviewerManager: " + name
                        + " removed — at workload capacity (" + load + "/" + MAX_WORKLOAD + ").");
            } else {
                System.out.println("ReviewerManager: " + name
                        + " eligible — workload " + load + "/" + MAX_WORKLOAD + ".");
            }
        }
    }

    /** Called after a reviewer is assigned to increment their workload count. */
    public void incrementWorkload(String reviewerName) {
        workloadMap.merge(reviewerName, 1, Integer::sum);
    }

    /** Registers a conflict of interest between a reviewer and an author. */
    public void addConflict(String reviewer, String author) {
        conflictMap.computeIfAbsent(reviewer, k -> new HashSet<>()).add(author);
    }
}
