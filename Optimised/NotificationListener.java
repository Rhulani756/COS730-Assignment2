/**

 * Introduced to decouple NotificationService from the concrete Researcher class.
 * Any object that needs to receive submission outcome notifications implements
 * this interface. This replaces the direct dependency on Researcher, reducing
 * coupling and improving extensibility.
 *
 * Addresses: Task 2 issue — NotificationService tightly coupled to Researcher.
 */
public interface NotificationListener {
    void receiveNotification(String message);
}
