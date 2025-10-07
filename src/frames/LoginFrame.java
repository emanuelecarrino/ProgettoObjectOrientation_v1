package frames;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import dto.Controller;
import exception.ApplicationException;

public class LoginFrame extends JFrame {

	private JPanel contentPane;
	private JTextField textFieldUsername;
	private JPasswordField passwordField;
	private JLabel lblMessage;

	private final Controller controller = new Controller();

	public LoginFrame() {
		setTitle("Login");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 500, 350);
		setResizable(false); // blocca il ridimensionamento
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		// =================== LABEL TITOLO ===================
		JLabel lblTitle = new JLabel("LOGIN");
		lblTitle.setFont(new Font("Tahoma", Font.BOLD, 24));
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle.setBounds(0, 30, 484, 30);
		contentPane.add(lblTitle);

		// =================== LABEL USERNAME ===================
		JLabel lblUsername = new JLabel("Username:");
		lblUsername.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblUsername.setBounds(120, 110, 90, 25);
		contentPane.add(lblUsername);

		// =================== TEXTFIELD USERNAME ===================
		textFieldUsername = new JTextField();
		textFieldUsername.setBounds(210, 110, 160, 25);
		contentPane.add(textFieldUsername);
		textFieldUsername.setColumns(10);

		// =================== LABEL PASSWORD ===================
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblPassword.setBounds(120, 150, 90, 25);
		contentPane.add(lblPassword);

		// =================== PASSWORD FIELD ===================
		passwordField = new JPasswordField();
		passwordField.setBounds(210, 150, 160, 25);
		contentPane.add(passwordField);

		// =================== BUTTON LOGIN ===================
		JButton btnLogin = new JButton("Accedi");
		btnLogin.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnLogin.setBackground(new Color(100, 149, 237));
		btnLogin.setForeground(Color.WHITE);
		btnLogin.setFocusPainted(true);
		btnLogin.setBounds(195, 200, 100, 30);
		contentPane.add(btnLogin);

		// =================== MESSAGGIO ===================
		lblMessage = new JLabel("");
		lblMessage.setHorizontalAlignment(SwingConstants.CENTER);
		lblMessage.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblMessage.setForeground(Color.RED);
		lblMessage.setBounds(50, 250, 380, 25);
		contentPane.add(lblMessage);

		// =================== EVENTO BOTTONE ===================

		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String usernameOrEmail = textFieldUsername.getText();
				String password = new String(passwordField.getPassword());
				try {
                    // Usa direttamente il metodo login del Controller (ritorna UtenteDTO) ma
                    // qui ignoriamo l'oggetto perché ci serve solo verificare le credenziali.
                    controller.login(usernameOrEmail, password);
                    lblMessage.setForeground(new Color(0, 128, 0));
                    lblMessage.setText("Accesso effettuato.");
                    HomeFrame home = new HomeFrame();
                    home.setVisible(true);
                    dispose();
				} catch (ApplicationException ex) {
					lblMessage.setForeground(Color.RED);
					String msg = ex.getMessage();
					// if there's an underlying cause (e.g., SQLException), append its message for clarity
					if (ex.getCause() != null && ex.getCause().getMessage() != null) {
						msg += ": " + ex.getCause().getMessage();
					}
					lblMessage.setText(msg);
					ex.printStackTrace();
				} catch (Exception ex) {
					lblMessage.setForeground(Color.RED);
					lblMessage.setText("Errore inatteso: " + ex.getMessage());
					ex.printStackTrace();
                }
			}
		});
	}
}
