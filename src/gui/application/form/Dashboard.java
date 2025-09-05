/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package gui.application.form;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.MatteBorder;
import model.ActivityLogger;

import model.MySQL;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.RectangleInsets;
import gui.application.Application;


/**
 *
 * @author Oshen Sathsara <oshensathsara2003@gmail.com>
 */
public class Dashboard extends javax.swing.JPanel {

    // Blue theme colors for printing shop
    private final Color PRIMARY_COLOR = new Color(33, 150, 243); // Material Blue
    private final Color SUCCESS_COLOR = new Color(25, 118, 210); // Blue 600
    private final Color WARNING_COLOR = new Color(255, 152, 0); // Orange for warnings
    private final Color DANGER_COLOR = new Color(244, 67, 54); // Red for errors
    private final Color TEXT_COLOR = new Color(33, 37, 41);
    private final Color LIGHT_TEXT_COLOR = new Color(108, 117, 125);
    private final Color BORDER_COLOR = new Color(222, 226, 230);
    private final Color INFO_COLOR = new Color(3, 169, 244); // Light Blue

    /**
     * Creates new form Dashboard
     */
    public Dashboard() {
        initComponents();
        startClock();
        loadData();
        setupCharts();
        setupRecentActivities();
//        setupQuickActions();
        customizeScrollPane();

    }

    public void startClock() {
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                lblTime.setText("Time: " + (timeFormat.format(new Date())));

                lblDate1.setText("Today: " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
        });

        timer.start();
    }

    private String dailyRevenue;
    private String dailyRevenuePerc;
    private String newCustomers;
    private String newCustomersPerc;
    private String activeJobs;
    private String activeJobsPerc;
    private String completedJobs;
    private String completedJobsPerc;

private void loadData() {
    try {
        ResultSet rs = MySQL.executeSearch("SELECT "
                + "today_revenue.total AS today_revenue, "
                + "ROUND(((today_revenue.total - yesterday_revenue.total) / NULLIF(yesterday_revenue.total, 0)) * 100, 2) AS revenue_percentage, "
                + "today_customers.total AS today_customers, "
                + "ROUND(((today_customers.total - yesterday_customers.total) / NULLIF(yesterday_customers.total, 0)) * 100, 2) AS customers_percentage, "
                + "active_jobs.total AS active_jobs, "
                + "ROUND(((active_jobs.total - yesterday_active.total) / NULLIF(yesterday_active.total, 0)) * 100, 2) AS active_jobs_percentage, "
                + "completed_jobs.total AS completed_jobs, "
                + "ROUND(((completed_jobs.total - yesterday_completed.total) / NULLIF(yesterday_completed.total, 0)) * 100, 2) AS completed_percentage "
                + "FROM "
                + "(SELECT IFNULL(SUM(total_amount), 0) AS total FROM jobs WHERE order_date = CURDATE()) AS today_revenue, "
                + "(SELECT IFNULL(SUM(total_amount), 0) AS total FROM jobs WHERE order_date = CURDATE() - INTERVAL 1 DAY) AS yesterday_revenue, "
                + "(SELECT COUNT(*) AS total FROM customers WHERE DATE(created_at) = CURDATE()) AS today_customers, "
                + "(SELECT COUNT(*) AS total FROM customers WHERE DATE(created_at) = CURDATE() - INTERVAL 1 DAY) AS yesterday_customers, "
                + "(SELECT COUNT(*) AS total FROM jobs WHERE status_id IN (SELECT status_id FROM job_status_types WHERE status_code IN ('PENDING', 'IN_PROGRESS'))) AS active_jobs, "
                + "(SELECT COUNT(*) AS total FROM jobs WHERE status_id IN (SELECT status_id FROM job_status_types WHERE status_code IN ('PENDING', 'IN_PROGRESS')) AND order_date = CURDATE() - INTERVAL 1 DAY) AS yesterday_active, "
                + "(SELECT COUNT(*) AS total FROM jobs WHERE status_id = (SELECT status_id FROM job_status_types WHERE status_code = 'COMPLETED') AND completion_date = CURDATE()) AS completed_jobs, "
                + "(SELECT COUNT(*) AS total FROM jobs WHERE status_id = (SELECT status_id FROM job_status_types WHERE status_code = 'COMPLETED') AND completion_date = CURDATE() - INTERVAL 1 DAY) AS yesterday_completed");

        if (rs.next()) {
            this.dailyRevenue = rs.getString("today_revenue");
            this.dailyRevenuePerc = rs.getString("revenue_percentage");
            this.newCustomers = rs.getString("today_customers");
            this.newCustomersPerc = rs.getString("customers_percentage");
            this.activeJobs = rs.getString("active_jobs");
            this.activeJobsPerc = rs.getString("active_jobs_percentage");
            this.completedJobs = rs.getString("completed_jobs");
            this.completedJobsPerc = rs.getString("completed_percentage");
        }

        setupStatsCards();

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Something went wrong: " + e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
        e.printStackTrace();
    }
}

    private void setupStatsCards() {
        statspanel1.setLayout(new GridLayout(1, 4, 15, 0));
        statspanel1.add(createStatCard("Daily Revenue", "Rs. " + this.dailyRevenue, this.dailyRevenuePerc + "%", "/traitgen/icon/png/chart-line.png", PRIMARY_COLOR));
        statspanel1.add(createStatCard("New Customers", this.newCustomers, this.newCustomersPerc + "%", "/traitgen/icon/png/users.png", SUCCESS_COLOR));
        statspanel1.add(createStatCard("Active Jobs", this.activeJobs, this.activeJobsPerc + "%", "/traitgen/icon/png/shopping-cart.png", WARNING_COLOR));
        statspanel1.add(createStatCard("Completed Jobs", this.completedJobs, this.completedJobsPerc + "%", "/traitgen/icon/png//tools.png", new Color(13, 71, 161))); // Dark Blue
    }

    private JPanel createStatCard(String title, String value, String growth, String iconPath, Color color) {
        JPanel card = new JPanel(new BorderLayout(10, 0));

        card.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 5, 0, 0, color),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
            JLabel iconLabel = new JLabel(icon);
            card.add(iconLabel, BorderLayout.WEST);
        } catch (Exception e) {
            card.add(new JLabel(" "), BorderLayout.WEST);
        }

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(LIGHT_TEXT_COLOR);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JLabel growthLabel = new JLabel(growth);
        growthLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        growthLabel.setForeground(growth.startsWith("+") ? SUCCESS_COLOR : DANGER_COLOR);

        content.add(titleLabel);
        content.add(valueLabel);
        content.add(growthLabel);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private void setupCharts() {
        salesChartPanel1.setLayout(new BorderLayout());
        inventoryChartPanel1.setLayout(new BorderLayout());
        salesChartPanel1.add(createCustomersChart());
        inventoryChartPanel1.add(createOngoingJobsChart());
    }
    LocalDate currentDate = LocalDate.now();

    private ChartPanel createCustomersChart() {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        LocalDate currentDate = LocalDate.now();

        try {
            // Get customer data for the last 7 days
            for (int i = 6; i >= 0; i--) {
                LocalDate date = currentDate.minusDays(i);
                String dayName = date.format(DateTimeFormatter.ofPattern("EEE"));
                
                String query = "SELECT COUNT(*) as customer_count FROM customers WHERE DATE(created_at) = '" + date + "'";
                ResultSet rs = MySQL.executeSearch(query);
                
                int customerCount = 0;
                if (rs.next()) {
                    customerCount = rs.getInt("customer_count");
                }
                
                dataset.addValue(customerCount, "Customers", dayName);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Sample data
            dataset.addValue(3, "Customers", "Mon");
            dataset.addValue(5, "Customers", "Tue");
            dataset.addValue(2, "Customers", "Wed");
            dataset.addValue(7, "Customers", "Thu");
            dataset.addValue(4, "Customers", "Fri");
            dataset.addValue(6, "Customers", "Sat");
            dataset.addValue(1, "Customers", "Sun");
        }

        JFreeChart lineChart = ChartFactory.createLineChart("Weekly Customer Visits", "", "", dataset, PlotOrientation.VERTICAL, false, true, false);

        lineChart.setBackgroundPaint(null);
        lineChart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));
        lineChart.getTitle().setPaint(new Color(30, 50, 80)); // Dark blue text
        lineChart.getTitle().setBackgroundPaint(new Color(240, 248, 255, 240)); // Light blue background
        lineChart.getTitle().setPadding(new RectangleInsets(5, 170, 5, 170));

        String dateRange = currentDate.minusDays(6).format(DateTimeFormatter.ofPattern("MMM d"))
                + " - "
                + currentDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));

        TextTitle subtitle = new TextTitle(dateRange, new Font("Segoe UI", Font.ITALIC, 12));
        subtitle.setPaint(new Color(100, 150, 200, 200)); // Medium blue
        lineChart.addSubtitle(subtitle);

        CategoryPlot plot = (CategoryPlot) lineChart.getPlot();
        plot.setBackgroundPaint(null);
        plot.setOutlineVisible(false);
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(new Color(100, 150, 200, 200)); // Blue grid lines
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(new Color(100, 150, 200, 240)); // Blue grid lines

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        domainAxis.setLabelFont(new Font("Segoe UI", Font.BOLD, 12));
        domainAxis.setTickLabelPaint(new Color(60, 90, 120)); // Dark blue labels

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        rangeAxis.setLabelFont(new Font("Segoe UI", Font.BOLD, 12));
        rangeAxis.setNumberFormatOverride(new DecimalFormat("#"));
        rangeAxis.setTickLabelPaint(new Color(60, 90, 120)); // Dark blue labels

        rangeAxis.setAutoRange(true);
        rangeAxis.setAutoRangeIncludesZero(true);

        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(25, 118, 210)); // Blue 600
        renderer.setSeriesStroke(0, new BasicStroke(3.0f));
        renderer.setBaseShapesVisible(true);
        renderer.setSeriesShapesFilled(0, true);
        renderer.setSeriesShape(0, new Ellipse2D.Double(-4, -4, 8, 8));

        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setPreferredSize(new Dimension(400, 400));
        chartPanel.setOpaque(false);

        chartPanel.setMouseWheelEnabled(false);
        chartPanel.setDomainZoomable(false);
        chartPanel.setRangeZoomable(false);

        return chartPanel;
    }

private ChartPanel createOngoingJobsChart() {
    DefaultPieDataset dataset = new DefaultPieDataset();

    try {
        // Adjusted query to match the database schema
        String query = "SELECT sc.category_name, COUNT(j.job_id) as job_count "
                + "FROM jobs j "
                + "INNER JOIN service_categories sc ON j.service_category_id = sc.category_id "
                + "INNER JOIN job_status_types jst ON j.status_id = jst.status_id "
                + "WHERE jst.status_code IN ('PENDING', 'IN_PROGRESS') "
                + "GROUP BY sc.category_id, sc.category_name "
                + "ORDER BY job_count DESC";

        ResultSet rs = MySQL.executeSearch(query);
        boolean hasData = false;
        
        while (rs.next()) {
            hasData = true;
            String categoryName = rs.getString("category_name");
            int jobCount = rs.getInt("job_count");
            // Add data to the pie chart dataset
            dataset.setValue(categoryName, jobCount);
        }
        
        if (!hasData) {
            dataset.setValue("No Ongoing Jobs", 1);
        }

    } catch (Exception e) {
        dataset.setValue("No Data", 1);
        JOptionPane.showMessageDialog(this, "Something went wrong: " + e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
        e.printStackTrace();
    }

    JFreeChart pieChart = ChartFactory.createPieChart("Ongoing Jobs by Service Category", dataset, true, true, false);

    pieChart.setBackgroundPaint(null);
    pieChart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));
    pieChart.getTitle().setPaint(new Color(30, 50, 80)); // Dark blue text
    pieChart.getTitle().setBackgroundPaint(new Color(240, 248, 255, 200)); // Light blue background
    pieChart.getTitle().setPadding(new RectangleInsets(5, 135, 5, 135));

    PiePlot plot = (PiePlot) pieChart.getPlot();

    plot.setBackgroundPaint(null);
    plot.setOutlineVisible(false);

    // Blue color palette 
    Color[] colorPalette = {
        new Color(33, 150, 243),   // Material Blue
        new Color(25, 118, 210),   // Blue 600
        new Color(13, 71, 161),    // Blue 800
        new Color(3, 169, 244),    // Light Blue
        new Color(0, 188, 212),    // Cyan
        new Color(0, 150, 136),    // Teal
        new Color(63, 81, 181),    // Indigo
        new Color(103, 58, 183),   // Deep Purple
        new Color(156, 39, 176)    // Purple
    };

    int count = dataset.getItemCount();
    for (int i = 0; i < count; i++) {
        plot.setSectionPaint(dataset.getKey(i), colorPalette[i % colorPalette.length]);
    }

    plot.setShadowPaint(null);

    plot.setLabelBackgroundPaint(new Color(240, 248, 255, 120)); // Light blue background for labels
    plot.setLabelOutlinePaint(null);
    plot.setLabelShadowPaint(null);
    plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));

    plot.setInteriorGap(0.04);
    plot.setLabelGap(0.02);

    plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
            "{0}: {2}", // Format: Name: Percentage
            NumberFormat.getNumberInstance(),
            new DecimalFormat("0.0%"))); 

    LegendTitle legend = pieChart.getLegend();
    legend.setBorder(0, 0, 0, 0);
    legend.setBackgroundPaint(new Color(240, 248, 255, 200)); // Light blue background
    legend.setItemFont(new Font("Segoe UI", Font.PLAIN, 11));

    pieChart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));
    pieChart.getTitle().setPaint(new Color(30, 50, 80)); // Dark blue text

    TextTitle subtitle = new TextTitle(
            "Current Status",
            new Font("Segoe UI", Font.ITALIC, 12));
    subtitle.setPaint(new Color(100, 150, 200, 200)); // Medium blue
    pieChart.addSubtitle(subtitle);

    ChartPanel chartPanel = new ChartPanel(pieChart);
    chartPanel.setPreferredSize(new Dimension(400, 400));

    chartPanel.setOpaque(false);
    chartPanel.setBackground(new Color(0, 0, 0, 0));

    chartPanel.setMouseWheelEnabled(true);
    chartPanel.setZoomAroundAnchor(true);

    return chartPanel;
}

    private void setupRecentActivities() {

        activitiesList1.removeAll();

        activitiesList1.setLayout(new BoxLayout(activitiesList1, BoxLayout.Y_AXIS));

        try {
//            ResultSet rs = ActivityLogger.getRecentActivities(10);
//            boolean hasData = false;
//
//            while (rs != null && rs.next()) {
//                hasData = true;
//                String action = rs.getString("Action");
//                String description = rs.getString("Description");
//                java.sql.Timestamp timestamp = rs.getTimestamp("Timestamp");
//                String timeAgo = formatTimeAgo(timestamp);
//                Color color = getColorForAction(action);
//
//                addActivityItem(action, description, timeAgo, color);
//            }
//
//            if (!hasData) {
//                addNoDataMessage();
//            }

        } catch (Exception e) {
            addErrorMessage();
                            JOptionPane.showMessageDialog(this, "Something went wrong", "Warning", JOptionPane.WARNING_MESSAGE);

        }

        activitiesList1.revalidate();
        activitiesList1.repaint();

        // Reset scroll position to top
        SwingUtilities.invokeLater(() -> {
            if (jScrollPane2.getVerticalScrollBar() != null) {
                jScrollPane2.getVerticalScrollBar().setValue(0);
            }
        });
    }

    private String formatTimeAgo(java.sql.Timestamp timestamp) {
        if (timestamp == null) {
            return "Unknown";
        }

        long currentTime = System.currentTimeMillis();
        long activityTime = timestamp.getTime();
        long diffInMs = currentTime - activityTime;

        long diffInMinutes = diffInMs / (1000 * 60);
        long diffInHours = diffInMs / (1000 * 60 * 60);
        long diffInDays = diffInMs / (1000 * 60 * 60 * 24);

        if (diffInMinutes < 1) {
            return "Just now";
        } else if (diffInMinutes < 60) {
            return diffInMinutes + (diffInMinutes == 1 ? " min ago" : " mins ago");
        } else if (diffInHours < 24) {
            return diffInHours + (diffInHours == 1 ? " hr ago" : " hrs ago");
        } else if (diffInDays < 7) {
            return diffInDays + (diffInDays == 1 ? " day ago" : " days ago");
        } else {
            // For older activities, show the actual date
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy");
            return sdf.format(timestamp);
        }
    }

    private Color getColorForAction(String action) {
        if (action == null) {
            return PRIMARY_COLOR;
        }

        // Map different actions to appropriate colors with blue theme
        switch (action.toLowerCase()) {
            case "new order":
            case "order created":
                return PRIMARY_COLOR;
            case "repair completed":
            case "inventory updated":
                return SUCCESS_COLOR;
            case "low stock alert":
            case "stock alert":
                return WARNING_COLOR;
            case "payment received":
                return PRIMARY_COLOR;
            case "user login":
                return INFO_COLOR;
            case "new customer":
            case "customer registered":
                return SUCCESS_COLOR;
            default:
                return PRIMARY_COLOR;
        }
    }

    private void addNoDataMessage() {
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setOpaque(false);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        messagePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel messageLabel = new JLabel("No recent activities found", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        messageLabel.setForeground(LIGHT_TEXT_COLOR);

        messagePanel.add(messageLabel, BorderLayout.CENTER);
        activitiesList1.add(messagePanel);
    }

    private void addErrorMessage() {
        JPanel errorPanelLocal = new JPanel(new BorderLayout());
        errorPanelLocal.setOpaque(false);
        errorPanelLocal.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        errorPanelLocal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel errorLabel = new JLabel("Error loading activities", SwingConstants.CENTER);
        errorLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        errorLabel.setForeground(DANGER_COLOR);

        errorPanelLocal.add(errorLabel, BorderLayout.CENTER);
        activitiesList1.add(errorPanelLocal);
    }

    private void addActivityItem(String title, String desc, String time, Color color) {
        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setOpaque(true);
        item.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // Set maximum size to prevent excessive height
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Color indicator
        JPanel indicator = new JPanel();
        indicator.setPreferredSize(new Dimension(4, 60));
        indicator.setBackground(color);
        indicator.setOpaque(true);

        // Content panel
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel("<html>" + desc + "</html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(LIGHT_TEXT_COLOR);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        timeLabel.setForeground(LIGHT_TEXT_COLOR);
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(titleLabel);
        content.add(Box.createVerticalStrut(2));
        content.add(descLabel);
        content.add(Box.createVerticalStrut(2));
        content.add(timeLabel);

        item.add(indicator, BorderLayout.WEST);
        item.add(content, BorderLayout.CENTER);

        activitiesList1.add(item);
        activitiesList1.add(Box.createVerticalStrut(5));
    }

//    private void setupQuickActions() {
//        quickActionsPanel1.setLayout(new GridLayout(5, 2, 4, 10));
//        quickActionsPanel1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//
//        addQuickAction("Employee Management", PRIMARY_COLOR, "",
//                () -> Application.showForm(new Inventory()));
//
//        addQuickAction("Add Inventory", SUCCESS_COLOR, "",
//                () -> Application.showForm(new HR()));
//
//        addQuickAction("Generate Report", WARNING_COLOR, "",
//                () -> Application.showForm(new Reports()));
//
//    }

    private void addQuickAction(String text, Color bgColor, String iconPath, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Add action listener
        btn.addActionListener(e -> action.run());

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.darker());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });

        try {
            btn.setIcon(new ImageIcon(getClass().getResource(iconPath)));
        } catch (Exception e) {
            System.out.println("Icon not found: " + iconPath);
        }

        quickActionsPanel1.add(btn);
    }

    private void customizeScrollPane() {
        // Configure scroll pane policies
        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Smooth scrolling configuration
        jScrollPane2.getVerticalScrollBar().setUnitIncrement(16);
        jScrollPane2.getVerticalScrollBar().setBlockIncrement(64);

        // Visual styling
//        jScrollPane2.setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createLineBorder(BORDER_COLOR, 1),
//                BorderFactory.createEmptyBorder(5, 5, 5, 5)
//        ));
        // Set proper size constraints
        jScrollPane2.setPreferredSize(new Dimension(400, 300));
        jScrollPane2.setMinimumSize(new Dimension(300, 200));

        // Configure viewport
        jScrollPane2.setOpaque(false);

        // Ensure the activitiesList1 can be scrolled
        activitiesList1.setAutoscrolls(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        statspanel = new javax.swing.JPanel();
        headerPanel = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        bottomPanel = new javax.swing.JPanel();
        activitiesPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        activitiesList = new javax.swing.JPanel();
        quickActionsPanel = new javax.swing.JPanel();
        lblDate = new javax.swing.JLabel();
        chartsPanel = new javax.swing.JPanel();
        salesChartPanel = new javax.swing.JPanel();
        inventoryChartPanel = new javax.swing.JPanel();
        maincontainer = new javax.swing.JPanel();
        headerPanel1 = new javax.swing.JPanel();
        statspanel1 = new javax.swing.JPanel();
        jSeparator2 = new javax.swing.JSeparator();
        chartsPanel1 = new javax.swing.JPanel();
        salesChartPanel1 = new javax.swing.JPanel();
        inventoryChartPanel1 = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        bottomPanel1 = new javax.swing.JPanel();
        activitiesPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        activitiesList1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        quickActionsPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        dateTimePanel = new javax.swing.JPanel();
        lblDate1 = new javax.swing.JLabel();
        lblTime = new javax.swing.JLabel();

        statspanel.setLayout(new java.awt.GridLayout(1, 4));

        headerPanel.setLayout(new java.awt.BorderLayout());

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTitle.setText("Dashboard");
        headerPanel.add(lblTitle, java.awt.BorderLayout.CENTER);

        bottomPanel.setLayout(new java.awt.GridLayout(1, 2));

        activitiesPanel.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(null);

        activitiesList.setLayout(new javax.swing.BoxLayout(activitiesList, javax.swing.BoxLayout.Y_AXIS));
        jScrollPane1.setViewportView(activitiesList);

        activitiesPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        bottomPanel.add(activitiesPanel);

        javax.swing.GroupLayout quickActionsPanelLayout = new javax.swing.GroupLayout(quickActionsPanel);
        quickActionsPanel.setLayout(quickActionsPanelLayout);
        quickActionsPanelLayout.setHorizontalGroup(
            quickActionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 539, Short.MAX_VALUE)
        );
        quickActionsPanelLayout.setVerticalGroup(
            quickActionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 188, Short.MAX_VALUE)
        );

        bottomPanel.add(quickActionsPanel);

        lblDate.setText("Today: YYY-MM-DD");

        chartsPanel.setLayout(new java.awt.GridLayout(1, 2));

        javax.swing.GroupLayout salesChartPanelLayout = new javax.swing.GroupLayout(salesChartPanel);
        salesChartPanel.setLayout(salesChartPanelLayout);
        salesChartPanelLayout.setHorizontalGroup(
            salesChartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 539, Short.MAX_VALUE)
        );
        salesChartPanelLayout.setVerticalGroup(
            salesChartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 265, Short.MAX_VALUE)
        );

        chartsPanel.add(salesChartPanel);

        javax.swing.GroupLayout inventoryChartPanelLayout = new javax.swing.GroupLayout(inventoryChartPanel);
        inventoryChartPanel.setLayout(inventoryChartPanelLayout);
        inventoryChartPanelLayout.setHorizontalGroup(
            inventoryChartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 539, Short.MAX_VALUE)
        );
        inventoryChartPanelLayout.setVerticalGroup(
            inventoryChartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 265, Short.MAX_VALUE)
        );

        chartsPanel.add(inventoryChartPanel);

        maincontainer.setLayout(new javax.swing.BoxLayout(maincontainer, javax.swing.BoxLayout.Y_AXIS));

        headerPanel1.setLayout(new java.awt.BorderLayout());
        maincontainer.add(headerPanel1);

        statspanel1.setLayout(new java.awt.GridLayout(1, 4));
        maincontainer.add(statspanel1);
        maincontainer.add(jSeparator2);

        chartsPanel1.setLayout(new java.awt.GridLayout(1, 2));

        javax.swing.GroupLayout salesChartPanel1Layout = new javax.swing.GroupLayout(salesChartPanel1);
        salesChartPanel1.setLayout(salesChartPanel1Layout);
        salesChartPanel1Layout.setHorizontalGroup(
            salesChartPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 624, Short.MAX_VALUE)
        );
        salesChartPanel1Layout.setVerticalGroup(
            salesChartPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 286, Short.MAX_VALUE)
        );

        chartsPanel1.add(salesChartPanel1);

        javax.swing.GroupLayout inventoryChartPanel1Layout = new javax.swing.GroupLayout(inventoryChartPanel1);
        inventoryChartPanel1.setLayout(inventoryChartPanel1Layout);
        inventoryChartPanel1Layout.setHorizontalGroup(
            inventoryChartPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 624, Short.MAX_VALUE)
        );
        inventoryChartPanel1Layout.setVerticalGroup(
            inventoryChartPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 286, Short.MAX_VALUE)
        );

        chartsPanel1.add(inventoryChartPanel1);

        maincontainer.add(chartsPanel1);

        jSeparator1.setMinimumSize(new java.awt.Dimension(0, 5));
        maincontainer.add(jSeparator1);

        bottomPanel1.setLayout(new java.awt.GridLayout(1, 0));

        activitiesPanel1.setLayout(new java.awt.BorderLayout());

        jScrollPane2.setBorder(null);
        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        activitiesList1.setLayout(new javax.swing.BoxLayout(activitiesList1, javax.swing.BoxLayout.Y_AXIS));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("Recent Activities");
        activitiesList1.add(jLabel1);

        jScrollPane2.setViewportView(activitiesList1);

        activitiesPanel1.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        activitiesList1.setLayout(new javax.swing.BoxLayout(activitiesList1, javax.swing.BoxLayout.Y_AXIS));
        activitiesList1.setAlignmentX(Component.LEFT_ALIGNMENT);

        bottomPanel1.add(activitiesPanel1);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Quick Actions");

        dateTimePanel.setLayout(new java.awt.GridLayout(0, 2));

        lblDate1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblDate1.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        lblDate1.setText("Today: YYY-MM-DD");
        dateTimePanel.add(lblDate1);

        lblTime.setFont(new java.awt.Font("Segoe UI Historic", 1, 18)); // NOI18N
        lblTime.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTime.setText("Time: HH-mm");
        dateTimePanel.add(lblTime);

        javax.swing.GroupLayout quickActionsPanel1Layout = new javax.swing.GroupLayout(quickActionsPanel1);
        quickActionsPanel1.setLayout(quickActionsPanel1Layout);
        quickActionsPanel1Layout.setHorizontalGroup(
            quickActionsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(quickActionsPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(quickActionsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dateTimePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 612, Short.MAX_VALUE)
                    .addGroup(quickActionsPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        quickActionsPanel1Layout.setVerticalGroup(
            quickActionsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(quickActionsPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dateTimePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(165, Short.MAX_VALUE))
        );

        bottomPanel1.add(quickActionsPanel1);

        maincontainer.add(bottomPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(maincontainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(maincontainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel activitiesList;
    private javax.swing.JPanel activitiesList1;
    private javax.swing.JPanel activitiesPanel;
    private javax.swing.JPanel activitiesPanel1;
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JPanel bottomPanel1;
    private javax.swing.JPanel chartsPanel;
    private javax.swing.JPanel chartsPanel1;
    private javax.swing.JPanel dateTimePanel;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JPanel headerPanel1;
    private javax.swing.JPanel inventoryChartPanel;
    private javax.swing.JPanel inventoryChartPanel1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblDate1;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel maincontainer;
    private javax.swing.JPanel quickActionsPanel;
    private javax.swing.JPanel quickActionsPanel1;
    private javax.swing.JPanel salesChartPanel;
    private javax.swing.JPanel salesChartPanel1;
    private javax.swing.JPanel statspanel;
    private javax.swing.JPanel statspanel1;
    // End of variables declaration//GEN-END:variables
}
