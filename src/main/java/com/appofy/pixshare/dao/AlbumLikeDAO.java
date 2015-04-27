package com.appofy.pixshare.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.json.JSONArray;
import org.json.JSONObject;

public class AlbumLikeDAO {
	static Connection conn;
	static PreparedStatement prepStat;
	static ResultSet rs;
	static int queryStatus;
	public static boolean selectLikes(int albumId, JSONArray likes) { 
		JSONObject like;
		rs = null;
		Timestamp timestamp = null;
		String date = null;
		try {
			conn = new DBConnection().getConnection();
			String query = "SELECT album_likes.user_id, user_name, first_name, last_name, album_likes.date_created, album_likes.date_updated FROM album_likes "
					+ "INNER JOIN users ON users.user_id = album_likes.user_id "
					+ "WHERE album_id = ?";
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, albumId);
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
	
	public static boolean selectCountOfLikes(int albumId, JSONObject album) { 
		rs = null;
		try {
			conn = new DBConnection().getConnection();
			String query = "SELECT count(user_id) FROM album_likes WHERE album_id = ?";
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, albumId);
			rs = prepStat.executeQuery();
			if(rs.next()) {
				album.put("likeCount", rs.getInt(1));
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
	
	public static boolean createLike(int userId, int albumId) {
		queryStatus = 0;
		try {
			conn = new DBConnection().getConnection();
			String query = "INSERT INTO album_likes (user_id, album_id) VALUES (?, ?)";
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, userId);
			prepStat.setInt(2, albumId);
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
	
	public static boolean deleteLike(int userId, int albumId) {
		queryStatus = 0;
		try {
			conn = new DBConnection().getConnection();
			String query = "DELETE FROM album_likes WHERE user_id = ? AND album_id = ?";
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, userId);
			prepStat.setInt(2, albumId);
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
