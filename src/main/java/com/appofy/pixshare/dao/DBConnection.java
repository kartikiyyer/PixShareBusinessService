/**
 * 
 */
package com.appofy.pixshare.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author user
 *
 */
public class DBConnection {

	private String JDBC_DRIVER;
	private String DB_URL;
	private String USER;
	private String PASS;
	public DBConnection() {

		// JDBC driver name and database URL
		JDBC_DRIVER = "com.mysql.jdbc.Driver";  
		DB_URL = "jdbc:mysql://localhost/pixshare";

		//  Database credentials
		USER = "root";
		PASS = "admin";
	}
	public Connection getConnection(){
		Connection conn = null;
		
		try{
			//Register JDBC driver
			Class.forName(JDBC_DRIVER);

			//Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			
		}catch(SQLException se){
			//Handle errors for JDBC
			se.printStackTrace();
		}catch(Exception e){
			//Handle errors for Class.forName
			e.printStackTrace();
		}
		return conn;
	}
	
	public static void main(String[] args){
		Boolean b=true;
		System.out.println(b.toString());
		

		/*Connection conn = null;
		Statement stmt = null;
		try{
			//Register JDBC driver
			Class.forName(JDBC_DRIVER);

			//Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);

			//Execute a query
			System.out.println("Creating statement...");
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT user_id FROM users";
			ResultSet rs = stmt.executeQuery(sql);

			//Extract data from result set
			while(rs.next()){
				//Retrieve by column name
				int id  = rs.getInt("user_id");
				
				//Display values
				System.out.print("ID: " + id);				
			}
			//Clean-up environment
			rs.close();
			stmt.close();
			conn.close();
		}catch(SQLException se){
			//Handle errors for JDBC
			se.printStackTrace();
		}catch(Exception e){
			//Handle errors for Class.forName
			e.printStackTrace();
		}finally{
			//finally block used to close resources
			try{
				if(stmt!=null)
					stmt.close();
			}catch(SQLException se2){
			}// nothing we can do
			try{
				if(conn!=null)
					conn.close();
			}catch(SQLException se){
				se.printStackTrace();
			}//end finally try
		}//end try
*/
	
	}


}
