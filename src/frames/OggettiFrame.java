package frames;

import dto.Controller;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Pannello "I tuoi oggetti" costruito esternamente a HomeFrame.
 * Usa solo il Controller (niente import di altri DTO) e lavora con stringhe formattate.
 */
public class OggettiFrame extends JFrame {
    private final Controller controller;
    private final String matricola;
    private final JPanel contentPanel;

    // Stato UI
    private DefaultListModel<String> listModel;
    private JList<String> list;
    private JLabel statusLabel;

    public OggettiFrame(Controller controller, String matricola) {
        this.controller = controller;
        this.matricola = matricola;
        setTitle("UninaSwap - I tuoi oggetti");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 560);
        setLocationRelativeTo(null);
        this.contentPanel = buildContentPanel();
        setContentPane(contentPanel);
    }

    public JPanel buildContentPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        header.setBackground(Color.WHITE);
        JLabel title = new JLabel("I tuoi oggetti");
        title.setFont(new Font("Tahoma", Font.BOLD, 20));
        header.add(title, BorderLayout.WEST);
    // Rimosso pulsante "Aggiorna" – il refresh è automatico all'apertura / attivazione finestra e dopo operazioni
        root.add(header, BorderLayout.NORTH);

    listModel = new DefaultListModel<>();
    list = new JList<>(listModel);
    list.setFont(new Font("Tahoma", Font.PLAIN, 13));
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.setFixedCellHeight(-1); // altezza variabile
    list.setCellRenderer(new OggettoCardRenderer()); // ora model contiene record raw (con ID)

        root.add(new JScrollPane(list), BorderLayout.CENTER);

    JPanel bottom = new JPanel(new BorderLayout());
    bottom.setBorder(BorderFactory.createEmptyBorder(6,12,6,12));
    bottom.setBackground(Color.WHITE);
    statusLabel = new JLabel(" ");
    statusLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
    bottom.add(statusLabel, BorderLayout.WEST);
    JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
    rightActions.setOpaque(false);
    JButton addBtn = createPrimaryButton("Aggiungi oggetto");
    addBtn.addActionListener(e -> apriDialogNuovoOggetto());
    rightActions.add(addBtn);
    JButton delBtn = createPrimaryButton("Elimina selezionato");
    delBtn.setEnabled(false);
    delBtn.addActionListener(e -> {
        String sel = list.getSelectedValue();
        if (sel != null) tentaEliminaOggetto(sel);
    });
    JButton editBtn = createPrimaryButton("Modifica selezionato");
    editBtn.setEnabled(false);
    editBtn.addActionListener((ActionEvent e) -> {
        String sel = list.getSelectedValue();
        if (sel != null) apriDialogModificaOggetto(sel);
    });
    list.addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting()) {
            boolean any = list.getSelectedIndex() >= 0;
            delBtn.setEnabled(any);
            editBtn.setEnabled(any);
        }
    });
    rightActions.add(delBtn);
    rightActions.add(editBtn);
    bottom.add(rightActions, BorderLayout.EAST);
    root.add(bottom, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(this::refreshData);
        return root;
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public void refreshContent() {
        refreshData();
    }

    private Component getDialogParent() {
        Component parent = SwingUtilities.getWindowAncestor(contentPanel);
        return parent != null ? parent : contentPanel;
    }

    private void dismissModalOverlay(Component parentCandidate) {
        Component parent = parentCandidate != null ? parentCandidate : getDialogParent();
        if (parent == null) return;
        JRootPane rootPane;
        if (parent instanceof RootPaneContainer container) {
            rootPane = container.getRootPane();
        } else {
            rootPane = SwingUtilities.getRootPane(parent);
        }
        if (rootPane != null) {
            Component glass = rootPane.getGlassPane();
            if (glass != null && glass.isVisible()) {
                glass.setVisible(false);
                glass.repaint();
            }
        }
    }

    // Auto refresh: quando la finestra torna attiva
    {
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowActivated(java.awt.event.WindowEvent e) { refreshData(); }
        });
    }

    private void refreshData() {
        try {
            List<String> raw = controller.oggettiUtenteFormattati(matricola);
            listModel.clear();
            // Inseriamo nel model i record grezzi (ID|Nome|...)
            for (String r : raw) listModel.addElement(r);
            if (statusLabel != null) statusLabel.setText("Oggetti: " + raw.size());
        } catch (Exception ex) {
            Component parent = getDialogParent();
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            dismissModalOverlay(parent);
        }
    }

    private void tentaEliminaOggetto(String rawRecord) {
        if (rawRecord == null) return;
        // Ora il valore selezionato è il record grezzo, possiamo estrarre l'ID in modo robusto
        String id = controller.estraiIdOggetto(rawRecord);
        if (id == null || id.isEmpty()) {
            Component parent = getDialogParent();
            JOptionPane.showMessageDialog(parent, "Impossibile derivare ID oggetto da: " + rawRecord);
            dismissModalOverlay(parent);
            return;
        }
        Component parent = getDialogParent();
        int res = JOptionPane.showConfirmDialog(parent, "Eliminare oggetto?" , "Conferma eliminazione", JOptionPane.YES_NO_OPTION);
        dismissModalOverlay(parent);
        if (res == JOptionPane.YES_OPTION) {
            try {
                controller.eliminaOggetto(id); // eliminazione semplice per ID
                refreshData();
                JOptionPane.showMessageDialog(parent, "Oggetto eliminato");
                dismissModalOverlay(parent);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parent, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                dismissModalOverlay(parent);
            }
        }
    }

    private void apriDialogModificaOggetto(String rawRecord) {
        if (rawRecord == null) return;
        String[] p = rawRecord.split("\\|", -1);
        String id = p.length>0? p[0]:null;
        String nome0 = p.length>1? p[1]:"";
        int numProp0 = 1; try { numProp0 = p.length>2? Integer.parseInt(p[2]):1; } catch (Exception ignore) {}
        String cond0 = p.length>3? p[3]:"";
        String dim0 = p.length>4? p[4]:"";
        String peso0 = p.length>5? p[5]:"";

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4,4,4,4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1; int row=0;
        JTextField nomeField = new JTextField(nome0);
    JLabel numPropLabel = new JLabel(String.valueOf(numProp0));
        JTextField condField = new JTextField(cond0);
        JTextField dimField = new JTextField(dim0);
        JTextField pesoField = new JTextField("-".equals(peso0)? "" : peso0);
        gc.gridx=0; gc.gridy=row; panel.add(new JLabel("Nome:"), gc); gc.gridx=1; panel.add(nomeField, gc); row++;
    gc.gridx=0; gc.gridy=row; panel.add(new JLabel("# Proprietari:"), gc); gc.gridx=1; panel.add(numPropLabel, gc); row++;
        gc.gridx=0; gc.gridy=row; panel.add(new JLabel("Condizione:"), gc); gc.gridx=1; panel.add(condField, gc); row++;
        gc.gridx=0; gc.gridy=row; panel.add(new JLabel("Dimensione (cm):"), gc); gc.gridx=1; panel.add(dimField, gc); row++;
        gc.gridx=0; gc.gridy=row; panel.add(new JLabel("Peso (kg):"), gc); gc.gridx=1; panel.add(pesoField, gc); row++;
        Component parent = getDialogParent();
        int res = JOptionPane.showConfirmDialog(parent, panel, "Modifica Oggetto "+id, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        dismissModalOverlay(parent);
        if (res == JOptionPane.OK_OPTION) {
            try {
                String nome = nomeField.getText();
                int numProp = numProp0;
                String cond = condField.getText();
                String dim = dimField.getText();
                String pesoTxt = pesoField.getText().trim();
                Float peso = null;
                if (!pesoTxt.isEmpty()) {
                    try { peso = Float.parseFloat(pesoTxt.replace(",",".")); } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(parent, "Formato peso non valido", "Errore", JOptionPane.ERROR_MESSAGE);
                        dismissModalOverlay(parent);
                        return;
                    }
                }
                controller.aggiornaOggetto(id, nome, numProp, cond, dim, peso);
                refreshData();
                JOptionPane.showMessageDialog(parent, "Oggetto aggiornato");
                dismissModalOverlay(parent);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parent, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                dismissModalOverlay(parent);
            }
        }
    }

    private JButton createPrimaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Tahoma", Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(100,149,237));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(6,12,6,12));
        return b;
    }

    private void apriDialogNuovoOggetto() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4,4,4,4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        int row = 0;

        JTextField nomeField = new JTextField();
        JLabel numPropValue = new JLabel("1");
        JTextField condField = new JTextField();
        JTextField dimField = new JTextField();
        JTextField pesoField = new JTextField();

    gc.gridx=0; gc.gridy=row; panel.add(new JLabel("Nome:"), gc); gc.gridx=1; panel.add(nomeField, gc); row++;
    gc.gridx=0; gc.gridy=row; panel.add(new JLabel("# Proprietari:"), gc); gc.gridx=1; panel.add(numPropValue, gc); row++;
        gc.gridx=0; gc.gridy=row; panel.add(new JLabel("Condizione:"), gc); gc.gridx=1; panel.add(condField, gc); row++;
        gc.gridx=0; gc.gridy=row; panel.add(new JLabel("Dimensione (cm):"), gc); gc.gridx=1; panel.add(dimField, gc); row++;
        gc.gridx=0; gc.gridy=row; panel.add(new JLabel("Peso (kg):"), gc); gc.gridx=1; panel.add(pesoField, gc); row++;

        Component parent = getDialogParent();
        int res = JOptionPane.showConfirmDialog(parent, panel, "Nuovo Oggetto", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        dismissModalOverlay(parent);
        if (res == JOptionPane.OK_OPTION) {
            try {
                String nome = nomeField.getText();
                int numProp = 1;
                String cond = condField.getText();
                String dim = dimField.getText();
                String pesoTxt = pesoField.getText().trim();
                Float peso = null;
                if (!pesoTxt.isEmpty()) {
                    try { peso = Float.parseFloat(pesoTxt.replace(",",".")); } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(parent, "Formato peso non valido", "Errore", JOptionPane.ERROR_MESSAGE);
                        dismissModalOverlay(parent);
                        return;
                    }
                }
                controller.creaOggetto(nome, numProp, cond, dim, peso, matricola);
                refreshData();
                JOptionPane.showMessageDialog(parent, "Oggetto creato");
                dismissModalOverlay(parent);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parent, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                dismissModalOverlay(parent);
            }
        }
    }

    // Card renderer simile a quelli usati negli annunci/homepage
    // Riceve il record grezzo e mostra la versione formattata.
    private class OggettoCardRenderer extends JPanel implements ListCellRenderer<String> {
        private final JLabel title = new JLabel();
        private final JLabel subtitle = new JLabel();
        private final JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT,4,0));

        OggettoCardRenderer() {
            setLayout(new BorderLayout(6,4));
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(8,10,8,10));
            title.setFont(new Font("Tahoma", Font.BOLD, 13));
            subtitle.setFont(new Font("Tahoma", Font.PLAIN, 11));
            subtitle.setForeground(new Color(90,90,95));
            badges.setOpaque(false);
            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);
            top.add(title, BorderLayout.NORTH);
            top.add(subtitle, BorderLayout.CENTER);
            add(top, BorderLayout.NORTH);
            add(badges, BorderLayout.CENTER);
        }

        private JLabel badge(String txt, Color bg) {
            JLabel l = new JLabel(txt.toUpperCase());
            l.setForeground(Color.WHITE);
            l.setFont(new Font("Tahoma", Font.BOLD, 9));
            l.setOpaque(true);
            l.setBackground(bg);
            l.setBorder(new EmptyBorder(2,6,2,6));
            return l;
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
            // value è il record raw; lo formattiamo per display
            badges.removeAll();
            if (value == null) {
                title.setText("");
                subtitle.setText("");
                return this;
            }
            // value raw: ID|Nome|NumProp|Condizione|Dimensione|Peso
            String[] p = value.split("\\|", -1);
            String id = p.length>0? p[0]:"";
            String nome = p.length>1? p[1]:"";
            String numProp = p.length>2? p[2]:"";
            String cond = p.length>3? p[3]:"";
            String dim = p.length>4? p[4]:"";
            String peso = p.length>5? p[5]:"";

            title.setText(nome);
            subtitle.setText(cond.isEmpty()? "Nessuna condizione" : "Condizione: " + cond);

            // Badges ordinati: dimensione, proprietari, peso
            if (!dim.isEmpty()) badges.add(badge(dim + " (cm)", new Color(46,160,67)));
            if (!numProp.isEmpty()) badges.add(badge(numProp+" proprietari", new Color(0,120,212)));
            if (peso != null && !peso.isEmpty() && !peso.equals("-")) badges.add(badge(peso+"kg", new Color(218,112,37)));
            setBackground(isSelected ? new Color(218,230,247) : Color.WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return this;
        }

        // addIfContains non più necessario, sostituito da parsing strutturato
    }
}
