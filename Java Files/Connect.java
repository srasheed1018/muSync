package muSync;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Connect {
	static Connection con;
	static Statement st;
	
	public Connect() {
		connect();
	}

	public static void connect() {
		try {
			Class.forName("org.postgresql.Driver");
			System.out.println("Driver Loaded Successfully");
			con = DriverManager.getConnection("jdbc:postgresql://localhost/musync","postgres","2615");
			System.out.println("Successfull Connection");
			st = con.createStatement();
			System.out.println("Database Access Granted.");
		}catch (ClassNotFoundException cnfe) {
			System.err.println(cnfe);
		}catch (SQLException sqle) {
			System.err.println(sqle);
		}
	}
}
