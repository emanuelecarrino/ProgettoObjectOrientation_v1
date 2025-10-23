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
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screen.width - 500) / 2;
		int y = (screen.height - 350) / 2;
		setBounds(x, y, 500, 350);
		int frameWidth = 500;
		int centerX = frameWidth / 2;
		setVisible(true);
		setResizable(false); // blocca il ridimensionamento
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		setContentPane(contentPane);
		contentPane.setLayout(null);

	// Sezione: Titolo
		JLabel lblTitle = new JLabel("LOGIN");
		lblTitle.setFont(new Font("Tahoma", Font.BOLD, 24));
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle.setBounds(centerX - 150, 30, 300, 30);
		contentPane.add(lblTitle);

	// Sezione: Username
		JLabel lblUsername = new JLabel("Username o email:");
		lblUsername.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblUsername.setBounds(67, 110, 130, 25);
		contentPane.add(lblUsername);

	// Campo input username/email
		textFieldUsername = new JTextField();
		textFieldUsername.setBounds(210, 110, 160, 25);
		contentPane.add(textFieldUsername);
		textFieldUsername.setColumns(10);

	// Sezione: Password
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblPassword.setBounds(120, 150, 90, 25);
		contentPane.add(lblPassword);

		// Campo input password
		passwordField = new JPasswordField();
		passwordField.setBounds(210, 150, 160, 25);
		contentPane.add(passwordField);
		// Invio su username passa al campo password
		textFieldUsername.addActionListener(e -> passwordField.requestFocusInWindow());

		// Invio su password esegue il login
		passwordField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					performLogin();
				}
			}
		});

		// Sezione: Link registrazione
		JLabel lblForgot = new JLabel("<html><a href=''>Nuovo account? Registrati</a></html>");
		lblForgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lblForgot.setBounds(centerX - 39, 178, 170, 20);
		lblForgot.setToolTipText("Crea un nuovo account");
		lblForgot.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				new RegistrationDialog(LoginFrame.this, controller).setVisible(true);
			}
		});
		contentPane.add(lblForgot);

		// Sezione: Pulsante login
		JButton btnLogin = new JButton("Accedi");
		btnLogin.setFont(new Font("Tahoma", Font.BOLD, 13));
		btnLogin.setBackground(new Color(100, 149, 237));
		btnLogin.setForeground(Color.WHITE);
		btnLogin.setFocusPainted(true);
		btnLogin.setBounds(centerX - 60, 220, 120, 30);
		contentPane.add(btnLogin);

		// Sezione: Messaggio esito
		lblMessage = new JLabel("");
		lblMessage.setHorizontalAlignment(SwingConstants.CENTER);
		lblMessage.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblMessage.setForeground(Color.RED);
		lblMessage.setBounds(50, 250, 380, 25);
		contentPane.add(lblMessage);

		// Azione: click su Accedi
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				performLogin();
			}
		});
	}

	// Esegue il login e apre la Home
	private void performLogin() {
		String usernameOrEmail = textFieldUsername.getText();
		String password = new String(passwordField.getPassword());
		try {
            controller.login(usernameOrEmail, password);
            String matricola = controller.recuperaMatricolaDaUsernameOEmail(usernameOrEmail.trim());
            if (matricola == null) {
                throw new ApplicationException("Utente non trovato dopo login");
            }
            lblMessage.setForeground(new Color(0, 128, 0));
            lblMessage.setText("Accesso effettuato.");
            HomeFrame home = new HomeFrame(controller, matricola);
            home.setVisible(true);
            dispose();
		} catch (ApplicationException ex) {
			lblMessage.setForeground(Color.RED);
			lblMessage.setText(ex.getMessage());
		} catch (Exception ex) {
			lblMessage.setForeground(Color.RED);
			lblMessage.setText("Errore inatteso: " + ex.getMessage());
        }
	}

	// Dialog di registrazione
	private static class RegistrationDialog extends JDialog {
		private final Controller controller;
		private JTextField tfNome, tfCognome, tfEmail, tfMatricola, tfUsername;
		private JComboBox<String> cbDay, cbMonth, cbYear;
		private JPasswordField pfPassword;
		private JComboBox<String> cbGenere;

		RegistrationDialog(Frame owner, Controller controller) {
			super(owner, "Registrazione", true);
			this.controller = controller;
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			setSize(480, 500);
			setLocationRelativeTo(owner);

			JPanel panel = new JPanel(new GridBagLayout());
			panel.setBorder(new EmptyBorder(12,12,12,12));
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(6,6,6,6); gbc.anchor = GridBagConstraints.WEST;

			java.util.function.BiConsumer<String, JComponent> addRow = (label, comp) -> {
				gbc.gridx = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
				panel.add(new JLabel(label+":"), gbc);
				gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
				panel.add(comp, gbc);
				gbc.gridy++;
			};

			tfNome = new JTextField();
			tfCognome = new JTextField();
			tfEmail = new JTextField();
			tfMatricola = new JTextField();
			tfUsername = new JTextField();
			pfPassword = new JPasswordField();
			cbDay = new JComboBox<>();
			cbMonth = new JComboBox<>();
			cbYear = new JComboBox<>();

			for (int m = 1; m <= 12; m++) {
				cbMonth.addItem(String.format("%02d", m));
			}

			int currentYear = java.time.LocalDate.now().getYear();
			for (int y = currentYear; y >= 1900; y--) {
				cbYear.addItem(Integer.toString(y));
			}

			for (int d = 1; d <= 31; d++) cbDay.addItem(String.format("%02d", d));

			ActionListener adjustDays = e -> adjustDaysCombo();
			cbMonth.addActionListener(adjustDays);
			cbYear.addActionListener(adjustDays);

			cbGenere = new JComboBox<>(new String[]{"M","F","Altro"});

			addRow.accept("Nome", tfNome);
			addRow.accept("Cognome", tfCognome);
			addRow.accept("Email", tfEmail);
			addRow.accept("Matricola", tfMatricola);
			addRow.accept("Username", tfUsername);
			addRow.accept("Password", pfPassword);
			JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
			datePanel.add(cbDay);
			datePanel.add(cbMonth);
			datePanel.add(cbYear);
			addRow.accept("Data di nascita (GG MM YYYY)", datePanel);
			addRow.accept("Genere", cbGenere);

			JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
			JButton annulla = new JButton("Annulla");
			JButton registra = new JButton("Registra");
			Font btnFont = new Font("Tahoma", Font.PLAIN, 13);
			annulla.setFont(btnFont);
			registra.setFont(btnFont);
			annulla.setPreferredSize(new Dimension(100, 28));
			registra.setPreferredSize(new Dimension(100, 28));
			annulla.addActionListener(e -> dispose());
			registra.addActionListener(e -> onRegister());
			buttons.add(annulla);
			buttons.add(registra);

			gbc.gridx = 0; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1; gbc.insets = new Insets(12,6,6,6);
			panel.add(buttons, gbc);

			setContentPane(panel);
		}

		private void onRegister() {
			String nome = tfNome.getText();
			String cognome = tfCognome.getText();
			String email = tfEmail.getText();
			String matricola = tfMatricola.getText();
			String username = tfUsername.getText();
			String password = new String(pfPassword.getPassword());
			String day = (String) cbDay.getSelectedItem();
			String month = (String) cbMonth.getSelectedItem();
			String year = (String) cbYear.getSelectedItem();
			String dataNascita = null;
			if (day != null && month != null && year != null) {
				dataNascita = String.format("%s-%s-%s", year, month, day);
			}
			String genere = (String) cbGenere.getSelectedItem();
			try {
				controller.registraNuovoUtente(nome, cognome, email, matricola, username, password, dataNascita, genere);
				JOptionPane.showMessageDialog(this, "Registrazione completata. Ora puoi accedere.", "Successo", JOptionPane.INFORMATION_MESSAGE);
				dispose();
			} catch (exception.ApplicationException ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
			}
		}

		// Helper to adjust the days dropdown according to selected month/year
		private void adjustDaysCombo() {
			if (cbDay == null || cbMonth == null || cbYear == null) return;
			String selMonth = (String) cbMonth.getSelectedItem();
			String selYear = (String) cbYear.getSelectedItem();
			if (selMonth == null || selYear == null) return;
			int month = Integer.parseInt(selMonth);
			int year = Integer.parseInt(selYear);
			int maxDays = java.time.Month.of(month).length(java.time.Year.isLeap(year));
			int currentCount = cbDay.getItemCount();
			if (currentCount < maxDays) {
				for (int d = currentCount + 1; d <= maxDays; d++) cbDay.addItem(String.format("%02d", d));
			} else if (currentCount > maxDays) {
				for (int d = currentCount; d > maxDays; d--) cbDay.removeItemAt(d-1);
			}
			// ensure selected day is valid
			if (cbDay.getItemCount() >= 1) {
				int selIndex = cbDay.getSelectedIndex();
				if (selIndex == -1) cbDay.setSelectedIndex(0);
				else if (selIndex >= cbDay.getItemCount()) cbDay.setSelectedIndex(cbDay.getItemCount()-1);
			}
		}
	}
}
