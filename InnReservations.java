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
  public static void main (String[] args) {

    establishDBConnection();
    //promptUserInput();
  }

  public static void establishDBConnection() {
    //Step 1: Establish connection to RDBMS
    try {
      String jdbcUrl = System.getenv("APP_JDBC_URL");
      String dbUsername = System.getenv("APP_JDBC_USER");
      String dbPassword =  System.getenv("APP_JDBC_PW");
      Connection conn = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);

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
          System.out.println("Rooms query selected");
          break;
        case "Reservations":
            System.out.println("Reservations query selected");
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
}
