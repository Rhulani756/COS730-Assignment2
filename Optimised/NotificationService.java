/**
 * NotificationService now depends on the NotificationListener interface rather
 * than the concrete Researcher class. This resolves the tight coupling identified
 * in Task 2 and allows the service to notify any compliant recipient without
 * modification.
 *
 * Change from baseline: field type changed from Researcher to NotificationListener.
 */
public class NotificationService {

    private NotificationListener listener;

    public NotificationService(NotificationListener listener) {
        this.listener = listener;
    }

    public void notifyAcceptance() {
        sendNotification("Submission Accepted");
    }

    public void notifyRejection() {
        sendNotification("Submission Rejected");
    }

    public void notifyRevision() {
        sendNotification("Submission Requires Revision");
    }

    private void sendNotification(String message) {
        listener.receiveNotification(message);
    }
}
