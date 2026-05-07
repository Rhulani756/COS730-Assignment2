import java.util.Map;

public class UI {
    private SubmissionController controller;

    public UI(SubmissionController controller) {
        this.controller = controller;
    }

    public void submitResearchOutput(Map<String, Object> data) {
        String response = controller.submit(data); 
        if ("error".equals(response)) {
            returnError(); 
        }
    }

    private void returnError() {
        System.out.println("UI Display: Validation Error. Please check your submission.");
    }
}