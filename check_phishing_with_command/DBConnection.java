import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.sql.*;

public class DBConnection {
	Connection cn = null;
	Statement st = null;
	
	public DBConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			cn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/test", "root", "0000");
			st = cn.createStatement();
			
			System.out.println("DB connect");
			
		}catch(Exception e) {
			e.printStackTrace();
		}	
	}
	
	/*
	 * insert the input data. (verb, object)
	 */
	public void DBadd(ArrayList<String> v, ArrayList<String> o ) {
		String table = "inputword"; 
		if(0 == v.size()) return;
		String sql = "insert into " + table + " values('" + v.get(0) +"','" + o.get(0) + "')";
		
		for(int i = 1; i < v.size(); i++) {
			String verb = v.get(i);
			String obj = o.get(i);
			sql = sql + ",('" + verb + "','" + obj + "')"; 
		}
		sql += ";";
		
		System.out.println(sql);
		
		try {
			st.executeUpdate(sql);
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
}
