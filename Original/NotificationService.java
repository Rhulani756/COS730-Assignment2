public class NotificationService {
    private Researcher researcher;

    public NotificationService(Researcher researcher) {
        this.researcher = researcher;
    }

    public void notifyAcceptance() { sendNotification("Submission Accepted"); } 
    public void notifyRejection() { sendNotification("Submission Rejected"); }  
    public void notifyRevision() { sendNotification("Submission Requires Revision"); } 

    private void sendNotification(String message) {
        researcher.receiveNotification(message); 
    }
}