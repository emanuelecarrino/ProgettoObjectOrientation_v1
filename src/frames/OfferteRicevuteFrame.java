package frames;

import dto.Controller;
import javax.swing.*;
import java.awt.*;

public class OfferteRicevuteFrame extends JFrame {
    private final Controller controller;
    private final String matricola;

    public OfferteRicevuteFrame(Controller controller, String matricola) {
        this.controller = controller;
        this.matricola = matricola;
        setTitle("UninaSwap - Offerte Ricevute");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900,600);
        setLocationRelativeTo(null);
        setContentPane(buildContentPanel());
    }
    public JPanel buildContentPanel() {
        JPanel root = new JPanel(new BorderLayout());
        JLabel l = new JLabel("Offerte Ricevute", SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 26));
        root.add(l, BorderLayout.CENTER);
        return root;
    }
}
