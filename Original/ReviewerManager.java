import java.util.List;

public class ReviewerManager {
    private Database database;

    public ReviewerManager(Database database) {
        this.database = database;
    }

    public List<String> getAvailableReviewers() {
        List<String> reviewerList = database.fetchReviewers(); 
        
        filterConflicts(reviewerList); 
        checkWorkload(reviewerList);   
        
        return reviewerList; 
    }

    private void filterConflicts(List<String> reviewerList) {}
    private void checkWorkload(List<String> reviewerList) {}
}