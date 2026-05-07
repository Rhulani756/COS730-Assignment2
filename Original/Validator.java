import java.util.Map;

public class Validator {
    public boolean validateFormat(Map<String, Object> data) {
        return data != null && !data.isEmpty();
    }
}