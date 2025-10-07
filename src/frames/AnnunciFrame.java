package frames;

import dto.Controller;
import javax.swing.*;
import java.awt.*;

public class AnnunciFrame extends JFrame {
    private final Controller controller;
    private final String matricola;

    public AnnunciFrame(Controller controller, String matricola) {
        this.controller = controller;
        this.matricola = matricola;
        setTitle("UninaSwap - Annunci");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // non chiudere l'intera app se usato stand-alone
        setSize(900,600);
        setLocationRelativeTo(null);
        setContentPane(buildContentPanel());
    }
    public JPanel buildContentPanel() {
        JPanel root = new JPanel(new BorderLayout());
        JLabel l = new JLabel("Sezione Annunci", SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 26));
        root.add(l, BorderLayout.CENTER);
        return root;
    }
}
