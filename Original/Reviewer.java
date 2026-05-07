public class Reviewer {
    private String name;

    public Reviewer(String name) {
        this.name = name;
    }

    public void assignReview() {
    }

    public void submitReviewScore(int score, EvaluationManager evaluationManager) {
        evaluationManager.submitScore(score);
    }
}