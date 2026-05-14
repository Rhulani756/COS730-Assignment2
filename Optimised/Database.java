import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class Database {

    private static final String DB_DIR      = "db";
    private static final String SUBMISSIONS = DB_DIR + "/submissions.txt";
    private static final String SCORES      = DB_DIR + "/scores.txt";
    private static final String REVIEWERS   = DB_DIR + "/reviewers.txt";

    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final List<String> DEFAULT_REVIEWERS =
            Arrays.asList("Reviewer_A", "Reviewer_B", "Reviewer_C");

    public Database() {
        initialise();
    }

    private void initialise() {
        try {
            Files.createDirectories(Paths.get(DB_DIR));
            Path reviewerPath = Paths.get(REVIEWERS);
            if (!Files.exists(reviewerPath)) {
                try (BufferedWriter bw = Files.newBufferedWriter(reviewerPath)) {
                    for (String r : DEFAULT_REVIEWERS) {
                        bw.write(r);
                        bw.newLine();
                    }
                }
                System.out.println("Database: reviewers.txt seeded.");
            }
        } catch (IOException e) {
            System.out.println("Database ERROR during initialisation: " + e.getMessage());
        }
    }

    public String saveSubmission(Map<String, Object> data) {
        String submissionId = "SUB-" + System.currentTimeMillis();
        String timestamp    = LocalDateTime.now().format(TIMESTAMP);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(SUBMISSIONS, true))) {
            bw.write("---");                              bw.newLine();
            bw.write("id="        + submissionId);       bw.newLine();
            bw.write("timestamp=" + timestamp);          bw.newLine();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String value = entry.getValue() == null ? ""
                        : entry.getValue().toString().replace("\n", " ");
                bw.write(entry.getKey() + "=" + value);  bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Database ERROR saving submission: " + e.getMessage());
            return "error";
        }
        System.out.println("Database: submission saved → " + submissionId);
        return submissionId;
    }

    public List<String> fetchReviewers() {
        List<String> reviewers = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(REVIEWERS))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) reviewers.add(line);
            }
        } catch (IOException e) {
            System.out.println("Database ERROR fetching reviewers: " + e.getMessage());
            return new ArrayList<>(DEFAULT_REVIEWERS);
        }
        System.out.println("Database: fetched " + reviewers.size() + " reviewer(s).");
        return reviewers;
    }

    public void saveScore(int score) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(SCORES, true))) {
            bw.write(timestamp + " | score=" + score);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Database ERROR saving score: " + e.getMessage());
            return;
        }
        System.out.println("Database: score saved → " + score);
    }
}
