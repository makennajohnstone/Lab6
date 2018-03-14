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

public class InnReservations {


  public static void main (String[] args) {
    Connection conn = establishDBConnection();
    run(conn);
  }

  //Establish connection to RDBMS
  private static Connection establishDBConnection() {
    Connection conn = null;
    try {
      String jdbcUrl = System.getenv("APP_JDBC_URL");
      String dbUsername = System.getenv("APP_JDBC_USER");
      String dbPassword =  System.getenv("APP_JDBC_PW");
      conn = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);
    }
    catch (SQLException e) {
      System.out.println("SQLException : " + e.getMessage());
    }
    return conn;
  }

  private static void run(Connection conn) {
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
          RoomsQuery roomsQuery = new RoomsQuery(conn);
          break;
        case "Reservations":
          ReservationsQuery reservationsQuery = new ReservationsQuery(conn);
          break;
        case "Search":
          SearchQuery searchQuery = new SearchQuery(conn); 
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
      System.out.println("\nChoose another database query: \n");
    }
  }
}
