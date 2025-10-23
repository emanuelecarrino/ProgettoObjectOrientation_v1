package frames;

import dto.Controller;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Finestra per gestione annunci (ricerca, filtri, creazione annuncio, creazione offerta).
 * Mantiene la logica originale, riorganizzata con commenti-separatore compatti.
 */
public class AnnunciFrame extends JFrame {

    // === COSTANTI ===
    private static final String PLACEHOLDER_COMMENTO = "Commento (facoltativo)";
    private static final Color COMBO_BORDER = new Color(214, 219, 226);
    private static final Color COMBO_BORDER_FOCUS = new Color(99, 134, 235);
    private static final Color COMBO_TEXT = new Color(45, 52, 63);
    private static final Color COMBO_PLACEHOLDER = new Color(146, 152, 161);
    private static final Color COMBO_SELECTION_BG = new Color(99, 134, 235);
    private static final Font COMBO_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    // === CAMPI ISTANZA / STATO ===
    private final Controller controller;
    private final String matricola;
    private final JPanel contentPanel;

    private DefaultListModel<String> annunciListModel;
    private JList<String> annunciList;
    private JComboBox<String> tipoFilter;
    private JComboBox<String> categoriaFilter;
    private JTextField searchField;
    private JComboBox<String> orderByCombo;
    private JLabel statusLabel;
    private java.util.List<String> annunciUltimaRicercaRaw = new java.util.ArrayList<>();


    // === COSTRUTTORE ===
    public AnnunciFrame(Controller controller, String matricola) {
        this.controller = controller;
        this.matricola = matricola;

        setTitle("UninaSwap - Annunci");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        this.contentPanel = buildContentPanel();
        setContentPane(contentPanel);
    }


    // === BUILD UI PRINCIPALE ===
    public JPanel buildContentPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        // --- HEADER: ricerca + filtri + ordine ---
        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        header.setBackground(Color.WHITE);

        // sinistra: search
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftHeader.setOpaque(false);

        searchField = new JTextField(26);
        searchField.setPreferredSize(new Dimension(380, 28));
        searchField.putClientProperty("placeholder", "Cerca matricola o titolo");
        searchField.setForeground(Color.GRAY);
        searchField.setText("Cerca matricola o titolo");

        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (searchField.getForeground().equals(Color.GRAY)) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().trim().isEmpty()) {
                    searchField.setForeground(Color.GRAY);
                    searchField.setText("Cerca matricola o titolo");
                }
            }
        });

        searchField.addActionListener(e -> eseguiRicerca());
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void upd() { eseguiRicerca(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { upd(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { upd(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { upd(); }
        });

        leftHeader.add(searchField);
        header.add(leftHeader, BorderLayout.WEST);

        // destra: filtri + ordine
    JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
    controls.setOpaque(false);

        tipoFilter = new JComboBox<>(buildTipoFiltroModel());
        categoriaFilter = new JComboBox<>(buildCategoriaFiltroModel());
        orderByCombo = new JComboBox<>(new String[] { "Default", "Prezzo crescente", "Prezzo decrescente" });
    styleCombo(tipoFilter, 130);
    styleCombo(categoriaFilter, 150);
    styleCombo(orderByCombo, 180);

        tipoFilter.addActionListener(e -> eseguiRicerca());
        categoriaFilter.addActionListener(e -> eseguiRicerca());
        orderByCombo.addActionListener(e -> eseguiRicerca());

    controls.add(createFilterLabel("Tipo:"));
        controls.add(tipoFilter);
    controls.add(createFilterLabel("Categoria:"));
        controls.add(categoriaFilter);
    controls.add(createFilterLabel("Ordina:"));
        controls.add(orderByCombo);

        header.add(controls, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);

        // --- CENTRO: lista annunci ---
        annunciListModel = new DefaultListModel<>();
        annunciList = new JList<>(annunciListModel);
        annunciList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    annunciList.setFixedCellHeight(-1);
        annunciList.setCellRenderer(new AnnuncioCardRenderer());
        root.add(new JScrollPane(annunciList), BorderLayout.CENTER);

        // selezione annuncio => abilita/disabilita pulsante "Offri"
        annunciList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int i = annunciList.getSelectedIndex();
                JButton offriBtn = findOffriButtonInFooter(root);
                if (i >= 0 && i < annunciUltimaRicercaRaw.size()) {
                    String raw = annunciUltimaRicercaRaw.get(i);
                    String[] parts = raw.split("\\|", -1);
                    String tipo = parts.length > 2 ? parts[2] : "";
                    String creatore = parts.length > 5 ? parts[5] : null;
                    boolean isMio = creatore != null && creatore.equalsIgnoreCase(matricola);
                    if (offriBtn != null) {
                        if (isMio) {
                            offriBtn.setEnabled(false);
                            offriBtn.setToolTipText("Questo è un tuo annuncio");
                        } else if ("Regalo".equalsIgnoreCase(tipo)) {
                            offriBtn.setEnabled(true);
                            offriBtn.setToolTipText("Annuncio Regalo: puoi lasciare solo un commento");
                        } else {
                            offriBtn.setEnabled(true);
                            offriBtn.setToolTipText("Crea offerta");
                        }
                    }
                } else {
                    if (offriBtn != null) {
                        offriBtn.setEnabled(false);
                        offriBtn.setToolTipText("Seleziona un annuncio");
                    }
                }
            }
        });

        annunciList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int index = annunciList.locationToIndex(e.getPoint());
                    if (index >= 0 && index < annunciUltimaRicercaRaw.size()) {
                        Rectangle cellBounds = annunciList.getCellBounds(index, index);
                        if (cellBounds != null && cellBounds.contains(e.getPoint())) {
                            mostraDettaglioAnnuncio(annunciUltimaRicercaRaw.get(index));
                        }
                    }
                }
            }
        });

        // --- FOOTER: stato + azioni ---
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(BorderFactory.createEmptyBorder(4, 12, 8, 12));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
        bottom.add(statusLabel, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        JButton aggiungiBtn = createPrimaryButton("Aggiungi annuncio");
        JButton offriBtn = createPrimaryButton("Offri");
        offriBtn.setEnabled(false);
        offriBtn.setToolTipText("Seleziona un annuncio");

        aggiungiBtn.addActionListener(e -> apriDialogNuovoAnnuncio());
        offriBtn.addActionListener(e -> apriDialogOffri());

        rightActions.add(aggiungiBtn);
        rightActions.add(offriBtn);

        bottom.add(rightActions, BorderLayout.EAST);
        root.add(bottom, BorderLayout.SOUTH);

        // inizializza dati
        SwingUtilities.invokeLater(this::eseguiRicerca);

        return root;
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public void refreshContent() {
        eseguiRicerca();
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


    
    // === UTIL: gestione messaggi di errore ===
    
    private void showErrorDialog(Component parent, String titolo, Exception ex) {
        String msg = ex != null ? ex.getMessage() : null;
        if (msg == null || msg.isBlank()) msg = "Operazione non riuscita.";
        JOptionPane.showMessageDialog(parent, titolo + ": " + msg, "Errore", JOptionPane.ERROR_MESSAGE);
    }

    // Overload per mostrare un messaggio senza creare/propagare eccezioni dal livello UI
    private void showErrorDialog(Component parent, String titolo, String messaggio) {
        String msg = (messaggio != null && !messaggio.isBlank()) ? messaggio : "Operazione non riuscita.";
        JOptionPane.showMessageDialog(parent, titolo + ": " + msg, "Errore", JOptionPane.ERROR_MESSAGE);
    }


    // === METODI DI SUPPORTO COMPONENTI ===
    private JButton createPrimaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Tahoma", Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(100, 149, 237));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(6, 12, 6, 12));
        return b;
    }

    private JButton findOffriButtonInFooter(Container root) {
        // semplice ricerca nel pannello footer per trovare il pulsante "Offri"
        for (Component c : ((JPanel) ((BorderLayout) root.getLayout()).getLayoutComponent(root, BorderLayout.SOUTH)).getComponents()) {
            if (c instanceof JPanel) {
                for (Component inner : ((JPanel) c).getComponents()) {
                    if (inner instanceof JButton && "Offri".equals(((JButton) inner).getText())) {
                        return (JButton) inner;
                    }
                }
            }
        }
        return null;
    }

    private String[] buildTipoFiltroModel() {
        java.util.List<String> tipi = controller.elencoTipiAnnuncio();
        java.util.List<String> model = new java.util.ArrayList<>();
        model.add("Tutti");
        model.addAll(tipi);
        return model.toArray(new String[0]);
    }

    private String[] buildCategoriaFiltroModel() {
        java.util.List<String> cat = controller.elencoCategorieAnnuncio();
        java.util.List<String> model = new java.util.ArrayList<>();
        model.add("Tutte");
        model.addAll(cat);
        return model.toArray(new String[0]);
    }

    private JLabel createFilterLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(COMBO_FONT);
        label.setForeground(COMBO_TEXT.darker());
        return label;
    }

    private void styleCombo(JComboBox<?> combo) {
        styleCombo(combo, -1);
    }

    private void styleCombo(JComboBox<?> combo, int width) {
        if (combo == null) {
            return;
        }
        applyComboStyle(combo);
        if (width > 0) {
            Dimension size = new Dimension(width, 34);
            combo.setPreferredSize(size);
            combo.setMinimumSize(size);
            combo.setMaximumSize(size);
        }
    }

    private void applyComboStyle(JComboBox<?> combo) {
        if (combo == null) {
            return;
        }
        combo.setFont(COMBO_FONT);
        combo.setForeground(COMBO_TEXT);
        combo.setBackground(Color.WHITE);
        combo.setOpaque(true);
        combo.setFocusable(true);
        combo.setBorder(new CompoundBorder(new LineBorder(COMBO_BORDER, 1, true), new EmptyBorder(4, 10, 4, 10)));
        combo.setRenderer(new FilterComboRenderer(combo));
        combo.setUI(new FilterComboUI());
        combo.setMaximumRowCount(12);
        if (combo.getClientProperty("styledCombo") == null) {
            combo.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    combo.setBorder(new CompoundBorder(new LineBorder(COMBO_BORDER_FOCUS, 1, true), new EmptyBorder(4, 10, 4, 10)));
                }

                @Override
                public void focusLost(FocusEvent e) {
                    combo.setBorder(new CompoundBorder(new LineBorder(COMBO_BORDER, 1, true), new EmptyBorder(4, 10, 4, 10)));
                }
            });
            combo.putClientProperty("styledCombo", Boolean.TRUE);
        }
    }

    private static class FilterComboUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            BasicArrowButton arrow = new BasicArrowButton(BasicArrowButton.SOUTH, Color.WHITE, COMBO_BORDER, COMBO_TEXT, Color.WHITE);
            arrow.setBorder(new EmptyBorder(0, 8, 0, 8));
            arrow.setFocusPainted(false);
            arrow.setContentAreaFilled(false);
            arrow.setOpaque(false);
            return arrow;
        }
    }

    private static class FilterComboRenderer extends DefaultListCellRenderer {
        private final JComboBox<?> owner;

        private FilterComboRenderer(JComboBox<?> owner) {
            this.owner = owner;
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                       boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setFont(COMBO_FONT);
            label.setOpaque(true);
            label.setBorder(new EmptyBorder(6, 12, 6, 12));

            if (isSelected) {
                label.setBackground(COMBO_SELECTION_BG);
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground(Color.WHITE);
                label.setForeground(COMBO_TEXT);
            }

            boolean placeholder = false;
            if (value instanceof String str) {
                placeholder = isPlaceholder(str);
            }

            if (index == 0 || (index == -1 && owner.getSelectedIndex() == 0)) {
                placeholder = true;
            }

            if (placeholder && !isSelected) {
                label.setForeground(COMBO_PLACEHOLDER);
            }

            return label;
        }

        private boolean isPlaceholder(String value) {
            String lower = value.trim().toLowerCase();
            return lower.isEmpty() || lower.equals("tutti") || lower.equals("tutte") || lower.equals("default")
                    || lower.contains("nessun") || lower.contains("seleziona") || lower.startsWith("(");
        }
    }


    // === RICERCA / FILTRI / ORDINAMENTO ===
    private void eseguiRicerca() {
        try {
            String tipoSel = (String) tipoFilter.getSelectedItem();
            String catSel = (String) categoriaFilter.getSelectedItem();

            // recupera lista base (inclusi miei annunci)
            List<String> base = controller.annunciAttiviFormattatiInclusiMiei(matricola);
            annunciUltimaRicercaRaw = base;

            List<String> filtrati = controller.filtraAnnunciFormattati(base, tipoSel, catSel);

            // filtro testo / matricola
            String query = searchField.getText();
            boolean hasSearch = query != null && !query.trim().isEmpty() && !searchField.getForeground().equals(Color.GRAY);
            if (hasSearch) {
                String q = query.trim().toLowerCase();
                java.util.List<String> temp = new java.util.ArrayList<>();
                for (String rec : filtrati) {
                    String[] p = rec.split("\\|", -1);
                    String titolo = p.length > 1 ? p[1] : "";
                    String creatore = p.length > 5 ? p[5] : "";
                    if ((creatore != null && creatore.toLowerCase().contains(q)) || (titolo != null && titolo.toLowerCase().contains(q))) {
                        temp.add(rec);
                    }
                }
                filtrati = temp;
            }

            // ordinamento per prezzo (se richiesto)
            String ordSel = (String) orderByCombo.getSelectedItem();
            if (ordSel != null && !"Default".equals(ordSel)) {
                java.util.List<String> copia = new java.util.ArrayList<>(filtrati);
                java.util.Comparator<String> cmpPrezzo = (a, b) -> {
                    String[] pa = a.split("\\|", -1);
                    String[] pb = b.split("\\|", -1);
                    String sa = pa.length > 4 ? pa[4] : "-";
                    String sb = pb.length > 4 ? pb[4] : "-";
                    java.math.BigDecimal da = parsePrezzoOrNull(sa);
                    java.math.BigDecimal db = parsePrezzoOrNull(sb);
                    if (da == null && db == null) return 0;
                    if (da == null) return 1;
                    if (db == null) return -1;
                    return da.compareTo(db);
                };
                copia.sort(cmpPrezzo);
                if ("Prezzo decrescente".equals(ordSel)) java.util.Collections.reverse(copia);
                filtrati = copia;
            }

            // aggiorna modello lista
            annunciListModel.clear();
            for (String rec : filtrati) annunciListModel.addElement(rec);

            if (statusLabel != null) statusLabel.setText("Totali attivi: " + base.size() + " | Mostrati: " + filtrati.size());
        } catch (Exception ex) {
            Component parent = getDialogParent();
            showErrorDialog(parent, "Errore durante la ricerca degli annunci", ex);
            dismissModalOverlay(parent);
        }
    }

    private java.math.BigDecimal parsePrezzoOrNull(String s) {
        if (s == null || s.trim().isEmpty() || s.equals("-")) return null;
        try { return new java.math.BigDecimal(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private void mostraDettaglioAnnuncio(String rawRecord) {
        if (rawRecord == null) return;
        String idAnnuncio;
        try {
            idAnnuncio = controller.estraiIdAnnuncio(rawRecord);
        } catch (Exception ex) {
            showErrorDialog(getDialogParent(), "Annuncio non disponibile", ex);
            return;
        }
        if (idAnnuncio == null || idAnnuncio.isBlank()) {
            showErrorDialog(getDialogParent(), "Annuncio non disponibile", "ID annuncio mancante");
            return;
        }

        Component parent = getDialogParent();
        try {
            String[] fields = controller.recuperaAnnuncioFields(idAnnuncio);
            if (fields == null || fields.length < 6) {
                showErrorDialog(parent, "Impossibile mostrare i dettagli", "Dati annuncio incompleti");
                return;
            }

            String titolo = fields[0];
            String descrizione = fields[1];
            String categoria = fields[2];
            String stato = fields[3];
            String tipo = fields[4];
            String prezzo = fields[5];
            String dataPubblicazione = fields.length > 6 ? fields[6] : "";
            String creatore = fields.length > 7 ? fields[7] : "";
            String idOggetto = fields.length > 8 ? fields[8] : "";

            JPanel panel = new JPanel(new BorderLayout(10, 12));
            panel.setBackground(Color.WHITE);
            panel.setBorder(new EmptyBorder(16, 18, 16, 18));

            JLabel header = new JLabel(titolo + "  [" + tipo + "]");
            header.setFont(new Font("Segoe UI", Font.BOLD, 20));
            panel.add(header, BorderLayout.NORTH);

            String descrizioneVal = (descrizione == null || descrizione.isBlank()) ? "Nessuna descrizione fornita." : descrizione;
            JTextArea descrArea = new JTextArea(descrizioneVal, 6, 30);
            descrArea.setLineWrap(true);
            descrArea.setWrapStyleWord(true);
            descrArea.setEditable(false);
            descrArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            descrArea.setBackground(new Color(250, 250, 252));
            descrArea.setBorder(new EmptyBorder(8, 10, 8, 10));
            JScrollPane descrScroll = new JScrollPane(descrArea);
            descrScroll.setBorder(new LineBorder(new Color(230, 232, 236))); 
            descrScroll.setPreferredSize(new Dimension(420, 140));
            panel.add(descrScroll, BorderLayout.CENTER);

            JPanel infoGrid = new JPanel(new GridLayout(0, 2, 12, 8));
            infoGrid.setOpaque(false);

            infoGrid.add(makeInfoLabel("Categoria"));
            infoGrid.add(makeInfoValue(categoria));

            infoGrid.add(makeInfoLabel("Stato"));
            infoGrid.add(makeInfoValue(stato));

            // Mostra prezzo solo per annunci di tipo Vendita e se presente
            boolean isVendita = "Vendita".equalsIgnoreCase(tipo);
            if (isVendita && prezzo != null && !prezzo.isBlank() && !"-".equals(prezzo)) {
                infoGrid.add(makeInfoLabel("Prezzo"));
                infoGrid.add(makeInfoValue("€ " + prezzo));
            }

            infoGrid.add(makeInfoLabel("Pubblicato il"));
            infoGrid.add(makeInfoValue(dataPubblicazione == null || dataPubblicazione.isBlank() ? "--" : dataPubblicazione));

            infoGrid.add(makeInfoLabel("Creatore"));
            String creatoreDisplay = (creatore != null && creatore.equalsIgnoreCase(matricola)) ? "Tu" : creatore;
            infoGrid.add(makeInfoValue(creatoreDisplay));

            infoGrid.add(makeInfoLabel("Oggetto"));
            String oggettoDisplay = "--";
            if (idOggetto != null && !idOggetto.isBlank()) {
                try {
                    String nomeOggetto = controller.trovaNomeOggettoPerId(idOggetto);
                    if (nomeOggetto != null && !nomeOggetto.isBlank()) {
                        oggettoDisplay = nomeOggetto;
                    } else {
                        oggettoDisplay = idOggetto;
                    }
                } catch (Exception ignore) {
                    oggettoDisplay = idOggetto;
                }
            }
            infoGrid.add(makeInfoValue(oggettoDisplay));

            panel.add(infoGrid, BorderLayout.SOUTH);

            JOptionPane.showMessageDialog(parent, panel, "Dettaglio annuncio", JOptionPane.PLAIN_MESSAGE);
        } catch (Exception ex) {
            showErrorDialog(parent, "Impossibile mostrare i dettagli", ex);
        } finally {
            dismissModalOverlay(parent);
        }
    }

    private JLabel makeInfoLabel(String text) {
        JLabel label = new JLabel(text + ":");
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(90, 95, 105));
        return label;
    }

    private JLabel makeInfoValue(String text) {
        JLabel value = new JLabel(text != null ? text : "");
        value.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        value.setForeground(new Color(40, 45, 55));
        return value;
    }


    // === UTIL: area di testo con placeholder ===
    private JTextArea createPlaceholderTextArea(String placeholder, int rows, int cols) {
        JTextArea area = new JTextArea(rows, cols);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setForeground(Color.GRAY);
        area.setText(placeholder);

        final Color normalColor = UIManager.getColor("TextArea.foreground") != null ? UIManager.getColor("TextArea.foreground") : Color.BLACK;

        area.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (area.getText().equals(placeholder)) {
                    area.setText("");
                    area.setForeground(normalColor);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (area.getText().isEmpty()) {
                    area.setForeground(Color.GRAY);
                    area.setText(placeholder);
                }
            }
        });

        area.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() { /* non serve logica aggiuntiva qui */ }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        return area;
    }


    // === DIALOG: APRI OFFERTA ===
    private void apriDialogOffri() {
        int idx = annunciList.getSelectedIndex();
        if (idx < 0 || idx >= annunciUltimaRicercaRaw.size()) return;

        String raw = annunciUltimaRicercaRaw.get(idx);
        String idAnnuncio = controller.estraiIdAnnuncio(raw);
        String tipo = (raw.split("\\|", -1).length > 2) ? raw.split("\\|", -1)[2] : "";

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(new JLabel("Creazione offerta per annuncio: " + idAnnuncio + " (" + tipo + ")"), BorderLayout.NORTH);

        JTextArea commentoArea = createPlaceholderTextArea(PLACEHOLDER_COMMENTO, 5, 30);
        panel.add(new JScrollPane(commentoArea), BorderLayout.CENTER);

        JPanel extra = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        int r = 0;
        JTextField prezzoField = new JTextField();
        JComboBox<String> oggettoCombo = null;
        java.util.List<String> mieiOggetti = java.util.Collections.emptyList();

        if ("Vendita".equalsIgnoreCase(tipo)) {
            gc.gridx = 0; gc.gridy = r; extra.add(new JLabel("Prezzo offerto (€):"), gc);
            gc.gridx = 1; extra.add(prezzoField, gc); r++;
        } else if ("Scambio".equalsIgnoreCase(tipo)) {
            try { mieiOggetti = controller.oggettiUtenteFormattati(matricola); } catch (Exception ex) { /* fallback lista vuota */ }
            java.util.List<String> labels = new java.util.ArrayList<>();
            for (String rec : mieiOggetti) labels.add(controller.formatOggettoLabel(rec));
            if (labels.isEmpty()) {
                oggettoCombo = new JComboBox<>(new String[] { "(Nessun oggetto disponibile)" });
                oggettoCombo.setEnabled(false);
            } else {
                oggettoCombo = new JComboBox<>(labels.toArray(new String[0]));
            }
            styleCombo(oggettoCombo, 220);
            gc.gridx = 0; gc.gridy = r; extra.add(new JLabel("Oggetto da scambiare:"), gc);
            gc.gridx = 1; extra.add(oggettoCombo, gc); r++;
        }

        panel.add(extra, BorderLayout.SOUTH);

        Component parentComponent = getDialogParent();

        Object[] options = { "Conferma", "Annulla" };
        int res = JOptionPane.showOptionDialog(parentComponent, panel, "Nuova Offerta",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        dismissModalOverlay(parentComponent);
        if (res == 0) {
            try {
                String prezzoStr = null;
                String ogg = null;

                if ("Vendita".equalsIgnoreCase(tipo)) {
                    String pTxt = prezzoField.getText().trim();
                    if (pTxt.isEmpty()) {
                        showErrorDialog(parentComponent, "Impossibile creare l'offerta", "Prezzo richiesto");
                        dismissModalOverlay(parentComponent);
                        return;
                    }
                    prezzoStr = pTxt;
                } else if ("Scambio".equalsIgnoreCase(tipo)) {
                    if (oggettoCombo == null || !oggettoCombo.isEnabled()) {
                        showErrorDialog(parentComponent, "Impossibile creare l'offerta", "Nessun oggetto disponibile per lo scambio");
                        dismissModalOverlay(parentComponent);
                        return;
                    }
                    int sel = oggettoCombo.getSelectedIndex();
                    if (sel < 0 || sel >= mieiOggetti.size()) {
                        showErrorDialog(parentComponent, "Impossibile creare l'offerta", "Selezione oggetto non valida");
                        dismissModalOverlay(parentComponent);
                        return;
                    }
                    ogg = controller.estraiIdOggetto(mieiOggetti.get(sel));
                }

                String commento = commentoArea.getText();
                if (PLACEHOLDER_COMMENTO.equals(commento)) commento = "";

                controller.creaOfferta(idAnnuncio, matricola, tipo, prezzoStr, commento, ogg);
                JOptionPane.showMessageDialog(parentComponent, "Offerta creata");
                dismissModalOverlay(parentComponent);
            } catch (Exception ex) {
                showErrorDialog(parentComponent, "Impossibile creare l'offerta", ex);
                dismissModalOverlay(parentComponent);
            }
        }
    }


    // === DIALOG: APRI NUOVO ANNUNCIO ===
    private void apriDialogNuovoAnnuncio() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        JTextField titoloField = new JTextField();
        JTextArea descrArea = new JTextArea(4, 20);
    JComboBox<String> tipoBox = new JComboBox<>(controller.elencoTipiAnnuncio().toArray(new String[0]));
    JComboBox<String> catBox = new JComboBox<>(controller.elencoCategorieAnnuncio().toArray(new String[0]));
        JTextField prezzoField = new JTextField();

        java.util.List<String> oggettiUser = java.util.Collections.emptyList();
        try { oggettiUser = controller.oggettiUtenteFormattati(matricola); } catch (Exception ex) { /* ignore */ }

        java.util.List<String> oggLabels = new java.util.ArrayList<>();
        for (String rec : oggettiUser) oggLabels.add(controller.formatOggettoLabel(rec));

        JComboBox<String> oggettoCombo;
        if (oggLabels.isEmpty()) {
            oggettoCombo = new JComboBox<>(new String[] { "(Nessun oggetto disponibile)" });
            oggettoCombo.setEnabled(false);
        } else {
            oggettoCombo = new JComboBox<>(oggLabels.toArray(new String[0]));
        }
        styleCombo(tipoBox, 220);
        styleCombo(catBox, 220);
        styleCombo(oggettoCombo, 220);

        JLabel prezzoLabel = new JLabel("Prezzo (Vendita):");

        int row = 0;
        gc.gridx = 0; gc.gridy = row; fields.add(new JLabel("Titolo:"), gc);
        gc.gridx = 1; fields.add(titoloField, gc); row++;

        gc.gridx = 0; gc.gridy = row; fields.add(new JLabel("Descrizione:"), gc);
        gc.gridx = 1; gc.fill = GridBagConstraints.BOTH; gc.weighty = 1; fields.add(new JScrollPane(descrArea), gc);
        gc.fill = GridBagConstraints.HORIZONTAL; gc.weighty = 0; row++;

        gc.gridx = 0; gc.gridy = row; fields.add(new JLabel("Tipo:"), gc);
        gc.gridx = 1; fields.add(tipoBox, gc); row++;

        gc.gridx = 0; gc.gridy = row; fields.add(new JLabel("Categoria:"), gc);
        gc.gridx = 1; fields.add(catBox, gc); row++;

        gc.gridx = 0; gc.gridy = row; fields.add(prezzoLabel, gc);
        gc.gridx = 1; fields.add(prezzoField, gc); row++;

        gc.gridx = 0; gc.gridy = row; fields.add(new JLabel("Oggetto:"), gc);
        gc.gridx = 1; fields.add(oggettoCombo, gc); row++;

        // toggle visibilità prezzo in base al tipo
        Runnable togglePrezzo = () -> {
            boolean isVendita = "Vendita".equalsIgnoreCase((String) tipoBox.getSelectedItem());
            prezzoLabel.setVisible(isVendita);
            prezzoField.setVisible(isVendita);
        };
        tipoBox.addActionListener(e -> togglePrezzo.run());
        togglePrezzo.run();

        panel.add(fields, BorderLayout.CENTER);

    Component parent = getDialogParent();
    Object[] options = { "Conferma", "Annulla" };
    int res = JOptionPane.showOptionDialog(parent, panel, "Nuovo Annuncio",
        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
    dismissModalOverlay(parent);
    if (res == 0) {
            try {
                String titolo = titoloField.getText();
                String descr = descrArea.getText();
                String tipo = (String) tipoBox.getSelectedItem();
                String categoria = (String) catBox.getSelectedItem();
                String prezzoTxt = prezzoField.getText().trim();

                if (oggettoCombo == null || !oggettoCombo.isEnabled()) {
                    showErrorDialog(parent, "Impossibile creare l'annuncio", "Nessun oggetto selezionabile");
                    dismissModalOverlay(parent);
                    return;
                }
                int selO = oggettoCombo.getSelectedIndex();
                if (selO < 0 || selO >= oggettiUser.size()) {
                    showErrorDialog(parent, "Impossibile creare l'annuncio", "Selezione oggetto non valida");
                    dismissModalOverlay(parent);
                    return;
                }
                String idOggetto = controller.estraiIdOggetto(oggettiUser.get(selO));

                java.math.BigDecimal prezzo = null;
                if ("Vendita".equalsIgnoreCase(tipo)) {
                    if (prezzoTxt.isEmpty()) {
                        showErrorDialog(parent, "Impossibile creare l'annuncio", "Prezzo richiesto per Vendita");
                        dismissModalOverlay(parent);
                        return;
                    }
                    try {
                        prezzo = new java.math.BigDecimal(prezzoTxt.replace(",", "."));
                    } catch (NumberFormatException nfe) {
                        showErrorDialog(parent, "Impossibile creare l'annuncio", "Formato prezzo non valido");
                        dismissModalOverlay(parent);
                        return;
                    }
                }

                // delega al controller la creazione (UI -> controller)
                controller.creaAnnuncio(titolo, descr, tipo, categoria, (prezzo == null ? null : prezzo.toPlainString()), idOggetto, matricola);
                JOptionPane.showMessageDialog(parent, "Annuncio creato");
                dismissModalOverlay(parent);
                eseguiRicerca();
            } catch (Exception ex) {
                showErrorDialog(parent, "Impossibile creare l'annuncio", ex);
                dismissModalOverlay(parent);
            }
        }
    }


    // === RENDERER ANNUNCIO (card-like) ===
    private class AnnuncioCardRenderer extends JPanel implements ListCellRenderer<String> {
        private final JLabel titoloLabel = new JLabel();
        private final JLabel metaLabel = new JLabel();
        private final JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));

        AnnuncioCardRenderer() {
            setLayout(new BorderLayout(6, 4));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(6, 8, 6, 8),
                    BorderFactory.createLineBorder(new Color(230, 230, 235))
            ));
            setOpaque(true);

            titoloLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
            metaLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
            metaLabel.setForeground(new Color(90, 90, 95));
            badgePanel.setOpaque(false);

            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);
            top.add(titoloLabel, BorderLayout.CENTER);
            top.add(badgePanel, BorderLayout.EAST);

            add(top, BorderLayout.NORTH);
            add(metaLabel, BorderLayout.SOUTH);
        }

        private JLabel makeBadge(String text, Color bg) {
            JLabel l = new JLabel(text.toUpperCase());
            l.setFont(new Font("Tahoma", Font.BOLD, 10));
            l.setForeground(Color.WHITE);
            l.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            l.setOpaque(true);
            l.setBackground(bg);
            return l;
        }

        private String esc(String s) {
            if (s == null) return "";
            return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
            // value raw: ID|Titolo|Tipo|Categoria|Prezzo|Creatore
            String[] p = value != null ? value.split("\\|", -1) : new String[0];
            String id = p.length > 0 ? p[0] : "";
            String titolo = p.length > 1 ? p[1] : "";
            String tipo = p.length > 2 ? p[2] : "";
            String categoria = p.length > 3 ? p[3] : "";
            String prezzo = p.length > 4 ? p[4] : "";
            String creatore = p.length > 5 ? p[5] : "";

            boolean isMio = creatore.equalsIgnoreCase(matricola);
            String who = creatore;

            String suffixHtml = isMio ? " <span style='color:#0078D4'>(Tuo)</span>" : "";
            titoloLabel.setText("<html>" + esc(titolo) + suffixHtml + "</html>");

            metaLabel.setText("ID: " + id + "  •  Categoria: " + categoria + "  •  Creatore: " + who);

            badgePanel.removeAll();
            if (!"vendita".equalsIgnoreCase(tipo)) {
                Color tipoColor;
                switch (tipo.toLowerCase()) {
                    case "scambio": tipoColor = new Color(0, 120, 212); break;
                    case "regalo": tipoColor = new Color(218, 112, 37); break;
                    default: tipoColor = new Color(108, 117, 125); break;
                }
                badgePanel.add(makeBadge(tipo, tipoColor));
            }

            if ("Vendita".equalsIgnoreCase(tipo) && prezzo != null && !prezzo.equals("-") && !prezzo.isEmpty()) {
                badgePanel.add(makeBadge("€" + prezzo, new Color(90, 25, 200)));
            }

            setBackground(isSelected ? new Color(218, 230, 247) : Color.WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return this;
        }
    }
}
