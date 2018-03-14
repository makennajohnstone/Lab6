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

public class ReservationsQuery {
  public int reservationCode = 100000;
  private Connection conn;

  public ReservationsQuery(Connection conn) {
    this.conn = conn;
    run();
  }

  public void run() {
    try {
      //get reservation info from user
      ArrayList<String> userInput = promptForReservationInfo();
      String firstName = userInput.get(0);
      String lastName = userInput.get(1);
      String roomCode = userInput.get(2);
      String bedType = userInput.get(3);
      String checkIn = userInput.get(4);
      String checkOut = userInput.get(5);
      String numChildren = userInput.get(6);
      String numAdults = userInput.get(7);

      String query = createReservationsQuery(roomCode, bedType, checkIn, checkOut, numChildren, numAdults);
      ResultSet rs = executeQuery(query);

      //if there are no matching rooms, print other suggestions
      if (getRowCount(rs) == 0) {
        suggestSimilarRooms(roomCode, bedType, checkIn, checkOut, numChildren, numAdults);
      }
      //else, output the room options
      else {
        outputRoomOptions(rs);
      }
      confirmOrCancel(rs, lastName, firstName, numAdults, numChildren);

    }
    catch (SQLException e) {
      System.out.println("SQLException: " + e.getMessage());
    }
  }

  public ArrayList<String> promptForReservationInfo() {
    ArrayList<String> userInput = new ArrayList<String>();
    Scanner sc = new Scanner(System.in);
    System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~ A&M Bed and Breakfast Booking System ~~~~~~~~~~~~~~~~~~~~~~~~");
    System.out.println("\nPlease enter the following information: ");

    System.out.print("First name: ");
    String firstName = sc.next();
    userInput.add(firstName);

    System.out.print("Last name: ");
    String lastName = sc.next();
    userInput.add(lastName);

    System.out.print("Room code [Type 'Any' to indicate no preference]: ");
    String roomCode = sc.next();
    userInput.add(roomCode);

    System.out.print("Bed type [Type 'Any' to indicate no preference]: ");
    String bedType = sc.next();
    userInput.add(bedType);

    System.out.print("Desired check-in date [YYYY-MM-DD]: ");
    String checkIn = sc.next();
    userInput.add(checkIn);

    System.out.print("Desired check-out date [YYYY-MM-DD]: ");
    String checkOut = sc.next();
    userInput.add(checkOut);

    System.out.print("Number of children: ");
    String numChildren = sc.next();
    userInput.add(numChildren);

    System.out.print("Number of adults: ");
    String numAdults = sc.next();
    userInput.add(numAdults);

    return userInput;
  }

  public String createReservationsQuery(String roomCode, String bedType, String checkIn, String checkOut, String numChildren, String numAdults) {

    int numOccupants = Integer.parseInt(numChildren) + Integer.parseInt(numAdults);
    String roomCodeStatement = "";
    String bedTypeStatement = "";

    if (!(roomCode.equals("Any"))) {
      roomCodeStatement = "RoomCode = '" + roomCode + "' and ";
    }
    if (!(bedType.equals("Any"))) {
      bedTypeStatement = "bedType = '"  + bedType + "' and ";
    }

    //TODO: change to preparedStatement
    //How do you do the prepared statement with the "any" in the room code and bed type.
    return
      "select distinct RoomCode, RoomName, bedType, basePrice" +
      " from mejohnst.lab6_rooms " +
      "   join mejohnst.lab6_reservations on RoomCode = Room" +
      " where " + roomCodeStatement + bedTypeStatement +
      "('" + checkIn + "' not between CheckIn and Checkout) and " +
      "('" + checkOut + "' not between CheckIn and Checkout) and " +
      "   maxOcc >= " + numOccupants +
      ";"
      ;
  }

  public ResultSet executeQuery(String query) throws SQLException {
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery(query);
    return rs;
  }

  private int getRowCount(ResultSet rs) throws SQLException {
    rs.last();
    return rs.getRow();

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
  private void outputRoomOptions(ResultSet rs) throws SQLException {
    int count = 1;
    rs.beforeFirst();
    System.out.println("\nThe following rooms matching your reservation request: ");
    while (rs.next()) {
      String roomCode = rs.getString("RoomCode");
      System.out.println(count + ". " + roomCode);
      count++;
    }
  }

  private void confirmOrCancel(ResultSet rs, String lastName, String firstName, String numAdults, String numChildren) throws SQLException {
    System.out.println("\nTo book a room, type it's corresponding option number");
    System.out.println("To cancel the current request, type 'Cancel'");
    Scanner sc = new Scanner(System.in);

    while (sc.hasNext()) {
      String userChoice = sc.next();
      String roomCode = "", roomName = "", bedType = "", checkIn = "", checkOut = "";
      double basePrice = 0, totalCost = 0;

      //return back to the main menu
      if (userChoice.equals("Cancel")) {
        promptForReservationInfo();
      }
      //create an entry in the lab6_reservations table
      else if (userChoice.equals("Confirm")) {
        double rate = calculateRate(checkIn, checkOut, totalCost);
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
        checkIn = rs.getString("CheckIn");
        checkOut = rs.getString("Checkout");
        basePrice = rs.getDouble("basePrice");
        totalCost = calculateTotalCost(checkIn, checkOut, basePrice);
        outputReservationDetails(roomCode, roomName, bedType, checkIn, checkOut, firstName, lastName, numAdults, numChildren, totalCost);
      }
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
