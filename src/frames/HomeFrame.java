package frames;

import dto.Controller;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.math.BigDecimal;

public class HomeFrame extends JFrame {

    private final Controller controller;
    private final String matricola;
    // Username risolto on-demand per display (può essere null se non trovato)
    private final String usernameDisplay;
    private JPanel sidebar;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private final List<JToggleButton> navButtons = new ArrayList<>();
    private final List<SectionEntry> sectionHandlers = new ArrayList<>();
    private AnnunciFrame annunciFrame;
    private OggettiFrame oggettiFrame;
    private ProfiloFrame profiloFrame;
    private ConsegnaFrame consegnaFrame;
    private RitiroFrame ritiroFrame;
    private ReportFrame reportFrame;

    public HomeFrame(Controller controller, String matricola) {
        this.controller = controller;
        this.matricola = matricola;
        String resolved;
        try {
            resolved = controller.recuperaUsernameDaMatricola(matricola);
        } catch (Exception ex) {
            resolved = null;
        }
        this.usernameDisplay = (resolved != null ? resolved : matricola);


        setTitle("Home");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    // Nuove dimensioni più larghe (fisse) richieste
    int targetW = 1500;
    int targetH = 750;
    int x = Math.max(0, (screen.width - targetW) / 2);
    int y = Math.max(0, (screen.height - targetH) / 2);
    setBounds(x, y, targetW, targetH);
    setMinimumSize(new Dimension(targetW, targetH));
    // Consenti ingrandimento (anche massimizzazione) ma non rimpicciolire sotto la minimum size
    setResizable(true);
        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        buildSidebar();
        buildSections();
        selectSection("Homepage");
    }

    private void buildSidebar() {
        sidebar = new JPanel();
        sidebar.setLayout(new GridBagLayout());
        sidebar.setBackground(new Color(245,245,247));
        sidebar.setPreferredSize(new Dimension(200, getHeight()));
        // Sidebar fissa: niente scrollpane
        add(sidebar, BorderLayout.WEST);

    String[] sections = {
        "Homepage",
        "Annunci",
        "I tuoi oggetti",
        "Profilo",
        "Consegna",
        "Ritiro",
        "Report"
    };

        ButtonGroup group = new ButtonGroup();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        for (String s : sections) {
            JToggleButton btn = createNavButton(s);
            group.add(btn);
            gbc.gridy = row++;
            sidebar.add(btn, gbc);
            navButtons.add(btn);
        }
        gbc.gridy = row;
        gbc.weighty = 1;
        sidebar.add(Box.createVerticalGlue(), gbc);

        // Bottone Logout (versione originale stilizzata)
        gbc.gridy = ++row;
        gbc.weighty = 0;
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBackground(new Color(220, 53, 69));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFont(new Font("Tahoma", Font.BOLD, 14));
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(10,14,10,14));
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            int conf = JOptionPane.showConfirmDialog(this, "Vuoi davvero uscire?", "Conferma logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (conf == JOptionPane.YES_OPTION) {
                SwingUtilities.invokeLater(() -> {
                    dispose();
                    new LoginFrame().setVisible(true);
                });
            }
        });
        sidebar.add(logoutBtn, gbc);
    }

    private JToggleButton createNavButton(String text) {
        JToggleButton btn = new JToggleButton(text);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBackground(new Color(245,245,247));
        btn.setBorder(BorderFactory.createEmptyBorder(10,14,10,14));
        btn.setFont(new Font("Tahoma", Font.PLAIN, 15));
        btn.setOpaque(true);

        Color hover = new Color(228,230,235);
        Color selected = new Color(100, 149, 237);
        Color normal = btn.getBackground();

        btn.addChangeListener(e -> {
            if (btn.isSelected()) {
                btn.setBackground(selected);
                btn.setFont(btn.getFont().deriveFont(Font.BOLD));
            } else {
                btn.setBackground(normal);
                btn.setFont(btn.getFont().deriveFont(Font.PLAIN));
            }
        });
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { if (!btn.isSelected()) btn.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e) { if (!btn.isSelected()) btn.setBackground(normal); }
        });
        btn.addActionListener(e -> selectSection(text));
        return btn;
    }

    private void buildSections() {
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        JPanel homepagePanel = buildHomepagePanel();
        cardPanel.add(homepagePanel, "Homepage");
        sectionHandlers.add(new SectionEntry("Homepage", this::refreshDashboard));

        annunciFrame = new AnnunciFrame(controller, matricola);
        cardPanel.add(annunciFrame.getContentPanel(), "Annunci");
        sectionHandlers.add(new SectionEntry("Annunci", annunciFrame::refreshContent));

        oggettiFrame = new OggettiFrame(controller, matricola);
        cardPanel.add(oggettiFrame.getContentPanel(), "I tuoi oggetti");
        sectionHandlers.add(new SectionEntry("I tuoi oggetti", oggettiFrame::refreshContent));

        profiloFrame = new ProfiloFrame(controller, matricola);
        cardPanel.add(profiloFrame.getContentPanel(), "Profilo");
        sectionHandlers.add(new SectionEntry("Profilo", profiloFrame::refreshContent));

    consegnaFrame = new ConsegnaFrame(controller, matricola);
    cardPanel.add(consegnaFrame.getContentPanel(), "Consegna");
    sectionHandlers.add(new SectionEntry("Consegna", consegnaFrame::refreshContent));

    ritiroFrame = new RitiroFrame(controller, matricola);
    cardPanel.add(ritiroFrame.getContentPanel(), "Ritiro");
    sectionHandlers.add(new SectionEntry("Ritiro", ritiroFrame::refreshContent));

        reportFrame = new ReportFrame(controller, matricola);
        cardPanel.add(reportFrame.getContentPanel(), "Report");
        sectionHandlers.add(new SectionEntry("Report", reportFrame::refreshContent));
        add(cardPanel, BorderLayout.CENTER);
    }

    private JPanel buildHomepagePanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(18,18,12,18));
        header.setBackground(Color.WHITE);
    JLabel benv = new JLabel("Benvenuto " + usernameDisplay + "!");
        benv.setFont(new Font("Tahoma", Font.BOLD, 30));
        header.add(benv, BorderLayout.WEST);
        wrapper.add(header, BorderLayout.NORTH);

        // Center content with GridBag
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        wrapper.add(new JScrollPane(content), BorderLayout.CENTER);

        // Prepare panels placeholders updated by refreshDashboard()
    kpiPanel = new JPanel(new GridLayout(1,4,12,0));
        kpiPanel.setOpaque(false);
        kpiCardContainers = new JPanel[4];
        for (int i=0;i<4;i++) {
            kpiCardContainers[i] = buildKpiCard("--","--");
            kpiPanel.add(kpiCardContainers[i]);
        }

    recentAnnunciPanel = buildListPanel("I tuoi annunci");
    mieOffertePanel = buildListPanel("Le tue offerte");
    offerteDaGestirePanel = buildListPanel("Offerte da Gestire");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=3; gbc.weightx=1; gbc.fill=GridBagConstraints.HORIZONTAL; gbc.insets=new Insets(0,18,20,18);
        content.add(kpiPanel, gbc);

        gbc.gridwidth=1; gbc.gridy=1; gbc.gridx=0; gbc.weighty=1; gbc.fill=GridBagConstraints.BOTH; gbc.insets=new Insets(0,18,18,12);
        content.add(recentAnnunciPanel, gbc);
        gbc.gridx=1; gbc.insets=new Insets(0,0,18,12);
        content.add(mieOffertePanel, gbc);
        gbc.gridx=2; gbc.insets=new Insets(0,0,18,18);
        content.add(offerteDaGestirePanel, gbc);

    // Double-click su offerte: solo "Le tue offerte" e "Offerte da Gestire" per mostrare il dettaglio offerta
    attachDoubleClickToMieOfferte();
    attachDoubleClickToOfferteDaGestire();
    attachDoubleClickToAnnunci();

    // Wiring selezione esclusiva tra le tre liste
    wireMutualSelection();

        // Wrapper azioni dinamico (inizialmente nascosto)
    dynamicActionsWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,4));
    dynamicActionsWrapper.setOpaque(false);
    // Altezza fissa per evitare "saltelli" quando compaiono/scompaiono i pulsanti
    Dimension actionPref = new Dimension(10, 46); // larghezza verrà espansa dal BorderLayout
    dynamicActionsWrapper.setPreferredSize(actionPref);
    dynamicActionsWrapper.setMinimumSize(actionPref);
    dynamicActionsWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        wrapper.add(dynamicActionsWrapper, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(this::refreshDashboard);
        return wrapper;
    }


    private JPanel kpiPanel;
    private JPanel[] kpiCardContainers;
    private JPanel recentAnnunciPanel;
    private JPanel mieOffertePanel;
    private JPanel offerteDaGestirePanel;
    private JPanel dynamicActionsWrapper;
    private JButton eliminaBtn;
    private JButton modificaBtn;
    private JButton accettaBtn;
    private JButton rifiutaBtn;


    private JPanel buildKpiCard(String value, String label) {
        JPanel panel = new JPanel(new BorderLayout(0,4)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0,0,getWidth(),getHeight(),18,18);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBackground(new Color(250,250,252));
        panel.setBorder(new EmptyBorder(14,18,14,18));
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.addMouseListener(new MouseAdapter() {
            Color base = panel.getBackground();
            @Override public void mouseEntered(MouseEvent e) { panel.setBackground(new Color(245,245,250)); panel.repaint(); }
            @Override public void mouseExited(MouseEvent e) { panel.setBackground(base); panel.repaint(); }
        });
        JLabel val = new JLabel(value, SwingConstants.LEFT);
        val.setFont(new Font("Tahoma", Font.BOLD, 30));
        JLabel lab = new JLabel(label.toUpperCase());
        lab.setFont(new Font("Tahoma", Font.PLAIN, 11));
        lab.setForeground(new Color(120,120,125));
        panel.add(val, BorderLayout.CENTER);
        panel.add(lab, BorderLayout.SOUTH);
        panel.putClientProperty("valueLabel", val);
        panel.putClientProperty("nameLabel", lab);
        panel.putClientProperty("elevated", Boolean.TRUE);
        return panel;
    }

    private JPanel buildListPanel(String title) {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0,0,0,0),
                BorderFactory.createLineBorder(new Color(235,235,238))));
        JLabel t = new JLabel("  " + title);
        t.setFont(new Font("Tahoma", Font.BOLD, 14));
        t.setBorder(BorderFactory.createMatteBorder(0,0,1,0,new Color(230,230,233)));
        container.add(t, BorderLayout.NORTH);
    JList<String> list = new JList<>();
        list.setFont(new Font("Tahoma", Font.PLAIN, 13));
        list.setFixedCellHeight(-1); // variable height for card style
        list.setCellRenderer(new DashboardListCardRenderer());
        container.add(new JScrollPane(list), BorderLayout.CENTER);
        container.putClientProperty("jlist", list);
        // Listener per aggiornare pulsanti azione in base al pannello e selezione
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateActionButtonsState();
        });
        return container;
    }

    private void updateKpi(int index, String value, String label) {
        JPanel card = kpiCardContainers[index];
        JLabel val = (JLabel) card.getClientProperty("valueLabel");
        JLabel lab = (JLabel) card.getClientProperty("nameLabel");
        val.setText(value);
        lab.setText(label.toUpperCase());
    }

    private void refreshDashboard() {
        try {
            int annunciAttiviMiei = controller.contaAnnunciAttiviCreatore(matricola);
            int offerteMieAttesa = controller.contaOfferteMieInAttesa(matricola);
            int offerteRicevuteAttesa = controller.contaOfferteRicevuteInAttesa(matricola);
            String ultimoAnnuncioData = controller.dataUltimoAnnuncioCreato(matricola);

            updateKpi(0, String.valueOf(annunciAttiviMiei), "Miei annunci Attivi");
            updateKpi(1, String.valueOf(offerteMieAttesa), "Offerte (mie) in attesa");
            updateKpi(2, String.valueOf(offerteRicevuteAttesa), "Offerte ricevute attesa");
            updateKpi(3, ultimoAnnuncioData, "Ultimo annuncio");

            List<String> ultimiAnnunci = controller.ultimiAnnunciCreatore(matricola,5);
            setListData(recentAnnunciPanel, ultimiAnnunci);

            List<String> mieOfferte = controller.ultimeOfferteUtente(matricola,5);
            setListData(mieOffertePanel, mieOfferte);

            List<String> gestire = controller.offerteDaGestire(matricola,5);
            setListData(offerteDaGestirePanel, gestire);

            updateActionButtonsState();

        } catch (Exception ex) {
            showErrorDialog(this, "Impossibile aggiornare la dashboard", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private JList<String> getJList(JPanel listPanel) {
        return (JList<String>) listPanel.getClientProperty("jlist");
    }

    private void setListData(JPanel panel, List<String> values) {
        JList<String> list = getJList(panel);
        if (list != null) {
            list.setListData(values.toArray(String[]::new));
        }
    }

    private void showErrorDialog(Component parent, String titolo, Exception ex) {
        String msg = ex != null ? ex.getMessage() : null;
        if (msg == null || msg.isBlank()) msg = "Operazione non riuscita. Riprova più tardi.";
        JOptionPane.showMessageDialog(parent, titolo + ": " + msg, "Errore", JOptionPane.ERROR_MESSAGE);
    }

    // Overload: mostra un messaggio diretto senza creare eccezioni lato UI
    private void showErrorDialog(Component parent, String titolo, String messaggio) {
        String msg = (messaggio != null && !messaggio.isBlank()) ? messaggio : "Operazione non riuscita.";
        JOptionPane.showMessageDialog(parent, titolo + ": " + msg, "Errore", JOptionPane.ERROR_MESSAGE);
    }

    private void ensureActionButtons() {
        if (eliminaBtn != null) return; // già creati
        eliminaBtn = createPrimaryActionButton("Elimina selezionato");
        modificaBtn = createPrimaryActionButton("Modifica selezionato");
        accettaBtn = createPrimaryActionButton("Accetta");
        rifiutaBtn = createPrimaryActionButton("Rifiuta");
        eliminaBtn.addActionListener(e -> onElimina());
        modificaBtn.addActionListener(e -> onModifica());
        accettaBtn.addActionListener(e -> onAccetta());
        rifiutaBtn.addActionListener(e -> onRifiuta());
    }

    // Stile uniforme: stesso background / font bold / foreground bianco del bottone "Aggiorna"
    private JButton createPrimaryActionButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Tahoma", Font.BOLD, 14));
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(100, 149, 237));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(6,14,6,14));
        return b;
    }

    private void updateActionButtonsState() {
        if (dynamicActionsWrapper == null) return;
        ensureActionButtons();
    dynamicActionsWrapper.removeAll();
        // Determina quale lista ha focus/selezione
        JList<String> annunciList = getJList(recentAnnunciPanel);
        JList<String> mieOffList = getJList(mieOffertePanel);
        JList<String> gestireList = getJList(offerteDaGestirePanel);
        String selected = null;
        boolean fromAnnunci = false;
        boolean fromMieOfferte = false;
        boolean fromGestire = false;
        if (annunciList.getSelectedValue() != null) { selected = annunciList.getSelectedValue(); fromAnnunci = true; }
        else if (mieOffList.getSelectedValue() != null) { selected = mieOffList.getSelectedValue(); fromMieOfferte = true; }
        else if (gestireList.getSelectedValue() != null) { selected = gestireList.getSelectedValue(); fromGestire = true; }
        if (selected == null) {
            // Nessuna selezione: area resta vuota ma mantiene l'altezza
            dynamicActionsWrapper.revalidate();
            dynamicActionsWrapper.repaint();
            return;
        }
        // Formati attuali:
        // Annuncio: Titolo [Tipo] Stato - dd/MM
        // Offerta mia / da gestire: ID_OFFERTA [Tipo] Stato
        // Decidiamo: se stringa inizia con "OFF-" -> offerta, se no annuncio.
        boolean isOfferta = selected.startsWith("OFF-");
        if (isOfferta) {
            String stato = null;
            int lastSpace = selected.lastIndexOf(' ');
            if (lastSpace > -1) stato = selected.substring(lastSpace+1).trim();
            if (stato != null && stato.equalsIgnoreCase("Attesa")) {
                if (fromMieOfferte) { dynamicActionsWrapper.add(modificaBtn); dynamicActionsWrapper.add(eliminaBtn); }
                if (fromGestire) { dynamicActionsWrapper.add(accettaBtn); dynamicActionsWrapper.add(rifiutaBtn); }
            }
        } else {
            if (fromAnnunci) {
                // Parse stato dell'annuncio tra ']' e ' -'
                String statoLower = null;
                int rb = selected.indexOf(']');
                if (rb > -1) {
                    int dash = selected.indexOf(" -", rb);
                    String after = dash > -1 ? selected.substring(rb + 1, dash) : selected.substring(rb + 1);
                    statoLower = after.trim().toLowerCase();
                }
                boolean canModify = "attivo".equals(statoLower);
                if (canModify) {
                    dynamicActionsWrapper.add(modificaBtn);
                    dynamicActionsWrapper.add(eliminaBtn);
                }
            }
        }
        // Non nascondiamo: wrapper rimane sempre visibile per stabilità layout
        dynamicActionsWrapper.revalidate();
        dynamicActionsWrapper.repaint();
    }

    // Attiva doppio click solo per la lista "Le tue offerte" per mostrare un dettaglio leggibile
    private void attachDoubleClickToMieOfferte() {
        JList<String> list = getJList(mieOffertePanel);
        if (list == null) return;
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int index = list.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        Rectangle cellBounds = list.getCellBounds(index, index);
                        if (cellBounds != null && cellBounds.contains(e.getPoint())) {
                            String row = list.getModel().getElementAt(index);
                            String id = estraiIdDaRiga(row);
                            if (id != null && id.startsWith("OFF-")) {
                                mostraDettaglioOfferta(id);
                            }
                        }
                    }
                }
            }
        });
    }

    private void attachDoubleClickToOfferteDaGestire() {
        JList<String> list = getJList(offerteDaGestirePanel);
        if (list == null) return;
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int index = list.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        Rectangle cellBounds = list.getCellBounds(index, index);
                        if (cellBounds != null && cellBounds.contains(e.getPoint())) {
                            String row = list.getModel().getElementAt(index);
                            String id = estraiIdDaRiga(row);
                            if (id != null && id.startsWith("OFF-")) {
                                mostraDettaglioOfferta(id);
                            }
                        }
                    }
                }
            }
        });
    }

    // Doppio click su "I tuoi annunci": se lo stato è non Attivo (Venduto/Scambiato/Regalato/Chiuso) mostra i dettagli
    private void attachDoubleClickToAnnunci() {
        JList<String> list = getJList(recentAnnunciPanel);
        if (list == null) return;
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int index = list.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        Rectangle cellBounds = list.getCellBounds(index, index);
                        if (cellBounds != null && cellBounds.contains(e.getPoint())) {
                            String row = list.getModel().getElementAt(index);
                            if (row == null || row.startsWith("OFF-")) return; // safety
                            String id = estraiIdDaRiga(row);
                            String stato = estraiStatoAnnuncioDaRiga(row);
                            if (id != null && stato != null) {
                                String s = stato.toLowerCase();
                                if (!s.equals("attivo")) {
                                    mostraDettaglioAnnuncio(id);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    // Estrae lo stato dall'etichetta annuncio formattata: "ID Titolo [Tipo] Stato - dd/MM"
    private String estraiStatoAnnuncioDaRiga(String riga) {
        if (riga == null) return null;
        int rb = riga.indexOf(']');
        if (rb < 0) return null;
        int dash = riga.indexOf(" -", rb);
        if (dash < 0) dash = riga.length();
        String after = riga.substring(rb + 1, dash);
        return after != null ? after.trim() : null;
    }

    private void mostraDettaglioAnnuncio(String idAnnuncio) {
        try {
            String[] a = controller.recuperaAnnuncioFields(idAnnuncio);
            if (a == null || a.length < 9) {
                showErrorDialog(this, "Impossibile mostrare i dettagli", "Dati annuncio incompleti");
                return;
            }
            String titolo = a[0];
            String descrizione = a[1];
            String categoria = a[2];
            String stato = a[3];
            String tipo = a[4];
            String prezzo = a[5];
            String dataPub = a[6];
            String creatore = a[7];
            String idOggetto = a[8];

            JPanel panel = new JPanel(new BorderLayout(10, 12));
            panel.setBackground(Color.WHITE);
            panel.setBorder(new EmptyBorder(16, 18, 16, 18));

            JLabel header = new JLabel(titolo);
            header.setFont(new Font("Segoe UI", Font.BOLD, 18));
            panel.add(header, BorderLayout.NORTH);

            JTextArea descrArea = new JTextArea((descrizione == null || descrizione.isBlank()) ? "Nessuna descrizione." : descrizione, 6, 40);
            descrArea.setLineWrap(true);
            descrArea.setWrapStyleWord(true);
            descrArea.setEditable(false);
            descrArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            descrArea.setBackground(new Color(250, 250, 252));
            descrArea.setBorder(new EmptyBorder(8, 10, 8, 10));
            JScrollPane descrScroll = new JScrollPane(descrArea);
            descrScroll.setBorder(new LineBorder(new Color(230, 232, 236)));
            descrScroll.setPreferredSize(new Dimension(460, 140));
            panel.add(descrScroll, BorderLayout.CENTER);

            JPanel info = new JPanel(new GridLayout(0, 2, 12, 8));
            info.setOpaque(false);
            info.add(new JLabel("Tipo:"));
            info.add(new JLabel(tipo));
            info.add(new JLabel("Categoria:"));
            info.add(new JLabel(categoria));
            info.add(new JLabel("Prezzo:"));
            info.add(new JLabel((prezzo != null && !prezzo.isBlank() && !"-".equals(prezzo)) ? ("€ " + prezzo) : "-"));
            info.add(new JLabel("Data pubblicazione:"));
            info.add(new JLabel(dataPub != null ? dataPub : "-"));
            info.add(new JLabel("Oggetto:"));
            info.add(new JLabel(idOggetto != null ? idOggetto : "-"));
            panel.add(info, BorderLayout.SOUTH);

            JOptionPane.showMessageDialog(this, panel, "Dettaglio annuncio " + idAnnuncio, JOptionPane.PLAIN_MESSAGE);
        } catch (Exception ex) {
            showErrorDialog(this, "Impossibile mostrare i dettagli", ex);
        }
    }

    private void mostraDettaglioOfferta(String idOfferta) {
        if (idOfferta == null || idOfferta.isBlank()) return;
        try {
            String[] fields = controller.recuperaOffertaFields(idOfferta);
            // Ordine: tipo, prezzo, commento, idOggettoOfferto, stato, idAnnuncio
            if (fields == null || fields.length < 6) {
                showErrorDialog(this, "Impossibile mostrare i dettagli", "Dati offerta incompleti");
                return;
            }
            String tipo = fields[0];
            String prezzo = fields[1];
            String commento = fields[2];
            String idOggetto = fields[3];
            String stato = fields[4];
            String idAnnuncio = fields[5];

            JPanel panel = new JPanel(new BorderLayout(10, 12));
            panel.setBackground(Color.WHITE);
            panel.setBorder(new EmptyBorder(16, 18, 16, 18));

            JLabel header = new JLabel("Offerta " + idOfferta + "  [" + tipo + "]");
            header.setFont(new Font("Segoe UI", Font.BOLD, 18));
            panel.add(header, BorderLayout.NORTH);

            String commentoVal = (commento == null || commento.isBlank()) ? "Nessun commento." : commento;
            JTextArea commentoArea = new JTextArea(commentoVal, 5, 30);
            commentoArea.setLineWrap(true);
            commentoArea.setWrapStyleWord(true);
            commentoArea.setEditable(false);
            commentoArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            commentoArea.setBackground(new Color(250, 250, 252));
            commentoArea.setBorder(new EmptyBorder(8, 10, 8, 10));
            JScrollPane commentoScroll = new JScrollPane(commentoArea);
            commentoScroll.setBorder(new LineBorder(new Color(230, 232, 236)));
            commentoScroll.setPreferredSize(new Dimension(420, 120));
            panel.add(commentoScroll, BorderLayout.CENTER);

            JPanel infoGrid = new JPanel(new GridLayout(0, 2, 12, 8));
            infoGrid.setOpaque(false);

            infoGrid.add(new JLabel("Stato:"));
            infoGrid.add(new JLabel(stato));

            // Mostra Prezzo solo per offerte di tipo Vendita e se presente
            if ("Vendita".equalsIgnoreCase(tipo) && prezzo != null && !prezzo.isBlank() && !"-".equals(prezzo)) {
                infoGrid.add(new JLabel("Prezzo:"));
                infoGrid.add(new JLabel("€ " + prezzo));
            }

            infoGrid.add(new JLabel("Annuncio:"));
            infoGrid.add(new JLabel(idAnnuncio != null ? idAnnuncio : ""));

            // Mostra Oggetto solo per offerte di tipo Scambio e se presente, usando il nome
            if ("Scambio".equalsIgnoreCase(tipo) && idOggetto != null && !idOggetto.isBlank()) {
                String oggettoDisplay = idOggetto;
                try {
                    String nomeOggetto = controller.trovaNomeOggettoPerId(idOggetto);
                    if (nomeOggetto != null && !nomeOggetto.isBlank()) {
                        oggettoDisplay = nomeOggetto;
                    }
                } catch (Exception ignore) {
                    // fallback: lascia ID
                }
                infoGrid.add(new JLabel("Oggetto (scambio):"));
                infoGrid.add(new JLabel(oggettoDisplay));
            }

            panel.add(infoGrid, BorderLayout.SOUTH);

            JOptionPane.showMessageDialog(this, panel, "Dettaglio offerta", JOptionPane.PLAIN_MESSAGE);
        } catch (Exception ex) {
            showErrorDialog(this, "Impossibile mostrare i dettagli", ex);
        }
    }

    // Garantisce che solo una lista abbia una selezione attiva alla volta
    private void wireMutualSelection() {
        JList<String> lAnn = getJList(recentAnnunciPanel);
        JList<String> lMie = getJList(mieOffertePanel);
        JList<String> lGest = getJList(offerteDaGestirePanel);
        javax.swing.event.ListSelectionListener listenerAnn = e -> {
            if (!e.getValueIsAdjusting() && lAnn.getSelectedIndex() >= 0) {
                lMie.clearSelection();
                lGest.clearSelection();
                updateActionButtonsState();
            }
        };
        javax.swing.event.ListSelectionListener listenerMie = e -> {
            if (!e.getValueIsAdjusting() && lMie.getSelectedIndex() >= 0) {
                lAnn.clearSelection();
                lGest.clearSelection();
                updateActionButtonsState();
            }
        };
        javax.swing.event.ListSelectionListener listenerGest = e -> {
            if (!e.getValueIsAdjusting() && lGest.getSelectedIndex() >= 0) {
                lAnn.clearSelection();
                lMie.clearSelection();
                updateActionButtonsState();
            }
        };
        lAnn.addListSelectionListener(listenerAnn);
        lMie.addListSelectionListener(listenerMie);
        lGest.addListSelectionListener(listenerGest);
    }

    private String estraiIdDaRiga(String riga) {
        if (riga == null) return null;
        String trimmed = riga.trim();
        if (trimmed.isEmpty()) return null;
        int space = trimmed.indexOf(' ');
        return space > 0 ? trimmed.substring(0, space) : trimmed;
    }

    private void onModifica() {
        // Determina selezione e tipo
        JList<String> aList = getJList(recentAnnunciPanel);
        JList<String> mList = getJList(mieOffertePanel);
        JList<String> gList = getJList(offerteDaGestirePanel);
        String sel = null; boolean isOfferta=false; boolean isAnnuncio=false; boolean fromMieOfferte=false;
        if (mList.getSelectedValue()!=null) { sel = mList.getSelectedValue(); isOfferta=true; fromMieOfferte=true; }
    else if (aList.getSelectedValue()!=null) { sel = aList.getSelectedValue(); isAnnuncio=true; }
        else if (gList.getSelectedValue()!=null) { sel = gList.getSelectedValue(); isOfferta=true; }
        if (sel == null) return;

    String id = estraiIdDaRiga(sel);
    if (id == null) {
        JOptionPane.showMessageDialog(this, "Seleziona un elemento valido prima di continuare.");
        return;
    }

        try {
            if (isOfferta && fromMieOfferte) {
                // Modifica contenuti offerta (solo in Attesa)
                String[] fields = controller.recuperaOffertaFields(id);
                // fields: tipo, prezzo, commento, idOggettoOfferto, stato, idAnnuncio
                if (!"Attesa".equalsIgnoreCase(fields[4])) {
                    JOptionPane.showMessageDialog(this, "Puoi modificare solo le offerte ancora in attesa.");
                    return;
                }

                final String tipoOfferta = fields[0];
                final String statoOfferta = fields[4];
                JPanel p = new JPanel(new GridBagLayout());
                GridBagConstraints gc = new GridBagConstraints();
                gc.insets = new Insets(4,4,4,4);
                gc.fill = GridBagConstraints.HORIZONTAL;
                gc.weightx = 1;
                int row = 0;
                gc.gridwidth = 1;

                JTextField commentoField = new JTextField(fields[2] == null ? "" : fields[2]);
                JTextField prezzoField = null;
                JComboBox<String> oggettoCombo = null;
                java.util.List<String> oggettoIds = java.util.Collections.emptyList();

                if ("Vendita".equalsIgnoreCase(tipoOfferta)) {
                    prezzoField = new JTextField("-".equals(fields[1]) ? "" : fields[1]);
                    gc.gridx = 0; gc.gridy = row; p.add(new JLabel("Prezzo offerto (€):"), gc);
                    gc.gridx = 1; p.add(prezzoField, gc); row++;
                }

                gc.gridx = 0; gc.gridy = row; p.add(new JLabel("Commento:"), gc);
                gc.gridx = 1; p.add(commentoField, gc); row++;

                if ("Scambio".equalsIgnoreCase(tipoOfferta)) {
                    java.util.List<String> records = new java.util.ArrayList<>();
                    java.util.List<String> labels = new java.util.ArrayList<>();
                    java.util.List<String> ids = new java.util.ArrayList<>();
                    try {
                        records = controller.oggettiUtenteFormattati(matricola);
                    } catch (Exception ex) {
                        records = new java.util.ArrayList<>();
                    }
                    for (String rec : records) {
                        String idOg = controller.estraiIdOggetto(rec);
                        ids.add(idOg);
                        labels.add(controller.formatOggettoLabel(rec));
                    }
                    String idAttuale = fields[3];
                    if ((ids.isEmpty() || (idAttuale != null && !ids.contains(idAttuale))) && idAttuale != null && !idAttuale.isBlank()) {
                        ids.add(idAttuale);
                        labels.add(idAttuale + " (attuale)");
                    }
                    oggettoIds = ids;
                    if (labels.isEmpty()) {
                        oggettoCombo = new JComboBox<>(new String[] { "(Nessun oggetto disponibile)" });
                        oggettoCombo.setEnabled(false);
                    } else {
                        oggettoCombo = new JComboBox<>(labels.toArray(new String[0]));
                        if (idAttuale != null) {
                            int toSelect = ids.indexOf(idAttuale);
                            if (toSelect >= 0) oggettoCombo.setSelectedIndex(toSelect);
                        }
                    }
                    gc.gridx = 0; gc.gridy = row; p.add(new JLabel("Oggetto da scambiare:"), gc);
                    gc.gridx = 1; p.add(oggettoCombo, gc); row++;
                }

                Object[] options = { "Conferma", "Annulla" };
                int res = JOptionPane.showOptionDialog(this, p, "Modifica Offerta " + id,
                        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
                if (res == 0) {
                    String commentoStr = commentoField.getText().trim();
                    if (commentoStr.isEmpty()) commentoStr = null;

                    Float prezzoVal = null;
                    if ("Vendita".equalsIgnoreCase(tipoOfferta)) {
                        if (prezzoField == null) {
                            showErrorDialog(this, "Impossibile completare la modifica", "Campo prezzo mancante");
                            return;
                        }
                        String prezzoStr = prezzoField.getText().trim();
                        if (prezzoStr.isEmpty()) {
                            showErrorDialog(this, "Impossibile completare la modifica", "Prezzo richiesto per Vendita");
                            return;
                        }
                        try {
                            prezzoVal = Float.parseFloat(prezzoStr.replace(',','.'));
                        } catch (NumberFormatException nfe) {
                            showErrorDialog(this, "Impossibile completare la modifica", "Formato prezzo non valido");
                            return;
                        }
                    }

                    String idObjVal = null;
                    if ("Scambio".equalsIgnoreCase(tipoOfferta)) {
                        if (oggettoCombo == null || !oggettoCombo.isEnabled()) {
                            showErrorDialog(this, "Impossibile completare la modifica", "Nessun oggetto disponibile per lo scambio");
                            return;
                        }
                        int selIndex = oggettoCombo.getSelectedIndex();
                        if (selIndex < 0 || selIndex >= oggettoIds.size()) {
                            showErrorDialog(this, "Impossibile completare la modifica", "Selezione oggetto non valida");
                            return;
                        }
                        idObjVal = oggettoIds.get(selIndex);
                    }

                    controller.aggiornaOfferta(id, matricola, prezzoVal, idObjVal, commentoStr);
                    refreshDashboard();
                }
            } else if (isAnnuncio) {
                String[] a = controller.recuperaAnnuncioFields(id);
                String titolo0 = a[0];
                String descr0 = a[1];
                String categoria0 = a[2];
                String stato0 = a[3];
                String tipo0 = a[4];
                String prezzo0 = a[5];

                java.util.List<String> categorie = controller.elencoCategorieAnnuncio();

                JPanel panel = new JPanel(new BorderLayout(8, 8));
                panel.add(new JLabel("Modifica annuncio " + id + " (" + tipo0 + ")"), BorderLayout.NORTH);

                JTextArea descrArea = new JTextArea(descr0, 5, 30);
                descrArea.setLineWrap(true);
                descrArea.setWrapStyleWord(true);
                panel.add(new JScrollPane(descrArea), BorderLayout.CENTER);

                JPanel form = new JPanel(new GridBagLayout());
                GridBagConstraints gc = new GridBagConstraints();
                gc.insets = new Insets(4, 4, 4, 4);
                gc.fill = GridBagConstraints.HORIZONTAL;
                gc.weightx = 1;

                int row = 0;
                JTextField titoloField = new JTextField(titolo0);
                JComboBox<String> categoriaBox = new JComboBox<>(categorie.toArray(String[]::new));
                categoriaBox.setSelectedItem(categoria0);
                JTextField prezzoField = null;

                gc.gridx = 0; gc.gridy = row; form.add(new JLabel("Titolo:"), gc);
                gc.gridx = 1; form.add(titoloField, gc); row++;

                gc.gridx = 0; gc.gridy = row; form.add(new JLabel("Categoria:"), gc);
                gc.gridx = 1; form.add(categoriaBox, gc); row++;

                if ("Vendita".equalsIgnoreCase(tipo0)) {
                    prezzoField = new JTextField(prezzo0 != null && !"-".equals(prezzo0) ? prezzo0 : "");
                    gc.gridx = 0; gc.gridy = row; form.add(new JLabel("Prezzo (€):"), gc);
                    gc.gridx = 1; form.add(prezzoField, gc); row++;
                }

                panel.add(form, BorderLayout.SOUTH);

                Object[] options = { "Conferma", "Annulla" };
                int res = JOptionPane.showOptionDialog(this, panel, "Modifica annuncio",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
                if (res == 0) {
                    String nuovoTitolo = titoloField.getText() != null ? titoloField.getText().trim() : "";
                    if (nuovoTitolo.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Il titolo non può essere vuoto.");
                        return;
                    }
                    String nuovaDescrizione = descrArea.getText();
                    String nuovaCategoria = (String) categoriaBox.getSelectedItem();
                    String prezzoStr = "-";
                    if (prezzoField != null) {
                        String rawPrezzo = prezzoField.getText();
                        String sanitized = rawPrezzo == null ? "" : rawPrezzo.replace("€", "").trim();
                        sanitized = sanitized.replaceAll("\\s+", "");
                        if (!sanitized.isEmpty() && !"-".equals(sanitized)) {
                            sanitized = sanitized.replace(',', '.');
                            try {
                                BigDecimal prezzoVal = new BigDecimal(sanitized);
                                if (prezzoVal.compareTo(BigDecimal.ZERO) <= 0) {
                                    JOptionPane.showMessageDialog(this, "Il prezzo deve essere maggiore di zero.");
                                    return;
                                }
                                prezzoStr = prezzoVal.toPlainString();
                            } catch (NumberFormatException nfe) {
                                JOptionPane.showMessageDialog(this, "Formato prezzo non valido. Usa solo cifre ed eventualmente virgola.");
                                return;
                            }
                        }
                    }

                    controller.aggiornaAnnuncio(id, nuovoTitolo, nuovaDescrizione, nuovaCategoria, stato0, prezzoStr);
                    refreshDashboard();
                }
            }
        } catch (Exception ex) {
            showErrorDialog(this, "Impossibile completare la modifica", ex);
        }
    }

    private void onElimina() {
        String id = null; boolean isOfferta=false; boolean isAnnuncio=false;
        JList<String> aList = getJList(recentAnnunciPanel);
        JList<String> mList = getJList(mieOffertePanel);
        if (mList.getSelectedValue()!=null) {
            id = estraiIdDaRiga(mList.getSelectedValue());
            isOfferta=true;
        }
        else if (aList.getSelectedValue()!=null) {
            id = estraiIdDaRiga(aList.getSelectedValue());
            isAnnuncio=true;
        }
        if (id==null) {
            JOptionPane.showMessageDialog(this, "Seleziona un elemento valido prima di procedere.");
            return;
        }
        int conferma = JOptionPane.showConfirmDialog(this, "Confermi eliminazione?", "Conferma", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conferma != JOptionPane.YES_OPTION) return;
        try {
            if (isOfferta) {
                controller.ritiraOfferta(id, matricola); // elimina via ritira (cancella in Attesa)
            } else if (isAnnuncio) {
                controller.eliminaAnnuncio(id);
            }
            if (aList != null) aList.clearSelection();
            if (mList != null) mList.clearSelection();
            JList<String> gList = getJList(offerteDaGestirePanel);
            if (gList != null) gList.clearSelection();
            refreshDashboard();
            if (annunciFrame != null) annunciFrame.refreshContent();
            if (oggettiFrame != null) oggettiFrame.refreshContent();
            updateActionButtonsState();
        } catch (Exception ex) {
            showErrorDialog(this, "Eliminazione non riuscita", ex);
        }
    }

    private void onAccetta() {
        JList<String> gList = getJList(offerteDaGestirePanel);
        String sel = gList.getSelectedValue();
        if (sel==null) return;
        String id = estraiIdDaRiga(sel);
        if (id==null) {
            JOptionPane.showMessageDialog(this, "Seleziona un'offerta valida dalla lista.");
            return;
        }
        int conferma = JOptionPane.showConfirmDialog(this, "Accettare l'offerta?", "Conferma", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (conferma != JOptionPane.YES_OPTION) return;
        try {
            controller.accettaOfferta(id, matricola);
            refreshDashboard();
        } catch (Exception ex) {
            showErrorDialog(this, "Impossibile accettare l'offerta", ex);
        }
    }

    private void onRifiuta() {
        JList<String> gList = getJList(offerteDaGestirePanel);
        String sel = gList.getSelectedValue();
        if (sel==null) return;
        String id = estraiIdDaRiga(sel);
        if (id==null) {
            JOptionPane.showMessageDialog(this, "Seleziona un'offerta valida dalla lista.");
            return;
        }
        int conferma = JOptionPane.showConfirmDialog(this, "Rifiutare l'offerta?", "Conferma", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conferma != JOptionPane.YES_OPTION) return;
        try {
            controller.rifiutaOfferta(id, matricola);
            refreshDashboard();
        } catch (Exception ex) {
            showErrorDialog(this, "Impossibile rifiutare l'offerta", ex);
        }
    }



    private JPanel buildPlaceholder(String name) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        JLabel l = new JLabel(name, SwingConstants.CENTER);
        l.setFont(new Font("Tahoma", Font.PLAIN, 26));
        p.add(l, BorderLayout.CENTER);
        return p;
    }

    // (Logica annunci rimossa da HomeFrame)

    private void selectSection(String name) {
        if (cardLayout != null && cardPanel != null) {
            cardLayout.show(cardPanel, name);
            for (SectionEntry entry : sectionHandlers) {
                if (entry.name.equals(name) && entry.refresher != null) {
                    SwingUtilities.invokeLater(entry.refresher);
                    break;
                }
            }
        }
        for (JToggleButton b : navButtons) {
            if (b.getText().equals(name)) {
                if (!b.isSelected()) b.setSelected(true);
            }
        }
    }

    private static class SectionEntry {
        final String name;
        final Runnable refresher;

        SectionEntry(String name, Runnable refresher) {
            this.name = name;
            this.refresher = refresher;
        }
    }

    // Renderer card per liste dashboard (annunci, offerte, da gestire)
    private static class DashboardListCardRenderer extends JPanel implements ListCellRenderer<String> {
        private final JLabel primary = new JLabel();
        private final JLabel secondary = new JLabel();
        private final JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT,4,0));

        DashboardListCardRenderer() {
            setLayout(new BorderLayout(6,4));
            setOpaque(true);
            setBorder(new EmptyBorder(8,10,8,10));
            primary.setFont(new Font("Tahoma", Font.BOLD, 13));
            secondary.setFont(new Font("Tahoma", Font.PLAIN, 11));
            secondary.setForeground(new Color(110,110,115));
            badgePanel.setOpaque(false);
            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);
            top.add(primary, BorderLayout.CENTER);
            top.add(badgePanel, BorderLayout.EAST);
            add(top, BorderLayout.NORTH);
            add(secondary, BorderLayout.SOUTH);
        }

        private JLabel badge(String txt, Color bg) {
            JLabel l = new JLabel(txt.toUpperCase());
            l.setFont(new Font("Tahoma", Font.BOLD, 9));
            l.setForeground(Color.WHITE);
            l.setOpaque(true);
            l.setBackground(bg);
            l.setBorder(new EmptyBorder(2,6,2,6));
            return l;
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
            // Heuristics: Offerta se inizia con OFF-
            boolean isOfferta = value != null && value.startsWith("OFF-");
            primary.setText(value != null ? value : "");
            // Rimuove completamente la label secondaria (né "Annuncio" né "Offerta")
            secondary.setText("");
            secondary.setVisible(false);

            badgePanel.removeAll();
            if (isOfferta) {
                String label = "OFFERTA";
                Color c = new Color(108,117,125);
                if (value.contains(" Attesa")) { label = "IN ATTESA"; c = new Color(255,140,0); }
                else if (value.contains(" Accettata")) { label = "ACCETTATA"; c = new Color(46,160,67); }
                else if (value.contains(" Rifiutata")) { label = "RIFIUTATA"; c = new Color(200,60,60); }
                badgePanel.add(badge(label, c));
            } else {
                // Badge sullo STATO annuncio (non il tipo)
                if (value != null) {
                    // Rimuove la porzione di stato dal testo (" [Tipo] Stato - dd/MM" => " [Tipo] - dd/MM")
                    String display = value;
                    int rb = display.indexOf(']');
                    if (rb >= 0) {
                        int dash = display.indexOf(" -", rb);
                        if (dash > rb) {
                            String left = display.substring(0, rb + 1);
                            String right = display.substring(dash);
                            display = left + right;
                            primary.setText(display);
                        }
                    }
                }
                String label = null;
                Color c = new Color(108,117,125);
                if (value.contains(" Attivo ")) { label = "ATTIVO"; c = new Color(0,120,212); }
                else if (value.contains(" Venduto ")) { label = "VENDUTO"; c = new Color(46,160,67); }
                else if (value.contains(" Scambiato ")) { label = "SCAMBIATO"; c = new Color(0,120,212); }
                else if (value.contains(" Regalato ")) { label = "REGALATO"; c = new Color(218,112,37); }
                else if (value.contains(" Chiuso ")) { label = "CHIUSO"; c = new Color(108,117,125); }
                if (label != null) badgePanel.add(badge(label, c));
            }

            setBackground(isSelected ? new Color(218,230,247) : Color.WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return this;
        }
    }

}