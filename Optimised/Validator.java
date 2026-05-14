import java.util.Map;

/**
 * Optimised Implementation – Task 5
 *
 * Validator is functionally unchanged from the baseline. It correctly requires
 * non-empty title and author fields. No structural changes were needed here.
 */
public class Validator {

    public boolean validateFormat(Map<String, Object> data) {
        if (data == null || data.isEmpty()) return false;
        String title  = data.getOrDefault("title",  "").toString().trim();
        String author = data.getOrDefault("author", "").toString().trim();
        return !title.isEmpty() && !author.isEmpty();
    }
}
