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

import org.json.JSONArray;
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

		PreparedStatement prepStmt = null, prepStmt1 = null, prepStmt2 = null, prepStmt3 = null;			
		Connection conn=null;
		int last_inserted_user_id=-1;
		JSONObject jsonObject = new JSONObject();	
		System.out.println("in register/email");
		try{
			System.out.println("in register/email with userName - "+userName);			
			jsonObject.put("responseFlag", "fail");

			DBConnection dbConnection =new DBConnection();
			conn=dbConnection.getConnection();			
			conn.setAutoCommit(false);
			String query = "INSERT INTO users(first_name,last_name,user_name,email,hash_password) VALUES (?,?,?,?,?)";
			prepStmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
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

				ResultSet rs = prepStmt.getGeneratedKeys();
				if(rs.next())
				{
					last_inserted_user_id = rs.getInt(1);
					//					jsonObject.put("userId", last_inserted_user_id);
				}
				//add friend requests from the users who have sent invites
				query = "SELECT user_id from email_invites where email = ?";
				prepStmt1 = conn.prepareStatement(query);
				prepStmt1.setString(1,email);
				ResultSet rs1 = prepStmt1.executeQuery();
				if(rs1.isBeforeFirst()){				
					while(rs1.next()){
						query = "INSERT INTO friends (user_id,friend_id,approved) VALUES (?,?,?)";
						prepStmt2 = conn.prepareStatement(query);
						prepStmt2.setInt(1,rs1.getInt("user_id"));
						prepStmt2.setInt(2,last_inserted_user_id);						
						prepStmt2.setInt(3,0); //0 for not approved
						prepStmt2.executeUpdate();
					}					
				}else{
					//no invites from any users
				}
				//now delete the records from email_invites table
				query = "DELETE from email_invites where email = ?";
				prepStmt3 = conn.prepareStatement(query);
				prepStmt3.setString(1,email);
				prepStmt3.executeUpdate();

				jsonObject.put("responseFlag", "success");
				conn.commit();						
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
				if(prepStmt2!=null)
					prepStmt2.close();
				if(prepStmt3!=null)
					prepStmt3.close();
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


	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Path("emailinvite")
	public Response sendEmailInvite(@FormParam("userId") String userId, @FormParam("inviteeList") String inviteeList){		

		PreparedStatement prepStmt = null;			
		Connection conn=null;
		JSONObject jsonObject = new JSONObject();	

		try{
			JSONArray inviteeListJsonArray = new JSONArray(inviteeList);
			System.out.println("in emailinvite with userId - "+userId);		
			System.out.println("inviteeList: -->  "+inviteeListJsonArray);

			jsonObject.put("responseFlag", "fail");

			DBConnection dbConnection =new DBConnection();
			conn=dbConnection.getConnection();	
			conn.setAutoCommit(false);

			for(int i=0;i<inviteeListJsonArray.length();i++){
				String query = "INSERT INTO email_invites(user_id,email) VALUES (?,?)";
				prepStmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
				prepStmt.setString(1, userId);
				prepStmt.setString(2, inviteeListJsonArray.getString(i));
				prepStmt.executeUpdate();				
			}		
			jsonObject.put("responseFlag", "success");
			conn.commit();

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
		}catch(JSONException je){
			//Handle errors for Class.forName
			je.printStackTrace();
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
	@Path("acceptRejectFriendRequest")
	public Response acceptRejectFriendRequest(@FormParam("userId") String userId, @FormParam("requesterUserId") String requesterUserId,@FormParam("acceptRejectFlag") String acceptRejectFlag){		

		PreparedStatement prepStmt = null;			
		Connection conn=null;
		JSONObject jsonObject = new JSONObject();

		try{
			System.out.println("acceptRejectFriendRequest/social - userId : "+userId);				
			jsonObject.put("responseFlag", "fail");

			DBConnection dbConnection =new DBConnection();
			conn=dbConnection.getConnection();	
			conn.setAutoCommit(false);

			if(acceptRejectFlag.equals("1")){
				String query = "UPDATE friends SET approved = ? WHERE user_id = ? and friend_id = ?";
				prepStmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
				prepStmt.setInt(1, 1);			
				prepStmt.setInt(2, Integer.parseInt(requesterUserId));	
				prepStmt.setInt(3, Integer.parseInt(userId));					

				if(prepStmt.executeUpdate()==1){   			
					jsonObject.put("responseFlag", "success");
					conn.commit();
				}else{
					//record not found
				}

			}else if(acceptRejectFlag.equals("0")){
				String query = "DELETE from friends WHERE user_id = ? and friend_id = ?";
				prepStmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
				prepStmt.setInt(1, Integer.parseInt(requesterUserId));	
				prepStmt.setInt(2, Integer.parseInt(userId));					


				if(prepStmt.executeUpdate()==1){   			
					jsonObject.put("responseFlag", "success");
					conn.commit();
				}else{
					//record not found
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
	@Path("friendRequest")
	public Response sendFriendRequest(@FormParam("userId") String userId, @FormParam("inviteeList") String inviteeList){		

		PreparedStatement prepStmt = null;			
		Connection conn=null;

		JSONObject jsonObject = new JSONObject();	
		System.out.println("in friendRequest");
		try{
			JSONArray inviteeListJsonArray = new JSONArray(inviteeList);
			System.out.println("in friendRequest with userId - "+userId);		
			System.out.println("inviteeList: -->  "+inviteeListJsonArray);

			jsonObject.put("responseFlag", "fail");

			DBConnection dbConnection =new DBConnection();
			conn=dbConnection.getConnection();	
			conn.setAutoCommit(false);

			for(int i=0;i<inviteeListJsonArray.length();i++){
				String query = "INSERT INTO friends(user_id,friend_id) VALUES (?,?)";
				prepStmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
				prepStmt.setString(1, userId);
				prepStmt.setString(2, inviteeListJsonArray.getString(i));
				prepStmt.executeUpdate();				
			}		
			jsonObject.put("responseFlag", "success");
			conn.commit();
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

	@GET
	@Consumes("application/x-www-form-urlencoded")
	@Path("friendList")
	public Response getFriendList(@QueryParam("userId") String userId){		

		PreparedStatement prepStmt = null;			
		Connection conn=null;
		JSONObject jsonObject = new JSONObject();	
		JSONObject jsonObject2;
		JSONArray jsonArray = new JSONArray();
		JSONArray jsonArray2 = new JSONArray();
		System.out.println("in friendList");
		try{
			System.out.println("in friendList with userId - "+userId);		

			jsonObject.put("responseFlag", "fail");

			DBConnection dbConnection =new DBConnection();
			conn=dbConnection.getConnection();	

			String query = "SELECT friend_id FROM friends WHERE user_id = ? and approved=1";
			prepStmt = conn.prepareStatement(query);
			prepStmt.setString(1, userId);
			ResultSet rs = prepStmt.executeQuery();
			ResultSet rs1=null;
			while(rs.next()){
				int i=0;
				jsonArray = new JSONArray();
				jsonArray.put(i, rs.getInt("friend_id"));
				query = "SELECT user_name,first_name,last_name,profile_pic_path FROM users WHERE user_id = ?";
				prepStmt = conn.prepareStatement(query);
				prepStmt.setInt(1, rs.getInt("friend_id"));
				rs1 = prepStmt.executeQuery();
				while(rs1.next()){
					jsonArray.put(rs1.getString("user_name"));
					jsonArray.put(rs1.getString("first_name"));
					jsonArray.put(rs1.getString("last_name"));
					jsonArray.put(rs1.getString("profile_pic_path"));
				}
				jsonObject2 = new JSONObject();
				jsonObject2.put("friendDetails", jsonArray);
				jsonArray2.put(jsonObject2);
			}

			jsonObject.put("responseFlag", "success");
			jsonObject.put("friendList", jsonArray2);
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

	@GET
	@Consumes("application/x-www-form-urlencoded")
	@Path("friendDetails")
	public Response getFriendDetails(@QueryParam("friendId") String friendId){		

		PreparedStatement prepStmt = null;			
		Connection conn=null;
		JSONObject jsonObject = new JSONObject();	
		JSONArray jsonArray = new JSONArray();

		System.out.println("in friendDetails");
		try{
			System.out.println("in friendDetails with friendId - "+friendId);		

			jsonObject.put("responseFlag", "fail");

			DBConnection dbConnection =new DBConnection();
			conn=dbConnection.getConnection();	

			String query = "SELECT user_name,first_name,last_name,profile_pic_path FROM users WHERE user_id = ?";
			prepStmt = conn.prepareStatement(query);
			prepStmt.setInt(1, Integer.parseInt(friendId));
			ResultSet rs = prepStmt.executeQuery();
			while(rs.next()){
				jsonArray.put(friendId);
				jsonArray.put(rs.getString("user_name"));
				jsonArray.put(rs.getString("first_name"));
				jsonArray.put(rs.getString("last_name"));
				jsonArray.put(rs.getString("profile_pic_path"));
			}
			jsonObject.put("friendDetails", jsonArray);
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


}