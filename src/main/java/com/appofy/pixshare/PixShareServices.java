/**
 * 
 */
package com.appofy.pixshare;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

import com.appofy.pixshare.dao.DBConnection;

import java.sql.PreparedStatement;
import java.util.Iterator;

/**
 * @author rohan
 *
 */

@Path("/pixshare")
public class PixShareServices {


	@GET
	@Path("authenticate/email")
	public Response authenticateUser(@QueryParam("userName") String userName, @QueryParam("password") String password) {

		JSONObject jsonObject = new JSONObject();
		PreparedStatement prepStmt = null,prepStmt1 = null;			
		Connection conn=null;
		ResultSet rs= null;
		System.out.println("in authenticate/email");
		try{
			jsonObject.put("responseFlag", "fail");
			DBConnection dbConnection =new DBConnection();
			conn=dbConnection.getConnection();					
			String query = "SELECT * FROM users where user_name = ? and hash_password= ?";
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, userName);

			// encrypt password String
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(password.getBytes());
			String encryptedString = new String(messageDigest.digest());

			prepStmt.setString(2, encryptedString);			
			rs = prepStmt.executeQuery();			

			if(!rs.isBeforeFirst() ){
				//Not Found				
				jsonObject.put("authenticated", "false");
				jsonObject.put("socialMediaFlag",-1);
				jsonObject.put("socialMediaId",-1);
			}else{
				int user_id=-1;

				while(rs.next()){				
					user_id  = rs.getInt("user_id");				
					System.out.print("User ID: " + user_id);
					//jsonObject.put("userFound", "true");
					jsonObject.put("userId", rs.getString("user_id"));
					jsonObject.put("firstName", rs.getString("first_name"));
					jsonObject.put("socialMediaFlag", rs.getInt("social_media_flag"));
					jsonObject.put("authenticated", "true");					
				}
				if(jsonObject.get("socialMediaFlag")=="1"){
					//user registered with social media -- handle accordingly		
					//Find the social media id and add to the response
					query = "SELECT source_id from social_media_logins where user_id = ?";
					prepStmt1 = conn.prepareStatement(query);
					prepStmt1.setInt(1, user_id);
					ResultSet rs1 = prepStmt1.executeQuery();
					while(rs1.next()){
						jsonObject.put("socialMediaId",rs1.getInt("source_id"));
					}
				}else{
					//user registered with email -- handle accordingly
				}				
			}
			jsonObject.put("responseFlag", "success");

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
		System.out.println(jsonObject.toString());
		return Response.status(200).entity(jsonObject.toString()).build();

	}

	/*@GET
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

	}*/

	@GET
	@Path("checkAvailableUserName")
	public Response checkUserNameAvailability(@QueryParam("userName") String userName){
		JSONObject jsonObject=new JSONObject();	
		PreparedStatement prepStmt = null;
		Connection conn = null;
		System.out.println("in checkAvailableUserName");
		try {
			jsonObject.put("available","W");
			conn = new DBConnection().getConnection();
			String query = "SELECT * FROM users where user_name = ?";
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, userName);
			ResultSet rs = prepStmt.executeQuery();	
			if(!rs.isBeforeFirst() ){				
				jsonObject.put("available", "A");
			}else{
				jsonObject.put("available", "N");
			}

		} catch (JSONException e) {			
			e.printStackTrace();
		} catch( SQLException e){
			e.printStackTrace();
		} finally{
			//finally block used to close resources
			try{
				if(prepStmt!=null)
					prepStmt.close();				
			}catch(SQLException se2){
			}// nothing we can do
			try{
				if(conn!=null)
					conn.close();
			}catch(SQLException se){
				se.printStackTrace();
			}//end finally try
		}
		System.out.println(jsonObject.toString());
		return Response.status(200).entity(jsonObject.toString()).build();
	}

	@GET
	@Path("checkSocialUserIdPresent")
	public Response checkSocialUserNamePresent(@QueryParam("socialUserId") String socialUserId, @QueryParam("token") String token){
		JSONObject jsonObject=new JSONObject();	
		PreparedStatement prepStmt = null;
		Connection conn = null;
		System.out.println("in checkSocialUserIdPresent");
		try {
			jsonObject.put("responseFlag", "fail");
			jsonObject.put("present","W");
			conn = new DBConnection().getConnection();
			String query = "SELECT * FROM social_media_logins where social_user_id = ?";
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, socialUserId);
			ResultSet rs = prepStmt.executeQuery();	
			if(rs.isBeforeFirst()){				
				jsonObject.put("present", "Y");
				while(rs.next()){
					jsonObject.put("userId", rs.getInt("user_id"));
				}
			}else{
				jsonObject.put("present", "N");
			}
			jsonObject.put("responseFlag", "success");

		} catch (JSONException e) {			
			e.printStackTrace();
		} catch( SQLException e){
			e.printStackTrace();
		} finally{
			//finally block used to close resources
			try{
				if(prepStmt!=null)
					prepStmt.close();				
			}catch(SQLException se2){
			}// nothing we can do
			try{
				if(conn!=null)
					conn.close();
			}catch(SQLException se){
				se.printStackTrace();
			}//end finally try
		}
		System.out.println(jsonObject.toString());
		return Response.status(200).entity(jsonObject.toString()).build();
	}


	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Path("register/email")
	public Response registerUser(@FormParam("firstName") String firstName, @FormParam("lastName") String lastName, @FormParam("userName") String userName,
			@FormParam("email") String email, @FormParam("password") String password){		

		PreparedStatement prepStmt = null;			
		Connection conn=null;
		JSONObject jsonObject = new JSONObject();	
		System.out.println("in register/email");
		try{
			System.out.println("in register/email with userName - "+userName);			
			jsonObject.put("responseFlag", "fail");

			DBConnection dbConnection =new DBConnection();
			conn=dbConnection.getConnection();					
			String query = "INSERT INTO users(first_name,last_name,user_name,email,hash_password) VALUES (?,?,?,?,?)";
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, firstName);
			prepStmt.setString(2, lastName);
			prepStmt.setString(3, userName);
			prepStmt.setString(4, email);

			//encrypt password
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(password.getBytes());
			String encryptedString = new String(messageDigest.digest());

			prepStmt.setString(5, encryptedString);			
			if(prepStmt.executeUpdate()==1){
				jsonObject.put("responseFlag", "success");
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
			}catch(SQLException se2){
			}// nothing we can do
			try{
				if(conn!=null)
					conn.close();
			}catch(SQLException se){
				se.printStackTrace();
			}//end finally try
		}
		System.out.println(jsonObject.toString());
		return Response.status(200).entity(jsonObject.toString()).build();		
	} 

	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Path("register/social")
	public Response registerUserSocial(@FormParam("socialDetails") String socialDetails){		

		PreparedStatement prepStmt = null;			
		Connection conn=null;
		JSONObject jsonObject = new JSONObject();			
		String userName= null,password=null,firstName=null,lastName=null,
				email=null,sourceName=null,socialUserId=null,gender=null,profilePicURL=null,phone=null,websiteURL=null,bio=null;

		try{
			//JSONObject socialFieldsInJSON = new JSONObject(socialDetails);
			JSONObject socialFieldsInJSONObj = new JSONObject(socialDetails);
			//socialFieldsInJSONObj=socialFieldsInJSON.getJSONObject("socialDetails");
			Iterator<?> keys = socialFieldsInJSONObj.keys();
			while( keys.hasNext() ) {
			    String key = (String)keys.next();
			    System.out.println(key);
			    if(key.contains("socialUserId")){
			    	socialUserId = socialFieldsInJSONObj.getString("socialUserId");
                }else if(key.contains("email")){
                	email = socialFieldsInJSONObj.getString("email");
                }else if(key.contains("userName")){
                	userName = socialFieldsInJSONObj.getString("userName");
                }else if(key.contains("gender")){
                	gender = socialFieldsInJSONObj.getString("gender");
                }else if(key.contains("profilePicURL")){                    
                    profilePicURL = socialFieldsInJSONObj.getString("profilePicURL");
                }else if(key.contains("phone")){
                	phone = socialFieldsInJSONObj.getString("phone");
                }else if(key.contains("website")){
                	websiteURL = socialFieldsInJSONObj.getString("website");
                }else if(key.contains("bio")){
                	bio = socialFieldsInJSONObj.getString("bio");
                }else if(key.contains("token")){
                	password = socialFieldsInJSONObj.getString("token");
                }else if(key.contains("sourceName")){
                	sourceName = socialFieldsInJSONObj.getString("sourceName");
                }else if(key.contains("firstName")){
                	firstName = socialFieldsInJSONObj.getString("firstName");
                }else if(key.contains("lastName")){
                	lastName = socialFieldsInJSONObj.getString("lastName");
                }
			}	
			
			System.out.println("in register/social with userName - "+userName);		
			System.out.println("token: -->  "+password); //password is token
			jsonObject.put("responseFlag", "fail"); //default to fail
			jsonObject.put("socialMediaFlag", -1);
			
			DBConnection dbConnection =new DBConnection();
			conn=dbConnection.getConnection();	
			conn.setAutoCommit(false);
			String query = "INSERT INTO users(first_name,last_name,user_name,email,social_media_flag,gender,bio,website_url,profile_pic_path,phone_number) VALUES (?,?,?,?,?,?,?,?,?,?)";
			prepStmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			prepStmt.setString(1, firstName);
			prepStmt.setString(2, lastName);
			prepStmt.setString(3, userName);
			prepStmt.setString(4, email);
			prepStmt.setInt(5, 1);
			prepStmt.setString(6, gender);
			prepStmt.setString(7, bio);
			prepStmt.setString(8, websiteURL);
			prepStmt.setString(9, profilePicURL);
			prepStmt.setString(10, phone);

			if(prepStmt.executeUpdate()==1){				
				ResultSet rs = prepStmt.getGeneratedKeys();
				if(rs.next())
				{
					int last_inserted_user_id = rs.getInt(1);
					jsonObject.put("userId", last_inserted_user_id);
				}
				query = "SELECT source_id from social_media_sources where name = ?";
				prepStmt.close();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, sourceName);
				ResultSet rs1=prepStmt.executeQuery();
				while(rs1.next()){
					jsonObject.put("socialMediaId", rs1.getInt("source_id"));
				}
				prepStmt.close();
				//insert into social_media_logins

				query = "INSERT INTO social_media_logins(user_id,source_id,token,social_user_id,session) VALUES (?,?,?,?,?)";
				prepStmt = conn.prepareStatement(query);
				prepStmt.setInt(1, jsonObject.getInt("userId"));
				prepStmt.setInt(2, jsonObject.getInt("socialMediaId"));
				prepStmt.setString(3, password);
				prepStmt.setString(4, socialUserId);
				prepStmt.setInt(5, 1);    					
				if(prepStmt.executeUpdate()==1){
					conn.commit();
					jsonObject.put("socialMediaFlag", 1);
					jsonObject.put("responseFlag", "success");
				}				
			}

		}catch(SQLException se){
			//Handle errors for JDBC
			if(conn!=null){
				try {
					conn.rollback();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			se.printStackTrace();
		}catch(Exception e){
			//Handle errors for Class.forName
			e.printStackTrace();
		}finally{
			//finally block used to close resources
			try{
				if(prepStmt!=null)
					prepStmt.close();				
			}catch(SQLException se2){
			}// nothing we can do
			try{
				if(conn!=null)
					conn.close();
			}catch(SQLException se){
				se.printStackTrace();
			}//end finally try
		}
		System.out.println(jsonObject.toString());
		return Response.status(200).entity(jsonObject.toString()).build();		
	} 


	@PUT
	@Consumes("application/x-www-form-urlencoded")
	@Path("accesstoken/social")
	public Response updateAccessTokenSocial(@FormParam("socialUserId") String socialUserId, @FormParam("accessToken") String accessToken){		

		PreparedStatement prepStmt = null, prepStmt1 = null;			
		Connection conn=null;
		JSONObject jsonObject = new JSONObject();	

		try{
			System.out.println("accesstoken/social socialUserId - "+socialUserId);				
			jsonObject.put("responseFlag", "fail");
			jsonObject.put("socialMediaFlag",-1);
			jsonObject.put("socialMediaId",-1);
			jsonObject.put("userId",-1);
			jsonObject.put("token",-1);
			
			DBConnection dbConnection =new DBConnection();
			conn=dbConnection.getConnection();				
			String query = "UPDATE social_media_logins SET token = ? WHERE social_user_id = ?";
			prepStmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			prepStmt.setString(1, accessToken);			
			prepStmt.setString(2, socialUserId);	

			if(prepStmt.executeUpdate()==1){   			
				jsonObject.put("responseFlag", "success");
			}
			
			query = "SELECT user_id,source_id,token from social_media_logins where social_user_id = ?";
			prepStmt1 = conn.prepareStatement(query);
			prepStmt1.setString(1, socialUserId);
			ResultSet rs1 = prepStmt1.executeQuery();
			while(rs1.next()){
				jsonObject.put("socialMediaFlag",1);
				jsonObject.put("socialMediaId",rs1.getInt("source_id"));
				jsonObject.put("userId",rs1.getInt("user_id"));
				jsonObject.put("token",rs1.getString("token"));
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
		System.out.println(jsonObject.toString());
		return Response.status(200).entity(jsonObject.toString()).build();		
	} 


	/*@POST
	@Consumes("application/x-www-form-urlencoded")
	@Path("emailinvite")
	public Response sendEmailInvite(@FormParam("userId") String userId, @FormParam("inviteeList") String inviteeList){		

		PreparedStatement prepStmt = null;			
		Connection conn=null;
		JSONObject jsonObject = new JSONObject();	
		JSONObject inviteeListJsonObj = new JSONObject(inviteeList);
		
		try{
			System.out.println("in emailinvite with userId - "+userId);		
			System.out.println("inviteeList: -->  "+inviteeListJsonObj);
			
			jsonObject.put("responseFlag", "fail");

			DBConnection dbConnection =new DBConnection();
			conn=dbConnection.getConnection();	
			conn.setAutoCommit(false);
			String query = "INSERT INTO users(first_name,last_name,user_name,email,social_media_flag) VALUES (?,?,?,?,?)";
			prepStmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			prepStmt.setString(1, firstName);
			prepStmt.setString(2, lastName);
			prepStmt.setString(3, userName);
			prepStmt.setString(4, email);
			prepStmt.setInt(5, 1);

			if(prepStmt.executeUpdate()==1){				
				ResultSet rs = prepStmt.getGeneratedKeys();
				if(rs.next())
				{
					int last_inserted_user_id = rs.getInt(1);
					jsonObject.put("user_id", last_inserted_user_id);
				}
				query = "SELECT source_id from social_media_sources where name = ?";
				prepStmt.close();
				prepStmt = conn.prepareStatement(query);
				prepStmt.setString(1, sourceName);
				ResultSet rs1=prepStmt.executeQuery();
				while(rs1.next()){
					jsonObject.put("sourceId", rs1.getInt("source_id"));
				}
				prepStmt.close();
				//insert into social_media_logins

				query = "INSERT INTO social_media_logins(user_id,source_id,token,social_user_id,session) VALUES (?,?,?,?,?)";
				prepStmt = conn.prepareStatement(query);
				prepStmt.setInt(1, jsonObject.getInt("user_id"));
				prepStmt.setInt(2, jsonObject.getInt("sourceId"));
				prepStmt.setString(3, password);
				prepStmt.setString(4, socialUserId);
				prepStmt.setInt(5, 1);    					
				if(prepStmt.executeUpdate()==1){
					conn.commit();
					jsonObject.put("responseFlag", "success");
				}

				//
			}

		}catch(SQLException se){
			//Handle errors for JDBC
			if(conn!=null){
				try {
					conn.rollback();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			se.printStackTrace();
		}catch(Exception e){
			//Handle errors for Class.forName
			e.printStackTrace();
		}finally{
			//finally block used to close resources
			try{
				if(prepStmt!=null)
					prepStmt.close();				
			}catch(SQLException se2){
			}// nothing we can do
			try{
				if(conn!=null)
					conn.close();
			}catch(SQLException se){
				se.printStackTrace();
			}//end finally try
		}
		System.out.println(jsonObject.toString());
		return Response.status(200).entity(jsonObject.toString()).build();		
	} 
	*/

	/*@POST
	@Path("register")
	public Response registerUser(@QueryParam("firstName") String firstName, @QueryParam("lastName") String lastName, @QueryParam("userName") String userName,
			@QueryParam("email") String email, @QueryParam("password") String password){

		JSONObject jsonObject = new JSONObject();		
		System.out.println(userName);

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
	} */

}