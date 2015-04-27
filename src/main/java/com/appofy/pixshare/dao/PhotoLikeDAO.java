package com.appofy.pixshare.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.json.JSONArray;
import org.json.JSONObject;

public class PhotoLikeDAO {
	static Connection conn;
	static PreparedStatement prepStat;
	static ResultSet rs;
	static int queryStatus;
	public static boolean selectLikes(int photoId, JSONArray likes) { 
		JSONObject like;
		rs = null;
		Timestamp timestamp = null;
		String date = null;
		try {
			conn = new DBConnection().getConnection();
			String query = "SELECT photo_likes.user_id, user_name, first_name, last_name, photo_likes.date_created, photo_likes.date_updated FROM photo_likes "
					+ "INNER JOIN users ON users.user_id = photo_likes.user_id "
					+ "WHERE photo_id = ?";
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, photoId);
			rs = prepStat.executeQuery();
			while(rs.next()) {
				like = new JSONObject();
				like.put("userId", rs.getInt("user_id"));
				like.put("userName", rs.getString("user_name"));
				like.put("firstName", rs.getString("first_name"));
				like.put("lastName", rs.getString("last_name"));
				timestamp = rs.getTimestamp("date_created");
				date = timestamp.getMonth() + ":" + timestamp.getDate() + ":" + timestamp.getYear();
				like.put("dateCreated", date);
				timestamp = rs.getTimestamp("date_updated");
				if(timestamp != null) {
					date = timestamp.getMonth() + ":" + timestamp.getDate() + ":" + timestamp.getYear();
				}
				like.put("dateUpdated", date);
				likes.put(like);
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
	
	public static boolean selectCountOfLikes(int photoId, JSONObject photo) { 
		rs = null;
		try {
			conn = new DBConnection().getConnection();
			String query = "SELECT count(user_id) FROM photo_likes WHERE photo_id = ?";
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, photoId);
			rs = prepStat.executeQuery();
			if(rs.next()) {
				photo.put("likeCount", rs.getInt(1));
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
	
	public static boolean createLike(int userId, int photoId) {
		queryStatus = 0;
		try {
			conn = new DBConnection().getConnection();
			String query = "INSERT INTO photo_likes (user_id, photo_id) VALUES (?, ?)";
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, userId);
			prepStat.setInt(2, photoId);
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
	
	public static boolean deleteLike(int userId, int photoId) {
		queryStatus = 0;
		try {
			conn = new DBConnection().getConnection();
			String query = "DELETE FROM photo_likes WHERE user_id = ? AND photo_id = ?";
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, userId);
			prepStat.setInt(2, photoId);
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
