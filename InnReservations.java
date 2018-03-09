/*
* @author Anna Rulloda and Makenna Johnstone
* @date 3/5/18
*/

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Scanner;

public class InnReservations {
  public Connection conn;

  public static void main (String[] args) {
    InnReservations innRes = new InnReservations();
    innRes.establishDBConnection();
    innRes.promptUserInput();
  }

//Establish connection to RDBMS
  public void establishDBConnection() {
    try {
      String jdbcUrl = System.getenv("APP_JDBC_URL");
      String dbUsername = System.getenv("APP_JDBC_USER");
      String dbPassword =  System.getenv("APP_JDBC_PW");
      conn = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);
      System.out.println("Connection established");
    }
    catch (SQLException e) {
      System.out.println("SQLException : " + e.getMessage());
    }
  }

  public void promptUserInput() {
    Scanner sc = new Scanner(System.in);
    String [] userInput;
    System.out.println("Please choose from the following queries: ");
    System.out.println("[Rooms] : to view a list of rooms sorted by popularity");
    System.out.println("[Reservations] : to view a numbered list of matching rooms available for booking");
    System.out.println("[Revenue] : to view a month-by-month overview of revenue for an entire year \n");

    while (sc.hasNext()) {
      userInput = sc.nextLine().split(" ");
      switch (userInput[0]) {
        case "Rooms":
          roomsQuery();
          break;
        case "Reservations":
            reservationsQuery();
            break;
        case "Revenue":
            System.out.println("Revenue query selected");
            break;
        case "Quit":
            System.exit(0);
        default:
            System.out.println("Unrecognized query, please try again.\n");
            break;
      }
      System.out.println("Choose another database query: \n");
    }
  }

  //Outputs a list of rooms sorted by popularity
  private void roomsQuery() {
    //Construct a SQL Statement
    String query = "select * from mejohnst.lab6_rooms";
    //send query
    ResultSet rs = null;
    try {
      rs = sendQuery(query);
      //output results
      while (rs.next()) {
        String roomCode = rs.getString("RoomCode");
        String roomName = rs.getString("RoomName");
        int beds = rs.getInt("Beds");
        String bedType = rs.getString("bedType");
        int maxOcc = rs.getInt("maxOcc");
        double basePrice = rs.getDouble("basePrice");
        String decor = rs.getString("decor");

        System.out.format("%s %s %d %s %d %.2f %s %n", roomCode, roomName, beds,
          bedType, maxOcc, basePrice, decor);
      }
    }
    catch (SQLException e) {
      System.out.println("SQLException: " + e.getMessage());
    }
  }

  private ResultSet sendQuery(String query) throws SQLException {
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery(query);

    return rs;
  }

  private void reservationsQuery() {
    String [] userInput = promptForReservationInfo();
    String firstName = userInput[0];
    String lastName = userInput[1];
    String roomCode = userInput[2];
    String bedType = userInput[3];
    String checkIn = userInput[4];
    String checkOut = userInput[5];
    String numChildren = userInput[6];
    String numAdults = userInput[7];

    String query = createMatchingRoomsQuery(roomCode, bedType, checkIn, checkOut, numChildren, numAdults);
    ResultSet rs = null;
    ArrayList<ResultSet> matchingRooms = new ArryaList<ResultSet>();

    try {
      rs = sendQuery(query);
      //There were no matching rooms.
      if (rs == null) {
        suggestSimilarRooms();
      }
      else {
        int count = 1;
        while (rs.next()) {
          //place each resultSet into a spot in the array starting at spot 1.
          matchingRooms.add(count, rs);
          String roomCode = rs.getString("RoomCode");
          System.out.println(count + ". " + roomCode);
          count++;
        }
      }

      System.out.println("To book a room, enter it's corresponding option number");
      System.out.println("To cancel the current request, enter 'Cancel'");
      Scanner sc = new Scanner(System.in);

      while (sc.hasNext()) {
        if (sc.next().equals("Cancel")) {
          //return back to the main menu
        }
        //the user enters an option number
        else {
          int optionNum = sc.nextInt();
          //check to see if that number is in range
          //if it is in range, then provide a confirmation screen
          if (optionNum <= matcingRooms.size() && optionNum > 0) {
            ResultSet chosenRoom = matchingRooms.get(optionNum);
            String roomCode = chosenRoom.getString("RoomCode");
            String roomName = chosenRoom.getString("RoomName");
            String bedType = chosenRoom.getString("bedType");
            confirmReservation(roomCode, roomName, bedType, firstName, lastName, numAdults, numChildren);
          }
          else {
            System.out.println("Please choose a valid option number.");
          }
        }
      }
    }
    catch (SQLException e) {
      System.out.println("SQLException: " + e.getMessage());
    }

  }

  private String[] promptForReservationInfo() {
    String [] userInput = new String[8];
    Scanner sc = new Scanner(System.in);
    System.out.println("Please enter the following information: ");

    System.out.print("First name: ");
    String firstName = sc.next();
    userInput[0] = firstName;

    System.out.print("\nLast name: ");
    String lastName = sc.next();
    userInput[1] = lastName;

    System.out.println("\nRoom code: [Type 'Any' to indicate no preference]");
    String roomCode = sc.next();
    userInput[2] = roomCode;

    System.out.println("\nBed type: [Type 'Any' to indicate no preference]");
    String bedType = sc.next();
    userInput[3] = bedType;

    System.out.println("\nDesired check-in date: [YYYY-MM-DD]");
    String checkIn = sc.next();
    userInput[4] = checkIn;

    System.out.println("\nDesired check-out date: [YYYY-MM-DD]");
    String checkOut = sc.next();
    userInput[5] = checkOut;

    System.out.println("\nNumber of children: ");
    String numChildren = sc.next();
    userInput[6] = numChildren;

    System.out.println("\nNumber of adults: \n");
    String numAdults = sc.next();
    userInput[7] = numAdults;

    return userInput;
  }
  private String createMatchingRoomsQuery(String roomCode, String bedType, String checkIn,
    String checkOut, String numChildren, String numAdults) {

    String roomCodeStatement = "";
    String bedTypeStatement = "";
    int numOccupants = Integer.parseInt(numChildren) + Integer.parseInt(numAdults);

    if (!(roomCode.equals("Any"))) {
      roomCodeStatement = "RoomCode = '" + roomCode + "' and ";
    }
    if (!(bedType.equals("Any"))) {
      bedTypeStatement = "bedType = '"  + bedType + "' and ";
    }

    return
      "select * " +
      " from mejohnst.lab6_rooms " +
      "   join mejohnst.lab6_reservations on RoomCode = Room"
      " where " + roomCodeStatement + bedTypeStatement +
      "   maxOcc >= " + numOccupants +
      ";"
  }
  private void suggestSimilarRooms(String roomCode String bedType, String checkIn, String checkOut, String numChildren, String numAdults) {
    System.out.println("There were no rooms found matching your original search.");
    System.out.println("However, may we suggest the following rooms with nearby availablity and/or similar features and decor: ");

    String suggestedBed = "";
    int numOccupants = Integer.parseInt(numChildren) + Integer.parseInt(numAdults);

    if (bedType.equals("Any")) {
      //QUESTION: make a suggestion of a bed type based on maxOccupancy

    }
    else if (bedType.equals("King")) {
        suggestedBed = "Queen";
    }
    else if (bedType.equals("Queen")) {
      suggestedBed = "King";
    }
    else {
      suggestedBed = "Double";
    }
  }
  private void confirmReservation(String roomCode, String roomName, String bedType, String firstName, String lastName, String numAdults, String numChildren) {

    System.out.println("Your reservation is comfirmed!");
    System.out.println(firstName + " " + lastName);
    System.out.println(roomCode + ", " + roomName + ", " + bedType);
    System.out.println("CheckIn: " + checkIn + "      " + "CheckOut: " + checkOut);
    System.out.println("Number of adults: " + numAdults + "     " + "Number of children: " + numChildren);
    System.out.println("Total cost: " + calculateTotalCost(checkIn, checkOut);
    //number of weekdays * room base rate
    //number of weekend days
    //an 18% tourism tax applied to the total oft
  }
  private double calculateTotalCost(String checkIn, String checkOut) {
    DateFormat dfCheckIn = new SimpleDateFormat("YYYY-MM-DD");
    Date result = df.parse(dfCheckIn); 
    //find the number of week days
    //find the number of weekend days

  }
}
