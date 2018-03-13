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

import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.concurrent.TimeUnit;
import RoomsQuery;

public class InnReservations {
  public Connection conn;
  public int reservationCode = 100000;

  public static void main (String[] args) {
    InnReservations innRes = new InnReservations();
    innRes.establishDBConnection();
    innRes.run();
  }

  //Establish connection to RDBMS
  private void establishDBConnection() {
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

  public void run() {
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
          RoomsQuery.roomsQuery();
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
    //createRoomsQuery();
    //String query = createRoomsQuery();
    String query = "select * from mejohnst.lab6_rooms";
    //send query
    ResultSet rs = null;
    try {
      rs = sendQuery(query);
      printRoomResults(rs);

    }
    catch (SQLException e) {
      System.out.println("SQLException: " + e.getMessage());
    }
  }
  private void printRoomResults(ResultSet rs) throws SQLException {
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

    //create query
    String query = createReservationsQuery(roomCode, bedType, checkIn, checkOut, numChildren, numAdults);
    ResultSet rs = null;

    try {
      //if there are no rows in the method.
      int rowCount = 0;
      rs = sendQuery(query);
      rs.last();
      rowCount = rs.getRow();
      if (rowCount == 0) {
        suggestSimilarRooms(roomCode, bedType, checkIn, checkOut, numChildren, numAdults);
      }
      else {
        int count = 1;
        rs.beforeFirst();
        System.out.println("\nThe following rooms matching your reservation request: ");
        while (rs.next()) {
          roomCode = rs.getString("RoomCode");
          System.out.println(count + ". " + roomCode);
          count++;
        }
      }

      //book or cancel options
      System.out.println("\nTo book a room, type it's corresponding option number");
      System.out.println("To cancel the current request, type 'Cancel'");
      Scanner sc = new Scanner(System.in);

      while (sc.hasNext()) {
        String userChoice = sc.next();
        String roomName = "";
        double basePrice = 0, totalCost = 0, rate = 0;

        if (userChoice.equals("Cancel")) {
          //return back to the main menu
          promptForReservationInfo();
        }
        else if (userChoice.equals("Confirm")) {
          //create an entry in the lab6_reservations table
          rate = calculateRate(checkIn, checkOut, totalCost);
          confirmReservation(roomCode, checkIn, checkOut, rate, lastName, firstName, numAdults, numChildren);
        }
        //the user enters an option number
        else {
          int optionNum = Integer.parseInt(userChoice);
          //set the cursor to point to the user specified reservation
          rs.absolute(optionNum);
          roomCode = rs.getString("RoomCode");
          roomName = rs.getString("RoomName");
          bedType = rs.getString("bedType");
          basePrice = rs.getDouble("basePrice");
          totalCost = calculateTotalCost(checkIn, checkOut, basePrice);
          outputReservationDetails(roomCode, roomName, bedType, checkIn, checkOut, firstName, lastName, numAdults, numChildren, totalCost);
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
    System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~ A&M Bed and Breakfast Booking System ~~~~~~~~~~~~~~~~~~~~~~~~");
    System.out.println("\nPlease enter the following information: ");

    System.out.print("First name: ");
    String firstName = sc.next();
    userInput[0] = firstName;

    System.out.print("Last name: ");
    String lastName = sc.next();
    userInput[1] = lastName;

    System.out.print("Room code [Type 'Any' to indicate no preference]: ");
    String roomCode = sc.next();
    userInput[2] = roomCode;

    System.out.print("Bed type [Type 'Any' to indicate no preference]: ");
    String bedType = sc.next();
    userInput[3] = bedType;

    System.out.print("Desired check-in date [YYYY-MM-DD]: ");
    String checkIn = sc.next();
    userInput[4] = checkIn;

    System.out.print("Desired check-out date [YYYY-MM-DD]: ");
    String checkOut = sc.next();
    userInput[5] = checkOut;

    System.out.print("Number of children: ");
    String numChildren = sc.next();
    userInput[6] = numChildren;

    System.out.print("Number of adults: ");
    String numAdults = sc.next();
    userInput[7] = numAdults;

    return userInput;
  }
  private String createReservationsQuery(String roomCode, String bedType, String checkIn,
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
      "select distinct RoomCode, RoomName, bedType, basePrice" +
      " from mejohnst.lab6_rooms " +
      "   join mejohnst.lab6_reservations on RoomCode = Room" +
      " where " + roomCodeStatement + bedTypeStatement +
      " CheckIn = '" + checkIn + "' and " +
      " Checkout = '" + checkOut + "' and " +
      "   maxOcc >= " + numOccupants +
      ";"
      ;
  }
  private void suggestSimilarRooms(String roomCode, String bedType, String checkIn, String checkOut, String numChildren, String numAdults) {
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
  private void outputReservationDetails(String roomCode, String roomName, String bedType, String checkIn, String checkOut, String firstName, String lastName, String numAdults, String numChildren, double totalCost) {

    System.out.println("\nYour reservation details");
    System.out.println("Name: " + firstName + " " + lastName);
    System.out.println("Room: " + roomCode + ", " + roomName);
    System.out.println(bedType + " bed");
    System.out.println("CheckIn: " + checkIn + "      " + "CheckOut: " + checkOut);
    System.out.println("Number of adults: " + numAdults + "     " + "Number of children: " + numChildren);
    System.out.println("Total cost: " + totalCost);
    System.out.println("\nType 'Cancel' to cancel your reservation or 'Confirm' to confirm your reservation");
  }
  private double calculateTotalCost(String checkIn, String checkOut, double basePrice) {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    int weekdays = 0, weekends = 0;

    //calculate the number of weekends and weekdays
    try {
      Calendar start = Calendar.getInstance();
      start.setTime(df.parse(checkIn));
      Calendar end = Calendar.getInstance();
      end.setTime(df.parse(checkOut));

      while (!start.after(end)) {
        int day = start.get(Calendar.DAY_OF_WEEK);
        if ((day == Calendar.SATURDAY) || (day == Calendar.SUNDAY)) {
          weekends++;
        }
        else {
          weekdays++;
        }
        start.add(Calendar.DATE, 1);
      }
    }
    catch (ParseException e) {
      e.getMessage();
    }

    //get the room base rate
    //calculate the total cost
    double noTaxTotal = (weekdays * basePrice) + (weekends * (basePrice * 1.1));
    double tourismTax = noTaxTotal * 0.18;
    return noTaxTotal + tourismTax;
  }
  private double calculateRate(String checkIn, String checkOut, double totalCost) {
    long numDays = 0;

    try {
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
      Date dateIn = df.parse(checkIn);
      Date dateOut = df.parse(checkOut);
      long diff = dateOut.getTime() - dateIn.getTime();
      numDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }
    catch (ParseException e) {
      e.getMessage();
    }
    System.out.println(totalCost / numDays);

    return totalCost / numDays;
  }
  private void confirmReservation(String roomCode, String checkIn, String checkOut, double rate, String lastName, String firstName, String numAdults, String numChildren) {
    String insert = "insert into mejohnst.lab6_reservations " +
                        "(CODE, Room, CheckIn, CheckOut, Rate, LastName, FirstName, Adults, Kids) " +
                        "values(" + reservationCode + ", '" + roomCode + "', STR_TO_DATE('" + checkIn + "', '%Y-%m-%d'), STR_TO_DATE('" + checkOut + "', '%Y-%m-%d'), " +
                                rate + ", '" + lastName + "', '" + firstName + "', " + numAdults + ", " + numChildren + ");";


    System.out.println(insert);

    try {
      //send the insert statement to the database
      Statement stmt = conn.createStatement();
      int rowsAffected = stmt.executeUpdate(insert);
      System.out.println("It's official, you're headed to A&M Bed and Breakfast. Pack your bags!");
    }

    catch (SQLException e) {
      System.out.println("SQLException: " + e.getMessage());
    }
    reservationCode++;
  }
}
