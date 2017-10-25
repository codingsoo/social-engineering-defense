import java.util.ArrayList;
import java.util.List;

import edu.mit.jwi.item.POS;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;



class DBConnection {
	Connection cn = null;
	Statement st = null;
	PreparedStatement pst = null;
	ResultSet rs = null;
	WordNet wn = new WordNet();
	
	DBConnection() {
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
	 * check whether input word's hypernyms are in List, 
	 */
	boolean DBcheck(String table, String verb, String obj) {
		List<String> verbHn = wn.getHypernyms(verb, POS.VERB);
		List<String> objHn = wn.getHypernyms(obj, POS.NOUN);
		
		try {
			pst = cn.prepareStatement("SELECT * FROM " + table);
			rs = pst.executeQuery();

			while(rs.next()) {
				String DBverb = rs.getString(1);
				String DBobj = rs.getString(2);
				if(verbHn.contains(DBverb) && objHn.contains(DBobj)) return true;
			}
			
		}catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	/*
	 * print the table DB data.
	 */
	void DBread(String table){
		try {
			pst = cn.prepareStatement("SELECT * FROM " + table);
			rs = pst.executeQuery();

			while(rs.next()) {
				String DBverb = rs.getString(1);
				String DBobj = rs.getString(2);
				System.out.println(DBverb + " " + DBobj);
			}
			
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * insert the input data. (verb, object)
	 */
	void DBadd(String table, ArrayList<String> v, ArrayList<String> o ) {
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
