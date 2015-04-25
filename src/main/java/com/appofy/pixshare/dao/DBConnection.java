/**
 * 
 */
package com.appofy.pixshare.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import org.json.JSONArray;
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
		String str= "[\"rohan\",\"tan\"]";
		try {
			JSONArray ja = new JSONArray(str);
			System.out.println(ja.get(1));
												
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
}
