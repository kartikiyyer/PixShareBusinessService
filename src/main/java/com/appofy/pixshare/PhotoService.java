package com.appofy.pixshare;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;

import javax.imageio.ImageIO;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.appofy.pixshare.dao.AlbumCommentDAO;
import com.appofy.pixshare.dao.AlbumDAO;
import com.appofy.pixshare.dao.AlbumLikeDAO;
import com.appofy.pixshare.dao.PhotoCommentDAO;
import com.appofy.pixshare.dao.PhotoDAO;
import com.appofy.pixshare.dao.PhotoLikeDAO;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@Path("/photo")
public class PhotoService {

	@Path("/albums")
	@GET
	public Response getAlbums(@QueryParam("userId") int userId) {
		JSONObject albumsJSON = new JSONObject();
		JSONArray albums = new JSONArray();
		boolean daoSuccess = false;
		try {
			albumsJSON.put("userId", userId);
			albumsJSON.put("responseFlag", "fail");
			
			daoSuccess = AlbumDAO.selectAlbums(userId,albums);
			if(daoSuccess) {
				albumsJSON.put("albums", albums);
				albumsJSON.put("responseFlag", "success");
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(albumsJSON.toString()).build();
	}
	
	@Path("/album")
	@GET
	public Response getAlbum(@QueryParam("albumId") int albumId) {
		JSONObject albumJSON = new JSONObject();
		JSONObject album = new JSONObject();
		boolean daoSuccess = false;
		try {
			albumJSON.put("albumId", albumId);
			albumJSON.put("responseFlag", "fail");
			
			daoSuccess = AlbumDAO.selectAlbum(albumId,album);
			if(daoSuccess) {
				albumJSON.put("album", album);
				albumJSON.put("responseFlag", "success");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(albumJSON.toString()).build();
	}
	
	@Path("/album")
	@POST
	public Response createAlbum(@FormParam("userId") int userId, @FormParam("albumName") String albumName,
			@FormParam("albumsDescription") String albumsDescription, @FormParam("latitude") float latitude,
			@FormParam("longitude") float longitude) {
		JSONObject albumJSON = new JSONObject();
		boolean daoSuccess = false;
		try {
			albumJSON.put("userId", userId);
			albumJSON.put("responseFlag", "fail");
			// TODO: Need to check with Android.
			//float lat = Float.parseFloat(latitude);
			//float lon = Float.parseFloat(longitude);
			daoSuccess = AlbumDAO.createAlbum(userId, albumName, albumsDescription, latitude, longitude);
			if(daoSuccess) {
				albumJSON.put("responseFlag", "success");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(albumJSON.toString()).build();
	}
	
	@Path("/album")
	@PUT
	public Response updateAlbum(@FormParam("albumId") int albumId, @FormParam("albumName") String albumName,
			@FormParam("albumsDescription") String albumsDescription, @FormParam("latitude") float latitude,
			@FormParam("longitude") float longitude) {
		JSONObject albumJSON = new JSONObject();
		boolean daoSuccess = false;
		try {
			albumJSON.put("albumId", albumId);
			albumJSON.put("responseFlag", "fail");
			// TODO: Need to check with Android.
			//float lat = Float.parseFloat(latitude);
			//float lon = Float.parseFloat(longitude);
			daoSuccess = AlbumDAO.updateAlbum(albumId, albumName, albumsDescription, latitude, longitude);
			if(daoSuccess) {
				albumJSON.put("responseFlag", "success");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(albumJSON.toString()).build();
	}
	
	@Path("/album")
	@DELETE
	public Response deleteAlbum(@FormParam("albumId") int albumId) {
		JSONObject albumJSON = new JSONObject();
		boolean daoSuccess = false;
		try {
			albumJSON.put("albumId", albumId);
			albumJSON.put("responseFlag", "fail");
			daoSuccess = AlbumDAO.deleteAlbum(albumId);
			if(daoSuccess) {
				albumJSON.put("responseFlag", "success");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(albumJSON.toString()).build();
	}
	
	@Path("/album/photo")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response createPhoto(@FormDataParam("userId") int userId, @FormDataParam("albumId") int albumId,
			@FormDataParam("caption") String caption, @FormDataParam("latitude") float latitude,
			@FormDataParam("longitude") float longitude, @FormDataParam("file") File photoObject,
			@FormDataParam("file") FormDataContentDisposition contentDispositionHeader) {
		JSONObject photoJSON = new JSONObject();
		boolean daoSuccess = false;
		String imagePath = new String();
		long imageSize, imageWidth, imageHeight;
		String imageType;
		boolean fileSuccess = false;
		try {
			photoJSON.put("userId", userId);
			photoJSON.put("albumId", albumId);
			photoJSON.put("responseFlag", "fail");
			// TODO: Need to check with Android.
			//float lat = Float.parseFloat(latitude);
			//float lon = Float.parseFloat(longitude);
			AWSS3BucketHandling awss3BucketHandling=new AWSS3BucketHandling();
			imagePath =awss3BucketHandling.addS3BucketObjects(photoObject, contentDispositionHeader);
			/*Calendar time = Calendar.getInstance();
			String fileName=Integer.toString(time.get(Calendar.HOUR))+Integer.toString(time.get(Calendar.MINUTE))+Integer.toString(time.get(Calendar.MILLISECOND))+contentDispositionHeader.getFileName();
			 
			System.out.println("Working Directory = " +
		              System.getProperty("user.dir"));
			String rootPath = System.getProperty("user.dir");
			rootPath = rootPath.substring(0,rootPath.lastIndexOf(File.separatorChar));
			rootPath += File.separator + "webapps" + File.separator + "pixsharebusinessservice" + File.separator + "photos";  
			imagePath = rootPath + File.separator + fileName;*/
			fileSuccess = copyFileUsingFileStreams(photoObject, new File(imagePath));
			
			if(fileSuccess) {
				BufferedImage image = ImageIO.read(photoObject);
				imageSize = photoObject.length();
				// TODO: Need to find content type.
				//imageType = contentDispositionHeader.getType();
				imageType = "jpeg";
				imageWidth = image.getWidth();
				imageHeight = image.getHeight();
				daoSuccess = PhotoDAO.createPhoto(userId, albumId, caption, latitude, longitude, imagePath, imageSize,
						imageWidth, imageHeight, imageType);
				if(daoSuccess) {
					photoJSON.put("responseFlag", "success");
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(photoJSON.toString()).build();
	}
	
	@Path("/album/photo")
	@PUT
	public Response updatePhoto(@FormParam("photoId") int photoId, @FormParam("caption") String caption, 
			@FormParam("latitude") float latitude, @FormParam("longitude") float longitude) {
		JSONObject photoJSON = new JSONObject();
		boolean daoSuccess = false;
		try {
			photoJSON.put("photoId", photoId);
			photoJSON.put("responseFlag", "fail");
			// TODO: Need to check with Android.
			//float lat = Float.parseFloat(latitude);
			//float lon = Float.parseFloat(longitude);
			
			daoSuccess = PhotoDAO.updatePhoto(photoId, caption, latitude, longitude);
			if(daoSuccess) {
				photoJSON.put("responseFlag", "success");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(photoJSON.toString()).build();
	}
	
	@Path("/album/photo")
	@DELETE
	public Response deletePhoto(@FormParam("photoId") int photoId) {
		JSONObject photoJSON = new JSONObject();
		boolean daoSuccess = false;
		try {
			photoJSON.put("photoId", photoId);
			photoJSON.put("responseFlag", "fail");
			daoSuccess = PhotoDAO.deletePhoto(photoId);
			if(daoSuccess) {
				photoJSON.put("responseFlag", "success");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(photoJSON.toString()).build();
	}
	
	@Path("/album/photos")
	@GET
	public Response getPhotos(@QueryParam("albumId") int albumId) {
		JSONObject photosJSON = new JSONObject();
		JSONArray photos = new JSONArray();
		boolean daoSuccess = false;
		try {
			photosJSON.put("albumId", albumId);
			photosJSON.put("responseFlag", "fail");
			
			daoSuccess = PhotoDAO.selectPhotos(albumId,photos);
			if(daoSuccess) {
				photosJSON.put("photos", photos);
				photosJSON.put("responseFlag", "success");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(photosJSON.toString()).build();
	}
	
	@Path("/album/photo")
	@GET
	public Response getPhoto(@QueryParam("photoId") int photoId) {
		JSONObject photoJSON = new JSONObject();
		JSONObject photo = new JSONObject();
		boolean daoSuccess = false;
		try {
			photoJSON.put("photoId", photoId);
			photoJSON.put("responseFlag", "fail");
			
			daoSuccess = PhotoDAO.selectPhoto(photoId,photo);
			if(daoSuccess) {
				photoJSON.put("photo", photo);
				photoJSON.put("responseFlag", "success");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(photoJSON.toString()).build();
	}
	
	@Path("/album/photo/comment")
	@POST
	public Response createPhotoComment(@FormParam("userId") int userId, @FormParam("photoId") int photoId,
			@FormParam("comment") String comment) {
		JSONObject commentJSON = new JSONObject();
		boolean daoSuccess = false;
		try {
			commentJSON.put("userId", userId);
			commentJSON.put("photoId", photoId);
			commentJSON.put("comment", comment);
			commentJSON.put("responseFlag", "fail");
			daoSuccess = PhotoCommentDAO.createComment(userId, photoId, comment);
			if(daoSuccess) {
				commentJSON.put("responseFlag", "success");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(commentJSON.toString()).build();
	}
	
	@Path("/album/photo/comment")
	@PUT
	public Response updatePhotoComment(@FormParam("id") int id, @FormParam("comment") String comment) {
		JSONObject commentJSON = new JSONObject();
		boolean daoSuccess = false;
		try {
			commentJSON.put("id", id);
			commentJSON.put("responseFlag", "fail");
			daoSuccess = PhotoCommentDAO.updateComment(id, comment);
			if(daoSuccess) {
				commentJSON.put("responseFlag", "success");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(commentJSON.toString()).build();
	}
	
	@Path("/album/photo/comment")
	@DELETE
	public Response deletePhotoComment(@FormParam("id") int id) {
		JSONObject commentJSON = new JSONObject();
		boolean daoSuccess = false;
		try {
			commentJSON.put("id", id);
			commentJSON.put("responseFlag", "fail");
			daoSuccess = PhotoCommentDAO.deleteComment(id);
			if(daoSuccess) {
				commentJSON.put("responseFlag", "success");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(commentJSON.toString()).build();
	}
	
	@Path("/album/comment")
	@POST
	public Response createAlbumComment(@FormParam("userId") int userId, @FormParam("albumId") int albumId,
			@FormParam("comment") String comment) {
		JSONObject commentJSON = new JSONObject();
		boolean daoSuccess = false;
		try {
			commentJSON.put("userId", userId);
			commentJSON.put("photoId", albumId);
			commentJSON.put("comment", comment);
			commentJSON.put("responseFlag", "fail");
			daoSuccess = AlbumCommentDAO.createComment(userId, albumId, comment);
			if(daoSuccess) {
				commentJSON.put("responseFlag", "success");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(commentJSON.toString()).build();
	}
	
	@Path("/album/comment")
	@PUT
	public Response updateAlbumComment(@FormParam("id") int id, @FormParam("comment") String comment) {
		JSONObject commentJSON = new JSONObject();
		boolean daoSuccess = false;
		try {
			commentJSON.put("id", id);
			commentJSON.put("responseFlag", "fail");
			daoSuccess = AlbumCommentDAO.updateComment(id, comment);
			if(daoSuccess) {
				commentJSON.put("responseFlag", "success");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(commentJSON.toString()).build();
	}
	
	@Path("/album/comment")
	@DELETE
	public Response deleteAlbumComment(@FormParam("id") int id) {
		JSONObject commentJSON = new JSONObject();
		boolean daoSuccess = false;
		try {
			commentJSON.put("id", id);
			commentJSON.put("responseFlag", "fail");
			daoSuccess = AlbumCommentDAO.deleteComment(id);
			if(daoSuccess) {
				commentJSON.put("responseFlag", "success");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(commentJSON.toString()).build();
	}
	
	@Path("/album/photo/like")
	@POST
	public Response createPhotoLike(@FormParam("userId") int userId, @FormParam("photoId") int photoId) {
		JSONObject likeJSON = new JSONObject();
		boolean daoSuccess = false;
		try {
			likeJSON.put("userId", userId);
			likeJSON.put("photoId", photoId);
			likeJSON.put("responseFlag", "fail");
			daoSuccess = PhotoLikeDAO.createLike(userId, photoId);
			if(daoSuccess) {
				likeJSON.put("responseFlag", "success");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(likeJSON.toString()).build();
	}
	
	@Path("/album/photo/like")
	@DELETE
	public Response deletePhotoLike(@FormParam("userId") int userId, @FormParam("photoId") int photoId) {
		JSONObject likeJSON = new JSONObject();
		boolean daoSuccess = false;
		try {
			likeJSON.put("userId", userId);
			likeJSON.put("photoId", photoId);
			likeJSON.put("responseFlag", "fail");
			daoSuccess = PhotoLikeDAO.deleteLike(userId, photoId);
			if(daoSuccess) {
				likeJSON.put("responseFlag", "success");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(likeJSON.toString()).build();
	}
	
	@Path("/album/like")
	@POST
	public Response createAlbumLike(@FormParam("userId") int userId, @FormParam("albumId") int albumId) {
		JSONObject likeJSON = new JSONObject();
		boolean daoSuccess = false;
		try {
			likeJSON.put("userId", userId);
			likeJSON.put("albumId", albumId);
			likeJSON.put("responseFlag", "fail");
			daoSuccess = AlbumLikeDAO.createLike(userId, albumId);
			if(daoSuccess) {
				likeJSON.put("responseFlag", "success");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(likeJSON.toString()).build();
	}
	
	@Path("/album/like")
	@DELETE
	public Response deleteAlbumLike(@FormParam("userId") int userId, @FormParam("albumId") int albumId) {
		JSONObject likeJSON = new JSONObject();
		boolean daoSuccess = false;
		try {
			likeJSON.put("userId", userId);
			likeJSON.put("albumId", albumId);
			likeJSON.put("responseFlag", "fail");
			daoSuccess = AlbumLikeDAO.deleteLike(userId, albumId);
			if(daoSuccess) {
				likeJSON.put("responseFlag", "success");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(likeJSON.toString()).build();
	}
	
	@Path("/album/photo/groups")
	@GET
	public Response getShareGroupsOfPhoto(@QueryParam("photoId") int photoId) {
		JSONObject groupsJSON = new JSONObject();
		JSONArray groups = new JSONArray();
		boolean daoSuccess = false;
		try {
			groupsJSON.put("photoId", photoId);
			groupsJSON.put("responseFlag", "fail");
			
			daoSuccess = PhotoDAO.selectShareGroupsOfPhoto(photoId, groups);
			if(daoSuccess) {
				groupsJSON.put("groups", groups);
				groupsJSON.put("responseFlag", "success");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(groupsJSON.toString()).build();
	}
	
	@Path("/album/photo/users")
	@GET
	public Response getShareUsersOfPhoto(@QueryParam("photoId") int photoId) {
		JSONObject usersJSON = new JSONObject();
		JSONArray users = new JSONArray();
		boolean daoSuccess = false;
		try {
			usersJSON.put("photoId", photoId);
			usersJSON.put("responseFlag", "fail");
			
			daoSuccess = PhotoDAO.selectShareUsersOfPhoto(photoId, users);
			if(daoSuccess) {
				usersJSON.put("users", users);
				usersJSON.put("responseFlag", "success");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(usersJSON.toString()).build();
	}
	
	@Path("/album/photo/groups")
	@PUT
	public Response updateSharePhotoToGroups(@FormParam("photoId") int photoId, @FormParam("shareGroupIds") String shareGroupIds) {
		JSONObject groupsJSON = new JSONObject();
		ArrayList<Integer> groups = new ArrayList<Integer>();
		boolean daoSuccess = false;
		try {
			JSONObject groupsString = new JSONObject(shareGroupIds);
			JSONArray groupsArrayString = (JSONArray)groupsString.get("shareGroupIds");
			groupsJSON.put("shareGroupIds", groupsArrayString);
			groupsJSON.put("photoId", photoId);
			groupsJSON.put("responseFlag", "fail");
			
			int count = groupsArrayString.length(); 
			for(int i=0 ; i< count; i++){   
				JSONObject jsonObject = groupsArrayString.getJSONObject(i);  
				groups.add(jsonObject.getInt("shareGroupId"));
			}
			daoSuccess = PhotoDAO.updateSharePhotoToGroups(photoId, groups);
			if(daoSuccess) {
				groupsJSON.put("responseFlag", "success");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(groupsJSON.toString()).build();
	}
	
	@Path("/album/photo/users")
	@PUT
	public Response updateSharePhotoToUsers(@FormParam("photoId") int photoId, @FormParam("shareUserIds") String shareUserIds) {
		JSONObject usersJSON = new JSONObject();
		ArrayList<Integer> users = new ArrayList<Integer>();
		boolean daoSuccess = false;
		try {
			JSONObject usersString = new JSONObject(shareUserIds);
			JSONArray usersArrayString =(JSONArray)usersString.get("shareUserIds");
			usersJSON.put("shareUserIds", usersArrayString);
			usersJSON.put("photoId", photoId);
			usersJSON.put("responseFlag", "fail");
			
			int count = usersArrayString.length(); 
			for(int i=0 ; i< count; i++){   
				JSONObject jsonObject = usersArrayString.getJSONObject(i);  
				users.add(jsonObject.getInt("shareUserId"));
			}
			daoSuccess = PhotoDAO.updateSharePhotoToUsers(photoId, users);
			if(daoSuccess) {
				usersJSON.put("responseFlag", "success");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(usersJSON.toString()).build();
	}

	@Path("/album/groups")
	@GET
	public Response getShareGroupsOfAlbum(@QueryParam("albumId") int albumId) {
		JSONObject groupsJSON = new JSONObject();
		JSONArray groups = new JSONArray();
		boolean daoSuccess = false;
		try {
			groupsJSON.put("albumId", albumId);
			groupsJSON.put("responseFlag", "fail");
			
			daoSuccess = AlbumDAO.selectShareGroupsOfAlbum(albumId, groups);
			if(daoSuccess) {
				groupsJSON.put("groups", groups);
				groupsJSON.put("responseFlag", "success");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(groupsJSON.toString()).build();
	}
	
	@Path("/album/users")
	@GET
	public Response getShareUsersOfAlbum(@QueryParam("albumId") int albumId) {
		JSONObject usersJSON = new JSONObject();
		JSONArray users = new JSONArray();
		boolean daoSuccess = false;
		try {
			usersJSON.put("albumId", albumId);
			usersJSON.put("responseFlag", "fail");
			
			daoSuccess = AlbumDAO.selectShareUsersOfAlbum(albumId, users);
			if(daoSuccess) {
				usersJSON.put("users", users);
				usersJSON.put("responseFlag", "success");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(usersJSON.toString()).build();
	}
	
	@Path("/album/groups")
	@PUT
	public Response updateShareAlbumToGroups(@FormParam("albumId") int albumId, @FormParam("shareGroupIds") String shareGroupIds) {
		JSONObject groupsJSON = new JSONObject();
		ArrayList<Integer> groups = new ArrayList<Integer>();
		boolean daoSuccess = false;
		try {
			JSONObject groupsString = new JSONObject(shareGroupIds);
			JSONArray groupsArrayString = (JSONArray)groupsString.get("shareGroupIds");
			groupsJSON.put("shareGroupIds", groupsArrayString);
			groupsJSON.put("albumId", albumId);
			groupsJSON.put("responseFlag", "fail");
			
			int count = groupsArrayString.length(); 
			for(int i=0 ; i< count; i++){   
				JSONObject jsonObject = groupsArrayString.getJSONObject(i);  
				groups.add(jsonObject.getInt("shareGroupId"));
			}
			daoSuccess = AlbumDAO.updateShareAlbumToGroups(albumId, groups);
			if(daoSuccess) {
				groupsJSON.put("responseFlag", "success");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(groupsJSON.toString()).build();
	}
	
	@Path("/album/users")
	@PUT
	public Response updateShareAlbumToUsers(@FormParam("albumId") int albumId, @FormParam("shareUserIds") String shareUserIds) {
		JSONObject usersJSON = new JSONObject();
		ArrayList<Integer> users = new ArrayList<Integer>();
		boolean daoSuccess = false;
		try {
			JSONObject usersString = new JSONObject(shareUserIds);
			JSONArray usersArrayString = (JSONArray)usersString.get("shareUserIds");
			usersJSON.put("shareUserIds", usersArrayString);
			usersJSON.put("albumId", albumId);
			usersJSON.put("responseFlag", "fail");
			
			int count = usersArrayString.length(); 
			for(int i=0 ; i< count; i++){   
				JSONObject jsonObject = usersArrayString.getJSONObject(i);  
				users.add(jsonObject.getInt("shareUserId"));
			}
			daoSuccess = AlbumDAO.updateShareAlbumToUsers(albumId, users);
			if(daoSuccess) {
				usersJSON.put("responseFlag", "success");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(usersJSON.toString()).build();
	}
	
	@Path("/user/albums")
	@GET
	public Response getShareAlbumsForUser(@QueryParam("userId") int userId) {
		JSONObject albumsJSON = new JSONObject();
		JSONArray albums = new JSONArray();
		boolean daoSuccess = false;
		try {
			albumsJSON.put("userId", userId);
			albumsJSON.put("responseFlag", "fail");
			
			daoSuccess = AlbumDAO.selectShareAlbumsForUser(userId, albums);
			if(daoSuccess) {
				albumsJSON.put("albums", albums);
				albumsJSON.put("responseFlag", "success");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(albumsJSON.toString()).build();
	}
	
	
	@Path("/user/album/photos")
	@GET
	public Response getSharePhotosForUser(@QueryParam("userId") int userId, @QueryParam("albumId") int albumId) {
		JSONObject photosJSON = new JSONObject();
		JSONArray photos = new JSONArray();
		boolean daoSuccess = false;
		try {
			photosJSON.put("userId", userId);
			photosJSON.put("responseFlag", "fail");
			
			daoSuccess = PhotoDAO.selectSharePhotosForUser(userId, albumId, photos);
			if(daoSuccess) {
				photosJSON.put("photos", photos);
				photosJSON.put("responseFlag", "success");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(photosJSON.toString()).build();
	}
	
	private static boolean copyFileUsingFileStreams(File source, File dest) throws IOException {
	    InputStream input = null;
	    OutputStream output = null;
	    boolean fileSuccess = false;
	    try {
	        input = new FileInputStream(source);
	        output = new FileOutputStream(dest);
	        byte[] buf = new byte[1024];
	        int bytesRead;
	        while ((bytesRead = input.read(buf)) > 0) {
	            output.write(buf, 0, bytesRead);
	        }
	        fileSuccess = true;
	    } finally {
	        input.close();
	        output.close();
	    }
	    return fileSuccess;
	}

}
