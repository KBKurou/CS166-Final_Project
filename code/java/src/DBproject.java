/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.text.*;


/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class DBproject{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + DBproject.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		DBproject esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new DBproject (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Plane");
				System.out.println("2. Add Pilot");
				System.out.println("3. Add Flight");
				System.out.println("4. Add Technician");
				System.out.println("5. Book Flight");
				System.out.println("6. List number of available seats for a given flight.");
				System.out.println("7. List total number of repairs per plane in descending order");
				System.out.println("8. List total number of repairs per year in ascending order");
				System.out.println("9. Find total number of passengers with a given status");
				System.out.println("10. < EXIT");
				
				switch (readChoice()){
					case 1: AddPlane(esql); break;
					case 2: AddPilot(esql); break;
					case 3: AddFlight(esql); break;
					case 4: AddTechnician(esql); break;
					case 5: BookFlight(esql); break;
					case 6: ListNumberOfAvailableSeats(esql); break;
					case 7: ListsTotalNumberOfRepairsPerPlane(esql); break;
					case 8: ListTotalNumberOfRepairsPerYear(esql); break;
					case 9: FindPassengersCountWithStatus(esql); break;
					case 10: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	public static void AddPlane(DBproject esql) {//1
        try {
            String id, make, model, age, seats;

            System.out.print("Please enter the id: ");
            id = in.readLine();

            System.out.print("Please enter the make: ");
            make = in.readLine();

            System.out.print("Please enter the model: ");
            model = in.readLine();

            System.out.print("Please enter the age: ");
            age = in.readLine();

            System.out.print("Please enter the seats: ");
            seats = in.readLine();

            String query = "INSERT INTO Plane VALUES (" + id + ", '" + make + "','"  + model + "'," + age + ", " + seats + ");";
            esql.executeQuery(query); 

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
	}

	public static void AddPilot(DBproject esql) {//2
        try {
            String id, fullname, nationality;

            System.out.print("Please enter the id: ");
            id = in.readLine();

            System.out.print("Please enter the fullname: ");
            fullname = in.readLine();

            System.out.print("Please enter the nationality: ");
            nationality = in.readLine();

            String query = "INSERT INTO Pilot VALUES (" + id + ", '" + fullname + "', '" + nationality + "');";
            esql.executeQuery(query);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
	}

	public static void AddFlight(DBproject esql) {//3
        try {
            String fnum, cost, num_sold, num_stops, actual_departure_date, actual_arrival_date, arrival_airport, departure_airport, pilot_id, plane_id;

            System.out.print("Please enter the fnum: ");
            fnum = in.readLine();
            System.out.print("Please enter the cost: ");
            cost = in.readLine();
            System.out.print("Please enter the number sold: ");
            num_sold = in.readLine();
            System.out.print("Please enter the number of stops: ");
            num_stops = in.readLine();
            System.out.print("Please enter the actual departure date: ");
            actual_departure_date = in.readLine();
            System.out.print("Please enter the actual arrival date: ");
            actual_arrival_date = in.readLine();
            System.out.print("Please enter the arrival airport: ");
            arrival_airport = in.readLine();
            System.out.print("Please enter the departure airport: ");
            departure_airport = in.readLine();
            System.out.print("Please enter the pilot id: ");
            pilot_id = in.readLine();
            System.out.print("Please enter the plane id: ");
            plane_id = in.readLine();

            // also need a check for the date correct format
            
            // Check if pilot and plane id are invalid
            String query1 = "SELECT * FROM Pilot P WHERE P.id = " + pilot_id + ";";
            String query2 = "SELECT * FROM Plane P WHERE P.id = " + plane_id + ";";

            int result1 = esql.executeQuery(query1);
            int result2 = esql.executeQuery(query2);
            if (result1 == 0 || result2 == 0) {
                System.out.println("Error: invalid plane or pilot id.");
                return;
            }

            // Get the next id for FlightInfo
            String query3 = "SELECT MAX(fiid) FROM FlightInfo";
            List<List<String>> result3 = esql.executeQueryAndReturnResult(query3);
            int newfiid = Integer.parseInt(result3.get(0).get(0)) + 1;


            String query = "INSERT INTO Flight VALUES (" + fnum + ", " + cost + ", " + num_sold + ", " + num_stops + ", '" + actual_departure_date + "', '" + actual_arrival_date + "', '" + arrival_airport + "', '" + departure_airport + "');";
            query += "INSERT INTO FlightInfo VALUES (" + newfiid + ", " + fnum + ", " + pilot_id + ", " + plane_id + ");";
            esql.executeQuery(query); 
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        
	}

	public static void AddTechnician(DBproject esql) {//4
        try {
            String id, full_name;
            
            System.out.print("Please enter the id: ");
            id = in.readLine();
            System.out.print("Plwas enter the full name: ");
            full_name = in.readLine();

            String query = "INSERT INTO Technician VALUES (" + id + ", '" + full_name + "');"; 
            esql.executeQuery(query);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
	}

	public static void BookFlight(DBproject esql) {//5
		// Given a customer and a flight that he/she wants to book, add a reservation to the DB
        try {
            String cid, fid;

            System.out.print("Please enter the customer id: ");
            cid = in.readLine();
            System.out.print("Please enter the flight id: ");
            fid = in.readLine();

            // Step 0: Check if customer id is valid and flight id is valid
            String query01 = "SELECT * FROM Customer C WHERE C.id = " + cid + ";";
            int result01 = esql.executeQuery(query01);
            String query02 = "SELECT * FROM Flight F WHERE F.fnum = " + fid + ";";
            int result02 = esql.executeQuery(query02);

            if (result01 == 0 || result02 == 0) {
                System.out.println("Error: Invalid customer or flight id.");
                return;
            }
                           
            // Step 1: Check if user already has a reservation for the specified flight.
            String query1 = "SELECT * FROM Reservation R WHERE R.cid = " + cid + " AND R.fid = " + fid + ";";
            int result1 = esql.executeQuery(query1);
            if (result1 != 0) {
                System.out.println("Error: User already has a reservation for the specified flight.");
                return;
            }
            
            // Step 2: Check that the reservation is for a future flight, not a previous or same day flight.
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String currentDate = dateFormat.format(new Date());
            String query2 = "SELECT * FROM Flight F WHERE F.fnum = " + fid + " AND '" + currentDate + "' < F.actual_departure_date;";
            int result2 = esql.executeQuery(query2);
            if (result2 == 0) {
                System.out.println("Error: The current date is not less than the departure date.");
                return;
            }

            // Step 3: Get next Rnum
            String query3 = "SELECT MAX(rnum) FROM Reservation";
            List<List<String>> result3 = esql.executeQueryAndReturnResult(query3);
            int newRnum = Integer.parseInt(result3.get(0).get(0)) + 1;

            // Step 4: Check whether there are seats available and create entry in Reservation table
            // SELECT *
            // FROM Flight F, Plane P, FlightInfo FI
            // WHERE F.fnum = fid AND F.fnum = FI.flight_id AND FI.plane_id = P.id AND F.num_sold < P.seats
            String query4 = "SELECT * FROM Flight F, Plane P, FlightInfo FI WHERE FI.flight_id = " + fid + " AND FI.flight_id = F.fnum AND FI.plane_id = P.id AND F.num_sold < P.seats";
            int result4 = esql.executeQuery(query4);
            if (result4 == 0) { // Add into waitlist.
                String query5 = "INSERT INTO Reservation VALUES (" + newRnum + ", " + cid + ", " + fid + ", 'W');";
                esql.executeQuery(query5); 
            } else { // Add into reserved. Increment num_sold.
                String query5 = "SELECT F.num_sold FROM Flight F WHERE F.fnum = " + fid + ";";
                List<List<String>> result5 = esql.executeQueryAndReturnResult(query5);
                int newNumSold = Integer.parseInt(result5.get(0).get(0)) + 1;

                String query6 = "INSERT INTO Reservation VALUES (" + newRnum + ", " + cid + ", " + fid + ", 'R');";
                query6 += "UPDATE Flight SET num_sold = " + newNumSold + " WHERE fnum = " + fid + ";";
                esql.executeQuery(query6);
            } 
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
	}

	public static void ListNumberOfAvailableSeats(DBproject esql) {//6
        // ask ta why we need depaturedate. did it without the departuredate for now.
        try {
            String flightNum, departureDate;
            flightNum = in.readLine();
            departureDate = in.readLine();

            // Check to see if flightNum is valid.

            String query1 = "SELECT F.num_sold FROM Flight F WHERE F.fnum = " + flightNum + ";";
            List<List<String>> result1 = esql.executeQueryAndReturnResult(query1);
            int numSold = Integer.parseInt(result1.get(0).get(0));

            String query2 = "SELECT P.seats FROM Plane P, FlightInfo FI WHERE FI.flight_id = " + flightNum + " AND FI.plane_id = P.id;";
            List<List<String>> result2 = esql.executeQueryAndReturnResult(query2);
            int totalSeats = Integer.parseInt(result2.get(0).get(0));

            System.out.println("Available number of seats: " + (totalSeats - numSold));

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
	}

	public static void ListsTotalNumberOfRepairsPerPlane(DBproject esql) {//7
		// Count number of repairs per planes and list them in descending order
        try {
            String query = "SELECT R.plane_id, COUNT(*) FROM Repairs R GROUP BY R.plane_id ORDER BY COUNT(*) DESC";

            List<List<String>> result = esql.executeQueryAndReturnResult(query);
            for (int i = 0; i < result.size(); i++) {
                String plane = result.get(i).get(0);
                String count = result.get(i).get(1);
                System.out.println(plane + ", " + count);
            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
	}

	public static void ListTotalNumberOfRepairsPerYear(DBproject esql) {//8
		// Count repairs per year and list them in ascending order
        // 
        // SELECT temp.year, COUNT(*)
        // FROM (SELECT date_part('year', R.repair_date) FROM Repairs R) as temp
        // GROUP BY temp.year
        //
        try {
            String query = "SELECT temp.year, COUNT(*) FROM (SELECT date_part('year', R.repair_date) as year FROM Repairs R) as temp GROUP BY temp.year ORDER BY COUNT(*) ASC;";

            List<List<String>> result = esql.executeQueryAndReturnResult(query);
            for (int i = 0; i < result.size(); i++) {
                String year = result.get(i).get(0);
                String count = result.get(i).get(1);
                System.out.println(year + ", " + count);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
	}
	
	public static void FindPassengersCountWithStatus(DBproject esql) {//9
		// Find how many passengers there are with a status (i.e. W,C,R) and list that number.
        //
        // SELECT *
        // FROM Reservation R
        // WHERE R.fid = flightNum AND R.status = 'reservationStatus'
        // 
        try {
            String flightNum, reservationStatus;
            flightNum = in.readLine();
            reservationStatus = in.readLine();

            String query = "SELECT * FROM Reservation R WHERE R.fid = " + flightNum + " AND R.status = '" + reservationStatus + "';";
            int result = esql.executeQuery(query);
            System.out.println("Number passengers with status: " + result);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
	}
}
