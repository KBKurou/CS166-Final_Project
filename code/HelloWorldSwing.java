import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.sql.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.text.*;

public class HelloWorldSwing {
    private static JFrame frame;
    private static Connection _connection;

    private static void createAndShowGUI() {
        frame = new JFrame("Flight Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(mainPagePanel());
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
    }

    private static int executeQuery(String query) throws SQLException {
        Statement stmt = _connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        int rowCount = 0;
        if (rs.next()) {
            rowCount++;
        }
        stmt.close();
        return rowCount;
    }

    private static List<List<String>> executeQueryAndReturnResult(String query) throws SQLException {
        Statement stmt = _connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        ResultSetMetaData rsmd = rs.getMetaData();
        int numCol = rsmd.getColumnCount();
        int rowCount = 0;

        boolean outputHeader = false;
        List<List<String>> result = new ArrayList<List<String>>();
        while (rs.next()) {
            List<String> record = new ArrayList<String>();
            for (int i = 1; i <= numCol; i++) {
                record.add(rs.getString(i));
            }
            result.add(record);
        }
        stmt.close();
        return result;
    }

    private static JButton[] mainPageButtons(JLabel errorLabel) {
        JButton[] buttons = new JButton[9];
        
        buttons[0] = new JButton("Add plane");
        buttons[0].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchPanel(addPlanePanel());
            }
        });

        buttons[1] = new JButton("Add pilot");
        buttons[1].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchPanel(addPilotPanel());
            }
        });

        buttons[2] = new JButton("Add flight");
        buttons[2].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchPanel(addFlightPanel());
            }
        });

        buttons[3] = new JButton("Add technician");
        buttons[3].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchPanel(addTechnicianPanel());
            }
        });

        buttons[4] = new JButton("Book flight");
        buttons[4].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchPanel(bookFlightPanel());
            }
        });

        buttons[5] = new JButton("List available seats of a flight");
        buttons[5].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchPanel(listSeatsPanel());
            }
        });

        buttons[6] = new JButton("List number of repairs per plane");
        buttons[6].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String query = "SELECT R.plane_id, COUNT(*) FROM Repairs R GROUP BY R.plane_id ORDER BY COUNT(*) DESC;";
                    if (executeQuery(query) == 0) {
                        errorLabel.setText("No planes with repairs.");
                        return;
                    }
                } catch (Exception exception) {
                    errorLabel.setText("No planes with repairs.");
                }
                switchPanel(listRepairsPerPlanePanel());
            }
        });

        buttons[7] = new JButton("List number of repairs per year");
        buttons[7].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String query = "SELECT temp.year, COUNT(*) FROM (SELECT date_part('year', R.repair_date) as year FROM Repairs R) as temp GROUP BY temp.year ORDER BY COUNT(*) ASC;";
                    if (executeQuery(query) == 0) {
                        errorLabel.setText("No planes with repairs.");
                        return;
                    }
                } catch (Exception exception) {
                    errorLabel.setText("No planes with repairs.");
                }
                switchPanel(listRepairsPerYearPanel());
            }
        });

        buttons[8] = new JButton("Get total number of passengers of a flight with given status");
        buttons[8].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchPanel(listNumPassengersPerPlaneStatus());
            }
        });
        
        return buttons; 
    }

    private static JPanel mainPagePanel() {
        JPanel panel = new JPanel();

        JLabel title = new JLabel("Welcome to Flight Application! Please select from the nine actions below.");
        JLabel errorLabel = new JLabel("");
        errorLabel.setForeground(Color.red);

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(title);
        panel.add(new JSeparator(SwingConstants.HORIZONTAL));
        JButton[] buttons = mainPageButtons(errorLabel);
        for (int i = 0; i < buttons.length; i++) {
            panel.add(buttons[i]);
        }
        panel.add(errorLabel);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        return panel;
    }

    // only use when panel is correctly setup as a gridbag layout with the correct gridbag constraints
    private static JTextField addInputToPanel(String label_string, JPanel panel, GridBagConstraints left, GridBagConstraints right) {
        JLabel label = new JLabel(label_string);
        JTextField textfield = new JTextField(20);
        panel.add(label, left);
        panel.add(textfield, right);

        return textfield;
    }

    private static void initInputPanel(JPanel panel, GridBagConstraints left, GridBagConstraints right) {
        panel.setLayout(new GridBagLayout());
        left.anchor = GridBagConstraints.EAST;
        right.weightx = 2.0;
        right.fill = GridBagConstraints.HORIZONTAL;
        right.gridwidth = GridBagConstraints.REMAINDER;
    }

    private static JPanel generateInputPage(String titleText, JPanel centerPanel, JButton submitButton, JLabel label) {
        JPanel rootPanel = new JPanel();
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));

        // Set titleText
        JLabel titleLabel = new JLabel(titleText);
        Font font = titleLabel.getFont();
        Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
        titleLabel.setFont(boldFont);

        // Set titlePanel
        JPanel titlePanel = new JPanel();
        titlePanel.add(backToMainButton());
        titlePanel.add(titleLabel);

        // Set submitPanel
        JPanel submitPanel = new JPanel();
        submitPanel.setLayout(new BoxLayout(submitPanel, BoxLayout.Y_AXIS));
        submitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        submitPanel.add(label);
        submitPanel.add(submitButton);
        submitPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        rootPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        rootPanel.add(titlePanel);
        rootPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        rootPanel.add(centerPanel);
        rootPanel.add(submitPanel);

        return rootPanel;
    }

    private static JPanel generateListPage(String titleText, String[] columnNames, Object[][] data) {
        JPanel rootPanel = new JPanel();
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));

        // Set titleText
        JLabel titleLabel = new JLabel(titleText);
        Font font = titleLabel.getFont();
        Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
        titleLabel.setFont(boldFont);

        // Set titlePanel
        JPanel titlePanel = new JPanel();
        titlePanel.add(backToMainButton());
        titlePanel.add(titleLabel);

        // Set scrollable table
        JTable table = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        table.setDefaultEditor(Object.class, null);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    
        rootPanel.add(titlePanel);
        rootPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        rootPanel.add(scrollPane);
        
        return rootPanel;
    }

    private static boolean isNonNegInt(String str) {
        return str.matches("\\d+");
    }

    private static boolean isPos(int v) {
        return v > 0;
    }

    private static boolean isEmpty(String s) {
        return s.trim().length() == 0;
    }
    
	private static boolean isDateString(String datev){
		try{
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
			Date mydate = fmt.parse(datev);
			if(datev.equals(fmt.format(mydate))){
				return true;
			} else{
				return false;
			}
		} catch(Exception e){
			return false;
		}
	}
	
	private static boolean isEarly(String arr, String dep){
		try{
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
			Date A = fmt.parse(arr);
			Date B = fmt.parse(arr);
			if(A.before(B)){
				return true;
			}	
			else{
				return false;
			}
		} catch(Exception e){
			return false;
		}
				
	}
	
    private static void displayError(JLabel label, String text) {
        label.setText(text);
        label.setForeground(Color.red);
    }

    private static void displaySuccess(JLabel label, String text) {
        label.setText(text);
        label.setForeground(Color.green);
    }

    private static JPanel addPlanePanel() {
        JPanel panel = new JPanel();
        GridBagConstraints left = new GridBagConstraints();
        GridBagConstraints right = new GridBagConstraints();
        initInputPanel(panel, left, right);

        JTextField id_tf = addInputToPanel("Plane id: ", panel, left, right);
        JTextField make_tf = addInputToPanel("Make: ", panel, left, right);
        JTextField model_tf = addInputToPanel("Model: ", panel, left, right);
        JTextField age_tf = addInputToPanel("Age: ", panel, left, right);
        JTextField seats_tf = addInputToPanel("Seats: ", panel, left, right);

        JLabel label = new JLabel();
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (!isNonNegInt(id_tf.getText())) {
                        displayError(label, "Error: plane id must be a non-negative integer.");
                        return;
                    } else if (isEmpty(make_tf.getText())) {
                        displayError(label, "Error: make cannot be empty.");
                        return;
                    } else if (isEmpty(model_tf.getText())) {
                        displayError(label, "Error: model cannot be empty.");
                        return;
                    } else if (!isNonNegInt(age_tf.getText())) {
                        displayError(label, "Error: age must be a non-negative integer. ");
                        return;
                    } else if (!isNonNegInt(seats_tf.getText()) || !isPos(Integer.parseInt(seats_tf.getText()))) {
                        displayError(label, "Error: seats must be a positive integer.");
                        return;
                    } else {
                        String query = "SELECT * FROM Plane P WHERE P.id = " + id_tf.getText() + ";";
                        if (executeQuery(query) != 0) {
                            displayError(label, "Error: plane id must be unique.");
                            return;
                        }
                    }

                    String query = "INSERT INTO Plane VALUES(" + id_tf.getText() + ",'" + make_tf.getText() + "','" + model_tf.getText() + "'," + age_tf.getText() + ", " + seats_tf.getText() + ");";
                    executeQuery(query);
                } catch (Exception exception) {
                    if (exception.getMessage().equals("No results were returned by the query.")) {
                        displaySuccess(label, "Success.");
                    } else {
                        displayError(label, exception.getMessage());
                    }
                }
            }
        });

        return generateInputPage("Add Plane", panel, submitButton, label);
    }

    private static JPanel addPilotPanel() {
        JPanel panel = new JPanel();
        GridBagConstraints left = new GridBagConstraints();
        GridBagConstraints right = new GridBagConstraints();
        initInputPanel(panel, left, right);

        JTextField id_tf = addInputToPanel("Pilot id: ", panel, left, right);
        JTextField name_tf = addInputToPanel("Full name: ", panel, left, right);
        JTextField nationality_tf = addInputToPanel("Nationality: ", panel, left, right);

        JLabel label = new JLabel();
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (!isNonNegInt(id_tf.getText()))  {
                        displayError(label, "Error: pilot id must be a non-negative integer.");
                        return;
                    } else if (isEmpty(name_tf.getText())) {
                        displayError(label, "Error: full name cannot be empty.");
                        return;
                    } else if (isEmpty(nationality_tf.getText())) {
                        displayError(label, "Error: nationality cannot be empty.");
                        return;
                    } else {
                        String query = "SELECT * FROM Pilot P WHERE P.id = " + id_tf.getText() + ";";
                        if (executeQuery(query) != 0) {
                            displayError(label, "Error: pilot id must be unique.");
                            return;
                        }
                    }
                    String query = "INSERT INTO Pilot VALUES (" + id_tf.getText() + ", '" + name_tf.getText() + "', '" + nationality_tf.getText() + "');";
                    executeQuery(query);
                } catch (Exception exception) {
                    if (exception.getMessage().equals("No results were returned by the query.")) {
                        displaySuccess(label, "Success.");
                    } else {
                        displayError(label, exception.getMessage());
                    }
                }
            }
        });

        return generateInputPage("Add Pilot", panel, submitButton, label);
    }

    private static JPanel addFlightPanel() {
        JPanel panel = new JPanel();
        GridBagConstraints left = new GridBagConstraints();
        GridBagConstraints right = new GridBagConstraints();
        initInputPanel(panel, left, right);

        JTextField fnum_tf = addInputToPanel("Flight number: ", panel, left, right);
        JTextField cost_tf = addInputToPanel("Cost: ", panel, left, right);
        JTextField num_sold_tf = addInputToPanel("Sold: ", panel, left, right);
        JTextField num_stops_tf = addInputToPanel("Stops: ", panel, left, right);
        JTextField actualDepDate_tf = addInputToPanel("Actual departure date: ", panel, left, right);
        JTextField actualArrDate_tf = addInputToPanel("Actual arrival date: ", panel, left, right);
        JTextField depAirport_tf = addInputToPanel("Departure airport: ", panel, left, right);
        JTextField arrAirport_tf = addInputToPanel("Arrival airport : ", panel, left, right);
        JTextField pilotid_tf = addInputToPanel("Pilot id: ", panel, left, right);
        JTextField planeid_tf = addInputToPanel("Plane id: ", panel, left, right);

        JLabel label = new JLabel();
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
					String query00 = "SELECT * FROM Pilot P WHERE P.id = " + pilotid_tf.getText() + ";";
					String query01 = "SELECT * FROM Plane P WHERE P.id = " + planeid_tf.getText() + ";";
					String query02 = "SELECT * FROM Flight F WHERE F.fnum = " + fnum_tf.getText() + ";";				
                    if (!isNonNegInt(fnum_tf.getText()))  {
                        displayError(label, "Error: flight number must be a non-negative integer.");
                        return;
                    } else if (executeQuery(query02) != 0) {
						displayError(label, "Error: flight number is not unique.");
						return;
                    } else if (!isNonNegInt(cost_tf.getText()) || !isPos(Integer.parseInt(cost_tf.getText()))) {
                        displayError(label, "Error: cost must be a positive integer.");
                        return;
                    } else if (!isNonNegInt(num_sold_tf.getText())) {
                        displayError(label, "Error: num_sold must be a non-negative integer.");
                        return;
					} else if (!isNonNegInt(num_stops_tf.getText())) {
						displayError(label, "Error: num_stops must be a non-negative integer.");
						return;
					} else if (!isDateString(actualDepDate_tf.getText())) {
                        displayError(label, "Error: depature date format is invalid.");
                        return;
                    } else if (!isDateString(actualArrDate_tf.getText())) {
                        displayError(label, "Error: arrive date format is invalid.");
                        return;
					} else if(isEarly(actualArrDate_tf.getText(), actualDepDate_tf.getText())){
						displayError(label, "Error: arrive date cannot be early than depature date.");
                        return;		
					} else if (depAirport_tf.getText().length() > 5 || isEmpty(depAirport_tf.getText())) {
                        displayError(label, "Error: airport name cannot longer than 5 letters and empty.");
                        return;
					} else if (arrAirport_tf.getText().length() > 5 || isEmpty(arrAirport_tf.getText())){
                        displayError(label, "Error: airport name cannot longer than 5 letters and empty.");
                        return;
					} else if (isEmpty(pilotid_tf.getText())) {
						displayError(label, "Error: pilot id cannot be empty.");
						return;
                    } else if (executeQuery(query00) == 0) {
						displayError(label, "Error: pilot id is not exist.");
						return;
                    } else if (isEmpty(planeid_tf.getText())) {
						displayError(label, "Error: plane id cannot be empty.");
						return;
                    } else if (executeQuery(query01) == 0) {
						displayError(label, "Error: plane id is not exist.");
						return;
                    } 
					
                    // Get the next id for FlightInfo
					String query1 = "SELECT MAX(fiid) FROM FlightInfo";
					List<List<String>> result1 = executeQueryAndReturnResult(query1);
					int newfiid = Integer.parseInt(result1.get(0).get(0)) + 1;
					
					String query2 = "INSERT INTO Flight VALUES (" + fnum_tf.getText() + ", " + cost_tf.getText() + ", " + num_sold_tf.getText() + ", " + num_stops_tf.getText() + ", '" + actualDepDate_tf.getText() + "', '" + actualArrDate_tf.getText() + "', '" + arrAirport_tf.getText() + "', '" + depAirport_tf.getText() + "');";
					query2 += "INSERT INTO FlightInfo VALUES (" + newfiid + ", " + fnum_tf.getText() + ", " + pilotid_tf.getText() + ", " + planeid_tf.getText() + ");";
					executeQuery(query2); 
					
                } catch (Exception exception) {
                    if (exception.getMessage().equals("No results were returned by the query.")) {
                        displaySuccess(label, "Success.");
                    } else {
                        displayError(label, exception.getMessage());
						System.out.println(exception.getMessage());
                    }
                }
            }
        });

        return generateInputPage("Add Flight", panel, submitButton, label);
    }

    private static JPanel addTechnicianPanel() {
        JPanel panel = new JPanel();
        GridBagConstraints left = new GridBagConstraints();
        GridBagConstraints right = new GridBagConstraints();
        initInputPanel(panel, left, right);

        JTextField tid_tf = addInputToPanel("Technician id: ", panel, left, right);
        JTextField fullname_tf = addInputToPanel("Full name: ", panel, left, right);

        JLabel label = new JLabel();
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
					String query0 = "SELECT * FROM Technician T WHERE T.id = " + tid_tf.getText() + ";";
					if (!isNonNegInt(tid_tf.getText()))  {
                        displayError(label, "Error: technician id must be a non-negative integer.");
                        return;
                    } else if (executeQuery(query0) != 0) {
						displayError(label, "Error: technician id is not unique.");
						return;
					} else if (isEmpty(fullname_tf.getText())) {
						displayError(label, "Error: technician full name cannot be empty.");
						return;
                    }
					String query1 = "INSERT INTO Technician VALUES (" + tid_tf.getText() + ", '" + fullname_tf.getText() + "');";
					executeQuery(query1); 
				}catch (Exception exception) {
                    if (exception.getMessage().equals("No results were returned by the query.")) {
                        displaySuccess(label, "Success.");
                    } else {
                        displayError(label, exception.getMessage());
						System.out.println(exception.getMessage());
                    }
                }
            }
        });

        return generateInputPage("Add Technician", panel, submitButton, label);
    }

    private static JPanel bookFlightPanel() {
        JPanel panel = new JPanel();
        GridBagConstraints left = new GridBagConstraints();
        GridBagConstraints right = new GridBagConstraints();
        initInputPanel(panel, left, right);

        JTextField cid_tf = addInputToPanel("Customer id: ", panel, left, right);
        JTextField fid_tf = addInputToPanel("Flight id: ", panel, left, right);

        JLabel label = new JLabel();
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					String currentDate = dateFormat.format(new Date());
					String query01 = "SELECT * FROM Customer C WHERE C.id = " + cid_tf.getText() + ";";
					String query02 = "SELECT * FROM Flight F WHERE F.fnum = " + fid_tf.getText() + ";";
					String query03 = "SELECT * FROM Reservation R WHERE R.cid = " + cid_tf.getText() + " AND R.fid = " + fid_tf.getText() + ";";
					String query04 = "SELECT * FROM Flight F WHERE F.fnum = " + fid_tf.getText() + " AND '" + currentDate + "' < F.actual_departure_date;";
					if (isEmpty(cid_tf.getText())){
						displayError(label, "Error: customer id cannot be empty.");
                        return;	
					} else if (executeQuery(query01) == 0)  {
                        displayError(label, "Error: customer id does not exist.");
                        return;
                    } else if (isEmpty(fid_tf.getText()))  {
                        displayError(label, "Error: flight id cannot be empty.");
                        return;
                    } else if (executeQuery(query02) == 0) {
                        displayError(label, "Error: flight id does not exist.");
                        return;
                    } else if (executeQuery(query03) != 0) {
						displayError(label, "Error: User already has a reservation for the specified flight.");
						return;
					} else if (executeQuery(query04) == 0) {
						displayError(label, "Error: cannot make late reservation.");
						return;
                    }
					String query1 = "SELECT MAX(rnum) FROM Reservation";
					List<List<String>> result1 = executeQueryAndReturnResult(query1); 
					int newRnum = Integer.parseInt(result1.get(0).get(0)) + 1;
					
					String query2 = "SELECT * FROM Flight F, Plane P, FlightInfo FI WHERE FI.flight_id = " + fid_tf.getText() + " AND FI.flight_id = F.fnum AND FI.plane_id = P.id AND F.num_sold < P.seats";
					if (executeQuery(query2) == 0) { // Add into waitlist.
						String query3 = "INSERT INTO Reservation VALUES (" + newRnum + ", " + cid_tf.getText() + ", " + fid_tf.getText() + ", 'W');";
						executeQuery(query3); 
					} else { // Add into reserved. Increment num_sold.
						String query4 = "SELECT F.num_sold FROM Flight F WHERE F.fnum = " + fid_tf.getText() + ";";
						List<List<String>> result4 = executeQueryAndReturnResult(query4);
						int newNumSold = Integer.parseInt(result4.get(0).get(0)) + 1;

						String query5 = "INSERT INTO Reservation VALUES (" + newRnum + ", " + cid_tf.getText() + ", " + fid_tf.getText() + ", 'R');";
						query5 += "UPDATE Flight SET num_sold = " + newNumSold + " WHERE fnum = " + fid_tf.getText() + ";";
						executeQuery(query5);
					}
					
				}catch (Exception exception) {
                    if (exception.getMessage().equals("No results were returned by the query.")) {
                        displaySuccess(label, "Success.");
                    } else {
                        displayError(label, exception.getMessage());
						System.out.println(exception.getMessage());
                    }
                }
            }
        });

        return generateInputPage("Book Flight", panel, submitButton, label);
    }

    private static JPanel listSeatsPanel() {
        JPanel panel = new JPanel();
        GridBagConstraints left = new GridBagConstraints();
        GridBagConstraints right = new GridBagConstraints();
        initInputPanel(panel, left, right);

        JTextField fNum_tf = addInputToPanel("Flight number: ", panel, left, right);

        JLabel label = new JLabel();
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (!isNonNegInt(fNum_tf.getText())) {
                        displayError(label, "Error: flight number must be a non-negative integer.");
                        return;
                    } else {
                        String query = "SELECT * FROM Flight F WHERE F.fnum = " + fNum_tf.getText() + ";";
                        if (executeQuery(query) == 0) {
                            displayError(label, "Error: flight with the specified number does not exist.");
                            return;
                        }
                    }

                    String query1 = "SELECT F.num_sold FROM Flight F WHERE F.fnum = " + fNum_tf.getText() + ";";
                    List<List<String>> result1 = executeQueryAndReturnResult(query1);
                    int numSold = Integer.parseInt(result1.get(0).get(0));
        
                    String query2 = "SELECT P.seats FROM Plane P, FlightInfo FI WHERE FI.flight_id = " + fNum_tf.getText() + " AND FI.plane_id = P.id;";
                    List<List<String>> result2 = executeQueryAndReturnResult(query2);
                    int totalSeats = Integer.parseInt(result2.get(0).get(0));

                    displaySuccess(label, "Available number of seats: " + (totalSeats - numSold));
                } catch (Exception exception) {
                    displayError(label, exception.getMessage());
                }
            }
        });

        return generateInputPage("List Number of Available Seats", panel, submitButton, label);
    }

    private static JPanel listRepairsPerPlanePanel() {
        JPanel panel = new JPanel();
        panel.add(backToMainButton());

        try {
            String query = "SELECT R.plane_id, COUNT(*) FROM Repairs R GROUP BY R.plane_id ORDER BY COUNT(*) DESC";
            List<List<String>> result = executeQueryAndReturnResult(query);

            String[] columnNames = {"Plane id", "# of repairs"};
            Object[][] data = new Object[result.size()][];
            for (int i = 0; i < data.length; i++) {
                data[i] = result.get(i).toArray();
            }
          
            return generateListPage("List of planes in decreasing order of number of years that have been made on the planes.", columnNames, data);
        } catch (Exception e) {
            // Exception handled before this function.
            return new JPanel();      
        }
    }

    private static JPanel listRepairsPerYearPanel() {
        try {
            String query = "SELECT temp.year, COUNT(*) FROM (SELECT date_part('year', R.repair_date) as year FROM Repairs R) as temp GROUP BY temp.year ORDER BY COUNT(*) ASC;";
            List<List<String>> result = executeQueryAndReturnResult(query);

            String[] columnNames = {"Year", "# of repairs"};
            Object[][] data = new Object[result.size()][];
            for (int i = 0; i < data.length; i++) {
                data[i] = result.get(i).toArray();
            }

            return generateListPage("Repairs made per year in ascending order.", columnNames, data);
        } catch (Exception e) {
            // Exception handled before this function.
            return new JPanel();
        }
    }

    private static boolean validPassengerStatus(String status) {
        return status.equals("W") || status.equals("C") || status.equals("R");
    }

    private static JPanel listNumPassengersPerPlaneStatus() {
        JPanel panel = new JPanel();
        GridBagConstraints left = new GridBagConstraints();
        GridBagConstraints right = new GridBagConstraints();
        initInputPanel(panel, left, right);

        JTextField flightNumber_tf = addInputToPanel("Flight number: ", panel, left, right);
        JTextField passengerStatus_tf = addInputToPanel("Passenger status: ", panel, left, right);

        JLabel label = new JLabel();
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (!isNonNegInt(flightNumber_tf.getText())) {
                        displayError(label, "Error: flight number must be a non-negative integer.");
                        return;
                    } else if (!validPassengerStatus(passengerStatus_tf.getText())) {
                        displayError(label, "Error: reservation status must either be W, C, or R");
                        return;
                    } else {
                        String query = "SELECT * FROM Flight F WHERE F.fnum = " + flightNumber_tf.getText() + ";";
                        if (executeQuery(query) == 0) {
                            displayError(label, "Error: flight with the specified number does not exist.");
                            return;
                        }
                    }

                    String query = "SELECT * FROM Reservation R WHERE R.fid = " + flightNumber_tf + " AND R.status = '" + passengerStatus_tf + "';";
                    int result = executeQuery(query);
                    displaySuccess(label, "Number passengers with status: " + result);
                } catch (Exception exception) {
                    displayError(label, exception.getMessage());
                }
            }
        });

        return generateInputPage("Number of Passenger of Flight With Status", panel, submitButton, label);
    }

    private static void switchPanel(JPanel panel) {
        frame.setContentPane(panel);
        frame.invalidate();
        frame.validate();
        frame.pack();
    }

    private static JButton backToMainButton() {
        JButton button = new JButton("Back");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchPanel(mainPagePanel());
            }
        });

        return button;
    }

    private static void connectToDB(String dbname, String dbport, String user, String passwd) {
        System.out.println("Connecting to database...");
        try {
            String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
            System.out.println("Connection URL: " + url);
            
            _connection = DriverManager.getConnection(url, user, passwd);
            System.out.println("Done. Start GUI.");
        } catch (Exception e) {
            System.err.println("Error - Unable to connect to database: " + e.getMessage());
            System.out.println("Make sure you started postgres on this machine");
            System.exit(-1);
        }
    }

    public static void main(String[] args) {

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (args.length != 3) {
                    System.err.println(
                        "Usage: " + "java [-classpath <classpath>] " + HelloWorldSwing.class.getName() + " <dbname> <port> <user>");
                    return;
                }
                
                String dbname = args[0];
                String dbport = args[1];
                String user = args[2];
                            
                connectToDB(dbname, dbport, user, "");
                createAndShowGUI();
            }
        });
    }
}
