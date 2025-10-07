import javax.swing.JOptionPane;

import dto.DB_Connection;
import frames.*;
import java.awt.EventQueue;

public class Main {
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                // Prova la connessione DB prima di aprire la UI
                DB_Connection.getConnection();

                LoginFrame frame = new LoginFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                    null,
                    "Connessione al database fallita:\n" + e.getMessage(),
                    "Errore connessione",
                    JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }
        });
    }
}
