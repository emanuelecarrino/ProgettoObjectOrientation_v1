package frames;

import dto.Controller;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;

public class HomeFrame extends JFrame {

    // === CAMPI ===
    private final Controller controller;
    private final String matricola;
    private final String usernameDisplay;
    private JPanel sidebar;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private final List<JToggleButton> navButtons = new ArrayList<>();
    private JPanel kpiPanel;
    private JPanel[] kpiCardContainers;
    private JPanel recentAnnunciPanel;
    private JPanel mieOffertePanel;
    private JPanel offerteDaGestirePanel;
    private JPanel dynamicActionsWrapper;
    private JButton eliminaBtn;
    private JButton accettaBtn;
    private JButton rifiutaBtn;

    // === COSTRUTTORE ===
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
        int targetW = 1500;
        int targetH = 750;
        int x = Math.max(0, (screen.width - targetW) / 2);
        int y = Math.max(0, (screen.height - targetH) / 2);
        setBounds(x, y, targetW, targetH);
        setMinimumSize(new Dimension(targetW, targetH));
        setResizable(true);
        buildUI();
        setVisible(true);
    }

    // === COSTRUZIONE UI ===
    private void buildUI() {
        setLayout(new BorderLayout());
        buildSidebar();
        buildSections();
        selectSection("Homepage");
    }

    private void buildSidebar() {
        sidebar = new JPanel();
        sidebar.setLayout(new GridBagLayout());
        sidebar.setBackground(new Color(245, 245, 247));
        sidebar.setPreferredSize(new Dimension(200, getHeight()));
        add(sidebar, BorderLayout.WEST);

        String[] sections = {"Homepage", "Annunci", "I tuoi oggetti", "Profilo", "ModConsegna"};
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

        gbc.gridy = ++row;
        gbc.weighty = 0;
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBackground(new Color(220, 53, 69));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFont(new Font("Tahoma", Font.BOLD, 14));
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
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
        btn.setBackground(new Color(245, 245, 247));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        btn.setFont(new Font("Tahoma", Font.PLAIN, 15));
        btn.setOpaque(true);

        Color hover = new Color(228, 230, 235);
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
        add(cardPanel, BorderLayout.CENTER);
    }


        // === HOMEPAGE ===
    private JPanel buildHomepagePanel() {
        JPanel homepage = new JPanel(new BorderLayout());
        homepage.setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        header.setBackground(Color.WHITE);
        JLabel titolo = new JLabel("Benvenuto, " + usernameDisplay + "!");
        titolo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        header.add(titolo, BorderLayout.WEST);
        homepage.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(10, 30, 30, 30));
        content.setBackground(Color.WHITE);
        homepage.add(content, BorderLayout.CENTER);

        buildKPIPanel(content);
        buildRecentAnnunci(content);
        buildMieOfferte(content);
        buildOfferteDaGestire(content);

        return homepage;
    }

    // === KPI PANEL ===
    private void buildKPIPanel(JPanel parent) {
        kpiPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        kpiPanel.setBackground(Color.WHITE);
        kpiPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        parent.add(kpiPanel);
        parent.add(Box.createVerticalStrut(20));

        String[] titles = {"Annunci attivi", "Oggetti in tuo possesso", "Offerte ricevute"};
        kpiCardContainers = new JPanel[titles.length];
        for (int i = 0; i < titles.length; i++) {
            JPanel container = buildKPICard(titles[i], "0");
            kpiPanel.add(container);
            kpiCardContainers[i] = container;
        }
    }

    private JPanel buildKPICard(String title, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        panel.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        return panel;
    }

    // === SEZIONE ANNUNCI RECENTI ===
    private void buildRecentAnnunci(JPanel parent) {
        JLabel lbl = new JLabel("Annunci recenti");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        parent.add(lbl);
        parent.add(Box.createVerticalStrut(10));

        recentAnnunciPanel = new JPanel(new GridLayout(0, 3, 15, 15));
        recentAnnunciPanel.setBackground(Color.WHITE);
        parent.add(recentAnnunciPanel);
        parent.add(Box.createVerticalStrut(25));
    }

    // === SEZIONE MIE OFFERTE ===
    private void buildMieOfferte(JPanel parent) {
        JLabel lbl = new JLabel("Le tue offerte");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        parent.add(lbl);
        parent.add(Box.createVerticalStrut(10));

        mieOffertePanel = new JPanel(new GridLayout(0, 3, 15, 15));
        mieOffertePanel.setBackground(Color.WHITE);
        parent.add(mieOffertePanel);
        parent.add(Box.createVerticalStrut(25));
    }

    // === SEZIONE OFFERTE DA GESTIRE ===
    
    private void buildOfferteDaGestire(JPanel parent) {
        JLabel lbl = new JLabel("Offerte da gestire");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        parent.add(lbl);
        parent.add(Box.createVerticalStrut(10));

        offerteDaGestirePanel = new JPanel(new GridLayout(0, 3, 15, 15));
        offerteDaGestirePanel.setBackground(Color.WHITE);
        parent.add(offerteDaGestirePanel);
        parent.add(Box.createVerticalStrut(25));

        dynamicActionsWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        dynamicActionsWrapper.setBackground(Color.WHITE);
        parent.add(dynamicActionsWrapper);

        eliminaBtn = createActionButton("Elimina", new Color(220, 53, 69));
        accettaBtn = createActionButton("Accetta", new Color(40, 167, 69));
        rifiutaBtn = createActionButton("Rifiuta", new Color(255, 193, 7));

        dynamicActionsWrapper.add(eliminaBtn);
        dynamicActionsWrapper.add(accettaBtn);
        dynamicActionsWrapper.add(rifiutaBtn);
    }

    private JButton createActionButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Tahoma", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // === NAVIGAZIONE ===
    private void selectSection(String section) {
        cardLayout.show(cardPanel, section);
        for (JToggleButton btn : navButtons) {
            if (btn.getText().equals(section)) {
                btn.setSelected(true);
            } else {
                btn.setSelected(false);
            }
        }
    }

    // === METODI DI SUPPORTO ===
    public void aggiornaKPI(int annunci, int oggetti, int offerte) {
        JLabel l1 = (JLabel) ((BorderLayout) kpiCardContainers[0].getLayout()).getLayoutComponent(BorderLayout.CENTER);
        JLabel l2 = (JLabel) ((BorderLayout) kpiCardContainers[1].getLayout()).getLayoutComponent(BorderLayout.CENTER);
        JLabel l3 = (JLabel) ((BorderLayout) kpiCardContainers[2].getLayout()).getLayoutComponent(BorderLayout.CENTER);
        l1.setText(String.valueOf(annunci));
        l2.setText(String.valueOf(oggetti));
        l3.setText(String.valueOf(offerte));
    }

}
