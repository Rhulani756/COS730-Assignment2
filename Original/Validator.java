import java.util.Map;

public class Validator {
    public boolean validateFormat(Map<String, Object> data) {
        if (data == null || data.isEmpty()) return false;

        // Title and author are required fields
        String title  = data.getOrDefault("title",  "").toString().trim();
        String author = data.getOrDefault("author", "").toString().trim();

        return !title.isEmpty() && !author.isEmpty();
    }
}