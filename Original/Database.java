import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Database {
    public String saveSubmission(Map<String, Object> data) {
        return "confirmation"; 
    }

    public List<String> fetchReviewers() {
        return Arrays.asList("Reviewer_A", "Reviewer_B", "Reviewer_C"); 
    }

    public void saveScore(int score) {
        
    }
}