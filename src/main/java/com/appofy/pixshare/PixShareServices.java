/**
 * 
 */
package com.appofy.pixshare;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import com.appofy.pixshare.dao.DBConnection;

import java.sql.PreparedStatement;

/**
 * @author rohan
 *
 */

@Path("/pixshare")
public class PixShareServices {

	@GET
	@Path("authenticate")
	public Response authenticateUser(@QueryParam("deviceId") String deviceId) {

		String output = "Device Id : " + deviceId;
		JSONObject jsonObject = new JSONObject();
		
		System.out.println(deviceId);
		PreparedStatement prepStmt = null;	
		PreparedStatement prepStmt1 = null;
		Connection conn=null;
		ResultSet rs= null;
		try{
			DBConnection dbConnection =new DBConnection();
			conn=dbConnection.getConnection();					
			String query = "SELECT * FROM users where device_id = ?";
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, deviceId);
			rs = prepStmt.executeQuery();			
			//prepStmt.close();
			
			if(!rs.isBeforeFirst() ){
				//Not Found
				jsonObject.put("userFound", "false");
			}else{
				int user_id=-1;
				int session=-1;
				while(rs.next()){				
					user_id  = rs.getInt("user_id");				
					System.out.print("User ID: " + user_id);
					jsonObject.put("userFound", "true");
					jsonObject.put("userId", rs.getString("user_id"));
					jsonObject.put("firstName", rs.getString("first_name"));					
				}
				query= "select * from  social_media_logins where user_id = ?";
				prepStmt.close();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setInt(1, user_id);
				rs = prepStmt.executeQuery();
				//prepStmt.close();
				if(rs.isBeforeFirst() ){					
					while(rs.next()){				
						session  = rs.getInt("session");				
						System.out.print("Session: " + session);	
						if(session==1){
							jsonObject.put("sessionLogin", "true");
							query= "select name from  social_media_sources where source_id = ?";
							prepStmt1 = conn.prepareStatement(query);
							prepStmt1.setInt(1, rs.getInt("source_id"));
							ResultSet rs1 = prepStmt1.executeQuery();
							//prepStmt.close();
							while(rs1.next()){
								jsonObject.put("socialMediaName", rs1.getString("name"));
							}							
							//send user to profile page
						}
						else if(session==0){
							jsonObject.put("sessionLogin", "false");
							//send user to login page
						}
					}				
				}else{
					//Not Found
					query= "select * from  users where user_id = ?";
					prepStmt = conn.prepareStatement(query);
					prepStmt.setInt(1, user_id);
					ResultSet rs2 = prepStmt.executeQuery();
					prepStmt.close();
					while(rs2.next()){
						if(null!=rs2.getString("email")){
							jsonObject.put("emailLogin", "true");
							//send user to profile page
						}else{
							jsonObject.put("emailLogin", "false");
							//send user to login page
						}
					}
				}
			}
			
		}catch(SQLException se){
			//Handle errors for JDBC
			se.printStackTrace();
		}catch(Exception e){
			//Handle errors for Class.forName
			e.printStackTrace();
		}finally{
			//finally block used to close resources
			try{
				if(prepStmt!=null)
					prepStmt.close();
				if(prepStmt1!=null)
					prepStmt1.close();
			}catch(SQLException se2){
			}// nothing we can do
			try{
				if(conn!=null)
					conn.close();
			}catch(SQLException se){
				se.printStackTrace();
			}//end finally try
		}
		return Response.status(200).entity(jsonObject.toString()).build();

	}
	
	@POST
	@Path("register")
	public Response registerUser(@QueryParam("deviceId") String deviceId, @QueryParam("email") String email, @QueryParam("password") String password,
			@QueryParam("firstName") String firstName, @QueryParam("lastName") String lastName,@QueryParam("userName") String userName){
		
		JSONObject jsonObject = new JSONObject();
		
		System.out.println(deviceId);
		PreparedStatement prepStmt = null;	
		PreparedStatement prepStmt1 = null;
		Connection conn=null;
		ResultSet rs= null;
		
		
		try{
			DBConnection dbConnection =new DBConnection();
			conn=dbConnection.getConnection();					
			String query = "SELECT * FROM users where device_id = ?";
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, deviceId);
			rs = prepStmt.executeQuery();			
			//prepStmt.close();
			
			if(!rs.isBeforeFirst() ){
				//Not Found
				jsonObject.put("userFound", "false");
			}else{
				int user_id=-1;
				int session=-1;
				while(rs.next()){				
					user_id  = rs.getInt("user_id");				
					System.out.print("User ID: " + user_id);
					jsonObject.put("userFound", "true");
					jsonObject.put("userId", rs.getString("user_id"));
					jsonObject.put("firstName", rs.getString("first_name"));					
				}
				query= "select * from  social_media_logins where user_id = ?";
				prepStmt.close();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setInt(1, user_id);
				rs = prepStmt.executeQuery();
				//prepStmt.close();
				if(rs.isBeforeFirst() ){					
					while(rs.next()){				
						session  = rs.getInt("session");				
						System.out.print("Session: " + session);	
						if(session==1){
							jsonObject.put("sessionLogin", "true");
							query= "select name from  social_media_sources where source_id = ?";
							prepStmt1 = conn.prepareStatement(query);
							prepStmt1.setInt(1, rs.getInt("source_id"));
							ResultSet rs1 = prepStmt1.executeQuery();
							//prepStmt.close();
							while(rs1.next()){
								jsonObject.put("socialMediaName", rs1.getString("name"));
							}							
							//send user to profile page
						}
						else if(session==0){
							jsonObject.put("sessionLogin", "false");
							//send user to login page
						}
					}				
				}else{
					//Not Found
					query= "select * from  users where user_id = ?";
					prepStmt = conn.prepareStatement(query);
					prepStmt.setInt(1, user_id);
					ResultSet rs2 = prepStmt.executeQuery();
					prepStmt.close();
					while(rs2.next()){
						if(null!=rs2.getString("email")){
							jsonObject.put("emailLogin", "true");
							//send user to profile page
						}else{
							jsonObject.put("emailLogin", "false");
							//send user to login page
						}
					}
				}
			}
			
		}catch(SQLException se){
			//Handle errors for JDBC
			se.printStackTrace();
		}catch(Exception e){
			//Handle errors for Class.forName
			e.printStackTrace();
		}finally{
			//finally block used to close resources
			try{
				if(prepStmt!=null)
					prepStmt.close();
				if(prepStmt1!=null)
					prepStmt1.close();
			}catch(SQLException se2){
			}// nothing we can do
			try{
				if(conn!=null)
					conn.close();
			}catch(SQLException se){
				se.printStackTrace();
			}//end finally try
		}
		return Response.status(200).entity(jsonObject.toString()).build();		
	} 

}