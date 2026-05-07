import java.util.ArrayList;
import java.util.List;

public class EvaluationManager {
    private Database database;
    private NotificationService notificationService;
    private List<Integer> scores;

    private static final int ACCEPTANCE_THRESHOLD = 75;
    private static final int REJECTION_THRESHOLD  = 50;

    public EvaluationManager(Database database, NotificationService notificationService) {
        this.database = database;
        this.notificationService = notificationService;
        this.scores = new ArrayList<>();
    }

    public void startEvaluation() {
        scores.clear();
        System.out.println("EvaluationManager: Evaluation started, scores cleared.");
    }

    public void submitScore(int score) {
        this.scores.add(score);
        this.database.saveScore(score);
        System.out.println("EvaluationManager: Score submitted → " + score);
    }

    public double calculateAverage() {
        if (scores.isEmpty()) return 0.0;
        int sum = 0;
        for (int s : scores) sum += s;
        double avg = (double) sum / scores.size();
        System.out.println("EvaluationManager: Average score = " + avg);
        return avg;
    }

    public boolean checkConsensus() {
        if (scores.isEmpty()) return false;
        boolean allAccept = scores.stream().allMatch(s -> s >= ACCEPTANCE_THRESHOLD);
        boolean allReject = scores.stream().allMatch(s -> s < REJECTION_THRESHOLD);
        boolean consensus = allAccept || allReject;
        System.out.println("EvaluationManager: Consensus reached = " + consensus);
        return consensus;
    }

    public void applyRules() {
        double avg = calculateAverage();
        String outcome;

        if (avg >= ACCEPTANCE_THRESHOLD) {
            outcome = "accepted";
        } else if (avg < REJECTION_THRESHOLD) {
            outcome = "rejected";
        } else {
            outcome = "revision";
        }

        System.out.println("EvaluationManager: Outcome = " + outcome);

        if (outcome.equals("accepted")) {
            notificationService.notifyAcceptance();
        } else if (outcome.equals("rejected")) {
            notificationService.notifyRejection();
        } else if (outcome.equals("revision")) {
            notificationService.notifyRevision();
        }
    }
}