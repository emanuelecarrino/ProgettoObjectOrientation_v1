package frames;

import dto.Controller;
import exception.ApplicationException;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

public class ProfiloFrame extends JFrame {
    private final Controller controller;
    private final String matricola;
    private final JPanel contentPanel;

    private JTextField nomeField;
    private JTextField cognomeField;
    private JTextField emailField;
    private JTextField usernameField;
    private JComboBox<String> genereBox;
    private JComboBox<String> giornoCombo;
    private JComboBox<String> meseCombo;
    private JComboBox<String> annoCombo;

    private String currentUsername = "";

    private static final Color FIELD_BORDER = new Color(214, 219, 226);
    private static final Color FIELD_BORDER_FOCUS = new Color(99, 134, 235);
    private static final Color FIELD_TEXT = new Color(45, 52, 63);
    private static final Color FIELD_PLACEHOLDER = new Color(146, 152, 161);
    private static final Color FIELD_SELECTION_BG = new Color(99, 134, 235);
    private static final Font BASE_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public ProfiloFrame(Controller controller, String matricola) {
        this.controller = controller;
        this.matricola = matricola;
        setTitle("UninaSwap - Profilo");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        this.contentPanel = buildContentPanel();
        setContentPane(contentPanel);
    }

    public JPanel buildContentPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(250, 251, 253));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(18, 24, 18, 24));
        JLabel headerLabel = new JLabel("Il tuo profilo", SwingConstants.LEFT);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.add(headerLabel, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 24, 24, 24),
                BorderFactory.createLineBorder(new Color(230, 230, 235), 1, true)
        ));
        card.setBackground(Color.WHITE);

        card.add(buildLeftColumn(), BorderLayout.WEST);
        card.add(buildFormPanel(), BorderLayout.CENTER);

        root.add(card, BorderLayout.CENTER);

        return root;
    }

    private JPanel buildLeftColumn() {
        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(Color.WHITE);
        left.setPreferredSize(new Dimension(260, 0));

        JPanel meta = new JPanel(new GridLayout(0, 1));
        meta.setBackground(Color.WHITE);
    meta.setBorder(new EmptyBorder(24, 24, 24, 24));
    JLabel title = new JLabel("Profilo personale");
    title.setFont(new Font("Segoe UI", Font.BOLD, 16));
    title.setForeground(new Color(60, 70, 80));
    meta.add(title);

    JLabel matricolaLabel = new JLabel("Matricola: " + (matricola != null ? matricola : ""));
    matricolaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    matricolaLabel.setForeground(new Color(80, 80, 80));
    meta.add(matricolaLabel);
        left.add(meta, BorderLayout.CENTER);

        return left;
    }

    private JPanel buildFormPanel() {
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;

        nomeField = createTextField();
        cognomeField = createTextField();
        emailField = createTextField();
        usernameField = createTextField();

        for (JTextField tf : new JTextField[]{nomeField, cognomeField, emailField, usernameField}) {
            tf.setEditable(false);
            tf.setBorder(null);
            tf.setOpaque(false);
        }

        genereBox = new JComboBox<>(new String[]{"", "M", "F", "ALTRO"});
        giornoCombo = new JComboBox<>(buildDayModel());
        meseCombo = new JComboBox<>(buildMonthModel());
        annoCombo = new JComboBox<>(buildYearModel());

        for (JComboBox<?> combo : new JComboBox[]{genereBox, giornoCombo, meseCombo, annoCombo}) {
            combo.setEnabled(false);
            combo.setUI(new BasicComboBoxUI());
            combo.setBorder(null);
            combo.setOpaque(false);
        }

        addLabeledField(right, gbc, "Nome", nomeField);
        addLabeledField(right, gbc, "Cognome", cognomeField);
        addLabeledField(right, gbc, "Email", emailField);
        addLabeledField(right, gbc, "Username", usernameField);

        JPanel birthDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        birthDatePanel.setOpaque(false);
        birthDatePanel.add(giornoCombo);
        birthDatePanel.add(meseCombo);
        birthDatePanel.add(annoCombo);

        addLabeledField(right, gbc, "Data di nascita", birthDatePanel);
        addLabeledField(right, gbc, "Genere", genereBox);

        resetDateCombos();
        loadProfilo();

        return right;
    }

    private void addLabeledField(JPanel parent, GridBagConstraints gbc, String label, JComponent component) {
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        parent.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        int previousFill = gbc.fill;
        int previousAnchor = gbc.anchor;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        parent.add(component, gbc);
        gbc.fill = previousFill;
        gbc.anchor = previousAnchor;
        gbc.gridy++;
    }

    private void loadProfilo() {
        try {
            String[] fields = controller.recuperaProfiloFields(matricola);
            if (fields == null || fields.length < 7) {
                return;
            }
            nomeField.setText(nullToEmpty(fields[0]));
            cognomeField.setText(nullToEmpty(fields[1]));
            emailField.setText(nullToEmpty(fields[2]));
            currentUsername = nullToEmpty(fields[3]);
            usernameField.setText(currentUsername);
            populateDateCombos(fields[5]);
            String genere = normalizeGenere(fields[6]);
            if (!genere.isBlank()) {
                genereBox.setSelectedItem(genere);
            } else {
                genereBox.setSelectedIndex(0);
            }
        } catch (ApplicationException ex) {
            showErrorDialog("Impossibile caricare il profilo", ex);
        } catch (Exception ex) {
            showErrorDialog("Errore inatteso durante il caricamento del profilo", ex);
        }
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(BASE_FONT);
        field.setForeground(FIELD_TEXT);
        field.setBackground(Color.WHITE);
        field.setBorder(new CompoundBorder(new LineBorder(FIELD_BORDER, 1, true), new EmptyBorder(6, 12, 6, 12)));
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(new CompoundBorder(new LineBorder(FIELD_BORDER_FOCUS, 1, true), new EmptyBorder(6, 12, 6, 12)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(new CompoundBorder(new LineBorder(FIELD_BORDER, 1, true), new EmptyBorder(6, 12, 6, 12)));
            }
        });
        return field;
    }

    private void populateDateCombos(String isoDate) {
        resetDateCombos();
        if (isoDate == null || isoDate.isBlank()) {
            return;
        }
        try {
            LocalDate date = LocalDate.parse(isoDate.trim());
            selectComboValue(giornoCombo, String.format("%02d", date.getDayOfMonth()));
            selectComboValue(meseCombo, String.format("%02d", date.getMonthValue()));
            selectComboValue(annoCombo, Integer.toString(date.getYear()));
        } catch (DateTimeParseException e) {
            // lascio selezioni vuote se formato non valido
        }
    }

    private void selectComboValue(JComboBox<String> combo, String value) {
        if (combo == null || value == null) return;
        ComboBoxModel<String> model = combo.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            if (value.equals(model.getElementAt(i))) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void resetDateCombos() {
        if (giornoCombo != null) giornoCombo.setSelectedIndex(0);
        if (meseCombo != null) meseCombo.setSelectedIndex(0);
        if (annoCombo != null) annoCombo.setSelectedIndex(0);
    }

    private String[] buildDayModel() {
        String[] values = new String[32];
        values[0] = "Giorno";
        for (int i = 1; i <= 31; i++) {
            values[i] = String.format("%02d", i);
        }
        return values;
    }

    private String[] buildMonthModel() {
        String[] values = new String[13];
        values[0] = "Mese";
        for (int i = 1; i <= 12; i++) {
            values[i] = String.format("%02d", i);
        }
        return values;
    }

    private String[] buildYearModel() {
        int currentYear = LocalDate.now().getYear();
        int minYear = currentYear - 100;
        String[] values = new String[(currentYear - minYear) + 2];
        values[0] = "Anno";
        int idx = 1;
        for (int year = currentYear; year >= minYear; year--) {
            values[idx++] = Integer.toString(year);
        }
        return values;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String normalizeGenere(String value) {
        if (value == null) return "";
        String upper = value.trim().toUpperCase();
        return switch (upper) {
            case "M", "F", "ALTRO" -> upper;
            default -> "";
        };
    }

    private Component getDialogParent() {
        Component parent = SwingUtilities.getWindowAncestor(contentPanel);
        if (parent == null) {
            parent = SwingUtilities.getRoot(contentPanel);
        }
        return parent != null ? parent : contentPanel;
    }

    private void showInfoDialog(String message) {
        Component parent = getDialogParent();
        JOptionPane.showMessageDialog(parent, message, "Informazioni", JOptionPane.INFORMATION_MESSAGE);
        dismissModalOverlay(parent);
    }

    private void showErrorDialog(String titolo, Exception ex) {
        Component parent = getDialogParent();
        String msg = ex != null ? ex.getMessage() : null;
        if (msg == null || msg.isBlank()) msg = "Operazione non riuscita.";
        JOptionPane.showMessageDialog(parent, titolo + ": " + msg, "Errore", JOptionPane.ERROR_MESSAGE);
        dismissModalOverlay(parent);
    }

    // Overload per messaggi UI specifici senza creare nuove Exception
    private void showErrorDialog(String titolo, String messaggio) {
        Component parent = getDialogParent();
        String msg = (messaggio != null && !messaggio.isBlank()) ? messaggio : "Operazione non riuscita.";
        JOptionPane.showMessageDialog(parent, titolo + ": " + msg, "Errore", JOptionPane.ERROR_MESSAGE);
        dismissModalOverlay(parent);
    }

    private void dismissModalOverlay(Component parentCandidate) {
        Component resolved = parentCandidate != null ? parentCandidate : getDialogParent();
        if (resolved == null) return;
        JRootPane rootPaneRef = (resolved instanceof RootPaneContainer container)
                ? container.getRootPane()
                : SwingUtilities.getRootPane(resolved);
        if (rootPaneRef != null) {
            Component glass = rootPaneRef.getGlassPane();
            if (glass != null && glass.isVisible()) {
                glass.setVisible(false);
                glass.repaint();
            }
        }
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public void refreshContent() {
        loadProfilo();
    }
}
