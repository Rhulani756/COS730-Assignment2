/**
 * Researcher now implements NotificationListener rather than being directly
 * referenced by NotificationService. This breaks the concrete dependency
 * and allows the notification mechanism to support other recipient types
 * without modifying NotificationService.
 *
 * Change from baseline: implements NotificationListener.
 */
public class Researcher implements NotificationListener {

    @Override
    public void receiveNotification(String message) {
        System.out.println("Researcher Notification: " + message);
    }
}
