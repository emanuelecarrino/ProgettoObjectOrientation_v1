package frames;

import dto.Controller;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class OggettiFrame extends JFrame {
    private final Controller controller;
    private final String matricola;
    private final JPanel contentPanel;

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
        root.add(header, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        list = new JList<>(listModel);
        list.setFont(new Font("Tahoma", Font.PLAIN, 13));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(-1);
        list.setCellRenderer(new OggettoCardRenderer());

        root.add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(6,12,6,12));
        bottom.setBackground(Color.WHITE);
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
        bottom.add(statusLabel, BorderLayout.WEST);
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
            JOptionPane.showMessageDialog(contentPanel, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
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
    }
}
