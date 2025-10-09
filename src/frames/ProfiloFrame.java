package frames;

import dto.Controller;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ProfiloFrame extends JFrame {
    private final Controller controller;
    private final String matricola;
    private final JPanel contentPanel;

    private JTextField nomeField;
    private JTextField cognomeField;
    private JTextField emailField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField dataField;
    private JComboBox<String> genereBox;

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

        nomeField = new JTextField();
        cognomeField = new JTextField();
        emailField = new JTextField();
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        passwordField.setEchoChar('\u2022');
        dataField = new JTextField();
        genereBox = new JComboBox<>(new String[]{"", "Male", "Female", "Other"});

        addLabeledField(right, gbc, "Nome", nomeField);
        addLabeledField(right, gbc, "Cognome", cognomeField);
        addLabeledField(right, gbc, "Email", emailField);
        addLabeledField(right, gbc, "Username", usernameField);
        addLabeledField(right, gbc, "Password", passwordField);
        addLabeledField(right, gbc, "Data di nascita (YYYY-MM-DD)", dataField);
        addLabeledField(right, gbc, "Genere", genereBox);

        loadProfilo();

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setBackground(Color.WHITE);

        JButton saveButton = new JButton("Salva");
        saveButton.setFocusPainted(false);
        saveButton.setBackground(new Color(40, 167, 69));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("Tahoma", Font.BOLD, 14));
        saveButton.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        saveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveButton.addActionListener(this::onSaveProfilo);
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
        parent.add(component, gbc);
        gbc.gridy++;
    }

    private void onSaveProfilo(ActionEvent event) {
        try {
            String nome = nomeField.getText();
            String cognome = cognomeField.getText();
            String email = emailField.getText();
            String username = usernameField.getText();
            String pwd = new String(passwordField.getPassword());
            String data = dataField.getText();
            String genere = (String) genereBox.getSelectedItem();

            controller.aggiornaProfilo(matricola, nome, cognome, email, username, pwd, data, genere);
            JOptionPane.showMessageDialog(this, "Profilo aggiornato con successo", "Successo", JOptionPane.INFORMATION_MESSAGE);
            loadProfilo();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadProfilo() {
        try {
            String[] fields = controller.recuperaProfiloFields(matricola);
            if (fields == null || fields.length < 7) {
                return;
            }
            nomeField.setText(fields[0]);
            cognomeField.setText(fields[1]);
            emailField.setText(fields[2]);
            usernameField.setText(fields[3]);
            passwordField.setText(fields[4]);
            dataField.setText(fields[5]);
            String genere = fields[6];
            if (genere != null && !genere.isBlank()) {
                genereBox.setSelectedItem(genere);
            } else {
                genereBox.setSelectedIndex(0);
            }
        } catch (Exception ignore) {
            // lascia i dati attuali se non recuperabili
        }
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public void refreshContent() {
        loadProfilo();
    }
}
