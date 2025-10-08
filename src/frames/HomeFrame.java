package frames;

import dto.Controller;
import java.util.List;
import java.util.ArrayList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;

public class HomeFrame extends JFrame {

    private final Controller controller;
    private final String matricola;
    // Username risolto on-demand per display (può essere null se non trovato)
    private final String usernameDisplay;
    private JPanel sidebar;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private final List<JToggleButton> navButtons = new ArrayList<>();

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
        "ModConsegna",
        "Offerte ricevute"
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
        cardPanel.add(buildHomepagePanel(), "Homepage");
        cardPanel.add(new AnnunciFrame(controller, matricola).buildContentPanel(), "Annunci");
        cardPanel.add(new OggettiFrame(controller, matricola).buildContentPanel(), "I tuoi oggetti");
        cardPanel.add(new ProfiloFrame(controller, matricola).buildContentPanel(), "Profilo");
        cardPanel.add(new ModConsegnaFrame(controller, matricola).buildContentPanel(), "ModConsegna");
        cardPanel.add(new OfferteRicevuteFrame(controller, matricola).buildContentPanel(), "Offerte ricevute");
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
    JButton refreshBtn = createPrimaryActionButton("Aggiorna");
    refreshBtn.setFont(new Font("Tahoma", Font.BOLD, 15));
    refreshBtn.addActionListener(e -> refreshDashboard());
        header.add(refreshBtn, BorderLayout.EAST);
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

    recentAnnunciPanel = buildListPanel("I tuoi ultimi annunci");
    mieOffertePanel = buildListPanel("Le tue Offerte");
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
            JOptionPane.showMessageDialog(this, "Errore caricamento dashboard: " + ex.getMessage());
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

    private void ensureActionButtons() {
        if (eliminaBtn != null) return; // già creati
        eliminaBtn = createPrimaryActionButton("Elimina selezionato");
        accettaBtn = createPrimaryActionButton("Accetta");
        rifiutaBtn = createPrimaryActionButton("Rifiuta");
        eliminaBtn.addActionListener(e -> onElimina());
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
                if (fromMieOfferte) dynamicActionsWrapper.add(eliminaBtn);
                if (fromGestire) { dynamicActionsWrapper.add(accettaBtn); dynamicActionsWrapper.add(rifiutaBtn); }
            }
        } else {
            if (fromAnnunci) dynamicActionsWrapper.add(eliminaBtn); // (ID annuncio non ancora nel testo)
        }
        // Non nascondiamo: wrapper rimane sempre visibile per stabilità layout
        dynamicActionsWrapper.revalidate();
        dynamicActionsWrapper.repaint();
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
        // Se offerta: inizia con OFF-
        if (riga.startsWith("OFF-")) {
            int space = riga.indexOf(' ');
            return space>0 ? riga.substring(0, space) : riga; 
        }
        // Se annuncio: formato Titolo [Tipo] Stato - data -> non contiene ID.
        // Necessario allora ricaricare? Per semplicità qui non possiamo eliminare senza ID.
        // TODO: Migliorare formato ultimiAnnunciCreatore per includere ID hidden.
        return null;
    }

    private void onElimina() {
        String id = null; boolean isOfferta=false; boolean isAnnuncio=false;
        JList<String> aList = getJList(recentAnnunciPanel);
        JList<String> mList = getJList(mieOffertePanel);
        if (mList.getSelectedValue()!=null) { id = estraiIdDaRiga(mList.getSelectedValue()); isOfferta=true; }
        else if (aList.getSelectedValue()!=null) { /* manca ID annuncio nel formato corrente */ }
        if (id==null && isOfferta) { JOptionPane.showMessageDialog(this, "Impossibile determinare ID offerta"); return; }
        int conferma = JOptionPane.showConfirmDialog(this, "Confermi eliminazione?", "Conferma", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conferma != JOptionPane.YES_OPTION) return;
        try {
            if (isOfferta) {
                controller.ritiraOfferta(id, matricola); // elimina via ritira (cancella in Attesa)
                refreshDashboard();
            } else if (isAnnuncio) {
                // controller.eliminaAnnuncio(idAnnuncio);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Errore eliminazione: "+ex.getMessage());
        }
    }

    private void onAccetta() {
        JList<String> gList = getJList(offerteDaGestirePanel);
        String sel = gList.getSelectedValue();
        if (sel==null) return;
        String id = estraiIdDaRiga(sel);
        if (id==null) { JOptionPane.showMessageDialog(this, "ID offerta non trovato"); return; }
        int conferma = JOptionPane.showConfirmDialog(this, "Accettare l'offerta?", "Conferma", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (conferma != JOptionPane.YES_OPTION) return;
        try {
            controller.accettaOfferta(id, matricola);
            refreshDashboard();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Errore accettazione: "+ex.getMessage());
        }
    }

    private void onRifiuta() {
        JList<String> gList = getJList(offerteDaGestirePanel);
        String sel = gList.getSelectedValue();
        if (sel==null) return;
        String id = estraiIdDaRiga(sel);
        if (id==null) { JOptionPane.showMessageDialog(this, "ID offerta non trovato"); return; }
        int conferma = JOptionPane.showConfirmDialog(this, "Rifiutare l'offerta?", "Conferma", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conferma != JOptionPane.YES_OPTION) return;
        try {
            controller.rifiutaOfferta(id, matricola);
            refreshDashboard();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Errore rifiuto: "+ex.getMessage());
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
        }
        for (JToggleButton b : navButtons) {
            if (b.getText().equals(name)) {
                if (!b.isSelected()) b.setSelected(true);
            }
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
                Color c = new Color(0,120,212);
                if (value.contains(" Attesa")) c = new Color(255,140,0);
                else if (value.contains(" Accettata")) c = new Color(46,160,67);
                else if (value.contains(" Rifiutata")) c = new Color(200,60,60);
                badgePanel.add(badge("OFF", c));
            } else {
                // estrai tipo tra [] se presente
                int lb = value!=null? value.indexOf('['):-1;
                int rb = value!=null? value.indexOf(']'):-1;
                if (lb>-1 && rb>lb) {
                    String tipo = value.substring(lb+1, rb).trim();
                    Color c;
                    switch (tipo.toLowerCase()) {
                        case "vendita": c = new Color(46,160,67); break;
                        case "scambio": c = new Color(0,120,212); break;
                        case "regalo": c = new Color(218,112,37); break;
                        default: c = new Color(108,117,125); break;
                    }
                    badgePanel.add(badge(tipo, c));
                }
            }

            setBackground(isSelected ? new Color(218,230,247) : Color.WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return this;
        }
    }

}