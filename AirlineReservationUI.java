import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class AirlineReservationUI {
    private final AirlineService service = new AirlineService();
    private User currentUser;

    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel cardsPanel;
    private JLabel statusLabel;

    private JTextField customerLoginEmailField;
    private JPasswordField customerLoginPasswordField;
    private JTextField registerNameField;
    private JTextField registerEmailField;
    private JPasswordField registerPasswordField;
    private JTextField registerPhoneField;
    private JTextField adminEmailField;
    private JPasswordField adminPasswordField;
    private JTextField adminRegisterNameField;
    private JTextField adminRegisterEmailField;
    private JPasswordField adminRegisterPasswordField;
    private JTextField adminRegisterPhoneField;
    private JTextField adminRegisterCodeField;

    private DefaultTableModel customerFlightTableModel;
    private DefaultTableModel customerReservationTableModel;
    private JTextField searchOriginField;
    private JTextField searchDestinationField;
    private JTextField bookingFlightNumberField;
    private JTextField bookingSeatsField;
    private JSpinner passengerAdultCountSpinner;
    private JSpinner passengerChildCountSpinner;
    private JTextField passengerNameField;
    private JTextField passengerEmailField;
    private JTextField passengerPhoneField;
    private JTextField passengerPassportField;
    private JComboBox<String> passengerTypeCombo;
    private JComboBox<String> seatPreferenceCombo;
    private JComboBox<String> travelClassCombo;
    private JComboBox<String> paymentMethodCombo;
    private DefaultTableModel pendingPassengerTableModel;
    private JButton addPassengerButton;
    private JButton confirmBookingButton;
    private JLabel passengerCounterLabel;
    private final java.util.List<BookingPassenger> pendingPassengers = new java.util.ArrayList<>();
    private JTabbedPane customerTabbedPane;

    private DefaultTableModel adminFlightTableModel;
    private DefaultTableModel adminReservationTableModel;
    private DefaultTableModel adminCustomerTableModel;
    private JTextField adminFlightNumberField;
    private JTextField adminOriginField;
    private JTextField adminDestinationField;
    private JTextField adminDepartField;
    private JTextField adminArriveField;
    private JTextField adminSeatsField;
    private JTextField adminPriceField;
    private JTextField adminCustomerSearchField;
    private JLabel overviewFlightsLabel;
    private JLabel overviewCustomersLabel;
    private JLabel overviewReservationsLabel;
    private JLabel overviewSeatsLabel;
    private JLabel topFlightsLabel;
    private JLabel topCustomersLabel;
    private JLabel topReservationsLabel;
    private JLabel topSeatsLabel;
    private JButton adminFlightButton;
    private JButton adminReservationButton;
    private JButton adminCustomerButton;
    private JButton adminOverviewButton;
    private JButton activeAdminNavButton;

    private static final String CARD_WELCOME = "welcome";
    private static final String CARD_CUSTOMER_LOGIN = "customerLogin";
    private static final String CARD_REGISTER = "register";
    private static final String CARD_ADMIN_LOGIN = "adminLogin";
    private static final String CARD_CUSTOMER_DASHBOARD = "customerDashboard";
    private static final String CARD_ADMIN_DASHBOARD = "adminDashboard";
    private static final String CARD_ADMIN_REGISTER = "adminRegister";

    public void show() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
        }

        frame = new JFrame("Airline Reservation System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1040, 720);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(new Color(245, 249, 252));

        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.add(createWelcomePanel(), CARD_WELCOME);
        cardsPanel.add(createCustomerLoginPanel(), CARD_CUSTOMER_LOGIN);
        cardsPanel.add(createRegistrationPanel(), CARD_REGISTER);
        cardsPanel.add(createAdminLoginPanel(), CARD_ADMIN_LOGIN);
        cardsPanel.add(createAdminRegistrationPanel(), CARD_ADMIN_REGISTER);
        cardsPanel.add(createCustomerDashboardPanel(), CARD_CUSTOMER_DASHBOARD);
        cardsPanel.add(createAdminDashboardPanel(), CARD_ADMIN_DASHBOARD);

        frame.setLayout(new BorderLayout());
        frame.add(cardsPanel, BorderLayout.CENTER);
        frame.add(createStatusPanel(), BorderLayout.SOUTH);
        switchToCard(CARD_WELCOME);
        frame.setVisible(true);
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint bg = new GradientPaint(0, 0, new Color(12, 44, 98), 0, getHeight(), new Color(26, 92, 175));
                g2.setPaint(bg);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 24));
                g2.fillOval(getWidth() - 420, 40, 360, 180);
                g2.fillOval(getWidth() / 8, getHeight() - 240, 380, 200);
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillOval(getWidth() - 320, getHeight() / 2, 260, 260);
            }
        };
        panel.setOpaque(true);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel brandLabel = new JLabel("SkyNova");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        brandLabel.setForeground(new Color(236, 244, 255));

        JButton adminLoginLink = createTextLinkButton("Admin login");
        adminLoginLink.addActionListener(e -> switchToCard(CARD_ADMIN_LOGIN));

        JButton adminRegisterLink = createTextLinkButton("Admin register");
        adminRegisterLink.addActionListener(e -> switchToCard(CARD_ADMIN_REGISTER));

        JPanel topNav = new JPanel(new BorderLayout());
        topNav.setOpaque(false);
        topNav.add(brandLabel, BorderLayout.WEST);

        JPanel adminNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        adminNav.setOpaque(false);
        adminNav.add(adminLoginLink);
        JLabel separator = new JLabel("•");
        separator.setForeground(new Color(196, 213, 238));
        adminNav.add(separator);
        adminNav.add(adminRegisterLink);
        topNav.add(adminNav, BorderLayout.EAST);

        panel.add(topNav, BorderLayout.NORTH);

        JLabel heroTitle = new JLabel("<html>Flight booking<br>made effortless</html>");
        heroTitle.setFont(new Font("Segoe UI", Font.BOLD, 52));
        heroTitle.setForeground(Color.WHITE);

        JLabel heroSubtitle = new JLabel("<html><div width='440'>Manage passengers, seat selection, passport details and payment in one fast workflow.</div></html>");
        heroSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        heroSubtitle.setForeground(new Color(220, 235, 255));

        JPanel featurePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        featurePanel.setOpaque(false);
        featurePanel.add(createHeroChip("Passport ready"));
        featurePanel.add(createHeroChip("Seat selection"));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(heroTitle);
        textPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        textPanel.add(heroSubtitle);
        textPanel.add(Box.createRigidArea(new Dimension(0, 24)));
        textPanel.add(featurePanel);

        JPanel loginCard = new JPanel(new BorderLayout(0, 18));
        loginCard.setOpaque(true);
        loginCard.setBackground(new Color(255, 255, 255, 240));
        loginCard.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(217, 226, 238), 1), BorderFactory.createEmptyBorder(28, 28, 28, 28)));

        JLabel loginCardTitle = new JLabel("Sign in");
        loginCardTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        loginCardTitle.setForeground(new Color(16, 38, 68));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        JTextField welcomeEmailField = new JTextField(18);
        JPasswordField welcomePasswordField = new JPasswordField(18);
        addFormField(formPanel, "Email address", welcomeEmailField, 0);
        addFormField(formPanel, "Password", welcomePasswordField, 1);

        JButton loginButton = new JButton("Sign in");
        applyButtonStyle(loginButton, new Color(0, 122, 204));
        loginButton.setPreferredSize(new Dimension(280, 48));
        loginButton.addActionListener(e -> {
            try {
                String email = welcomeEmailField.getText().trim();
                String password = new String(welcomePasswordField.getPassword());
                currentUser = service.authenticate(email, password);
                if (!currentUser.isCustomer()) {
                    throw new IllegalArgumentException("Please login with a customer account.");
                }
                refreshCustomerFlightTable(service.getAllFlights());
                refreshCustomerReservationTable();
                prefillCustomerDetails();
                setStatus("Welcome, " + currentUser.name() + "!");
                switchToCard(CARD_CUSTOMER_DASHBOARD);
            } catch (IllegalArgumentException ex) {
                showMessage("Login failed", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cardRegisterLink = createTextLinkButton("Create account");
        cardRegisterLink.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cardRegisterLink.setForeground(new Color(0, 96, 186));
        cardRegisterLink.addActionListener(e -> switchToCard(CARD_REGISTER));

        JPanel cardFooter = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
        cardFooter.setOpaque(false);
        cardFooter.add(loginButton);
        cardFooter.add(cardRegisterLink);

        loginCard.add(loginCardTitle, BorderLayout.NORTH);
        loginCard.add(formPanel, BorderLayout.CENTER);
        loginCard.add(cardFooter, BorderLayout.SOUTH);

        JPanel heroRow = new JPanel(new GridBagLayout());
        heroRow.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.6;
        gbc.insets = new Insets(0, 0, 0, 36);
        heroRow.add(textPanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.4;
        gbc.insets = new Insets(0, 0, 0, 0);
        heroRow.add(loginCard, gbc);

        panel.add(heroRow, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCustomerLoginPanel() {
        JPanel panel = createFormPanel("Customer Login", createTicketIcon(40));
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        customerLoginEmailField = new JTextField(14);
        customerLoginPasswordField = new JPasswordField(14);

        addFormField(formPanel, "Email:", customerLoginEmailField, 0);
        addFormField(formPanel, "Password:", customerLoginPasswordField, 1);

        JButton loginButton = new JButton("Login");
        JButton backButton = new JButton("Back");
        applyButtonStyle(loginButton, new Color(33, 150, 243));
        applyButtonStyle(backButton, new Color(117, 117, 117));
        loginButton.addActionListener(this::handleCustomerLogin);
        backButton.addActionListener(e -> switchToCard(CARD_WELCOME));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
        actionPanel.setOpaque(false);
        actionPanel.add(backButton);
        actionPanel.add(loginButton);

        JPanel card = new JPanel(new BorderLayout(0, 20));
        card.setOpaque(true);
        card.setBackground(new Color(255, 255, 255, 240));
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0, 82, 155), 2), BorderFactory.createEmptyBorder(24, 24, 24, 24)));
        card.add(formPanel, BorderLayout.CENTER);
        card.add(actionPanel, BorderLayout.SOUTH);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRegistrationPanel() {
        JPanel panel = createFormPanel("New Customer Registration", createPassportIcon(40));
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        registerNameField = new JTextField(14);
        registerEmailField = new JTextField(14);
        registerPasswordField = new JPasswordField(14);
        registerPhoneField = new JTextField(14);

        addFormField(formPanel, "Full Name:", registerNameField, 0);
        addFormField(formPanel, "Email:", registerEmailField, 1);
        addFormField(formPanel, "Password:", registerPasswordField, 2);
        addFormField(formPanel, "Phone:", registerPhoneField, 3);

        JButton submitButton = new JButton("Register");
        JButton backButton = new JButton("Back");
        applyButtonStyle(submitButton, new Color(76, 175, 80));
        applyButtonStyle(backButton, new Color(117, 117, 117));
        submitButton.addActionListener(this::handleCustomerRegistration);
        backButton.addActionListener(e -> switchToCard(CARD_WELCOME));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
        actionPanel.setOpaque(false);
        actionPanel.add(backButton);
        actionPanel.add(submitButton);

        JPanel card = new JPanel(new BorderLayout(0, 20));
        card.setOpaque(true);
        card.setBackground(new Color(255, 255, 255, 240));
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0, 82, 155), 2), BorderFactory.createEmptyBorder(24, 24, 24, 24)));
        card.add(formPanel, BorderLayout.CENTER);
        card.add(actionPanel, BorderLayout.SOUTH);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAdminLoginPanel() {
        JPanel panel = createFormPanel("Administrator Login", createPassportIcon(40));
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        adminEmailField = new JTextField(14);
        adminPasswordField = new JPasswordField(14);

        addFormField(formPanel, "Email:", adminEmailField, 0);
        addFormField(formPanel, "Password:", adminPasswordField, 1);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register Admin");
        JButton backButton = new JButton("Back");
        applyButtonStyle(loginButton, new Color(33, 150, 243));
        applyButtonStyle(registerButton, new Color(103, 58, 183));
        applyButtonStyle(backButton, new Color(117, 117, 117));
        loginButton.addActionListener(this::handleAdminLogin);
        registerButton.addActionListener(e -> switchToCard(CARD_ADMIN_REGISTER));
        backButton.addActionListener(e -> switchToCard(CARD_WELCOME));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
        actionPanel.setOpaque(false);
        actionPanel.add(backButton);
        actionPanel.add(registerButton);
        actionPanel.add(loginButton);

        JPanel card = new JPanel(new BorderLayout(0, 20));
        card.setOpaque(true);
        card.setBackground(new Color(255, 255, 255, 240));
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0, 82, 155), 2), BorderFactory.createEmptyBorder(24, 24, 24, 24)));
        card.add(formPanel, BorderLayout.CENTER);
        card.add(actionPanel, BorderLayout.SOUTH);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAdminRegistrationPanel() {
        JPanel panel = createFormPanel("Create Admin Account", createPassportIcon(40));
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        adminRegisterNameField = new JTextField(14);
        adminRegisterEmailField = new JTextField(14);
        adminRegisterPasswordField = new JPasswordField(14);
        adminRegisterPhoneField = new JTextField(14);
        adminRegisterCodeField = new JTextField(14);

        addFormField(formPanel, "Full Name:", adminRegisterNameField, 0);
        addFormField(formPanel, "Email:", adminRegisterEmailField, 1);
        addFormField(formPanel, "Password:", adminRegisterPasswordField, 2);
        addFormField(formPanel, "Phone:", adminRegisterPhoneField, 3);
        addFormField(formPanel, "Admin Code:", adminRegisterCodeField, 4);

        JButton submitButton = new JButton("Create Admin");
        JButton backButton = new JButton("Back");
        applyButtonStyle(submitButton, new Color(103, 58, 183));
        applyButtonStyle(backButton, new Color(117, 117, 117));
        submitButton.addActionListener(this::handleAdminRegistration);
        backButton.addActionListener(e -> switchToCard(CARD_WELCOME));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
        actionPanel.setOpaque(false);
        actionPanel.add(backButton);
        actionPanel.add(submitButton);

        JPanel card = new JPanel(new BorderLayout(0, 20));
        card.setOpaque(true);
        card.setBackground(new Color(255, 255, 255, 240));
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0, 82, 155), 2), BorderFactory.createEmptyBorder(24, 24, 24, 24)));
        card.add(formPanel, BorderLayout.CENTER);
        card.add(actionPanel, BorderLayout.SOUTH);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCustomerDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 249, 252));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JLabel title = new JLabel("Customer Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(25, 45, 75));
        JButton logoutButton = new JButton("Logout");
        applyButtonStyle(logoutButton, new Color(229, 57, 53));
        logoutButton.addActionListener(e -> {
            currentUser = null;
            switchToCard(CARD_WELCOME);
            setStatus("Logged out.");
        });
        topBar.add(title, BorderLayout.WEST);
        topBar.add(logoutButton, BorderLayout.EAST);
        topBar.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        customerTabbedPane = new JTabbedPane();
        customerTabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        customerTabbedPane.addTab("Search Flights", createCustomerSearchPanel());
        customerTabbedPane.addTab("Book a Flight", createCustomerBookingPanel());
        customerTabbedPane.addTab("My Reservations", createCustomerReservationsPanel());

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(customerTabbedPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCustomerSearchPanel() {
        JPanel panel = createContentPanel();

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        searchPanel.setOpaque(false);
        searchOriginField = new JTextField(14);
        searchDestinationField = new JTextField(14);
        JButton searchButton = new JButton("Search");
        JButton refreshButton = new JButton("Show All");
        applyButtonStyle(searchButton, new Color(0, 122, 204));
        applyButtonStyle(refreshButton, new Color(38, 166, 91));

        searchPanel.add(labelWithFont("Origin:"));
        searchPanel.add(searchOriginField);
        searchPanel.add(labelWithFont("Destination:"));
        searchPanel.add(searchDestinationField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);

        customerFlightTableModel = new DefaultTableModel(new Object[]{"Flight #", "Origin", "Destination", "Depart", "Arrive", "Seats", "Price"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable flightTable = new JTable(customerFlightTableModel);
        configureTable(flightTable);
        flightTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && flightTable.getSelectedRow() >= 0) {
                String selectedFlight = customerFlightTableModel.getValueAt(flightTable.getSelectedRow(), 0).toString();
                bookingFlightNumberField.setText(selectedFlight);
                prefillCustomerDetails();
                setStatus("Selected flight " + selectedFlight + ". Ready to book.");
                if (customerTabbedPane != null) {
                    customerTabbedPane.setSelectedIndex(1);
                }
            }
        });

        searchButton.addActionListener(e -> {
            String origin = searchOriginField.getText().trim();
            String destination = searchDestinationField.getText().trim();
            if (origin.isEmpty() || destination.isEmpty()) {
                showMessage("Validation error", "Please enter both origin and destination.", JOptionPane.WARNING_MESSAGE);
                return;
            }
            refreshCustomerFlightTable(service.searchFlights(origin, destination));
        });
        refreshButton.addActionListener(e -> refreshCustomerFlightTable(service.getAllFlights()));

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(flightTable), BorderLayout.CENTER);

        refreshCustomerFlightTable(service.getAllFlights());
        return panel;
    }

    private JPanel createCustomerBookingPanel() {
        JPanel panel = createContentPanel();

        JPanel bookingPanel = new JPanel(new GridBagLayout());
        bookingPanel.setOpaque(false);
        bookingPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(200, 211, 222), 1), "Booking Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 10, 2, 10);
        gbc.anchor = GridBagConstraints.WEST;

        bookingFlightNumberField = new JTextField(14);
        bookingFlightNumberField.setEditable(false);
        bookingSeatsField = new JTextField(6);
        bookingSeatsField.setEnabled(false);

        passengerAdultCountSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        passengerChildCountSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
        passengerNameField = new JTextField(14);
        passengerEmailField = new JTextField(14);
        passengerPhoneField = new JTextField(12);
        passengerPassportField = new JTextField(14);
        passengerTypeCombo = new JComboBox<>(new String[]{"Adult", "Child"});
        seatPreferenceCombo = new JComboBox<>(new String[]{"Aisle", "Middle", "Window"});
        travelClassCombo = new JComboBox<>(new String[]{"Economy", "Business", "First"});
        paymentMethodCombo = new JComboBox<>(new String[]{"Mpesa", "Bank Transaction"});

        addFieldToPanel(bookingPanel, labelWithFont("Flight #:"), bookingFlightNumberField, gbc, 0);
        addFieldToPanel(bookingPanel, labelWithFont("Adults:"), passengerAdultCountSpinner, gbc, 1);
        addFieldToPanel(bookingPanel, labelWithFont("Children:"), passengerChildCountSpinner, gbc, 2);
        addFieldToPanel(bookingPanel, labelWithFont("Passenger Type:"), passengerTypeCombo, gbc, 3);
        addFieldToPanel(bookingPanel, labelWithFont("Full Name:"), passengerNameField, gbc, 4);
        addFieldToPanel(bookingPanel, labelWithFont("Email:"), passengerEmailField, gbc, 5);
        addFieldToPanel(bookingPanel, labelWithFont("Phone:"), passengerPhoneField, gbc, 6);
        addFieldToPanel(bookingPanel, labelWithFont("Passport #:"), passengerPassportField, gbc, 7);
        addFieldToPanel(bookingPanel, labelWithFont("Seat Preference:"), seatPreferenceCombo, gbc, 8);
        addFieldToPanel(bookingPanel, labelWithFont("Travel Class:"), travelClassCombo, gbc, 9);
        addFieldToPanel(bookingPanel, labelWithFont("Payment Method:"), paymentMethodCombo, gbc, 10);

        passengerCounterLabel = new JLabel("Passengers added: 0");
        passengerCounterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passengerCounterLabel.setForeground(new Color(70, 90, 110));
        gbc.gridx = 0; gbc.gridy = 11; gbc.gridwidth = 2; bookingPanel.add(passengerCounterLabel, gbc);

        addPassengerButton = new JButton("Add Passenger");
        confirmBookingButton = new JButton("Finalize and Pay");
        applyButtonStyle(addPassengerButton, new Color(76, 175, 80));
        applyButtonStyle(confirmBookingButton, new Color(33, 150, 243));
        addPassengerButton.addActionListener(e -> handleAddPassenger());
        confirmBookingButton.addActionListener(e -> handleConfirmGroupBooking());

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        buttonBar.setOpaque(false);
        buttonBar.add(addPassengerButton);
        buttonBar.add(confirmBookingButton);

        pendingPassengerTableModel = new DefaultTableModel(new Object[]{"Name", "Type", "Passport", "Seat", "Class"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable pendingTable = new JTable(pendingPassengerTableModel);
        configureTable(pendingTable);
        pendingTable.setPreferredScrollableViewportSize(new Dimension(0, 160));

        JPanel formWrapper = new JPanel(new BorderLayout(20, 0));
        formWrapper.setOpaque(false);
        
        JPanel bookingWrapper = new JPanel(new BorderLayout());
        bookingWrapper.setOpaque(false);
        bookingWrapper.add(bookingPanel, BorderLayout.NORTH);
        
        formWrapper.add(bookingWrapper, BorderLayout.WEST);
        formWrapper.add(new JScrollPane(pendingTable), BorderLayout.CENTER);

        JScrollPane pageScroll = new JScrollPane(formWrapper);
        pageScroll.setBorder(BorderFactory.createEmptyBorder());
        pageScroll.setOpaque(false);
        pageScroll.getViewport().setOpaque(false);
        pageScroll.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(pageScroll, BorderLayout.CENTER);
        panel.add(buttonBar, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createCustomerReservationsPanel() {
        JPanel panel = createContentPanel();

        customerReservationTableModel = new DefaultTableModel(new Object[]{"Reservation #", "Flight #", "Seats", "Price", "Booked"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable reservationTable = new JTable(customerReservationTableModel);
        configureTable(reservationTable);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        bottomPanel.setOpaque(false);
        JTextField cancelReservationField = new JTextField(14);
        
        reservationTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && reservationTable.getSelectedRow() >= 0) {
                String selectedReservationId = customerReservationTableModel.getValueAt(reservationTable.getSelectedRow(), 0).toString();
                cancelReservationField.setText(selectedReservationId);
            }
        });

        JButton cancelButton = new JButton("Cancel Reservation");
        applyButtonStyle(cancelButton, new Color(229, 57, 53));
        cancelButton.addActionListener(e -> {
            String reservationId = cancelReservationField.getText().trim();
            if (reservationId.isEmpty()) {
                showMessage("Validation error", "Please enter a reservation ID.", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (service.cancelReservation(reservationId, currentUser.id())) {
                refreshCustomerReservationTable();
                refreshCustomerFlightTable(service.getAllFlights());
                refreshOverview();
                showMessage("Success", "Reservation cancelled successfully.", JOptionPane.INFORMATION_MESSAGE);
                setStatus("Reservation " + reservationId + " cancelled.");
                cancelReservationField.setText("");
            } else {
                showMessage("Cancel failed", "Unable to cancel reservation.", JOptionPane.ERROR_MESSAGE);
            }
        });

        bottomPanel.add(labelWithFont("Reservation ID:"));
        bottomPanel.add(cancelReservationField);
        bottomPanel.add(cancelButton);

        panel.add(new JScrollPane(reservationTable), BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createAdminDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(229, 241, 255));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JLabel title = new JLabel("Airline Operations Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(8, 35, 82));
        JLabel subtitle = new JLabel("Monitor flights, reservations and passenger activity in real time.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(77, 96, 132));

        JPanel headingPanel = new JPanel(new BorderLayout(0, 8));
        headingPanel.setOpaque(false);
        headingPanel.add(title, BorderLayout.NORTH);
        headingPanel.add(subtitle, BorderLayout.SOUTH);

        JButton logoutButton = new JButton("Logout");
        applyButtonStyle(logoutButton, new Color(220, 20, 60));
        logoutButton.setPreferredSize(new Dimension(140, 42));
        logoutButton.addActionListener(e -> {
            currentUser = null;
            switchToCard(CARD_WELCOME);
            setStatus("Admin logged out.");
        });

        topBar.add(headingPanel, BorderLayout.WEST);
        topBar.add(logoutButton, BorderLayout.EAST);
        topBar.setBorder(BorderFactory.createEmptyBorder(22, 22, 18, 22));

        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 18, 18));
        summaryPanel.setOpaque(false);
        topFlightsLabel = overviewCard("Flights", "0", Color.WHITE, new Color(0, 102, 204));
        topCustomersLabel = overviewCard("Customers", "0", Color.WHITE, new Color(33, 150, 243));
        topReservationsLabel = overviewCard("Reservations", "0", Color.WHITE, new Color(0, 150, 136));
        topSeatsLabel = overviewCard("Available Seats", "0", Color.WHITE, new Color(220, 20, 60));
        summaryPanel.add(topFlightsLabel);
        summaryPanel.add(topCustomersLabel);
        summaryPanel.add(topReservationsLabel);
        summaryPanel.add(topSeatsLabel);

        JPanel sideNav = new JPanel();
        sideNav.setLayout(new BoxLayout(sideNav, BoxLayout.Y_AXIS));
        sideNav.setOpaque(false);
        sideNav.setBorder(BorderFactory.createEmptyBorder(20, 18, 20, 18));

        adminFlightButton = createSideNavButton("Flight Management");
        adminReservationButton = createSideNavButton("Reservations");
        adminCustomerButton = createSideNavButton("Customers");
        adminOverviewButton = createSideNavButton("Overview");

        sideNav.add(adminFlightButton);
        sideNav.add(Box.createVerticalStrut(12));
        sideNav.add(adminReservationButton);
        sideNav.add(Box.createVerticalStrut(12));
        sideNav.add(adminCustomerButton);
        sideNav.add(Box.createVerticalStrut(12));
        sideNav.add(adminOverviewButton);
        sideNav.add(Box.createVerticalGlue());

        JPanel adminContentPanel = new JPanel(new CardLayout());
        adminContentPanel.setOpaque(false);
        adminContentPanel.add(createAdminFlightPanel(), "flights");
        adminContentPanel.add(createAdminReservationPanel(), "reservations");
        adminContentPanel.add(createAdminCustomerPanel(), "customers");
        adminContentPanel.add(createAdminOverviewPanel(), "overview");

        adminFlightButton.addActionListener(e -> {
            switchAdminCard(adminContentPanel, "flights");
            setActiveAdminNavButton(adminFlightButton);
        });
        adminReservationButton.addActionListener(e -> {
            switchAdminCard(adminContentPanel, "reservations");
            setActiveAdminNavButton(adminReservationButton);
        });
        adminCustomerButton.addActionListener(e -> {
            switchAdminCard(adminContentPanel, "customers");
            setActiveAdminNavButton(adminCustomerButton);
        });
        adminOverviewButton.addActionListener(e -> {
            switchAdminCard(adminContentPanel, "overview");
            setActiveAdminNavButton(adminOverviewButton);
        });

        setActiveAdminNavButton(adminFlightButton);

        JPanel contentWrapper = new JPanel(new BorderLayout(18, 18));
        contentWrapper.setOpaque(false);
        contentWrapper.setBorder(BorderFactory.createEmptyBorder(0, 22, 22, 22));
        contentWrapper.add(summaryPanel, BorderLayout.NORTH);

        JPanel adminPanel = new JPanel(new BorderLayout(18, 0));
        adminPanel.setOpaque(false);
        adminPanel.add(sideNav, BorderLayout.WEST);
        adminPanel.add(adminContentPanel, BorderLayout.CENTER);

        contentWrapper.add(adminPanel, BorderLayout.CENTER);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(contentWrapper, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAdminFlightPanel() {
        JPanel panel = createContentPanel();

        adminFlightTableModel = new DefaultTableModel(new Object[]{"Flight #", "Origin", "Destination", "Depart", "Arrive", "Seats", "Price"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable flightTable = new JTable(adminFlightTableModel);
        configureTable(flightTable);

        flightTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && flightTable.getSelectedRow() >= 0) {
                adminFlightNumberField.setText(adminFlightTableModel.getValueAt(flightTable.getSelectedRow(), 0).toString());
                adminOriginField.setText(adminFlightTableModel.getValueAt(flightTable.getSelectedRow(), 1).toString());
                adminDestinationField.setText(adminFlightTableModel.getValueAt(flightTable.getSelectedRow(), 2).toString());
                adminDepartField.setText(adminFlightTableModel.getValueAt(flightTable.getSelectedRow(), 3).toString());
                adminArriveField.setText(adminFlightTableModel.getValueAt(flightTable.getSelectedRow(), 4).toString());
                String selectedFlightNumber = adminFlightTableModel.getValueAt(flightTable.getSelectedRow(), 0).toString();
                Flight f = service.getFlightByNumber(selectedFlightNumber);
                if (f != null) {
                    adminSeatsField.setText(String.valueOf(f.getTotalSeats()));
                } else {
                    String seatsText = adminFlightTableModel.getValueAt(flightTable.getSelectedRow(), 5).toString();
                    adminSeatsField.setText(seatsText.contains("/") ? seatsText.split("/")[1] : seatsText);
                }
                adminPriceField.setText(adminFlightTableModel.getValueAt(flightTable.getSelectedRow(), 6).toString().replace("KSh ", ""));
            }
        });

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(200, 211, 222)), "Flight Editor"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 10, 4, 10);
        gbc.anchor = GridBagConstraints.WEST;

        adminFlightNumberField = new JTextField(12);
        adminOriginField = new JTextField(12);
        adminDestinationField = new JTextField(12);
        adminDepartField = new JTextField(12);
        adminArriveField = new JTextField(12);
        adminSeatsField = new JTextField(12);
        adminPriceField = new JTextField(12);

        addFieldToPanel(formPanel, labelWithFont("Flight #:"), adminFlightNumberField, gbc, 0);
        addFieldToPanel(formPanel, labelWithFont("Origin:"), adminOriginField, gbc, 1);
        addFieldToPanel(formPanel, labelWithFont("Destination:"), adminDestinationField, gbc, 2);
        addFieldToPanel(formPanel, labelWithFont("Depart:"), adminDepartField, gbc, 3);
        addFieldToPanel(formPanel, labelWithFont("Arrive:"), adminArriveField, gbc, 4);
        addFieldToPanel(formPanel, labelWithFont("Seats:"), adminSeatsField, gbc, 5);
        addFieldToPanel(formPanel, labelWithFont("Price:"), adminPriceField, gbc, 6);

        JButton addButton = new JButton("Add Flight");
        JButton updateButton = new JButton("Update Flight");
        JButton deleteButton = new JButton("Delete Flight");
        applyButtonStyle(addButton, new Color(76, 175, 80));
        applyButtonStyle(updateButton, new Color(255, 187, 51));
        applyButtonStyle(deleteButton, new Color(229, 57, 53));

        addButton.addActionListener(this::handleAddFlight);
        updateButton.addActionListener(this::handleUpdateFlight);
        deleteButton.addActionListener(this::handleDeleteFlight);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
        actionPanel.setOpaque(false);
        actionPanel.add(addButton);
        actionPanel.add(updateButton);
        actionPanel.add(deleteButton);

        JPanel leftPanel = new JPanel(new BorderLayout(0, 16));
        leftPanel.setOpaque(false);
        
        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setOpaque(false);
        formWrapper.add(formPanel, BorderLayout.NORTH);
        
        leftPanel.add(formWrapper, BorderLayout.CENTER);
        leftPanel.add(actionPanel, BorderLayout.SOUTH);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(new JScrollPane(flightTable), BorderLayout.CENTER);

        refreshAdminFlightTable();
        return panel;
    }

    private JPanel createAdminReservationPanel() {
        JPanel panel = createContentPanel();

        adminReservationTableModel = new DefaultTableModel(new Object[]{"Reservation #", "Flight #", "Passenger", "Seats", "Price", "Booked"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable reservationTable = new JTable(adminReservationTableModel);
        configureTable(reservationTable);

        panel.add(new JScrollPane(reservationTable), BorderLayout.CENTER);
        refreshAdminReservationTable();
        return panel;
    }

    private JPanel createAdminCustomerPanel() {
        JPanel panel = createContentPanel();

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
        top.setOpaque(false);
        adminCustomerSearchField = new JTextField(18);
        JButton searchButton = new JButton("Search");
        applyButtonStyle(searchButton, new Color(33, 150, 243));
        searchButton.addActionListener(e -> refreshAdminCustomerTable(service.searchCustomers(adminCustomerSearchField.getText())));
        top.add(labelWithFont("Search customers:"));
        top.add(adminCustomerSearchField);
        top.add(searchButton);

        adminCustomerTableModel = new DefaultTableModel(new Object[]{"Customer ID", "Name", "Email", "Phone"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable customerTable = new JTable(adminCustomerTableModel);
        configureTable(customerTable);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(customerTable), BorderLayout.CENTER);
        refreshAdminCustomerTable(service.getAllCustomers());
        return panel;
    }

    private JPanel createAdminOverviewPanel() {
        JPanel panel = createContentPanel();
        panel.setLayout(new GridLayout(2, 2, 20, 20));

        overviewFlightsLabel = overviewCard("Flights", "0", Color.WHITE, new Color(0, 102, 204));
        overviewCustomersLabel = overviewCard("Customers", "0", Color.WHITE, new Color(33, 150, 243));
        overviewReservationsLabel = overviewCard("Reservations", "0", Color.WHITE, new Color(0, 150, 136));
        overviewSeatsLabel = overviewCard("Available Seats", "0", Color.WHITE, new Color(220, 20, 60));

        panel.add(overviewFlightsLabel);
        panel.add(overviewCustomersLabel);
        panel.add(overviewReservationsLabel);
        panel.add(overviewSeatsLabel);
        refreshOverview();
        return panel;
    }

    private JLabel overviewCard(String title, String value, Color background, Color accent) {
        JLabel label = new JLabel(String.format("<html><div style='text-align:center'><span style='font-size:22pt; font-weight:bold; color:%s'>%s</span><br/><span style='font-size:12pt; color:#4a4a4a'>%s</span></div></html>",
                toHexString(accent), value, title));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(background);
        label.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(accent, 1), BorderFactory.createEmptyBorder(14, 10, 14, 10)));
        return label;
    }

    private String toHexString(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private JPanel createFormPanel(String heading, ImageIcon icon) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(9, 45, 108));
        JLabel title = new JLabel(heading, icon, SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(Color.WHITE);
        title.setIconTextGap(16);
        title.setBorder(BorderFactory.createEmptyBorder(24, 24, 16, 24));

        panel.add(title, BorderLayout.NORTH);
        panel.setBorder(BorderFactory.createEmptyBorder(36, 36, 36, 36));
        return panel;
    }

    private void addFormField(JPanel panel, String label, JComponent field, int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.weightx = 0;
        JLabel fieldLabel = labelWithFont(label);
        panel.add(fieldLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private void addFieldToPanel(JPanel panel, JLabel label, JComponent field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        panel.setBackground(new Color(237, 241, 245));
        statusLabel = new JLabel("Ready.");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(statusLabel, BorderLayout.WEST);
        return panel;
    }

    private void handleCustomerLogin(ActionEvent ignored) {
        try {
            String email = customerLoginEmailField.getText().trim();
            String password = new String(customerLoginPasswordField.getPassword());
            currentUser = service.authenticate(email, password);
            if (!currentUser.isCustomer()) {
                throw new IllegalArgumentException("Please login with a customer account.");
            }
            refreshCustomerFlightTable(service.getAllFlights());
            refreshCustomerReservationTable();
            prefillCustomerDetails();
            setStatus("Welcome, " + currentUser.name() + "!");
            switchToCard(CARD_CUSTOMER_DASHBOARD);
        } catch (IllegalArgumentException ex) {
            showMessage("Login failed", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCustomerRegistration(ActionEvent ignored) {
        try {
            String name = registerNameField.getText().trim();
            String email = registerEmailField.getText().trim();
            String password = new String(registerPasswordField.getPassword());
            String phone = registerPhoneField.getText().trim();
            currentUser = service.registerCustomer(name, email, password, phone);
            showMessage("Registered", "Customer account created successfully. Welcome, " + currentUser.name() + "!", JOptionPane.INFORMATION_MESSAGE);
            refreshCustomerFlightTable(service.getAllFlights());
            refreshCustomerReservationTable();
            prefillCustomerDetails();
            switchToCard(CARD_CUSTOMER_DASHBOARD);
            clearRegisterForm();
            setStatus("Logged in as " + currentUser.name() + ".");
        } catch (IllegalArgumentException ex) {
            showMessage("Registration failed", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAdminLogin(ActionEvent ignored) {
        try {
            String email = adminEmailField.getText().trim();
            String password = new String(adminPasswordField.getPassword());
            currentUser = service.authenticate(email, password);
            if (!currentUser.isAdmin()) {
                throw new IllegalArgumentException("Administrator credentials required.");
            }
            refreshAdminFlightTable();
            refreshAdminReservationTable();
            refreshAdminCustomerTable(service.getAllCustomers());
            refreshOverview();
            setStatus("Admin access granted.");
            switchToCard(CARD_ADMIN_DASHBOARD);
        } catch (IllegalArgumentException ex) {
            showMessage("Login failed", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAdminRegistration(ActionEvent ignored) {
        try {
            String name = adminRegisterNameField.getText().trim();
            String email = adminRegisterEmailField.getText().trim();
            String password = new String(adminRegisterPasswordField.getPassword());
            String phone = adminRegisterPhoneField.getText().trim();
            String code = adminRegisterCodeField.getText().trim();
            service.registerAdmin(name, email, password, phone, code);
            showMessage("Admin Created", "Administrator account created successfully. Please login using the admin credentials.", JOptionPane.INFORMATION_MESSAGE);
            clearAdminRegisterForm();
            setStatus("Admin account created.");
            switchToCard(CARD_ADMIN_LOGIN);
        } catch (IllegalArgumentException ex) {
            showMessage("Registration failed", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCustomerFlightBooking(ActionEvent ignored) {
        // legacy booking method not used in new customer workflow
    }

    private void prefillCustomerDetails() {
        if (currentUser != null && currentUser.isCustomer()) {
            if (passengerNameField != null && passengerNameField.getText().trim().isEmpty()) {
                passengerNameField.setText(currentUser.name());
            }
            if (passengerEmailField != null && passengerEmailField.getText().trim().isEmpty()) {
                passengerEmailField.setText(currentUser.email());
            }
            if (passengerPhoneField != null && passengerPhoneField.getText().trim().isEmpty()) {
                passengerPhoneField.setText(currentUser.phone());
            }
        }
    }

    private void handleAddPassenger() {
        if (currentUser == null || !currentUser.isCustomer()) {
            showMessage("Not logged in", "Please login as a customer before adding passengers.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String flightNumber = bookingFlightNumberField.getText().trim();
        if (flightNumber.isEmpty()) {
            showMessage("Validation error", "Please select a flight before adding passenger details.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String name = passengerNameField.getText().trim();
        if (name.isEmpty() && currentUser != null) {
            name = currentUser.name();
            passengerNameField.setText(name);
        }
        String email = passengerEmailField.getText().trim();
        if (email.isEmpty() && currentUser != null) {
            email = currentUser.email();
            passengerEmailField.setText(email);
        }
        String phone = passengerPhoneField.getText().trim();
        if (phone.isEmpty() && currentUser != null) {
            phone = currentUser.phone();
            passengerPhoneField.setText(phone);
        }
        String passport = passengerPassportField.getText().trim();
        if (passport.isEmpty()) {
            passport = "N/A";
        }
        String type = (String) passengerTypeCombo.getSelectedItem();
        String seat = (String) seatPreferenceCombo.getSelectedItem();
        String travelClass = (String) travelClassCombo.getSelectedItem();
        String paymentMethod = (String) paymentMethodCombo.getSelectedItem();

        if (name.isEmpty() || type == null || seat == null || travelClass == null || paymentMethod == null) {
            showMessage("Validation error", "Please complete all required passenger fields (Name, Type, Seat, Class, Payment) before adding.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int expectedAdults = (Integer) passengerAdultCountSpinner.getValue();
        int expectedChildren = (Integer) passengerChildCountSpinner.getValue();
        int expectedTotal = expectedAdults + expectedChildren;
        if (pendingPassengers.size() >= expectedTotal) {
            showMessage("Passenger limit reached", "You have already added the expected number of passengers.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BookingPassenger bookingPassenger = new BookingPassenger(name, email, phone, passport, type, seat, travelClass, paymentMethod);
        pendingPassengers.add(bookingPassenger);
        pendingPassengerTableModel.addRow(new Object[]{name, type, passport, seat, travelClass});
        passengerCounterLabel.setText("Passengers added: " + pendingPassengers.size() + " / " + expectedTotal);
        clearPassengerEntryFields();
        setStatus("Passenger added: " + name + ". Complete all passenger details before finalizing.");
    }

    private void handleConfirmGroupBooking() {
        if (currentUser == null || !currentUser.isCustomer()) {
            showMessage("Not logged in", "Please login as a customer before finalizing your booking.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String flightNumber = bookingFlightNumberField.getText().trim();
        if (flightNumber.isEmpty()) {
            showMessage("Validation error", "Please select a flight before completing checkout.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int expectedAdults = (Integer) passengerAdultCountSpinner.getValue();
        int expectedChildren = (Integer) passengerChildCountSpinner.getValue();
        int expectedTotal = expectedAdults + expectedChildren;

        // Auto-add current form passenger if pending list is empty
        if (pendingPassengers.isEmpty()) {
            handleAddPassenger();
        }

        if (pendingPassengers.size() != expectedTotal) {
            showMessage("Incomplete passenger list", "Please add details for all " + expectedTotal + " passenger(s) before finalizing.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Flight flight = service.getFlightByNumber(flightNumber);
        if (flight == null) {
            showMessage("Booking failed", "Selected flight is no longer available.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (flight.getAvailableSeats() < expectedTotal) {
            showMessage("Booking failed", "Not enough seats available for all passengers.", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double totalCost = 0.0;
        String selectedPaymentMethod = null;
        for (BookingPassenger passenger : pendingPassengers) {
            Reservation reservation = service.bookFlightForPassenger(
                    flightNumber,
                    currentUser.id(),
                    passenger.name(),
                    passenger.email(),
                    passenger.phone(),
                    passenger.passportNumber(),
                    passenger.passengerType(),
                    passenger.seatPreference(),
                    passenger.travelClass(),
                    passenger.paymentMethod());
            totalCost += reservation.totalPrice();
            selectedPaymentMethod = reservation.paymentMethod();
        }

        refreshCustomerFlightTable(service.getAllFlights());
        refreshCustomerReservationTable();
        refreshAdminReservationTable();
        refreshAdminFlightTable();
        refreshOverview();

        showMessage("Booking complete", String.format("All %d passengers are booked. Total amount: KSh %.2f via %s.", expectedTotal, totalCost, selectedPaymentMethod), JOptionPane.INFORMATION_MESSAGE);
        setStatus("Completed group booking for " + expectedTotal + " passengers.");
        clearGroupBookingState();
    }

    private void clearPassengerEntryFields() {
        passengerNameField.setText("");
        passengerEmailField.setText("");
        passengerPhoneField.setText("");
        passengerPassportField.setText("");
        passengerTypeCombo.setSelectedIndex(0);
        seatPreferenceCombo.setSelectedIndex(0);
        travelClassCombo.setSelectedIndex(0);
        paymentMethodCombo.setSelectedIndex(0);
    }

    private void clearGroupBookingState() {
        pendingPassengers.clear();
        pendingPassengerTableModel.setRowCount(0);
        passengerCounterLabel.setText("Passengers added: 0");
        passengerAdultCountSpinner.setValue(1);
        passengerChildCountSpinner.setValue(0);
        bookingFlightNumberField.setText("");
        clearPassengerEntryFields();
    }

    private void handleAddFlight(ActionEvent ignored) {
        try {
            String flightNumber = adminFlightNumberField.getText().trim();
            String origin = adminOriginField.getText().trim();
            String destination = adminDestinationField.getText().trim();
            String depart = adminDepartField.getText().trim();
            String arrive = adminArriveField.getText().trim();
            int seats = Integer.parseInt(adminSeatsField.getText().trim());
            double price = Double.parseDouble(adminPriceField.getText().trim());
            service.addFlight(flightNumber, origin, destination, depart, arrive, seats, price);
            refreshAdminFlightTable();
            refreshOverview();
            showMessage("Success", "Flight added successfully.", JOptionPane.INFORMATION_MESSAGE);
            setStatus("Added flight " + flightNumber + ".");
            clearAdminFlightForm();
        } catch (NumberFormatException ex) {
            showMessage("Input error", "Please enter valid numbers for seats and price.", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            showMessage("Unable to add flight", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdateFlight(ActionEvent ignored) {
        try {
            String flightNumber = adminFlightNumberField.getText().trim();
            String origin = adminOriginField.getText().trim();
            String destination = adminDestinationField.getText().trim();
            String depart = adminDepartField.getText().trim();
            String arrive = adminArriveField.getText().trim();
            int seats = Integer.parseInt(adminSeatsField.getText().trim());
            double price = Double.parseDouble(adminPriceField.getText().trim());
            service.updateFlight(flightNumber, origin, destination, depart, arrive, seats, price);
            refreshAdminFlightTable();
            refreshCustomerFlightTable(service.getAllFlights());
            refreshOverview();
            showMessage("Success", "Flight updated successfully.", JOptionPane.INFORMATION_MESSAGE);
            setStatus("Updated flight " + flightNumber + ".");
        } catch (NumberFormatException ex) {
            showMessage("Input error", "Please enter valid numbers for seats and price.", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            showMessage("Unable to update flight", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteFlight(ActionEvent ignored) {
        try {
            String flightNumber = adminFlightNumberField.getText().trim();
            if (flightNumber.isEmpty()) {
                showMessage("Validation error", "Please select a flight number to delete.", JOptionPane.WARNING_MESSAGE);
                return;
            }
            service.deleteFlight(flightNumber);
            refreshAdminFlightTable();
            refreshCustomerFlightTable(service.getAllFlights());
            refreshOverview();
            showMessage("Deleted", "Flight " + flightNumber + " has been removed.", JOptionPane.INFORMATION_MESSAGE);
            setStatus("Deleted flight " + flightNumber + ".");
            clearAdminFlightForm();
        } catch (IllegalArgumentException ex) {
            showMessage("Unable to delete flight", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshCustomerFlightTable() {
        refreshCustomerFlightTable(service.getAllFlights());
    }

    private void refreshCustomerFlightTable(List<Flight> flights) {
        customerFlightTableModel.setRowCount(0);
        for (Flight flight : flights) {
            customerFlightTableModel.addRow(new Object[]{
                    flight.getFlightNumber(),
                    flight.getOrigin(),
                    flight.getDestination(),
                    flight.getDepartureTime(),
                    flight.getArrivalTime(),
                    flight.getAvailableSeats() + "/" + flight.getTotalSeats(),
                    String.format("KSh %.2f", flight.getPrice())
            });
        }
        setStatus("Showing " + flights.size() + " flights.");
    }

    private void refreshCustomerReservationTable() {
        customerReservationTableModel.setRowCount(0);
        if (currentUser != null) {
            List<Reservation> reservations = service.getReservationsForUser(currentUser);
            for (Reservation reservation : reservations) {
                customerReservationTableModel.addRow(new Object[]{
                        reservation.reservationId(),
                        reservation.flight().getFlightNumber(),
                        reservation.seatCount(),
                        String.format("KSh %.2f", reservation.totalPrice()),
                        reservation.bookingDate()
                });
            }
            setStatus("You have " + reservations.size() + " reservation(s).");
        }
    }

    private void refreshAdminFlightTable() {
        adminFlightTableModel.setRowCount(0);
        for (Flight flight : service.getAllFlights()) {
            adminFlightTableModel.addRow(new Object[]{
                    flight.getFlightNumber(),
                    flight.getOrigin(),
                    flight.getDestination(),
                    flight.getDepartureTime(),
                    flight.getArrivalTime(),
                    flight.getAvailableSeats() + "/" + flight.getTotalSeats(),
                    String.format("KSh %.2f", flight.getPrice())
            });
        }
    }

    private void refreshAdminReservationTable() {
        adminReservationTableModel.setRowCount(0);
        for (Reservation reservation : service.getAllReservations()) {
            adminReservationTableModel.addRow(new Object[]{
                    reservation.reservationId(),
                    reservation.flight().getFlightNumber(),
                    reservation.passenger().name(),
                    reservation.seatCount(),
                    String.format("KSh %.2f", reservation.totalPrice()),
                    reservation.bookingDate()
            });
        }
    }

    private void refreshAdminCustomerTable(List<User> customers) {
        adminCustomerTableModel.setRowCount(0);
        for (User customer : customers) {
            adminCustomerTableModel.addRow(new Object[]{
                    customer.id(),
                    customer.name(),
                    customer.email(),
                    customer.phone()
            });
        }
    }

    private void refreshOverview() {
        int flights = service.getTotalFlightCount();
        int customers = service.getTotalCustomerCount();
        int reservations = service.getTotalReservationCount();
        int seats = service.getAvailableSeatCount();

        if (overviewFlightsLabel != null) updateOverviewCard(overviewFlightsLabel, "Flights", flights, new Color(0, 102, 204));
        if (overviewCustomersLabel != null) updateOverviewCard(overviewCustomersLabel, "Customers", customers, new Color(33, 150, 243));
        if (overviewReservationsLabel != null) updateOverviewCard(overviewReservationsLabel, "Reservations", reservations, new Color(0, 150, 136));
        if (overviewSeatsLabel != null) updateOverviewCard(overviewSeatsLabel, "Available Seats", seats, new Color(220, 20, 60));

        if (topFlightsLabel != null) updateOverviewCard(topFlightsLabel, "Flights", flights, new Color(0, 102, 204));
        if (topCustomersLabel != null) updateOverviewCard(topCustomersLabel, "Customers", customers, new Color(33, 150, 243));
        if (topReservationsLabel != null) updateOverviewCard(topReservationsLabel, "Reservations", reservations, new Color(0, 150, 136));
        if (topSeatsLabel != null) updateOverviewCard(topSeatsLabel, "Available Seats", seats, new Color(220, 20, 60));
    }

    private void updateOverviewCard(JLabel label, String title, int count, Color accent) {
        label.setText(String.format("<html><div style='text-align:center'><span style='font-size:22pt; font-weight:bold; color:%s'>%d</span><br/><span style='font-size:12pt; color:#4a4a4a'>%s</span></div></html>",
                toHexString(accent), count, title));
    }

    private void switchToCard(String cardName) {
        cardLayout.show(cardsPanel, cardName);
    }

    private void switchAdminCard(JPanel adminCardPanel, String cardName) {
        CardLayout adminLayout = (CardLayout) adminCardPanel.getLayout();
        adminLayout.show(adminCardPanel, cardName);
    }

    private void clearRegisterForm() {
        registerNameField.setText("");
        registerEmailField.setText("");
        registerPasswordField.setText("");
        registerPhoneField.setText("");
    }

    private void clearAdminFlightForm() {
        adminFlightNumberField.setText("");
        adminOriginField.setText("");
        adminDestinationField.setText("");
        adminDepartField.setText("");
        adminArriveField.setText("");
        adminSeatsField.setText("");
        adminPriceField.setText("");
    }

    private void clearAdminRegisterForm() {
        adminRegisterNameField.setText("");
        adminRegisterEmailField.setText("");
        adminRegisterPasswordField.setText("");
        adminRegisterPhoneField.setText("");
        adminRegisterCodeField.setText("");
    }

    private void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    private void showMessage(String title, String message, int messageType) {
        JOptionPane.showMessageDialog(frame, message, title, messageType);
    }

    private void applyButtonStyle(JButton button, Color background) {
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(160, 42));
    }

    private JButton createTextLinkButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setForeground(new Color(210, 225, 255));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JPanel createHeroChip(String text) {
        JLabel chip = new JLabel(text);
        chip.setOpaque(true);
        chip.setBackground(new Color(255, 255, 255, 220));
        chip.setForeground(new Color(32, 72, 118));
        chip.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        chip.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapper.setOpaque(false);
        wrapper.add(chip);
        return wrapper;
    }

    private JButton createSideNavButton(String title) {
        JButton button = new JButton(title);
        button.setBackground(new Color(255, 255, 255));
        button.setForeground(new Color(8, 35, 82));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0, 102, 204), 1), BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return button;
    }

    private void setActiveAdminNavButton(JButton button) {
        if (activeAdminNavButton != null) {
            activeAdminNavButton.setBackground(new Color(255, 255, 255));
            activeAdminNavButton.setForeground(new Color(8, 35, 82));
            activeAdminNavButton.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0, 102, 204), 1), BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        }
        activeAdminNavButton = button;
        activeAdminNavButton.setBackground(new Color(0, 102, 204));
        activeAdminNavButton.setForeground(Color.WHITE);
        activeAdminNavButton.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0, 82, 155), 2), BorderFactory.createEmptyBorder(12, 18, 12, 18)));
    }

    private JLabel labelWithFont(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(48, 63, 84));
        return label;
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        return panel;
    }

    private ImageIcon createTicketIcon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(255, 235, 59));
        g.fillRoundRect(size / 8, size / 6, size * 3 / 4, size / 2, 16, 16);
        g.setColor(new Color(255, 193, 7));
        g.fillRect(size / 8 + 8, size / 4, size / 3, size / 8);
        g.setColor(new Color(96, 125, 139));
        g.fillOval(size / 8 + 4, size / 3, size / 10, size / 10);
        g.fillOval(size * 5 / 8, size / 3, size / 10, size / 10);
        g.dispose();
        return new ImageIcon(image);
    }

    private ImageIcon createLuggageIcon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(76, 175, 80));
        g.fillRoundRect(size / 6, size / 5, size / 2, size / 2, 14, 14);
        g.fillRect(size / 3, size / 7, size / 6, size / 8);
        g.setColor(new Color(56, 142, 60));
        g.fillRect(size / 4, size * 3 / 5, size / 5, size / 10);
        g.setColor(new Color(255, 255, 255));
        g.fillRect(size / 3 + 4, size / 3, size / 10, size / 20);
        g.dispose();
        return new ImageIcon(image);
    }

    private ImageIcon createPassportIcon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(33, 150, 243));
        g.fillRoundRect(size / 6, size / 8, size / 2, size * 3 / 4, 18, 18);
        g.setColor(new Color(255, 235, 59));
        g.fillOval(size / 3, size / 4, size / 4, size / 4);
        g.setColor(new Color(255, 255, 255));
        g.drawLine(size / 3 + 4, size / 2, size / 3 + size / 4 - 4, size / 2);
        g.dispose();
        return new ImageIcon(image);
    }

    private void configureTable(JTable table) {
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(26);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(new Color(40, 55, 71));
        table.setSelectionBackground(new Color(0, 120, 215));
        table.setSelectionForeground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(237, 241, 245));
        header.setForeground(new Color(34, 49, 63));
    }
}
