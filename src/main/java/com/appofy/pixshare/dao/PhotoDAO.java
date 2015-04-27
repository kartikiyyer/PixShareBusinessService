package com.appofy.pixshare.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class PhotoDAO {
	static Connection conn;
	static PreparedStatement prepStat;
	static ResultSet rs;
	static int queryStatus;
	static boolean daoSuccess;
	
	public static boolean selectPhotos(int albumId, JSONArray photos) { 
		JSONObject photo;
		rs = null;
		try {
			conn = new DBConnection().getConnection();
			String query = "SELECT photo_id, image_path FROM photos WHERE album_id = ?";
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, albumId);
			rs = prepStat.executeQuery();
			while(rs.next()) {
				photo = new JSONObject();
				photo.put("photoId", rs.getInt("photo_id"));
				photo.put("imagePath", rs.getString("image_path"));
				photos.put(photo);
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
	
	public static boolean selectPhoto(int photoId, JSONObject photo) { 
		Timestamp timestamp = null;
		String date = null;
		rs = null;
		daoSuccess = false;
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
				JSONArray comments = new JSONArray();
				daoSuccess = PhotoCommentDAO.selectComments(photoId, comments);
				if(daoSuccess) {
					photo.put("comments", comments);
				} else {
					return false;
				}
				JSONArray likes = new JSONArray();
				daoSuccess = PhotoLikeDAO.selectLikes(photoId, likes);
				if(daoSuccess) {
					photo.put("likes", likes);
				} else {
					return false;
				}
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
	
	public static boolean createPhoto(int userId, int albumId, String caption, float latitude, float longitude, String imagePath, long imageSize,
			long imageWidth, long imageHeight, String imageType) {
		queryStatus = 0;
		try {
			conn = new DBConnection().getConnection();
			String query = "INSERT INTO photos (user_id, album_id, caption, latitude, longitude, image_path, image_size, image_width, image_height, image_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, userId);
			prepStat.setInt(2, albumId);
			prepStat.setString(3, caption);
			prepStat.setFloat(4, latitude);
			prepStat.setFloat(5, longitude);
			prepStat.setString(6, imagePath);
			prepStat.setLong(7, imageSize);
			prepStat.setLong(8, imageWidth);
			prepStat.setLong(9, imageHeight);
			prepStat.setString(10, imageType);
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
	
	public static boolean updatePhoto(int photoId, String caption, float latitude, float longitude) {
		queryStatus = 0;
		try {
			conn = new DBConnection().getConnection();
			String query = "UPDATE photos SET caption = ?, latitude = ?, longitude = ? WHERE photo_id = ?";
			prepStat = conn.prepareStatement(query);
			prepStat.setString(1, caption);
			prepStat.setFloat(2, latitude);
			prepStat.setFloat(3, longitude);
			prepStat.setInt(4, photoId);
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
	
	public static boolean deletePhoto(int photoId) {
		queryStatus = 0;
		try {
			conn = new DBConnection().getConnection();
			String query = "DELETE FROM photos WHERE photo_id = ?";
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, photoId);
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
	
	public static boolean selectShareGroupsOfPhoto(int photoId, JSONArray groups) { 
		JSONObject group;
		rs = null;
		try {
			conn = new DBConnection().getConnection();
			String query = "SELECT photo_share_groups.share_group_id, group_name FROM photo_share_groups "
					+ "INNER JOIN groups ON groups.group_id = photo_share_groups.share_group_id "
					+ "WHERE photo_id = ?";
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, photoId);
			rs = prepStat.executeQuery();
			while(rs.next()) {
				group = new JSONObject();
				group.put("shareGroupId", rs.getInt("share_group_id"));
				group.put("groupName", rs.getString("group_name"));
				groups.put(group);
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
	
	
	public static boolean createSharePhotoToGroups(int photoId, ArrayList<Integer>shareGroupIds) {
		queryStatus = 0;
		try {
			conn = new DBConnection().getConnection();
			String query = "INSERT INTO photo_share_groups (photo_id, share_group_id) VALUES (?, ?)";
			prepStat = conn.prepareStatement(query);
			for (Integer shareGroupId : shareGroupIds) {
				queryStatus = 0;
				prepStat.setInt(1, photoId);
				prepStat.setInt(2, shareGroupId);
				queryStatus = prepStat.executeUpdate();
				if(queryStatus == 0)
					return false;
				
			}
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
	
	public static boolean selectShareUsersOfPhoto(int photoId, JSONArray users) { 
		JSONObject user;
		rs = null;
		try {
			conn = new DBConnection().getConnection();
			String query = "SELECT photo_share_users.user_id, user_name FROM photo_share_users "
					+ "INNER JOIN users ON users.user_id = photo_share_users.user_id "
					+ "WHERE photo_id = ?";
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, photoId);
			rs = prepStat.executeQuery();
			while(rs.next()) {
				user = new JSONObject();
				user.put("userId", rs.getInt("user_id"));
				user.put("userName", rs.getString("user_name"));
				users.put(user);
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
	
	public static boolean createSharePhotoToUsers(int photoId, ArrayList<Integer>shareUsersIds) {
		queryStatus = 0;
		try {
			conn = new DBConnection().getConnection();
			String query = "INSERT INTO photo_share_users (photo_id, user_id) VALUES (?, ?)";
			prepStat = conn.prepareStatement(query);
			for (Integer shareUsersId : shareUsersIds) {
				queryStatus = 0;
				prepStat.setInt(1, photoId);
				prepStat.setInt(2, shareUsersId);
				queryStatus = prepStat.executeUpdate();
				if(queryStatus == 0)
					return false;
				
			}
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
	
	public static boolean updateSharePhotoToGroups(int photoId, ArrayList<Integer>shareGroupIds) {
		queryStatus = 0;
		try {
			conn = new DBConnection().getConnection();
			String query = "DELETE FROM photo_share_groups WHERE photo_id = ?";
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, photoId);
			queryStatus = prepStat.executeUpdate();
			daoSuccess = createSharePhotoToGroups(photoId, shareGroupIds);
			if(daoSuccess)
				return true;
			return false;
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
	
	public static boolean updateSharePhotoToUsers(int photoId, ArrayList<Integer>shareUsersIds) {
		queryStatus = 0;
		try {
			conn = new DBConnection().getConnection();
			String query = "DELETE FROM photo_share_users WHERE photo_id = ?";
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, photoId);
			queryStatus = prepStat.executeUpdate();
			daoSuccess = createSharePhotoToUsers(photoId, shareUsersIds);
			if(daoSuccess)
				return true;
			return false;
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
