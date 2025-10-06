package frames;

import javax.swing.*;
import java.awt.*;

public class HomeFrame extends JFrame{

    private JPanel contentPane;
    private JLabel label1;
    private JButton button1;
    private JButton button2;

    public HomeFrame(){
        setTitle("Home");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // massimizza orizz. + vert.
        setVisible(true);
        contentPane = new JPanel();
        contentPane.setLayout(new GridBagLayout());
        setContentPane(contentPane);

        label1 = new JLabel("Benvenuto!");
        label1.setFont(new Font ("Tahoma", Font.BOLD, 38));

        button1 = new JButton("VISUALIZZA ANNUNCI");
        button1.setFont(new Font("Tahoma", Font.PLAIN, 24));
        button1.setPreferredSize(new Dimension(350, 70));
        button1.setBackground(new Color(100, 149, 237));
        button1.setForeground(Color.WHITE);
        button1.setFocusPainted(false);

        button2 = new JButton("I MIEI ANNUNCI");
        button2.setFont(new Font("Tahoma", Font.PLAIN, 24));
        button2.setPreferredSize(new Dimension(350, 70));
        button2.setBackground(new Color(100, 149, 237));
        button2.setForeground(Color.WHITE);
        button2.setFocusPainted(false);

        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;
        contentPane.add(label1, gbc);

        gbc.gridy = 1;
        contentPane.add(button1, gbc);

        gbc.gridy = 2;
        contentPane.add(button2, gbc);

    }
}