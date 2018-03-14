import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class Query {

  public abstract ResultSet executeQuery(String query) throws SQLException;
  public abstract String createQuery();
  public abstract void printResults(ResultSet rs) throws SQLException;
}
