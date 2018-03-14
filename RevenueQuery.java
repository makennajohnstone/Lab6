public class RevenueQuery {
  private Connection conn;
  public RevenueQuery(Connection conn) {
    this.conn = conn;
    run();
  }
  public void run() {
    try {
      //create query
      //executeQuery
      //output results 
    }
    catch (SQLException e) {
      (System.out.println("SQLException: " + e.getMessage()));
    }
  }
}
