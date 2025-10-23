package frames;

import dto.Controller;
import exception.ApplicationException;
import exception.NotFoundException;
import exception.PersistenceException;
import exception.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ConsegnaFrame extends JFrame {

    // === COSTANTI DI STILE ===
    private static final Color SURFACE_BG = new Color(250, 251, 253);
    private static final Color CARD_BORDER = new Color(230, 230, 235);
    private static final Color TEXT_PRIMARY = new Color(45, 52, 63);
    private static final Color TEXT_MUTED = new Color(120, 129, 141);
    private static final Color ACCENT = new Color(99, 134, 235);
    private static final Color CONTROL_BORDER = new Color(214, 219, 226);
    private static final Color CONTROL_BORDER_FOCUS = new Color(99, 134, 235);
    private static final Font BASE_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    private static final String[] FASCIA_ORARIA_PRESETS = {
            "Mattina (09:00 - 11:00)",
            "Primo pomeriggio (12:00 - 14:00)",
            "Pomeriggio (14:00 - 16:00)",
            "Sera (16:00 - 18:00)",
            "Personalizzata"
    };

    // === STATO ===
    private final Controller controller;
    private final String matricola;
    private final JPanel contentPanel;

    private DefaultListModel<dto.AnnuncioDTO> annunciModel;
    private JList<dto.AnnuncioDTO> annunciList;
    private List<dto.AnnuncioDTO> annunciCorrenti = new ArrayList<>();
    private CardLayout listCardLayout;
    private JPanel listContainer;
    private JLabel emptyStateLabel;
    private JLabel countLabel;

    private JLabel headerTitolo;
    private JLabel headerMeta;
    private JTextField sedeField;
    private JComboBox<String> fasciaCombo;
    private JComboBox<String> giornoCombo;
    private JComboBox<String> meseCombo;
    private JComboBox<String> annoCombo;
    private JTextArea noteArea;
    private JButton salvaButton;
    private JButton eliminaButton;

    private dto.ModConsegnaDTO consegnaCorrente;

    public ConsegnaFrame(Controller controller, String matricola) {
        this.controller = controller;
        this.matricola = matricola;
        setTitle("UninaSwap - Modalità Consegna");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1024, 640);
        setLocationRelativeTo(null);
        this.contentPanel = buildContentPanel();
        setContentPane(contentPanel);
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    private JPanel buildContentPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(SURFACE_BG);

        JPanel header = buildHeader();
        root.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setBorder(new EmptyBorder(16, 16, 16, 16));
        body.setOpaque(false);

        body.add(buildColumns(), BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);

        return root;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel title = new JLabel("Modalità di Consegna", SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.add(title, BorderLayout.WEST);

    countLabel = new JLabel("Annunci completati: 0");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        countLabel.setForeground(TEXT_MUTED);
        header.add(countLabel, BorderLayout.EAST);
        return header;
    }

    private JComponent buildColumns() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 16);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.38;
        gbc.weighty = 1.0;
        container.add(buildLeftColumn(), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.weightx = 0.62;
        container.add(buildRightColumn(), gbc);

        return container;
    }

    private JPanel buildLeftColumn() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        card.setPreferredSize(new Dimension(340, 0));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel listTitle = new JLabel("Annunci completati", SwingConstants.LEFT);
        listTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        listTitle.setForeground(TEXT_PRIMARY);
        header.add(listTitle, BorderLayout.WEST);

        JButton refreshButton = createSecondaryButton("Aggiorna");
        refreshButton.addActionListener(e -> refreshContent());
        header.add(refreshButton, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        annunciModel = new DefaultListModel<>();
        annunciList = new JList<>(annunciModel);
        annunciList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        annunciList.setCellRenderer(new AnnuncioCardRenderer());
        annunciList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onAnnuncioSelected(annunciList.getSelectedValue());
            }
        });

        JScrollPane scroll = new JScrollPane(annunciList);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        emptyStateLabel = new JLabel("<html><div style='text-align:center;color:#858d9b;'>Nessun annuncio completato disponibile.</div></html>", SwingConstants.CENTER);
        emptyStateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        listCardLayout = new CardLayout();
        listContainer = new JPanel(listCardLayout);
        listContainer.setOpaque(false);
        listContainer.add(scroll, "LIST");
        listContainer.add(emptyStateLabel, "EMPTY");

        card.add(listContainer, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildRightColumn() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(24, 28, 24, 28)
        ));

        JPanel header = new JPanel(new GridLayout(0, 1));
        header.setOpaque(false);
        headerTitolo = new JLabel("Seleziona un annuncio", SwingConstants.LEFT);
        headerTitolo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerTitolo.setForeground(TEXT_PRIMARY);
        header.add(headerTitolo);

        headerMeta = new JLabel(" ", SwingConstants.LEFT);
        headerMeta.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        headerMeta.setForeground(TEXT_MUTED);
        header.add(headerMeta);

        card.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        sedeField = createTextField();
        addField(form, gbc, "Sede universitaria", sedeField);

        fasciaCombo = new JComboBox<>(FASCIA_ORARIA_PRESETS);
        enhanceCombo(fasciaCombo);
        addField(form, gbc, "Fascia oraria", fasciaCombo);

        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        datePanel.setOpaque(false);
        giornoCombo = new JComboBox<>(buildDayModel());
        meseCombo = new JComboBox<>(buildMonthModel());
        annoCombo = new JComboBox<>(buildYearModel());
        for (JComboBox<String> combo : new JComboBox[]{giornoCombo, meseCombo, annoCombo}) {
            enhanceCombo(combo);
        }
        giornoCombo.setPreferredSize(new Dimension(74, 32));
        meseCombo.setPreferredSize(new Dimension(90, 32));
        annoCombo.setPreferredSize(new Dimension(96, 32));

        datePanel.add(giornoCombo);
        datePanel.add(meseCombo);
        datePanel.add(annoCombo);
        addField(form, gbc, "Data consegna", datePanel);

        noteArea = new JTextArea(5, 20);
        noteArea.setFont(BASE_FONT);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CONTROL_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        JScrollPane noteScroll = new JScrollPane(noteArea);
        noteScroll.setBorder(BorderFactory.createEmptyBorder());
        noteScroll.setPreferredSize(new Dimension(0, 110));
        addField(form, gbc, "Note (opzionali)", noteScroll);

        card.add(form, BorderLayout.CENTER);

    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    actions.setOpaque(false);


    eliminaButton = createDestructiveButton("Elimina consegna");
    eliminaButton.addActionListener(e -> onDeleteConsegna());
    actions.add(eliminaButton);

    salvaButton = createPrimaryButton("Conferma dettagli");
    salvaButton.addActionListener(e -> onSaveConsegna());
    actions.add(salvaButton);

        card.add(actions, BorderLayout.SOUTH);

        setDetailEnabled(false);
        return card;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(BASE_FONT);
        tf.setColumns(22);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CONTROL_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return tf;
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(40, 167, 69));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(241, 243, 248));
        btn.setForeground(TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createDestructiveButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(231, 76, 60));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void addField(JPanel form, GridBagConstraints gbc, String label, JComponent component) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(TEXT_PRIMARY);

        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.gridx = 0;
        labelGbc.gridy = gbc.gridy;
        labelGbc.anchor = GridBagConstraints.WEST;
        labelGbc.fill = GridBagConstraints.HORIZONTAL;
        labelGbc.insets = new Insets(gbc.gridy == 0 ? 0 : 18, 0, 6, 0);
        form.add(lbl, labelGbc);

        GridBagConstraints compGbc = new GridBagConstraints();
        compGbc.gridx = 0;
        compGbc.gridy = gbc.gridy + 1;
        compGbc.fill = GridBagConstraints.HORIZONTAL;
        compGbc.weightx = 1.0;
        compGbc.insets = new Insets(0, 0, 0, 0);
        form.add(component, compGbc);

        gbc.gridy = compGbc.gridy + 1;
        gbc.gridx = 0;
    }

    private void setDetailEnabled(boolean enabled) {
        sedeField.setEnabled(enabled);
        fasciaCombo.setEnabled(enabled);
        giornoCombo.setEnabled(enabled);
        meseCombo.setEnabled(enabled);
        annoCombo.setEnabled(enabled);
        noteArea.setEnabled(enabled);
        salvaButton.setEnabled(enabled);
        eliminaButton.setEnabled(enabled && consegnaCorrente != null);
    }

    private void onAnnuncioSelected(dto.AnnuncioDTO annuncio) {
        consegnaCorrente = null;
        if (annuncio == null) {
            headerTitolo.setText("Seleziona un annuncio");
            headerMeta.setText(" ");
            clearDetailFields();
            setDetailEnabled(false);
            return;
        }

        headerTitolo.setText(annuncio.getTitolo());
        headerMeta.setText(String.format("ID: %s  •  Stato: %s  •  Tipo: %s", annuncio.getIdAnnuncio(), annuncio.getStato(), annuncio.getTipoAnnuncio()));

        try {
            consegnaCorrente = controller.trovaConsegnaPerAnnuncio(annuncio.getIdAnnuncio());
        } catch (ApplicationException ex) {
            if (ex instanceof NotFoundException) {
                // nessuna consegna esistente: il form rimane pronto per la creazione
            } else if (ex instanceof PersistenceException) {
                String metaText = headerMeta.getText();
                if (metaText == null) metaText = "";
                if (!metaText.contains("dettagli non disponibili")) {
                    headerMeta.setText(metaText + "  •  dettagli non disponibili");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Errore nel recupero della consegna: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }

        populateDetail(consegnaCorrente);
        setDetailEnabled(true);
    }

    private void populateDetail(dto.ModConsegnaDTO dto) {
        if (dto == null) {
            sedeField.setText("");
            fasciaCombo.setSelectedIndex(0);
            clearDateCombos();
            noteArea.setText("");
        } else {
            sedeField.setText(dto.getSedeUni() != null ? dto.getSedeUni() : "");
            boolean matched = false;
            if (dto.getFasciaOraria() != null) {
                for (int i = 0; i < fasciaCombo.getItemCount(); i++) {
                    if (dto.getFasciaOraria().equalsIgnoreCase((String) fasciaCombo.getItemAt(i))) {
                        fasciaCombo.setSelectedIndex(i);
                        matched = true;
                        break;
                    }
                }
            }
            if (!matched) {
                fasciaCombo.setSelectedItem("Personalizzata");
            }
            setDateCombosFrom(dto.getData());
            noteArea.setText(dto.getNote() != null ? dto.getNote() : "");
        }
        eliminaButton.setEnabled(consegnaCorrente != null);
    }

    private void clearDetailFields() {
        sedeField.setText("");
        fasciaCombo.setSelectedIndex(0);
        clearDateCombos();
        noteArea.setText("");
    }

    private void clearDateCombos() {
        giornoCombo.setSelectedIndex(0);
        meseCombo.setSelectedIndex(0);
        annoCombo.setSelectedIndex(0);
    }

    private void onNuovaConsegna() {
        dto.AnnuncioDTO annuncio = annunciList != null ? annunciList.getSelectedValue() : null;
        if (annuncio == null) {
            showWarning("Seleziona un annuncio da gestire");
            return;
        }
        consegnaCorrente = null;
        populateDetail(null);
        setDetailEnabled(true);
    }

    private void setDateCombosFrom(LocalDate date) {
        if (date == null) {
            clearDateCombos();
            return;
        }
        giornoCombo.setSelectedItem(String.format("%02d", date.getDayOfMonth()));
        meseCombo.setSelectedItem(String.format("%02d", date.getMonthValue()));
        annoCombo.setSelectedItem(String.valueOf(date.getYear()));
    }

    private void onSaveConsegna() {
    dto.AnnuncioDTO annuncio = annunciList.getSelectedValue();
        if (annuncio == null) {
            showWarning("Seleziona prima un annuncio");
            return;
        }

        String sede = sedeField.getText() != null ? sedeField.getText().trim() : "";
        if (sede.isBlank()) {
            showWarning("La sede universitaria è obbligatoria");
            return;
        }

        String fascia = (String) fasciaCombo.getSelectedItem();
        if (fascia == null || fascia.isBlank()) {
            showWarning("Seleziona una fascia oraria");
            return;
        }

        LocalDate data;
        try {
            data = parseDateFromCombos();
        } catch (ValidationException vex) {
            showWarning(vex.getMessage());
            return;
        }

        String note = noteArea.getText();

        try {
            if (consegnaCorrente == null) {
                controller.creaModConsegna(annuncio.getIdAnnuncio(), sede, note, fascia, data);
                showInfo("Consegna creata correttamente");
            } else {
                controller.aggiornaConsegna(consegnaCorrente.getIdConsegna(), sede, note, fascia, data);
                showInfo("Consegna aggiornata");
            }
            refreshContentPreservingSelection(annuncio.getIdAnnuncio());
        } catch (ApplicationException ex) {
            // mostra esclusivamente il messaggio dell'eccezione del controller
            JOptionPane.showMessageDialog(this, "Errore nel salvataggio della consegna: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDeleteConsegna() {
    dto.AnnuncioDTO annuncio = annunciList.getSelectedValue();
        if (annuncio == null || consegnaCorrente == null) {
            showWarning("Nessuna consegna da rimuovere");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Sei sicuro di voler eliminare questa modalità di consegna?",
                "Conferma eliminazione",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            controller.eliminaConsegna(consegnaCorrente.getIdConsegna());
            showInfo("Consegna rimossa");
            refreshContentPreservingSelection(annuncio.getIdAnnuncio());
        } catch (ApplicationException ex) {
            JOptionPane.showMessageDialog(this, "Errore nella rimozione della consegna: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshContent() {
        refreshContentPreservingSelection(null);
    }

    private void refreshContentPreservingSelection(String preferedId) {
        String keepId = preferedId;
        if (keepId == null && annunciList != null && annunciList.getSelectedValue() != null) {
            keepId = annunciList.getSelectedValue().getIdAnnuncio();
        }

        try {
            List<dto.AnnuncioDTO> miei = controller.cercaAnnunciPerCreatore(matricola);
            annunciCorrenti = new ArrayList<>();
            for (dto.AnnuncioDTO a : miei) {
                if (!isStateEligible(a.getStato())) continue;
                annunciCorrenti.add(a);
            }
            annunciCorrenti.sort((a, b) -> {
                LocalDate db = b.getDataPubblicazione();
                LocalDate da = a.getDataPubblicazione();
                if (db == null && da == null) return 0;
                if (db == null) return 1;
                if (da == null) return -1;
                return db.compareTo(da);
            });

            annunciModel.clear();
            for (dto.AnnuncioDTO a : annunciCorrenti) {
                annunciModel.addElement(a);
            }

            if (listCardLayout != null && listContainer != null) {
                if (annunciModel.isEmpty()) {
                    listCardLayout.show(listContainer, "EMPTY");
                } else {
                    listCardLayout.show(listContainer, "LIST");
                }
            }

            updateCountLabel();

            if (keepId != null) {
                selectAnnuncioById(keepId);
            } else if (!annunciCorrenti.isEmpty()) {
                annunciList.setSelectedIndex(0);
            } else {
                onAnnuncioSelected(null);
            }
        } catch (ApplicationException ex) {
            JOptionPane.showMessageDialog(this, "Errore nel caricamento degli annunci: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCountLabel() {
        countLabel.setText(String.format("Annunci completati: %d", annunciCorrenti.size()));
    }

    private void selectAnnuncioById(String id) {
        if (id == null) return;
        for (int i = 0; i < annunciModel.size(); i++) {
            dto.AnnuncioDTO item = annunciModel.getElementAt(i);
            if (item.getIdAnnuncio().equals(id)) {
                annunciList.setSelectedIndex(i);
                return;
            }
        }
        if (!annunciModel.isEmpty()) {
            annunciList.setSelectedIndex(0);
        } else {
            onAnnuncioSelected(null);
        }
    }

    private boolean isStateEligible(dto.StatoAnnuncioDTO stato) {
        return stato == dto.StatoAnnuncioDTO.Venduto
                || stato == dto.StatoAnnuncioDTO.Scambiato
                || stato == dto.StatoAnnuncioDTO.Regalato;
    }

    private LocalDate parseDateFromCombos() throws ValidationException {
        String giorno = (String) giornoCombo.getSelectedItem();
        String mese = (String) meseCombo.getSelectedItem();
        String anno = (String) annoCombo.getSelectedItem();

        if (giorno == null || mese == null || anno == null
                || giorno.equals("-") || mese.equals("-") || anno.equals("-")) {
            throw new ValidationException("Seleziona una data completa");
        }

        try {
            int day = Integer.parseInt(giorno);
            int month = Integer.parseInt(mese);
            int year = Integer.parseInt(anno);
            return LocalDate.of(year, month, day);
        } catch (NumberFormatException | DateTimeException ex) {
            throw new ValidationException("Data non valida");
        }
    }

    // Rimosso showError composito: usiamo direttamente i messaggi del controller dove serve

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Attenzione", JOptionPane.WARNING_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Informazione", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void enhanceCombo(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setForeground(TEXT_PRIMARY);
        combo.setBorder(BorderFactory.createLineBorder(CONTROL_BORDER, 1, true));
        combo.setBackground(Color.WHITE);
        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton b = new JButton("▾");
                b.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                b.setForeground(TEXT_MUTED);
                b.setBorder(BorderFactory.createEmptyBorder());
                b.setContentAreaFilled(false);
                return b;
            }
        });
        combo.setPreferredSize(new Dimension(220, 32));
        combo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                combo.setBorder(BorderFactory.createLineBorder(CONTROL_BORDER_FOCUS, 1, true));
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                combo.setBorder(BorderFactory.createLineBorder(CONTROL_BORDER, 1, true));
            }
        });
    }

    private static String[] buildDayModel() {
        String[] items = new String[32];
        items[0] = "-";
        for (int i = 1; i <= 31; i++) {
            items[i] = String.format("%02d", i);
        }
        return items;
    }

    private static String[] buildMonthModel() {
        String[] items = new String[13];
        items[0] = "-";
        for (int i = 1; i <= 12; i++) {
            items[i] = String.format("%02d", i);
        }
        return items;
    }

    private static String[] buildYearModel() {
        int current = LocalDate.now().getYear();
        int span = 5;
        String[] items = new String[span + 2];
        items[0] = "-";
        for (int i = 0; i <= span; i++) {
            items[i + 1] = String.valueOf(current + i);
        }
        return items;
    }

    private static class AnnuncioCardRenderer extends JPanel implements ListCellRenderer<dto.AnnuncioDTO> {
        private final JLabel title = new JLabel();
        private final JLabel meta = new JLabel();

        public AnnuncioCardRenderer() {
            setLayout(new BorderLayout(0, 4));
            setBorder(new EmptyBorder(10, 12, 10, 12));
            setOpaque(true);

            title.setFont(new Font("Segoe UI", Font.BOLD, 15));
            title.setForeground(TEXT_PRIMARY);
            meta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            meta.setForeground(TEXT_MUTED);

            add(title, BorderLayout.NORTH);
            add(meta, BorderLayout.CENTER);
        }

        @Override
    public Component getListCellRendererComponent(JList<? extends dto.AnnuncioDTO> list, dto.AnnuncioDTO value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value != null) {
                title.setText(value.getTitolo());
                String metaText = String.format("ID: %s  •  Stato: %s  •  %s",
                        value.getIdAnnuncio(),
                        value.getStato(),
                        value.getDataPubblicazione() != null ? value.getDataPubblicazione().toString() : "");
                meta.setText(metaText);
            } else {
                title.setText(" ");
                meta.setText(" ");
            }

            if (isSelected) {
                setBackground(new Color(228, 236, 255));
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT, 1, true),
                        new EmptyBorder(10, 12, 10, 12)
                ));
            } else {
                setBackground(Color.WHITE);
                setBorder(new EmptyBorder(10, 12, 10, 12));
            }

            return this;
        }
    }
}
