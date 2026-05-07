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
        if (!validator.validateFormat(data)) {
            return "error"; 
        }

        database.saveSubmission(data); 

        List<String> filteredReviewersData = reviewerManager.getAvailableReviewers();
        List<Reviewer> reviewers = new ArrayList<>();
        for (String name : filteredReviewersData) {
            reviewers.add(new Reviewer(name));
        }

        for (Reviewer reviewer : reviewers) {
            reviewer.assignReview(); 
        }

        evaluationManager.startEvaluation(); 

        for (Reviewer reviewer : reviewers) {
            reviewer.submitReviewScore(85, evaluationManager); 
        }

        evaluationManager.calculateAverage(); 
        evaluationManager.checkConsensus();   
        evaluationManager.applyRules();       

        return "success";
    }
}