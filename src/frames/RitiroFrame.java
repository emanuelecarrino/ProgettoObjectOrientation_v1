package frames;

import dto.Controller;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

// Frame complementare a "Consegna": lato acquirente, visualizza le consegne programmate dal venditore
// per le offerte dell'utente in stato ACCETTATA

public class RitiroFrame extends JFrame {

	private final Controller controller;
	private final String matricola;
	private final JPanel contentPanel;

	private JPanel listaPanel;
	private DefaultListModel<String> listModel;
	private JList<String> ritiriList;

	public RitiroFrame(Controller controller, String matricola) {
		this.controller = controller;
		this.matricola = matricola;
		setTitle("UninaSwap - Ritiro");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(900, 540);
		setLocationRelativeTo(null);
		this.contentPanel = buildContentPanel();
		setContentPane(contentPanel);
	}

	public JPanel getContentPanel() { return contentPanel; }

	private JPanel buildContentPanel() {
		JPanel root = new JPanel(new BorderLayout());
		root.setBackground(Color.WHITE);

		JPanel header = new JPanel(new BorderLayout());
		header.setBorder(new EmptyBorder(18,18,12,18));
		header.setBackground(Color.WHITE);
		JLabel title = new JLabel("Ritiri programmati", SwingConstants.LEFT);
		title.setFont(new Font("Segoe UI", Font.BOLD, 22));
		header.add(title, BorderLayout.WEST);

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		actions.setOpaque(false);
	JButton ricevutaBtn = createPrimaryButton("Ricevuta");
	ricevutaBtn.addActionListener(e -> onRicevuta());
		JButton refresh = createPrimaryButton("Aggiorna");
		refresh.addActionListener(e -> refreshContent());
		actions.add(ricevutaBtn);
		actions.add(refresh);
		header.add(actions, BorderLayout.EAST);
		root.add(header, BorderLayout.NORTH);

		listaPanel = new JPanel(new BorderLayout());
		listaPanel.setBackground(Color.WHITE);
		listaPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(0,18,18,18),
				BorderFactory.createLineBorder(new Color(235,235,238))
		));

		listModel = new DefaultListModel<>();
		ritiriList = new JList<>(listModel);
		ritiriList.setFont(new Font("Tahoma", Font.PLAIN, 13));
		ritiriList.setFixedCellHeight(-1);
		ritiriList.setCellRenderer(new CardRenderer());
		ritiriList.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) {
					int idx = ritiriList.locationToIndex(e.getPoint());
					if (idx>=0) {
						Rectangle r = ritiriList.getCellBounds(idx, idx);
						if (r!=null && r.contains(e.getPoint())) {
							String row = listModel.get(idx);
							String idAnn = estraiIdAnnuncio(row);
							if (idAnn != null) mostraDettaglioConsegna(idAnn);
						}
					}
				}
			}
		});
		ritiriList.addListSelectionListener(ev -> {
			if (!ev.getValueIsAdjusting()) {
				int idx = ritiriList.getSelectedIndex();
				boolean disable = false;
				if (idx >= 0) {
					String row = listModel.get(idx);
					if (row != null && row.contains("Ritirata")) disable = true;
				}
				ricevutaBtn.setEnabled(!disable);
			}
		});
		listaPanel.add(new JScrollPane(ritiriList), BorderLayout.CENTER);
		root.add(listaPanel, BorderLayout.CENTER);

		refreshContent();
		return root;
	}

	private JButton createPrimaryButton(String text) {
		JButton b = new JButton(text);
		b.setBackground(new Color(100, 149, 237));
		b.setForeground(Color.WHITE);
		b.setFocusPainted(false);
		b.setFont(new Font("Segoe UI", Font.BOLD, 13));
		b.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
		b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return b;
	}

	private void onRicevuta() {
		int idx = ritiriList.getSelectedIndex();
		if (idx < 0) {
			JOptionPane.showMessageDialog(this, "Seleziona un ritiro dalla lista", "Attenzione", JOptionPane.WARNING_MESSAGE);
			return;
		}
		String row = listModel.get(idx);
		String idAnn = estraiIdAnnuncio(row);
		if (idAnn == null || idAnn.isBlank()) {
			JOptionPane.showMessageDialog(this, "Elemento non valido", "Errore", JOptionPane.ERROR_MESSAGE);
			return;
		}
		int confirm = JOptionPane.showConfirmDialog(this,
				"Confermi che la consegna è avvenuta?",
				"Conferma ricezione",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (confirm != JOptionPane.YES_OPTION) return;
		try {
			String[] a = controller.recuperaAnnuncioFields(idAnn);
			String titolo = a[0];
			String descr = a[1];
			String categoria = a[2];
			String prezzoStr = a[5];
			controller.aggiornaAnnuncio(idAnn, titolo, descr, categoria, "Chiuso", prezzoStr);
			refreshContent();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
		}
	}

	private String estraiIdAnnuncio(String row) {
		if (row == null) return null;
		String trimmed = row.trim();
		int space = trimmed.indexOf(' ');
		return space > 0 ? trimmed.substring(0, space) : trimmed;
	}

	public void refreshContent() {
		try {
			List<String> items = controller.ritiriProgrammatiPerUtente(matricola);
			listModel.clear();
			for (String s : items) listModel.addElement(s);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Impossibile caricare i ritiri: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void mostraDettaglioConsegna(String idAnnuncio) {
		try {
			String[] f = controller.recuperaConsegnaFieldsPerAnnuncio(idAnnuncio);
			if (f == null || f.length < 6) {
				JOptionPane.showMessageDialog(this, "Dati consegna incompleti", "Dettaglio", JOptionPane.WARNING_MESSAGE);
				return;
			}
			String titolo = f[0];
			String sede = f[1];
			String fascia = f[2];
			String data = f[3];
			String note = f[4];
			String idConsegna = f[5];

			JPanel p = new JPanel(new BorderLayout(10,12));
			p.setBackground(Color.WHITE);
			p.setBorder(new EmptyBorder(16,18,16,18));
			JLabel h = new JLabel("Consegna " + (idConsegna==null?"":idConsegna) + " • Annuncio " + idAnnuncio);
			h.setFont(new Font("Segoe UI", Font.BOLD, 16));
			p.add(h, BorderLayout.NORTH);

			JPanel grid = new JPanel(new GridLayout(0,2,12,8));
			grid.setOpaque(false);
			if (titolo != null && !titolo.isBlank()) { grid.add(new JLabel("Titolo annuncio:")); grid.add(new JLabel(titolo)); }
			if (sede != null && !sede.isBlank()) { grid.add(new JLabel("Sede:")); grid.add(new JLabel(sede)); }
			if (fascia != null && !fascia.isBlank()) { grid.add(new JLabel("Fascia oraria:")); grid.add(new JLabel(fascia)); }
			if (data != null && !data.isBlank()) { grid.add(new JLabel("Data:")); grid.add(new JLabel(data)); }
			if (note != null && !note.isBlank()) { grid.add(new JLabel("Note:")); grid.add(new JLabel(note)); }
			p.add(grid, BorderLayout.CENTER);

			JOptionPane.showMessageDialog(this, p, "Dettaglio consegna", JOptionPane.PLAIN_MESSAGE);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Impossibile mostrare il dettaglio: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
		}
	}

	// Renderer stile card
	private static class CardRenderer extends JPanel implements ListCellRenderer<String> {
		private final JLabel primary = new JLabel();
		CardRenderer() {
			setLayout(new BorderLayout());
			setOpaque(true);
			setBorder(new EmptyBorder(10,12,10,12));
			primary.setFont(new Font("Tahoma", Font.BOLD, 13));
			add(primary, BorderLayout.CENTER);
		}
		@Override
		public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
			primary.setText(value==null?"":value);
			setBackground(isSelected ? new Color(218,230,247) : Color.WHITE);
			return this;
		}
	}
}
