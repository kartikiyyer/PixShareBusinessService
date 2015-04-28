package com.appofy.pixshare.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class AlbumDAO {
	static Connection conn;
	static PreparedStatement prepStat;
	static ResultSet rs;
	static int queryStatus;
	static boolean daoSuccess;
	
	public static boolean selectAlbums(int userId, JSONArray albums) { 
		Timestamp timestamp = null;
		JSONObject album;
		String date = null;
		rs = null;
		try {
			conn = new DBConnection().getConnection();
			String query = "SELECT album_id, album_name FROM albums WHERE user_id = ?";
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, userId);
			rs = prepStat.executeQuery();
			while(rs.next()) {
				album = new JSONObject();
				album.put("albumId", rs.getInt("album_id"));
				album.put("albumName", rs.getString("album_name"));
				albums.put(album);
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
	
	public static boolean selectAlbum(int albumId, JSONObject album) { 
		Timestamp timestamp = null;
		String date = null;
		rs = null;
		try {
			conn = new DBConnection().getConnection();
			String query = "SELECT * FROM albums WHERE album_id = ?";
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, albumId);
			rs = prepStat.executeQuery();
			if(rs.next()) {
				album.put("albumId", rs.getInt("album_id"));
				album.put("albumName", rs.getString("album_name"));
				album.put("albumsDescription", rs.getString("albums_description"));
				album.put("latitude", rs.getFloat("latitude"));
				album.put("longitude", rs.getFloat("longitude"));
				timestamp = rs.getTimestamp("date_created");
				date = timestamp.getMonth() + ":" + timestamp.getDate() + ":" + timestamp.getYear();
				album.put("dateCreated", date);
				timestamp = rs.getTimestamp("date_updated");
				if(timestamp != null) {
					date = timestamp.getMonth() + ":" + timestamp.getDate() + ":" + timestamp.getYear();
				}
				album.put("dateUpdated", date);
				JSONArray comments = new JSONArray();
				daoSuccess = AlbumCommentDAO.selectComments(albumId, comments);
				if(daoSuccess) {
					album.put("comments", comments);
				} else {
					return false;
				}
				JSONArray likes = new JSONArray();
				daoSuccess = AlbumLikeDAO.selectLikes(albumId, likes);
				if(daoSuccess) {
					album.put("likes", likes);
				} else {
					return false;
				}
				return true;
			}
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
	
	public static boolean createAlbum(int userId, String albumName, String albumsDescription, float latitude, float longitude) {
		queryStatus = 0;
		try {
			conn = new DBConnection().getConnection();
			String query = "INSERT INTO albums (user_id, album_name, albums_description, latitude, longitude) VALUES (?, ?, ?, ?, ?)";
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, userId);
			prepStat.setString(2, albumName);
			prepStat.setString(3, albumsDescription);
			prepStat.setFloat(4, latitude);
			prepStat.setFloat(5, longitude);
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
	
	public static boolean updateAlbum(int albumId, String albumName, String albumsDescription, float latitude, float longitude) {
		queryStatus = 0;
		try {
			conn = new DBConnection().getConnection();
			String query = "UPDATE albums SET album_name = ?, albums_description = ?, latitude = ?, longitude = ? WHERE album_id = ?";
			prepStat = conn.prepareStatement(query);
			prepStat.setString(1, albumName);
			prepStat.setString(2, albumsDescription);
			prepStat.setFloat(3, latitude);
			prepStat.setFloat(4, longitude);
			prepStat.setInt(5, albumId);
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
	
	public static boolean deleteAlbum(int albumId) {
		queryStatus = 0;
		try {
			conn = new DBConnection().getConnection();
			String query = "DELETE FROM albums WHERE album_id = ?";
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, albumId);
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
	
	public static boolean selectShareGroupsOfAlbum(int albumId, JSONArray groups) { 
		JSONObject group;
		rs = null;
		try {
			conn = new DBConnection().getConnection();
			String query = "SELECT album_share_groups.share_group_id, group_name FROM album_share_groups "
					+ "INNER JOIN groups ON groups.group_id = album_share_groups.share_group_id "
					+ "WHERE album_id = ?";
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, albumId);
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
	
	
	public static boolean createShareAlbumToGroups(int albumId, ArrayList<Integer>shareGroupIds) {
		queryStatus = 0;
		try {
			conn = new DBConnection().getConnection();
			String query = "INSERT INTO album_share_groups (album_id, share_group_id) VALUES (?, ?)";
			prepStat = conn.prepareStatement(query);
			for (Integer shareGroupId : shareGroupIds) {
				queryStatus = 0;
				prepStat.setInt(1, albumId);
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
	
	public static boolean selectShareUsersOfAlbum(int albumId, JSONArray users) { 
		JSONObject user;
		rs = null;
		try {
			conn = new DBConnection().getConnection();
			String query = "SELECT album_share_users.share_user_id, user_name FROM album_share_users "
					+ "INNER JOIN users ON users.user_id = album_share_users.share_user_id "
					+ "WHERE album_id = ?";
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, albumId);
			rs = prepStat.executeQuery();
			while(rs.next()) {
				user = new JSONObject();
				user.put("shareUserId", rs.getInt("share_user_id"));
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
	
	public static boolean selectShareAlbumsForUser(int userId, JSONArray albums) { 
		JSONObject album;
		HashMap<Integer, String> albumsInfo = new HashMap<Integer, String>();
		rs = null;
		try {
			conn = new DBConnection().getConnection();
			String query = "select distinct(albums.album_id) AS album_id, albums.album_name from albums "
					+ "INNER JOIN album_share_groups ON album_share_groups.album_id = albums.album_id "
					+ "inner join users_groups on users_groups.group_id = album_share_groups.share_group_id "
					+ "where users_groups.user_id = ?";
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, userId);
			rs = prepStat.executeQuery();
			while(rs.next()) {
				albumsInfo.put(rs.getInt("album_id"), rs.getString("album_name"));
			}
			
			query = "select distinct(albums.album_id) AS album_id, albums.album_name from albums "
					+ "INNER JOIN album_share_users ON album_share_users.album_id = albums.album_id "
					+ "where album_share_users.share_user_id = ?";
			
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, userId);
			rs = prepStat.executeQuery();
			while(rs.next()) {
				albumsInfo.put(rs.getInt("album_id"), rs.getString("album_name"));
			}
			
			query = "select distinct(albums.album_id), albums.album_name FROM albums "
					+ "INNER JOIN photos ON photos.album_id = albums.album_id "
					+ "INNER JOIN photo_share_groups ON photo_share_groups.photo_id = photos.photo_id "
					+ "inner join users_groups on users_groups.group_id = photo_share_groups.share_group_id "
					+ "where users_groups.user_id = ?";
			
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, userId);
			rs = prepStat.executeQuery();
			while(rs.next()) {
				albumsInfo.put(rs.getInt("album_id"), rs.getString("album_name"));
			}
			
			query = "select distinct(albums.album_id), albums.album_name FROM albums "
					+ "INNER JOIN photos on photos.album_id = albums.album_id "
					+ "INNER JOIN photo_share_users ON photo_share_users.photo_id = photos.photo_id "
					+ "where photo_share_users.user_id = ?";
			
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, userId);
			rs = prepStat.executeQuery();
			while(rs.next()) {
				albumsInfo.put(rs.getInt("album_id"), rs.getString("album_name"));
			}
			
			Iterator<Entry<Integer, String>> it = albumsInfo.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry<Integer, String> pair = (Map.Entry<Integer, String>)it.next();
		        album = new JSONObject();
		        album.put("albumId", pair.getKey());
		        album.put("albumName", pair.getValue());
		        albums.put(album);
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
	
	public static boolean createShareAlbumToUsers(int albumId, ArrayList<Integer>shareUsersIds) {
		queryStatus = 0;
		try {
			conn = new DBConnection().getConnection();
			String query = "INSERT INTO album_share_users (album_id, share_user_id) VALUES (?, ?)";
			prepStat = conn.prepareStatement(query);
			for (Integer shareUsersId : shareUsersIds) {
				queryStatus = 0;
				prepStat.setInt(1, albumId);
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
	
	public static boolean updateShareAlbumToGroups(int albumId, ArrayList<Integer>shareGroupIds) {
		queryStatus = 0;
		try {
			conn = new DBConnection().getConnection();
			String query = "DELETE FROM album_share_groups WHERE album_id = ?";
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, albumId);
			queryStatus = prepStat.executeUpdate();
			daoSuccess = createShareAlbumToGroups(albumId, shareGroupIds);
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
	
	public static boolean updateShareAlbumToUsers(int albumId, ArrayList<Integer>shareUsersIds) {
		queryStatus = 0;
		try {
			conn = new DBConnection().getConnection();
			String query = "DELETE FROM album_share_users WHERE album_id = ?";
			prepStat = conn.prepareStatement(query);
			prepStat.setInt(1, albumId);
			queryStatus = prepStat.executeUpdate();
			daoSuccess = createShareAlbumToUsers(albumId, shareUsersIds);
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
