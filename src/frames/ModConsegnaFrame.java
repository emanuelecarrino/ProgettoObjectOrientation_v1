package frames;

import dto.Controller;
import javax.swing.*;
import java.awt.*;

public class ModConsegnaFrame extends JFrame {
    private final Controller controller;
    private final String matricola;
    private final JPanel contentPanel;

    public ModConsegnaFrame(Controller controller, String matricola) {
        this.controller = controller;
        this.matricola = matricola;
        setTitle("UninaSwap - Modalità Consegna");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        this.contentPanel = buildContentPanel();
        setContentPane(contentPanel);
    }

    public JPanel buildContentPanel() {
        JPanel root = new JPanel(new BorderLayout());
        JLabel l = new JLabel("Modalità di Consegna", SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 26));
        root.add(l, BorderLayout.CENTER);
        return root;
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public void refreshContent() {
        // Nessun dato dinamico al momento, ma il metodo è previsto per uniformità.
    }
}
