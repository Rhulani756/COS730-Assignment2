public class Reviewer {
    private String name;
    private boolean assigned = false;

    public Reviewer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void assignReview() {
        this.assigned = true;
    }
}