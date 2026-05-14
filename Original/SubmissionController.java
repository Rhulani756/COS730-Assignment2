import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SubmissionController {
    private Validator validator;
    private Database database;
    private ReviewerManager reviewerManager;
    private EvaluationManager evaluationManager;

    public SubmissionController(Validator validator, Database database,
                                ReviewerManager reviewerManager, EvaluationManager evaluationManager) {
        this.validator = validator;
        this.database = database;
        this.reviewerManager = reviewerManager;
        this.evaluationManager = evaluationManager;
    }

    public String submit(Map<String, Object> data) {

        // [alt: invalid] – validateFormat(data) → valid/invalid
        if (!validator.validateFormat(data)) {
            return "error";
        }

        // [valid] – saveSubmission(data) → confirmation
        String confirmation = database.saveSubmission(data);
        System.out.println("SubmissionController: DB confirmed save → " + confirmation);

        // getAvailableReviewers() → filteredReviewers
        List<String> filteredReviewersData = reviewerManager.getAvailableReviewers();
        List<Reviewer> reviewers = new ArrayList<>();
        for (String name : filteredReviewersData) {
            reviewers.add(new Reviewer(name));
        }

        // [loop: assign reviewers] – assignReview()
        for (Reviewer reviewer : reviewers) {
            reviewer.assignReview();
        }

        // startEvaluation()
        evaluationManager.startEvaluation();

        // [loop: each reviewer] – submitScore(score) → EvaluationManager
        // → saveScore(score) → Database
        // Score randomly generated per reviewer so all three [alt]
        // outcome branches are reachable across runs.
        java.util.Random random = new java.util.Random();
        for (Reviewer reviewer : reviewers) {
            int score = random.nextInt(101);
            evaluationManager.submitScore(score);
        }

        // calculateAverage() → checkConsensus() → applyRules()
        evaluationManager.calculateAverage();
        evaluationManager.checkConsensus();
        evaluationManager.applyRules();

        return "success";
    }
}