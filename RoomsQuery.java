import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;

public class RoomsQuery {
  private Connection conn;

  public RoomsQuery(Connection conn) {
    this.conn = conn;
    run();
  }

  public void run() {
    try {
      String query = createQuery();
      ResultSet rs = executeQuery(query);
      printResults(rs);
    }
    catch (SQLException e) {
      System.out.println("SQLException: " + e.getMessage());
    }
  }

  public String createQuery() {
    return
    "SELECT lab6_rooms.*, (SELECT "+
       "CASE "+
       "WHEN not exists ( "+
         "SELECT second.CheckIn "+
         "FROM lab6_reservations second "+
         "WHERE second.Room = first.Room AND second.Checkout > CURDATE() AND second.CheckIn <= CURDATE() "+
       ") THEN CURDATE() "+
       "ELSE min(first.CheckOut) "+
       "END "+
       "FROM lab6_reservations first "+
       "WHERE first.Room = lab6_rooms.RoomCode AND first.CheckIn >= CURDATE() AND not exists ( "+
         "SELECT * "+
         "FROM lab6_reservations third "+
         "WHERE third.Room = first.Room and third.CheckIn = first.CheckOut "+
       ") "+
      ") as FirstAvailable, A1.Length, A1.RecentCheckOut, A2.PopularityScore "+
    "FROM lab6_rooms "+
    "JOIN "+
    "(SELECT lab6_reservations.Room as Room, DATEDIFF(MAX(lab6_reservations.CheckOut), MAX(lab6_reservations.CheckIn)) as Length, MAX(lab6_reservations.CheckOut) as RecentCheckOut "+
    "FROM lab6_reservations "+
    "WHERE lab6_reservations.CheckOut <= CURDATE() "+
    "GROUP BY lab6_reservations.Room) A1 ON A1.Room = lab6_rooms.RoomCode "+
    "JOIN "+
    "(SELECT lab6_rooms.RoomCode as Room,  ROUND((SUM(DATEDIFF(LEAST(lab6_reservations.CheckOut, CURDATE()), GREATEST(lab6_reservations.CheckIn, DATE_ADD(lab6_reservations.CheckIn, INTERVAL -180 DAY))))/180), 2) as PopularityScore "+
    "FROM lab6_reservations "+
    "JOIN lab6_rooms ON lab6_rooms.RoomCode = lab6_reservations.Room "+
    "WHERE (lab6_reservations.CheckIn <= CURDATE()) AND (lab6_reservations.CheckOut >= DATE_ADD(lab6_reservations.CheckIn, INTERVAL -180 DAY)) "+
    "GROUP BY lab6_reservations.Room) A2 ON A2.Room = lab6_rooms.RoomCode "+
    "ORDER BY A2.PopularityScore DESC; ";
  }

  public ResultSet executeQuery(String query) throws SQLException {
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery(query);
    return rs;
  }

  public void printResults(ResultSet rs) throws SQLException {
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
}
