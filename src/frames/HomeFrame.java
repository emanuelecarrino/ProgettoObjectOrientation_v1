package frames;

import dto.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;

public class HomeFrame extends JFrame {

    private final Controller controller;
    private final String matricola;
    // Username risolto on-demand per display (può essere null se non trovato)
    private final String usernameDisplay;
    private JPanel sidebar;
    // Navigazione semplice con CardLayout (senza AnimatedSwitcher)
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


        setTitle("UninaSwap - Home");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screen.width - 900) / 2;
        int y = (screen.height - 600) / 2;
        setBounds(x, y, 900, 600);
        setMinimumSize(new Dimension(820, 520));
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
    }

    private JToggleButton createNavButton(String text) {
        JToggleButton btn = new JToggleButton(text);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBackground(new Color(245,245,247));
        btn.setBorder(BorderFactory.createEmptyBorder(10,14,10,14));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btn.setOpaque(true);

        Color hover = new Color(228,230,235);
        Color selected = new Color(210,212,218);
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
        benv.setFont(new Font("Segoe UI", Font.BOLD, 30));
        header.add(benv, BorderLayout.WEST);
        JButton refreshBtn = new JButton("Aggiorna");
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

        SwingUtilities.invokeLater(this::refreshDashboard);
        return wrapper;
    }


    private JPanel kpiPanel;
    private JPanel[] kpiCardContainers;
    private JPanel recentAnnunciPanel;
    private JPanel mieOffertePanel;
    private JPanel offerteDaGestirePanel;

    private JPanel buildKpiCard(String value, String label) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(250,250,252));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230,230,234)),
                BorderFactory.createEmptyBorder(10,14,10,14)));
        JLabel val = new JLabel(value, SwingConstants.LEFT);
        val.setFont(new Font("Segoe UI", Font.BOLD, 28));
        JLabel lab = new JLabel(label.toUpperCase());
        lab.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lab.setForeground(new Color(90,90,95));
        panel.add(val, BorderLayout.CENTER);
        panel.add(lab, BorderLayout.SOUTH);
        panel.putClientProperty("valueLabel", val);
        panel.putClientProperty("nameLabel", lab);
        return panel;
    }

    private JPanel buildListPanel(String title) {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0,0,0,0),
                BorderFactory.createLineBorder(new Color(235,235,238))));
        JLabel t = new JLabel("  " + title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 14));
        t.setBorder(BorderFactory.createMatteBorder(0,0,1,0,new Color(230,230,233)));
        container.add(t, BorderLayout.NORTH);
        JList<String> list = new JList<>(new String[0]);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        list.setFixedCellHeight(24);
        container.add(new JScrollPane(list), BorderLayout.CENTER);
        container.putClientProperty("jlist", list);
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
            list.setListData(values.toArray(new String[0]));
        }
    }



    private JPanel buildPlaceholder(String name) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        JLabel l = new JLabel(name, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 26));
        p.add(l, BorderLayout.CENTER);
        return p;
    }

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

    // (Rimossa classe AnimatedSwitcher per semplificare: si usa direttamente CardLayout)

}