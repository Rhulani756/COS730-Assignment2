import java.util.Random;

/**

 * Reviewer generates its own review score when submitReviewScore() is called.
 * The score originates within the Reviewer — not passed externally — faithful
 * to the sequence diagram where submitReviewScore() is a Reviewer action.
 *
 * Each reviewer independently picks a random score from [0, 100] on each
 * submission. The three-reviewer average naturally produces all three outcomes:
 *   accepted : avg >= 75  (all reviewers score high)
 *   revision : 50 <= avg < 75 (mixed scores)
 *   rejected : avg < 50  (all reviewers score low)
 *
 * This eliminates the hardcoded 85 from the baseline and makes all three
 * [alt] outcome branches reachable without violating the diagram.
 */
public class Reviewer {

    private static final Random RANDOM = new Random();
    private final String name;

    public Reviewer(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public void assignReview() {
        System.out.println("Reviewer [" + name + "]: review assigned.");
    }

    /**
     * Generates a random score in [0, 100] and submits it to EvaluationManager.
     * Score is generated entirely within the Reviewer — not supplied externally.
     */
    public void submitReviewScore(EvaluationManager evaluationManager) {
        int score = RANDOM.nextInt(101);
        System.out.println("Reviewer [" + name + "]: submitting score " + score + ".");
        evaluationManager.submitScore(score);
    }
}