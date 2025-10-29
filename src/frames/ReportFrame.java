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

    // Palette colori coerente tra testo, legenda e grafici
    private static final Color COL_ACCETTATE = new Color(76, 175, 80);     // verde
    private static final Color COL_RIFIUTATE = new Color(244, 67, 54);     // rosso
    private static final Color COL_ATTESA    = new Color(255, 152, 0);     // arancione

    private static final Color COL_VENDITA   = new Color(33, 150, 243);    // blu
    private static final Color COL_SCAMBIO   = new Color(156, 39, 176);    // viola
    private static final Color COL_REGALO    = new Color(0, 150, 136);     // verde acqua
    private static final Color COL_COMPLETI  = new Color(96, 125, 139);    // grigio

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
        JPanel root = new JPanel(new BorderLayout(12,12));
        root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JLabel title = new JLabel("Report sintetico sulle tue offerte");
        title.setFont(root.getFont().deriveFont(Font.BOLD, 20f));
        title.setBorder(BorderFactory.createEmptyBorder(4,4,8,4));
        root.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10,10));
        center.setBorder(new RoundedPanelBorder());

        summaryLabel = new JLabel("Caricamento...");
        summaryLabel.setFont(root.getFont().deriveFont(Font.PLAIN, 13f));
        summaryLabel.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));
        center.add(summaryLabel, BorderLayout.NORTH);

        chartsPanel = new JPanel(new GridLayout(1,2,10,10));
        chartsPanel.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
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
            + "<b>Totali offerte per tipologia</b><br/>"
            + bullet(COL_VENDITA) + colorize(" Vendita: ", COL_VENDITA) + totVendita
            + " &nbsp; " + bullet(COL_SCAMBIO) + colorize(" Scambio: ", COL_SCAMBIO) + totScambio
            + " &nbsp; " + bullet(COL_REGALO) + colorize(" Regalo: ", COL_REGALO) + totRegalo + "<br/>"
            + "<b>Offerte accettate</b><br/>"
            + colorize(" Vendita: ", COL_VENDITA) + accVendita
            + " &nbsp; " + colorize(" Scambio: ", COL_SCAMBIO) + accScambio
            + " &nbsp; " + colorize(" Regalo: ", COL_REGALO) + accRegalo + "<br/>"
            + "<b>Statistiche prezzi Vendite accettate</b> "
            + colorize("min ", COL_COMPLETI) + formatEuro(minVend) +
            colorize("  · medio ", COL_COMPLETI) + formatEuro(avgVend) +
            colorize("  · max ", COL_COMPLETI) + formatEuro(maxVend)
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
            stylizePieChart(jfreeChartCls, pieChart);
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
            stylizeBarChart(jfreeChartCls, barChart);
            Component barPanel = (Component) chartPanelCls.getConstructor(jfreeChartCls).newInstance(barChart);

            return new Component[]{ piePanel, barPanel };
        } catch (Throwable t) {
            JPanel left = placeholderPanel("Totali per tipologia");
            JPanel right = placeholderPanel("Accettate per tipologia");
            return new Component[]{ left, right };
        }
    }

    // Helpers UI e stile

    private JPanel placeholderPanel(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(16,16,16,16)));
        JLabel l = new JLabel("JFreeChart non presente", SwingConstants.CENTER);
        l.setForeground(new Color(120,120,120));
        p.add(l, BorderLayout.CENTER);
        return p;
    }

    private void stylizePieChart(Class<?> jfreeChartCls, Object jfreeChart) {
        try {
            jfreeChartCls.getMethod("setAntiAlias", boolean.class).invoke(jfreeChart, true);
            Object plot = jfreeChartCls.getMethod("getPlot").invoke(jfreeChart);
            Class<?> plotCls = plot.getClass();

            try {
                plotCls.getMethod("setSectionPaint", Comparable.class, Paint.class)
                        .invoke(plot, "Vendita", COL_VENDITA);
                plotCls.getMethod("setSectionPaint", Comparable.class, Paint.class)
                        .invoke(plot, "Scambio", COL_SCAMBIO);
                plotCls.getMethod("setSectionPaint", Comparable.class, Paint.class)
                        .invoke(plot, "Regalo", COL_REGALO);
            } catch (NoSuchMethodException ignored) { }
            try { plotCls.getMethod("setBackgroundPaint", Paint.class).invoke(plot, Color.WHITE); } catch (Exception ignored) {}
        } catch (Exception ignored) {}
    }

    private void stylizeBarChart(Class<?> jfreeChartCls, Object jfreeChart) {
        try {
            jfreeChartCls.getMethod("setAntiAlias", boolean.class).invoke(jfreeChart, true);
            Object plot = jfreeChartCls.getMethod("getPlot").invoke(jfreeChart);
            Class<?> plotCls = plot.getClass();
            try {
                plotCls.getMethod("setBackgroundPaint", Paint.class).invoke(plot, Color.WHITE);
                plotCls.getMethod("setRangeGridlinePaint", Paint.class).invoke(plot, new Color(230,230,230));
                plotCls.getMethod("setDomainGridlinesVisible", boolean.class).invoke(plot, false);
            } catch (Exception ignored) {}
            try {
                Object renderer = plotCls.getMethod("getRenderer").invoke(plot);
                Class<?> rendererCls = renderer.getClass();
                rendererCls.getMethod("setSeriesPaint", int.class, Paint.class).invoke(renderer, 0, COL_ACCETTATE);
            } catch (Exception ignored) {}
        } catch (Exception ignored) {}
    }

    private String colorize(String text, Color color) {
        return "<span style='color:" + toHex(color) + "'>" + text + "</span>";
    }

    private String bullet(Color color) {
        return "<span style='color:" + toHex(color) + "'>■</span>";
    }


    private String toHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    private static class RoundedPanelBorder extends javax.swing.border.AbstractBorder {
        private final int arc = 12;
        private final Color line = new Color(0,0,0,30);
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(line);
            g2.drawRoundRect(x, y, width-1, height-1, arc, arc);
            g2.dispose();
        }
    }
}