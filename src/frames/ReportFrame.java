package frames;

import dto.Controller;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ReportFrame extends JFrame {
    private final Controller controller;
    private final String matricolaOfferente;

    private JLabel summaryLabel;
    private JPanel chartsPanel;

    public ReportFrame(Controller controller, String matricolaOfferente) {
        this.controller = controller;
        this.matricolaOfferente = matricolaOfferente;
        setTitle("UninaSwap - Report Offerte");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(860, 600);
        setLocationRelativeTo(null);
        setContentPane(buildContent());
        SwingUtilities.invokeLater(this::loadData);
    }

    public JPanel getContentPanel() {
        java.awt.Container c = getContentPane();
        if (c instanceof JPanel p) return p;
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(c);
        return panel;
    }

    public void refreshContent() {
        loadData();
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(10,10));
        root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JLabel title = new JLabel("Report sintetico sulle tue offerte");
        title.setFont(new Font("Tahoma", Font.BOLD, 20));
        root.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8,8));
        summaryLabel = new JLabel("Caricamento...");
        summaryLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
        center.add(summaryLabel, BorderLayout.NORTH);

        chartsPanel = new JPanel(new GridLayout(1,2,8,8));
        center.add(chartsPanel, BorderLayout.CENTER);

        root.add(center, BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Aggiorna");
        refreshBtn.addActionListener(e -> loadData());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(refreshBtn);
        root.add(south, BorderLayout.SOUTH);
        return root;
    }

    private void loadData() {
        try {
            Map<String, Integer> totaliPerTipo = controller.reportTotaleOffertePerTipo(matricolaOfferente);
            Map<String, Integer> accettatePerTipo = controller.reportOfferteAccettatePerTipo(matricolaOfferente);
            float[] statsVendita = controller.reportStatPrezziOfferteAccettateVendita(matricolaOfferente);

            int totVendita = totaliPerTipo.getOrDefault("Vendita", 0);
            int totScambio = totaliPerTipo.getOrDefault("Scambio", 0);
            int totRegalo = totaliPerTipo.getOrDefault("Regalo", 0);

            int accVendita = accettatePerTipo.getOrDefault("Vendita", 0);
            int accScambio = accettatePerTipo.getOrDefault("Scambio", 0);
            int accRegalo = accettatePerTipo.getOrDefault("Regalo", 0);

            int countVenditeAcc = (int) statsVendita[0];
            float minVend = statsVendita[1];
            float avgVend = statsVendita[2];
            float maxVend = statsVendita[3];

            String html = "<html>"
                    + "<b>Totale offerte inviate</b> (per tipologia): "
                    + "Vendita=" + totVendita + ", Scambio=" + totScambio + ", Regalo=" + totRegalo + "<br/>"
                    + "<b>Offerte accettate</b> (per tipologia): "
                    + "Vendita=" + accVendita + ", Scambio=" + accScambio + ", Regalo=" + accRegalo + "<br/>"
                    + "<b>Vendita accettate</b> (" + countVenditeAcc + ") - Prezzo €: min=" + formatEuro(minVend)
                    + ", medio=" + formatEuro(avgVend) + ", max=" + formatEuro(maxVend)
                    + "</html>";
            summaryLabel.setText(html);

            chartsPanel.removeAll();
            Component[] charts = tryBuildCharts(totaliPerTipo, accettatePerTipo);
            for (Component c : charts) {
                if (c != null) chartsPanel.add(c);
            }
            chartsPanel.revalidate();
            chartsPanel.repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore report", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatEuro(float v) {
        return String.format("%.2f", v);
    }

    private Component[] tryBuildCharts(Map<String, Integer> totaliPerTipo, Map<String, Integer> accettatePerTipo) {
        try {
            Class<?> chartFactoryCls = Class.forName("org.jfree.chart.ChartFactory");
            Class<?> chartPanelCls = Class.forName("org.jfree.chart.ChartPanel");
            Class<?> jfreeChartCls = Class.forName("org.jfree.chart.JFreeChart");
            Class<?> pieDatasetCls = Class.forName("org.jfree.data.general.DefaultPieDataset");
            Class<?> categoryDatasetCls = Class.forName("org.jfree.data.category.DefaultCategoryDataset");

            Object pieDataset = pieDatasetCls.getDeclaredConstructor().newInstance();
            pieDatasetCls.getMethod("setValue", Comparable.class, Number.class)
                    .invoke(pieDataset, "Vendita", totaliPerTipo.getOrDefault("Vendita", 0));
            pieDatasetCls.getMethod("setValue", Comparable.class, Number.class)
                    .invoke(pieDataset, "Scambio", totaliPerTipo.getOrDefault("Scambio", 0));
            pieDatasetCls.getMethod("setValue", Comparable.class, Number.class)
                    .invoke(pieDataset, "Regalo", totaliPerTipo.getOrDefault("Regalo", 0));

            java.lang.reflect.Method createPieChart = chartFactoryCls.getMethod(
                    "createPieChart", String.class, Class.forName("org.jfree.data.general.PieDataset"), boolean.class, boolean.class, boolean.class);
            Object pieChart = createPieChart.invoke(null, "Totali per tipologia", pieDataset, true, true, false);
            Component piePanel = (Component) chartPanelCls.getConstructor(jfreeChartCls).newInstance(pieChart);

            Object catDataset = categoryDatasetCls.getDeclaredConstructor().newInstance();
            categoryDatasetCls.getMethod("addValue", Number.class, Comparable.class, Comparable.class)
                    .invoke(catDataset, accettatePerTipo.getOrDefault("Vendita", 0), "Accettate", "Vendita");
            categoryDatasetCls.getMethod("addValue", Number.class, Comparable.class, Comparable.class)
                    .invoke(catDataset, accettatePerTipo.getOrDefault("Scambio", 0), "Accettate", "Scambio");
            categoryDatasetCls.getMethod("addValue", Number.class, Comparable.class, Comparable.class)
                    .invoke(catDataset, accettatePerTipo.getOrDefault("Regalo", 0), "Accettate", "Regalo");

            java.lang.reflect.Method createBarChart = chartFactoryCls.getMethod(
                    "createBarChart", String.class, String.class, String.class,
                    Class.forName("org.jfree.data.category.CategoryDataset"));
            Object barChart = createBarChart.invoke(null, "Accettate per tipologia", "Tipologia", "Numero", catDataset);
            Component barPanel = (Component) chartPanelCls.getConstructor(jfreeChartCls).newInstance(barChart);

            return new Component[]{ piePanel, barPanel };
        } catch (Throwable t) {
            JPanel left = new JPanel(new BorderLayout());
            left.add(new JLabel("Grafico torta Totali (JFreeChart non presente)", SwingConstants.CENTER), BorderLayout.CENTER);
            JPanel right = new JPanel(new BorderLayout());
            right.add(new JLabel("Grafico barre Accettate (JFreeChart non presente)", SwingConstants.CENTER), BorderLayout.CENTER);
            return new Component[]{ left, right };
        }
    }
}
