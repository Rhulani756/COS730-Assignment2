import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class UI extends JFrame {

    private SubmissionController controller;

    // ── Colour palette ──────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(13,  17,  23);
    private static final Color BG_CARD      = new Color(22,  30,  40);
    private static final Color BG_INPUT     = new Color(30,  41,  55);
    private static final Color ACCENT_BLUE  = new Color(56, 139, 253);
    private static final Color ACCENT_GREEN = new Color(46, 204, 113);
    private static final Color ACCENT_RED   = new Color(231,  76,  60);
    private static final Color ACCENT_AMBER = new Color(241, 196,  15);
    private static final Color TEXT_PRIMARY = new Color(230, 237, 243);
    private static final Color TEXT_MUTED   = new Color(125, 148, 168);
    private static final Color BORDER_COLOR = new Color(48,  54,  61);

    // ── Form fields ─────────────────────────────────────────────────
    private JTextField  titleField;
    private JTextField  authorField;
    private JComboBox<String> categoryCombo;
    private JTextArea   abstractArea;
    private JTextField  keywordsField;

    // ── Status / log ─────────────────────────────────────────────────
    private JTextArea logArea;
    private JLabel    statusBadge;
    private JButton   submitBtn;

    // ════════════════════════════════════════════════════════════════
    public UI(SubmissionController controller) {
        this.controller = controller;
        buildFrame();
    }

    // ════════════════════════════════════════════════════════════════
    //  Frame assembly
    // ════════════════════════════════════════════════════════════════
    private void buildFrame() {
        setTitle("Intelligent Submission & Review System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 720);
        setMinimumSize(new Dimension(820, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        setVisible(true);
    }

    // ── Header bar ───────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(BORDER_COLOR);
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(16, 28, 16, 28));

        // Left: logo + title stack
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        JLabel icon = new JLabel("◈");
        icon.setFont(new Font("Serif", Font.PLAIN, 26));
        icon.setForeground(ACCENT_BLUE);
        left.add(icon);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel appTitle = new JLabel("Research Submission Portal");
        appTitle.setFont(new Font("Georgia", Font.BOLD, 16));
        appTitle.setForeground(TEXT_PRIMARY);
        JLabel appSub = new JLabel("Intelligent Submission & Review System  ·  Baseline v1");
        appSub.setFont(new Font("Monospaced", Font.PLAIN, 10));
        appSub.setForeground(TEXT_MUTED);
        titles.add(appTitle);
        titles.add(Box.createRigidArea(new Dimension(0, 2)));
        titles.add(appSub);
        left.add(titles);

        // Right: live status badge
        statusBadge = new JLabel("  READY  ");
        statusBadge.setFont(new Font("Monospaced", Font.BOLD, 11));
        statusBadge.setForeground(ACCENT_GREEN);
        statusBadge.setOpaque(false);
        statusBadge.setBorder(new CompoundBorder(
                new LineBorder(ACCENT_GREEN, 1, true),
                new EmptyBorder(3, 8, 3, 8)));

        header.add(left,        BorderLayout.WEST);
        header.add(statusBadge, BorderLayout.EAST);
        return header;
    }

    // ── Body: form left | log right ──────────────────────────────────
    private JSplitPane buildBody() {
        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, buildFormPanel(), buildLogPanel());
        split.setDividerLocation(490);
        split.setDividerSize(3);
        split.setBorder(null);
        split.setBackground(BG_DARK);
        split.getLeftComponent().setMinimumSize(new Dimension(340, 0));
        split.getRightComponent().setMinimumSize(new Dimension(220, 0));
        return split;
    }

    // ── Left: submission form ────────────────────────────────────────
    private JScrollPane buildFormPanel() {
        JPanel form = new JPanel();
        form.setBackground(BG_DARK);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(24, 28, 32, 20));

        form.add(sectionLabel("ARTEFACT DETAILS"));
        form.add(vgap(10));

        titleField = styledField("e.g. AI in Healthcare: A Systematic Review");
        form.add(fieldBlock("Paper Title *", titleField));
        form.add(vgap(14));

        authorField = styledField("e.g. Dr. Jane Smith");
        form.add(fieldBlock("Primary Author *", authorField));
        form.add(vgap(14));

        categoryCombo = new JComboBox<>(new String[]{
                "Computer Science", "Artificial Intelligence", "Software Engineering",
                "Data Science", "Bioinformatics", "Human-Computer Interaction", "Other"
        });
        styleCombo(categoryCombo);
        form.add(fieldBlock("Research Category", categoryCombo));
        form.add(vgap(22));

        form.add(sectionLabel("ABSTRACT"));
        form.add(vgap(10));
        abstractArea = new JTextArea(6, 30);
        abstractArea.setLineWrap(true);
        abstractArea.setWrapStyleWord(true);
        abstractArea.setFont(new Font("Georgia", Font.PLAIN, 13));
        abstractArea.setBackground(BG_INPUT);
        abstractArea.setForeground(TEXT_PRIMARY);
        abstractArea.setCaretColor(ACCENT_BLUE);
        abstractArea.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        JScrollPane absScroll = new JScrollPane(abstractArea);
        absScroll.setBorder(null);
        absScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        absScroll.setAlignmentX(LEFT_ALIGNMENT);
        form.add(absScroll);
        form.add(vgap(14));

        keywordsField = styledField("e.g. machine learning, neural networks, healthcare");
        form.add(fieldBlock("Keywords (comma-separated)", keywordsField));
        form.add(vgap(30));

        // ── Submit button ─────────────────────────────────────────────
        submitBtn = new JButton("  Submit Artefact  ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!isEnabled()) {
                    g2.setColor(ACCENT_BLUE.darker().darker());
                } else if (getModel().isPressed()) {
                    g2.setColor(ACCENT_BLUE.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(80, 155, 255));
                } else {
                    g2.setColor(ACCENT_BLUE);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        submitBtn.setFont(new Font("Georgia", Font.BOLD, 14));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setContentAreaFilled(false);
        submitBtn.setBorderPainted(false);
        submitBtn.setFocusPainted(false);
        submitBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        submitBtn.setAlignmentX(LEFT_ALIGNMENT);
        submitBtn.setMaximumSize(new Dimension(210, 44));
        submitBtn.addActionListener(e -> handleSubmit());
        form.add(submitBtn);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.setBackground(BG_DARK);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    // ── Right: activity log panel ─────────────────────────────────────
    private JPanel buildLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CARD);
        panel.setBorder(new MatteBorder(0, 1, 0, 0, BORDER_COLOR));

        // Log header
        JPanel logHeader = new JPanel(new BorderLayout());
        logHeader.setBackground(BG_CARD);
        logHeader.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(11, 16, 11, 16)));
        JLabel logTitle = new JLabel("System Activity Log");
        logTitle.setFont(new Font("Monospaced", Font.BOLD, 11));
        logTitle.setForeground(TEXT_MUTED);
        JButton clearBtn = new JButton("Clear");
        clearBtn.setFont(new Font("Monospaced", Font.PLAIN, 10));
        clearBtn.setForeground(TEXT_MUTED);
        clearBtn.setBackground(BG_CARD);
        clearBtn.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        clearBtn.setFocusPainted(false);
        clearBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> logArea.setText(""));
        logHeader.add(logTitle, BorderLayout.WEST);
        logHeader.add(clearBtn, BorderLayout.EAST);

        // Log text area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(BG_CARD);
        logArea.setForeground(new Color(163, 213, 255));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBorder(new EmptyBorder(12, 14, 12, 14));
        logArea.setText("[ System initialised. Ready to accept submissions. ]\n");

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(null);
        logScroll.setBackground(BG_CARD);
        logScroll.getViewport().setBackground(BG_CARD);
        logScroll.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(logHeader, BorderLayout.NORTH);
        panel.add(logScroll, BorderLayout.CENTER);
        return panel;
    }

    // ── Footer ────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 7));
        footer.setBackground(BG_DARK);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_COLOR));
        JLabel note = new JLabel("COS 730  ·  Assignment 2  ·  Baseline Implementation  ·  Task 1");
        note.setFont(new Font("Monospaced", Font.PLAIN, 10));
        note.setForeground(TEXT_MUTED);
        footer.add(note);
        return footer;
    }

    // ════════════════════════════════════════════════════════════════
    //  Sequence-diagram handler: submitResearchOutput(data)
    // ════════════════════════════════════════════════════════════════
    private void handleSubmit() {
        String title  = titleField.getText().trim();
        String author = authorField.getText().trim();

        // Build the data map passed down the sequence diagram chain
        Map<String, Object> data = new HashMap<>();
        if (!title.isEmpty())  data.put("title",    title);
        if (!author.isEmpty()) data.put("author",   author);
        data.put("category", categoryCombo.getSelectedItem());
        data.put("abstract", abstractArea.getText().trim());
        data.put("keywords", keywordsField.getText().trim());

        submitBtn.setEnabled(false);
        setStatus("PROCESSING", ACCENT_AMBER);
        appendLog("\n──────────────────────────────────────────");
        appendLog("► Researcher → UI.submitResearchOutput(data)");

        // Off the EDT so the GUI stays live during processing
        SwingWorker<String, String> worker = new SwingWorker<>() {
            @Override protected String doInBackground() throws Exception {
                // Intercept System.out so backend log lines appear in the GUI
                java.io.OutputStream guiStream = new java.io.OutputStream() {
                    private final StringBuilder sb = new StringBuilder();
                    @Override public void write(int b) {
                        char c = (char) b;
                        if (c == '\n') { publish(sb.toString()); sb.setLength(0); }
                        else sb.append(c);
                    }
                };
                System.setOut(new java.io.PrintStream(guiStream, true));
                return controller.submit(data);   // ← main sequence-diagram call
            }

            @Override protected void process(java.util.List<String> chunks) {
                chunks.forEach(UI.this::appendLog);
            }

            @Override protected void done() {
                try {
                    String result = get();
                    if ("error".equals(result)) {
                        returnError();           // [alt: invalid]
                    } else {
                        appendLog("✔ UI: Pipeline completed successfully.");
                        setStatus("ACCEPTED", ACCENT_GREEN);
                        showDialog(true, "Artefact Submitted",
                                "Your artefact has been submitted and sent for peer review.\n" +
                                "You will be notified of the outcome.");
                    }
                } catch (Exception ex) {
                    appendLog("✘ Error: " + ex.getMessage());
                    setStatus("ERROR", ACCENT_RED);
                } finally {
                    submitBtn.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    // ════════════════════════════════════════════════════════════════
    //  Sequence-diagram method: returnError()
    // ════════════════════════════════════════════════════════════════
    private void returnError() {
        appendLog("✘ Validation failed — title and author are required fields.");
        setStatus("ERROR", ACCENT_RED);
        showDialog(false, "Validation Error",
                "Submission failed: Title and Author are required.\n" +
                "Please complete the form and try again.");
        submitBtn.setEnabled(true);
    }

    // ════════════════════════════════════════════════════════════════
    //  GUI utilities
    // ════════════════════════════════════════════════════════════════
    private void appendLog(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void setStatus(String text, Color color) {
        SwingUtilities.invokeLater(() -> {
            statusBadge.setText("  " + text + "  ");
            statusBadge.setForeground(color);
            statusBadge.setBorder(new CompoundBorder(
                    new LineBorder(color, 1, true),
                    new EmptyBorder(3, 8, 3, 8)));
        });
    }

    private void showDialog(boolean success, String title, String msg) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, msg, title,
                success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE));
    }

    // ── Widget builders ───────────────────────────────────────────────
    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Monospaced", Font.BOLD, 10));
        lbl.setForeground(ACCENT_BLUE);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField styledField(String placeholder) {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(TEXT_MUTED);
                    g2.setFont(getFont().deriveFont(Font.ITALIC, 12f));
                    g2.drawString(placeholder, 12, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        tf.setFont(new Font("Georgia", Font.PLAIN, 13));
        tf.setBackground(BG_INPUT);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT_BLUE);
        tf.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tf.setAlignmentX(LEFT_ALIGNMENT);
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                tf.setBorder(new CompoundBorder(
                        new LineBorder(ACCENT_BLUE, 1, true),
                        new EmptyBorder(8, 12, 8, 12)));
            }
            @Override public void focusLost(FocusEvent e) {
                tf.setBorder(new CompoundBorder(
                        new LineBorder(BORDER_COLOR, 1, true),
                        new EmptyBorder(8, 12, 8, 12)));
                tf.repaint();
            }
        });
        return tf;
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setBackground(BG_INPUT);
        combo.setForeground(TEXT_PRIMARY);
        combo.setFont(new Font("Georgia", Font.PLAIN, 13));
        combo.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        combo.setAlignmentX(LEFT_ALIGNMENT);

        // Custom renderer covering both the closed "editor" cell (index == -1)
        // and every row inside the open popup list.
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                lbl.setFont(new Font("Georgia", Font.PLAIN, 13));
                lbl.setOpaque(true);

                if (index == -1) {
                    // Closed combo "editor" cell — white background, black text
                    lbl.setBackground(Color.WHITE);
                    lbl.setForeground(Color.BLACK);
                    lbl.setBorder(new EmptyBorder(6, 10, 6, 10));
                } else if (isSelected) {
                    // Highlighted row in the open popup
                    lbl.setBackground(ACCENT_BLUE);
                    lbl.setForeground(Color.WHITE);
                    lbl.setBorder(new EmptyBorder(6, 12, 6, 12));
                } else {
                    // Normal row in the open popup
                    lbl.setBackground(BG_INPUT);
                    lbl.setForeground(TEXT_PRIMARY);
                    lbl.setBorder(new EmptyBorder(6, 12, 6, 12));
                }
                return lbl;
            }
        });
    }

    private JPanel fieldBlock(String label, JComponent field) {
        JPanel block = new JPanel();
        block.setOpaque(false);
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Monospaced", Font.PLAIN, 11));
        lbl.setForeground(TEXT_MUTED);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 0, 5, 0));
        block.add(lbl);
        block.add(field);
        return block;
    }

    private Component vgap(int h) { return Box.createRigidArea(new Dimension(0, h)); }

    // ════════════════════════════════════════════════════════════════
    //  Entry point
    // ════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        System.setProperty("apple.awt.application.appearance", "NSAppearanceNameDarkAqua");
        SwingUtilities.invokeLater(() -> {
            try {
                // Cross-platform LAF prevents Windows/macOS from overriding
                // our custom renderer colours inside the combo-box popup.
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {}

            // Belt-and-braces: stamp our colours into UIManager so every
            // combo popup cell picks them up unconditionally.
            UIManager.put("ComboBox.background",          new javax.swing.plaf.ColorUIResource(BG_INPUT));
            UIManager.put("ComboBox.foreground",          new javax.swing.plaf.ColorUIResource(TEXT_PRIMARY));
            UIManager.put("ComboBox.selectionBackground", new javax.swing.plaf.ColorUIResource(ACCENT_BLUE));
            UIManager.put("ComboBox.selectionForeground", new javax.swing.plaf.ColorUIResource(Color.WHITE));
            UIManager.put("List.background",              new javax.swing.plaf.ColorUIResource(BG_INPUT));
            UIManager.put("List.foreground",              new javax.swing.plaf.ColorUIResource(TEXT_PRIMARY));
            UIManager.put("List.selectionBackground",     new javax.swing.plaf.ColorUIResource(ACCENT_BLUE));
            UIManager.put("List.selectionForeground",     new javax.swing.plaf.ColorUIResource(Color.WHITE));

            Database             db              = new Database();
            Researcher           researcher      = new Researcher();
            NotificationService  notifService    = new NotificationService(researcher);
            EvaluationManager    evalManager     = new EvaluationManager(db, notifService);
            ReviewerManager      reviewerManager = new ReviewerManager(db);
            Validator            validator       = new Validator();
            SubmissionController controller      = new SubmissionController(
                    validator, db, reviewerManager, evalManager);

            new UI(controller);
        });
    }
}