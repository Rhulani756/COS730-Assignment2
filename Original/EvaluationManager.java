import java.util.ArrayList;
import java.util.List;

public class EvaluationManager {
    private Database database;
    private NotificationService notificationService;
    private List<Integer> scores;

    public EvaluationManager(Database database, NotificationService notificationService) {
        this.database = database;
        this.notificationService = notificationService;
        this.scores = new ArrayList<>();
    }

    public void startEvaluation() {}

    public void submitScore(int score) {
        this.scores.add(score);
        this.database.saveScore(score); 
    }

    public void calculateAverage() {} 
    public void checkConsensus() {}   

    public void applyRules() { 
        String outcome = "accepted"; 

        if (outcome.equals("accepted")) {
            notificationService.notifyAcceptance(); 
        } else if (outcome.equals("rejected")) {
            notificationService.notifyRejection();  
        } else if (outcome.equals("revision")) {
            notificationService.notifyRevision();   
        }
    }
}