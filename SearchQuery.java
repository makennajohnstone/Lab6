import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Scanner;
import java.util.*;

public class SearchQuery {
  private Connection conn;

  public SearchQuery (Connection conn) {
    this.conn = conn;
    run();
  }
  public void run() {
    try {
      ArrayList<String> userInput = promptForSearchInfo();
      String firstName = userInput.get(0);
      String lastName = userInput.get(1);
      String roomCode = userInput.get(2);
      String checkIn = userInput.get(3);
      String checkOut = userInput.get(4);
      String reservationCode = userInput.get(5);

      PreparedStatement pstmt = createPreparedStatement(firstName, lastName, roomCode, checkIn, checkOut, reservationCode);
      ResultSet rs = pstmt.executeQuery();
      printResults(rs);
    }
    catch (SQLException e) {
      System.out.println("SQLException: " + e.getMessage());
    }
  }
  private ArrayList<String> promptForSearchInfo() {
    ArrayList<String> userInput = new ArrayList<String>();
    Scanner sc = new Scanner(System.in);

    System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~ Search Reservations System ~~~~~~~~~~~~~~~~~~~~~~~~");
    System.out.println("\nPlease enter the following information: ");
    System.out.println("If you do not want to search on a certain input, type 'Any' or ' '");

    System.out.print("First name: ");
    userInput.add(sc.next());

    System.out.print("Last name: ");
    userInput.add(sc.next());

    System.out.print("Room code: ");
    userInput.add(sc.next());

    System.out.print("Check-in date [YYYY-MM-DD]: ");
    userInput.add(sc.next());

    System.out.print("Check-out date [YYYY-MM-DD]: ");
    userInput.add(sc.next());

    System.out.print("Reservation Code: ");
    userInput.add(sc.next());

    return userInput;
  }
  private PreparedStatement createPreparedStatement(String firstName, String lastName,
   String roomCode, String checkIn, String checkOut, String reservationCode) throws SQLException {
    PreparedStatement pstmt = null;
    String query = "select * from lab6_reservations ";
    int numSearchParams = 0;
    boolean containsFirst = false;
    boolean containsLast = false;
    boolean containsRoom = false;
    boolean containsIn = false;
    boolean containsOut = false;
    boolean containsReservation = false;

    //construct the query string
    if (!(firstName.equals(" ") && firstName.equals("Any"))) {
      query = query + "FirstName = ? and ";
      containsFirst = true;
      numSearchParams++;
    }
    if (!(lastName.equals(" ") && lastName.equals("Any"))) {
      query = query + "LastName = ? and ";
      containsLast = true;
      numSearchParams++;
    }
    if (!(roomCode.equals(" ") && roomCode.equals("Any"))) {
      query = query + "RoomCode = ? and ";
      containsRoom = true;
      numSearchParams++;
    }
    if (!(checkIn.equals(" ") && checkIn.equals("Any"))) {
      query = query + "CheckIn = ? and ";
      containsIn = true;
      numSearchParams++;
    }
    if (!(checkOut.equals(" ") && checkOut.equals("Any"))) {
      query = query + "Checkout = ? and ";
      containsOut = true;
      numSearchParams++;
    }
    if (!(reservationCode.equals(" ") && reservationCode.equals("Any"))) {
      query = query + "CODE = ? and ";
      containsReservation = true;
      numSearchParams++;
    }
    query = query + ";";

    //initialize the preparedStatement
    pstmt = conn.prepareStatement(query);

    //add the parameters to the preparedStatement
    if (containsFirst) {
      pstmt.setString(numSearchParams, firstName);
    }
    if (containsLast) {
      pstmt.setString(numSearchParams, lastName);
    }
    if (containsRoom) {
      pstmt.setString(numSearchParams, roomCode);
    }
    if (containsIn) {
      pstmt.setString(numSearchParams, checkIn);
    }
    if (containsOut) {
      pstmt.setString(numSearchParams, checkOut);
    }
    if (containsReservation) {
      pstmt.setString(numSearchParams, reservationCode);
    }
    return pstmt;
  }
  private void printResults(ResultSet rs) throws SQLException {
    while (rs.next()) {
      String reservationCode = rs.getString("CODE");
      String roomCode = rs.getString("Room");
      String roomName = rs.getString("RoomName");
      String checkIn = rs.getString("CheckIn");
      String checkOut = rs.getString("Checkout");
      double rate = rs.getDouble("Rate");
      String lastName = rs.getString("LastName");
      String firstName = rs.getString("FirstName");
      int adults = rs.getInt("Adults");
      int kids = rs.getInt("Kids");

      System.out.format("%s %s %s %s %s %.2f %s %s %d %d %n", reservationCode, roomCode, roomName,
        checkIn, checkOut, rate, lastName, firstName, adults, kids);
    }
  }
}
