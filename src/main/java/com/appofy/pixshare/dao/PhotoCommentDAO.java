package com.appofy.pixshare.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.json.JSONArray;
import org.json.JSONObject;

public class PhotoCommentDAO {
	static Connection conn;
	static PreparedStatement prepStat;
	static ResultSet rs;
	static int queryStatus;
	public static boolean selectComments(int photoId, JSONArray comments) { 
		JSONObject comment;
		rs = null;
		Timestamp timestamp = null;
		String date = null;
		try {
			conn = new DBConnection().getConnection();
			String query = "SELECT photo_comments.id, photo_comments.user_id, user_name, first_name, last_name, comment, photo_comments.date_created, photo_comments.date_updated FROM photo_comments "
					+ "INNER JOIN users ON users.user_id = photo_comments.user_id "
					+ "WHERE photo_id = ?";
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, photoId);
			rs = prepStat.executeQuery();
			while(rs.next()) {
				comment = new JSONObject();
				comment.put("id", rs.getInt("id"));
				comment.put("comment", rs.getString("comment"));
				comment.put("userId", rs.getInt("user_id"));
				comment.put("userName", rs.getString("user_name"));
				comment.put("firstName", rs.getString("first_name"));
				comment.put("lastName", rs.getString("last_name"));
				timestamp = rs.getTimestamp("date_created");
				date = timestamp.getMonth() + ":" + timestamp.getDate() + ":" + timestamp.getYear();
				comment.put("dateCreated", date);
				timestamp = rs.getTimestamp("date_updated");
				if(timestamp != null) {
					date = timestamp.getMonth() + ":" + timestamp.getDate() + ":" + timestamp.getYear();
				}
				comment.put("dateUpdated", date);
				comments.put(comment);
			}
			return true;
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				prepStat.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/*public static boolean selectPhoto(int photoId, JSONObject photo) { 
		Timestamp timestamp = null;
		String date = null;
		rs = null;
		try {
			conn = new DBConnection().getConnection();
			String query = "SELECT * FROM photos WHERE photo_id = ?";
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, photoId);
			rs = prepStat.executeQuery();
			if(rs.next()) {
				photo.put("photoId", rs.getInt("photo_id"));
				photo.put("userId", rs.getInt("user_id"));
				photo.put("albumId", rs.getInt("album_id"));
				photo.put("caption", rs.getString("caption"));
				photo.put("latitude", rs.getFloat("latitude"));
				photo.put("longitude", rs.getFloat("longitude"));
				photo.put("imagePath", rs.getString("image_path"));
				photo.put("imageSize", rs.getFloat("image_size"));
				photo.put("imageWidth", rs.getFloat("image_width"));
				photo.put("imageHeight", rs.getFloat("image_height"));
				photo.put("imageType", rs.getString("image_type"));
				timestamp = rs.getTimestamp("date_created");
				date = timestamp.getMonth() + ":" + timestamp.getDate() + ":" + timestamp.getYear();
				photo.put("dateCreated", date);
				timestamp = rs.getTimestamp("date_updated");
				if(timestamp != null) {
					date = timestamp.getMonth() + ":" + timestamp.getDate() + ":" + timestamp.getYear();
				}
				photo.put("dateUpdated", date);
			}
			return true;
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				prepStat.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}*/
	
	public static boolean createComment(int userId, int photoId, String comment) {
		queryStatus = 0;
		try {
			conn = new DBConnection().getConnection();
			String query = "INSERT INTO photo_comments (user_id, photo_id, comment) VALUES (?, ?, ?)";
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, userId);
			prepStat.setInt(2, photoId);
			prepStat.setString(3, comment);
			queryStatus = prepStat.executeUpdate();
			if(queryStatus > 0)
				return true;
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				prepStat.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public static boolean updateComment(int id, String comment) {
		// TODO: Need to give edit button to that user who's comment it is.
		queryStatus = 0;
		try {
			conn = new DBConnection().getConnection();
			String query = "UPDATE photo_comments SET comment = ? WHERE id = ?";
			prepStat = conn.prepareStatement(query);
			prepStat.setString(1, comment);
			prepStat.setInt(2, id);
			queryStatus = prepStat.executeUpdate();
			if(queryStatus > 0)
				return true;
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				prepStat.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public static boolean deleteComment(int id) {
		queryStatus = 0;
		try {
			conn = new DBConnection().getConnection();
			String query = "DELETE FROM photo_comments WHERE id = ?";
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, id);
			queryStatus = prepStat.executeUpdate();
			if(queryStatus > 0)
				return true;
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				prepStat.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}
