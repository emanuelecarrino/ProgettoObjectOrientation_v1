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

    private String currentPassword = "";
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

        JPanel avatarWrap = new JPanel();
        avatarWrap.setBackground(Color.WHITE);
        avatarWrap.setBorder(new EmptyBorder(24, 24, 12, 24));

        JLabel avatar = new JLabel();
        avatar.setPreferredSize(new Dimension(140, 140));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setVerticalAlignment(SwingConstants.CENTER);
        avatar.setOpaque(true);
        avatar.setBackground(new Color(100, 149, 237));
        avatar.setForeground(Color.WHITE);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 36));
        String initials = matricola != null && matricola.length() > 3
                ? matricola.substring(0, 3).toUpperCase()
                : matricola != null ? matricola.toUpperCase() : "?";
        avatar.setText(initials);
        avatar.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 2, true));
        avatarWrap.add(avatar);
        left.add(avatarWrap, BorderLayout.NORTH);

        JPanel meta = new JPanel(new GridLayout(0, 1));
        meta.setBackground(Color.WHITE);
        meta.setBorder(new EmptyBorder(8, 24, 24, 24));
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
        genereBox = new JComboBox<>(new String[]{"", "M", "F", "ALTRO"});
        giornoCombo = new JComboBox<>(buildDayModel());
        meseCombo = new JComboBox<>(buildMonthModel());
        annoCombo = new JComboBox<>(buildYearModel());

        for (JTextField tf : new JTextField[]{nomeField, cognomeField, emailField, usernameField}) {
            tf.setColumns(18);
            applyComponentWidth(tf, 260);
        }

        for (JComboBox<String> combo : new JComboBox[]{giornoCombo, meseCombo, annoCombo}) {
            enhanceCombo(combo);
        }
        applyComponentWidth(giornoCombo, 72);
        applyComponentWidth(meseCombo, 72);
        applyComponentWidth(annoCombo, 88);
        genereBox.setPrototypeDisplayValue("ALTRO");
        enhanceCombo(genereBox);
        applyComponentWidth(genereBox, 110);

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

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setBackground(Color.WHITE);

        JButton changePasswordButton = createLinkButton("\uD83D\uDD12 Cambia password");
        changePasswordButton.addActionListener(e -> openPasswordDialog());
        actions.add(changePasswordButton);

        JButton saveButton = new JButton("Salva");
        saveButton.setFocusPainted(false);
        saveButton.setBackground(new Color(40, 167, 69));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("Tahoma", Font.BOLD, 14));
        saveButton.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        saveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveButton.addActionListener(e -> onSaveProfilo());
        actions.add(saveButton);
        right.add(actions, gbc);

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

    private void onSaveProfilo() {
        try {
            String nome = safeTrim(nomeField.getText());
            String cognome = safeTrim(cognomeField.getText());
            String email = safeTrim(emailField.getText());
            String username = safeTrim(usernameField.getText());
            String genere = normalizeGenere((String) genereBox.getSelectedItem());
            String dataIso = composeBirthDateIso();

            if (currentPassword == null || currentPassword.isBlank()) {
                throw new IllegalStateException("Password corrente non disponibile. Ricarica il profilo e riprova.");
            }

            controller.aggiornaProfilo(matricola, nome, cognome, email, username, currentPassword, dataIso, genere);
            currentUsername = username;
            showInfoDialog("Profilo aggiornato con successo.");
            loadProfilo();
        } catch (IllegalArgumentException ex) {
            showErrorDialog("Impossibile aggiornare il profilo", ex);
        } catch (Exception ex) {
            showErrorDialog("Impossibile aggiornare il profilo", ex);
        }
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
            currentPassword = nullToEmpty(fields[4]);
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

    private void enhanceCombo(JComboBox<?> combo) {
        if (combo == null) {
            return;
        }
        combo.setFont(BASE_FONT);
        combo.setForeground(FIELD_TEXT);
        combo.setBackground(Color.WHITE);
        combo.setOpaque(true);
        combo.setFocusable(true);
        combo.setBorder(new CompoundBorder(new LineBorder(FIELD_BORDER, 1, true), new EmptyBorder(4, 10, 4, 10)));
        combo.setRenderer(new CoolComboRenderer(combo));
        combo.setUI(new CoolComboUI());
        combo.setMaximumRowCount(12);
        if (combo.getClientProperty("coolComboStyled") == null) {
            combo.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    combo.setBorder(new CompoundBorder(new LineBorder(FIELD_BORDER_FOCUS, 1, true), new EmptyBorder(4, 10, 4, 10)));
                }

                @Override
                public void focusLost(FocusEvent e) {
                    combo.setBorder(new CompoundBorder(new LineBorder(FIELD_BORDER, 1, true), new EmptyBorder(4, 10, 4, 10)));
                }
            });
            combo.putClientProperty("coolComboStyled", Boolean.TRUE);
        }
    }

    private JButton createLinkButton(String text) {
        final String normalHtml = "<html><span style='font-weight:600; color:#3D63F2;'>" + text + "</span></html>";
        final String hoverHtml = "<html><span style='font-weight:600; color:#1F3FE6; text-decoration:underline;'>" + text + "</span></html>";
        JButton button = new JButton(normalHtml);
        button.setFont(BASE_FONT.deriveFont(13f));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
    button.setMargin(new Insets(0, 0, 0, 0));
        button.setOpaque(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setText(hoverHtml);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setText(normalHtml);
            }
        });
        return button;
    }

    private static class CoolComboUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            BasicArrowButton arrow = new BasicArrowButton(BasicArrowButton.SOUTH, Color.WHITE, FIELD_BORDER, FIELD_TEXT, Color.WHITE);
            arrow.setBorder(new EmptyBorder(0, 8, 0, 8));
            arrow.setFocusPainted(false);
            arrow.setContentAreaFilled(false);
            arrow.setOpaque(false);
            return arrow;
        }
    }

    private static class CoolComboRenderer extends DefaultListCellRenderer {
        private final JComboBox<?> owner;

        private CoolComboRenderer(JComboBox<?> owner) {
            this.owner = owner;
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                       boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setFont(BASE_FONT);
            label.setOpaque(true);
            label.setBorder(new EmptyBorder(6, 12, 6, 12));

            if (isSelected) {
                label.setBackground(FIELD_SELECTION_BG);
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground(Color.WHITE);
                label.setForeground(FIELD_TEXT);
            }

            boolean placeholder = false;
            if (value instanceof String str) {
                placeholder = isPlaceholder(str);
            }

            if (index == 0 || (index == -1 && owner.getSelectedIndex() == 0)) {
                placeholder = true;
            }

            if (placeholder && !isSelected) {
                label.setForeground(FIELD_PLACEHOLDER);
            }

            return label;
        }

        private boolean isPlaceholder(String value) {
            String lower = value.trim().toLowerCase();
            return lower.isEmpty() || lower.equals("giorno") || lower.equals("mese") || lower.equals("anno")
                    || lower.equals("tipo") || lower.equals("categoria") || lower.startsWith("(")
                    || lower.contains("seleziona") || lower.contains("default");
        }
    }

    private void openPasswordDialog() {
        if (currentUsername == null || currentUsername.isBlank()) {
            showErrorDialog("Impossibile aggiornare la password", new IllegalStateException("Username non disponibile."));
            return;
        }

        JPasswordField currentField = new JPasswordField(12);
        JPasswordField newField = new JPasswordField(12);
        JPasswordField confirmField = new JPasswordField(12);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;

        panel.add(new JLabel("Password attuale"), gc);
        gc.gridx = 1;
        panel.add(currentField, gc);

        gc.gridx = 0;
        gc.gridy++;
        panel.add(new JLabel("Nuova password"), gc);
        gc.gridx = 1;
        panel.add(newField, gc);

        gc.gridx = 0;
        gc.gridy++;
        panel.add(new JLabel("Conferma nuova password"), gc);
        gc.gridx = 1;
        panel.add(confirmField, gc);

    Object[] options = {"Aggiorna", "Annulla"};
    Component parent = getDialogParent();
    int res = JOptionPane.showOptionDialog(parent, panel, "Cambia password",
        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
    dismissModalOverlay(parent);
        if (res == 0) {
            char[] current = currentField.getPassword();
            char[] nuova = newField.getPassword();
            char[] conferma = confirmField.getPassword();
            try {
                String currentStr = new String(current).trim();
                String nuovaStr = new String(nuova).trim();
                String confermaStr = new String(conferma).trim();

                if (currentStr.isEmpty() || nuovaStr.isEmpty() || confermaStr.isEmpty()) {
                    throw new IllegalArgumentException("Compila tutti i campi della password.");
                }
                if (!nuovaStr.equals(confermaStr)) {
                    throw new IllegalArgumentException("Le nuove password non coincidono.");
                }
                if (nuovaStr.length() < 6) {
                    throw new IllegalArgumentException("La nuova password deve contenere almeno 6 caratteri.");
                }

                controller.cambiaPassword(currentUsername, currentStr, nuovaStr);
                currentPassword = nuovaStr;
                showInfoDialog("Password aggiornata correttamente.");
            } catch (Exception ex) {
                showErrorDialog("Impossibile aggiornare la password", ex);
            } finally {
                Arrays.fill(current, '\0');
                Arrays.fill(nuova, '\0');
                Arrays.fill(conferma, '\0');
            }
        }
    }

    private String composeBirthDateIso() {
        if (giornoCombo.getSelectedIndex() <= 0 || meseCombo.getSelectedIndex() <= 0 || annoCombo.getSelectedIndex() <= 0) {
            throw new IllegalArgumentException("Seleziona giorno, mese e anno di nascita.");
        }

        String dayStr = (String) giornoCombo.getSelectedItem();
        String monthStr = (String) meseCombo.getSelectedItem();
        String yearStr = (String) annoCombo.getSelectedItem();

        try {
            int day = Integer.parseInt(dayStr);
            int month = Integer.parseInt(monthStr);
            int year = Integer.parseInt(yearStr);
            LocalDate date = LocalDate.of(year, month, day);
            return date.toString();
        } catch (NumberFormatException | DateTimeException ex) {
            throw new IllegalArgumentException("La data di nascita selezionata non è valida.", ex);
        }
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

    private void applyComponentWidth(JComponent component, int width) {
        int height = 36;
        Dimension size = new Dimension(width, height);
        component.setPreferredSize(size);
        component.setMinimumSize(size);
        component.setMaximumSize(size);
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

    private void showErrorDialog(String titolo, Throwable ex) {
        Component parent = getDialogParent();
        String messaggio = estraiMessaggioPulito(ex);
        if (messaggio == null || messaggio.isBlank()) {
            messaggio = "Operazione non riuscita.";
        }
        JOptionPane.showMessageDialog(parent, titolo + ": " + messaggio, "Errore", JOptionPane.ERROR_MESSAGE);
        dismissModalOverlay(parent);
    }

    private String estraiMessaggioPulito(Throwable ex) {
        if (ex == null) return null;

        String messaggio = normalizzaMessaggio(ex.getMessage());

        if (ex instanceof ApplicationException) {
            if (isMessaggioGenerico(messaggio)) {
                String daCausa = estraiMessaggioPulito(ex.getCause());
                if (daCausa != null && !daCausa.isBlank()) {
                    return daCausa;
                }
            }
            if (messaggio != null && !messaggio.isBlank()) {
                return messaggio;
            }
        }

        if (messaggio != null && !messaggio.isBlank()) {
            return messaggio;
        }

        if (ex.getCause() != null && ex.getCause() != ex) {
            return estraiMessaggioPulito(ex.getCause());
        }

        return ex.getClass().getSimpleName();
    }

    private String normalizzaMessaggio(String raw) {
        if (raw == null) return null;
        String trimmed = raw.strip();
        if (trimmed.isEmpty()) return null;
        int newline = trimmed.indexOf('\n');
        if (newline >= 0) {
            trimmed = trimmed.substring(0, newline).strip();
        }
        if (trimmed.startsWith("ERRORE:")) {
            trimmed = trimmed.substring("ERRORE:".length()).strip();
        } else if (trimmed.startsWith("ERROR:")) {
            trimmed = trimmed.substring("ERROR:".length()).strip();
        }
        return trimmed;
    }

    private boolean isMessaggioGenerico(String msg) {
        if (msg == null) return true;
        String lower = msg.toLowerCase();
        return lower.startsWith("errore") || lower.contains("persistence") || lower.contains("applicativo");
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
