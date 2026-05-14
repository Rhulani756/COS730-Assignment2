import java.util.*;
import java.io.*;
import java.nio.file.*;

/**
 * COS 730 -- Assignment 2 -- Task 6 Benchmark (Baseline)
 *
 * HOW TO RUN:
 *   1. Place this file inside the Original/ folder
 *   2. cd Original
 *   3. javac *.java
 *   4. java BenchmarkBaseline
 *
 * Take a screenshot of the output for the report.
 */
public class BenchmarkBaseline {

    static final PrintStream SILENT  = new PrintStream(new OutputStream() {
        public void write(int b) {}
    });
    static final PrintStream CONSOLE = System.out;
    static final int WARMUP = 200;
    static final int RUNS   = 10000;
    static final String DIR = ".";

    static final String[] CLASSES = {
        "Database", "EvaluationManager", "ReviewerManager",
        "NotificationService", "Validator", "Reviewer",
        "Researcher", "SubmissionController"
    };

    public static void main(String[] args) throws Exception {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║   COS 730 -- Task 6 -- BASELINE Benchmark                ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println("Runs: " + RUNS + "  |  Warm-up: " + WARMUP);
        System.out.println();

        // ── Execution time ────────────────────────────────────────────
        section("EXECUTION TIME");
        System.out.println("  Running warm-up...");
        System.setOut(SILENT);
        for (int i = 0; i < WARMUP; i++) run();
        System.setOut(CONSOLE);

        System.out.println("  Running " + RUNS + " timed iterations...");
        System.setOut(SILENT);
        long total = 0;
        for (int i = 0; i < RUNS; i++) {
            long t = System.nanoTime();
            run();
            total += System.nanoTime() - t;
        }
        System.setOut(CONSOLE);

        long avg = total / RUNS;
        double ms  = avg / 1e6;
        double tput = 1_000_000_000.0 / avg;
        System.out.printf("  Avg time per submission : %.4f ms%n", ms);
        System.out.printf("  Avg time per submission : %.0f ns%n", (double)avg);
        System.out.printf("  Throughput              : %.0f submissions/sec%n%n", tput);

        // ── Interaction count ─────────────────────────────────────────
        section("INTERACTION COUNT (from sequence diagram)");
        System.out.println("  Total interactions per submission: 31");
        System.out.println("  (traced from baseline sequence diagram)");
        System.out.println();

        // ── LOC ───────────────────────────────────────────────────────
        section("LINES OF CODE (excluding blanks and comments)");
        printTable("LOC", p -> loc(p));

        // ── Methods ───────────────────────────────────────────────────
        section("METHOD COUNT");
        printTable("Methods", p -> methods(p));

        // ── Cyclomatic complexity ─────────────────────────────────────
        section("CYCLOMATIC COMPLEXITY  (CC = 1 + decision points)");
        printTable("CC", p -> cc(p));

        // ── Fan-out ───────────────────────────────────────────────────
        section("FAN-OUT COUPLING  (distinct type dependencies)");
        printTable("Deps", p -> fanout(p));

        // ── Save ──────────────────────────────────────────────────────
        try (BufferedWriter w = new BufferedWriter(
                new FileWriter("benchmark_baseline.txt"))) {
            w.write("BASELINE -- avg: " + String.format("%.4f", ms)
                    + " ms/submission\n");
            w.write("Throughput: " + String.format("%.0f", tput)
                    + " submissions/sec\n");
        }
        System.out.println("Saved to benchmark_baseline.txt");
    }

    static void run() {
        Database             db  = new Database();
        Researcher           res = new Researcher();
        NotificationService  ns  = new NotificationService(res);
        EvaluationManager    em  = new EvaluationManager(db, ns);
        ReviewerManager      rm  = new ReviewerManager(db);
        Validator            v   = new Validator();
        SubmissionController sc  = new SubmissionController(v, db, rm, em);
        Map<String, Object>  d   = new LinkedHashMap<>();
        d.put("title",  "Test Paper");
        d.put("author", "Dr. Test");
        sc.submit(d);
    }

    // ── Table printer ─────────────────────────────────────────────────
    interface Fn { int apply(String p) throws Exception; }

    static void printTable(String label, Fn fn) throws Exception {
        System.out.printf("  %-26s %8s%n", "Class", label);
        System.out.println("  " + "-".repeat(36));
        int total = 0;
        for (String cls : CLASSES) {
            String path = DIR + "/" + cls + ".java";
            int val = new File(path).exists() ? fn.apply(path) : 0;
            total += val;
            System.out.printf("  %-26s %8d%n", cls, val);
        }
        System.out.printf("  %-26s %8d%n%n", "TOTAL", total);
    }

    // ── Metrics ───────────────────────────────────────────────────────
    static int loc(String p) throws Exception {
        return (int) Files.lines(Paths.get(p))
                .filter(l -> {
                    String t = l.trim();
                    return !t.isEmpty()
                        && !t.startsWith("//")
                        && !t.startsWith("*")
                        && !t.equals("{")
                        && !t.equals("}");
                }).count();
    }

    static int methods(String p) throws Exception {
        return (int) Files.lines(Paths.get(p))
                .filter(l -> l.matches(
                    "\\s*(public|private|protected)\\s+.*\\(.*\\).*\\{?\\s*")
                    && !l.contains("class ")
                    && !l.contains("interface ")
                    && !l.trim().startsWith("//"))
                .count();
    }

    static int cc(String p) throws Exception {
        long d = Files.lines(Paths.get(p)).mapToLong(l -> {
            long c = 0;
            c += occ(l,"if (");  c += occ(l,"if(");
            c += occ(l,"else if");
            c += occ(l,"while ("); c += occ(l,"while(");
            c += occ(l,"for (");   c += occ(l,"for(");
            c += occ(l,"case ");
            c += occ(l,"&&");      c += occ(l,"||");
            c += occ(l," ? ");
            return c;
        }).sum();
        return (int)(1 + d);
    }

    static int fanout(String p) throws Exception {
        String[] types = {
            "Database","EvaluationManager","ReviewerManager",
            "NotificationService","Validator","Reviewer","Researcher",
            "SubmissionController","NotificationListener",
            "List","Map","Random","Iterator","Set",
            "HashSet","ArrayList","BufferedWriter","BufferedReader"
        };
        String self = Paths.get(p).getFileName().toString().replace(".java","");
        Set<String> found = new HashSet<>();
        for (String line : Files.readAllLines(Paths.get(p)))
            for (String t : types)
                if (!t.equals(self) && line.contains(t))
                    found.add(t);
        return found.size();
    }

    static long occ(String s, String sub) {
        int c = 0, i = 0;
        while ((i = s.indexOf(sub, i)) != -1) { c++; i += sub.length(); }
        return c;
    }

    static void section(String title) {
        System.out.println("──────────────────────────────────────────");
        System.out.println("  " + title);
        System.out.println("──────────────────────────────────────────");
    }
}
