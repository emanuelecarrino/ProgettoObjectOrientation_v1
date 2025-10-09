package frames;

import dto.Controller;
import javax.swing.*;
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

    // === CAMPI ISTANZA / STATO ===
    private final Controller controller;
    private final String matricola;

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

        setContentPane(buildContentPanel());
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
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setBackground(Color.WHITE);

        tipoFilter = new JComboBox<>(buildTipoFiltroModel());
        categoriaFilter = new JComboBox<>(buildCategoriaFiltroModel());
        orderByCombo = new JComboBox<>(new String[] { "Default", "Prezzo crescente", "Prezzo decrescente" });

        tipoFilter.addActionListener(e -> eseguiRicerca());
        categoriaFilter.addActionListener(e -> eseguiRicerca());
        orderByCombo.addActionListener(e -> eseguiRicerca());

        controls.add(new JLabel("Tipo:"));
        controls.add(tipoFilter);
        controls.add(new JLabel("Categoria:"));
        controls.add(categoriaFilter);
        controls.add(orderByCombo);

        header.add(controls, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);

        // --- CENTRO: lista annunci ---
        annunciListModel = new DefaultListModel<>();
        annunciList = new JList<>(annunciListModel);
        annunciList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        annunciList.setFixedCellHeight(-1); // allow variable height
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
            JOptionPane.showMessageDialog(this, "Errore ricerca annunci: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
        }
    }

    private java.math.BigDecimal parsePrezzoOrNull(String s) {
        if (s == null || s.trim().isEmpty() || s.equals("-")) return null;
        try { return new java.math.BigDecimal(s.trim()); } catch (NumberFormatException e) { return null; }
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
            oggettoCombo = new JComboBox<>(labels.toArray(new String[0]));
            if (labels.isEmpty()) {
                oggettoCombo = new JComboBox<>(new String[] { "(Nessun oggetto disponibile)" });
                oggettoCombo.setEnabled(false);
            }
            gc.gridx = 0; gc.gridy = r; extra.add(new JLabel("Oggetto da scambiare:"), gc);
            gc.gridx = 1; extra.add(oggettoCombo, gc); r++;
        }

        panel.add(extra, BorderLayout.SOUTH);

        int res = JOptionPane.showConfirmDialog(this, panel, "Nuova Offerta", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            try {
                String prezzoStr = null;
                String ogg = null;

                if ("Vendita".equalsIgnoreCase(tipo)) {
                    String pTxt = prezzoField.getText().trim();
                    if (pTxt.isEmpty()) throw new IllegalArgumentException("Prezzo richiesto");
                    prezzoStr = pTxt;
                } else if ("Scambio".equalsIgnoreCase(tipo)) {
                    if (oggettoCombo == null || !oggettoCombo.isEnabled()) throw new IllegalArgumentException("Nessun oggetto disponibile per lo scambio");
                    int sel = oggettoCombo.getSelectedIndex();
                    if (sel < 0 || sel >= mieiOggetti.size()) throw new IllegalArgumentException("Selezione oggetto non valida");
                    ogg = controller.estraiIdOggetto(mieiOggetti.get(sel));
                }

                String commento = commentoArea.getText();
                if (PLACEHOLDER_COMMENTO.equals(commento)) commento = "";

                controller.creaOffertaDaUI(idAnnuncio, matricola, tipo, prezzoStr, commento, ogg);
                JOptionPane.showMessageDialog(this, "Offerta creata");
            } catch (Exception ex) {
                StringBuilder sb = new StringBuilder();
                sb.append(ex.getClass().getSimpleName()).append(": ").append(ex.getMessage());
                if (ex.getCause() != null) sb.append("\nCausa: ")
                        .append(ex.getCause().getClass().getSimpleName()).append(" - ")
                        .append(ex.getCause().getMessage());
                JOptionPane.showMessageDialog(this, "Errore creazione offerta:\n" + sb);
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

        int res = JOptionPane.showConfirmDialog(this, panel, "Nuovo Annuncio", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            try {
                String titolo = titoloField.getText();
                String descr = descrArea.getText();
                String tipo = (String) tipoBox.getSelectedItem();
                String categoria = (String) catBox.getSelectedItem();
                String prezzoTxt = prezzoField.getText().trim();

                if (oggettoCombo == null || !oggettoCombo.isEnabled()) throw new IllegalArgumentException("Nessun oggetto selezionabile");
                int selO = oggettoCombo.getSelectedIndex();
                if (selO < 0 || selO >= oggettiUser.size()) throw new IllegalArgumentException("Selezione oggetto non valida");
                String idOggetto = controller.estraiIdOggetto(oggettiUser.get(selO));

                java.math.BigDecimal prezzo = null;
                if ("Vendita".equalsIgnoreCase(tipo)) {
                    if (prezzoTxt.isEmpty()) throw new IllegalArgumentException("Prezzo richiesto per Vendita");
                    try { prezzo = new java.math.BigDecimal(prezzoTxt.replace(",", ".")); } 
                    catch (NumberFormatException nfe) { throw new IllegalArgumentException("Formato prezzo non valido"); }
                }

                // delega al controller la creazione (UI -> controller)
                controller.creaAnnuncioDaUI(titolo, descr, tipo, categoria, (prezzo == null ? null : prezzo.toPlainString()), idOggetto, matricola);
                JOptionPane.showMessageDialog(this, "Annuncio creato");
                eseguiRicerca();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Errore creazione annuncio: " + ex.getMessage());
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
            Color tipoColor;
            switch (tipo.toLowerCase()) {
                case "vendita": tipoColor = new Color(46, 160, 67); break;
                case "scambio": tipoColor = new Color(0, 120, 212); break;
                case "regalo": tipoColor = new Color(218, 112, 37); break;
                default: tipoColor = new Color(108, 117, 125); break;
            }
            badgePanel.add(makeBadge(tipo, tipoColor));

            if ("Vendita".equalsIgnoreCase(tipo) && prezzo != null && !prezzo.equals("-") && !prezzo.isEmpty()) {
                badgePanel.add(makeBadge("€" + prezzo, new Color(90, 25, 200)));
            }

            setBackground(isSelected ? new Color(218, 230, 247) : Color.WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return this;
        }
    }
}
