public abstract class Query {
  public abstract String createQuery();
  public ResultSet executeQuery(String query) {
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery(query);

    return rs;
  }
  public abstract void printResults();
}
