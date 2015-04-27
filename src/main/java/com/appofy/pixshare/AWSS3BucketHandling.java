package com.appofy.pixshare;

import java.io.File;
import java.util.Calendar;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.sun.jersey.core.header.FormDataContentDisposition;

public class AWSS3BucketHandling {
	public String addS3BucketObjects( File fileobject,FormDataContentDisposition contentDispositionHeader) throws Exception{
		String imagePath = null;
		AmazonS3Client s3 = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());
		Region usWest2 = Region.getRegion(Regions.US_WEST_1);
		s3.setRegion(usWest2);
		String bucketName="pixsharebucket";
		
		Calendar time = Calendar.getInstance();
		String fileName=Integer.toString(time.get(Calendar.HOUR))+Integer.toString(time.get(Calendar.MINUTE))+Integer.toString(time.get(Calendar.MILLISECOND))+contentDispositionHeader.getFileName();
		System.out.println("fileName: "+fileName);
		
		
		try {
			System.out.println("\nUploading a new object to S3...");
			System.out.println("upload file name:: "+fileName);

			PutObjectRequest putObj=new PutObjectRequest(bucketName, fileName, fileobject);

			//making the object Public
			putObj.setCannedAcl(CannedAccessControlList.PublicRead);
			s3.putObject(putObj);

			//AmazonS3Client awss3client=new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());

			System.out.println("File uploaded on S3 - location: "+bucketName+" -> "+fileName);
			
			imagePath = s3.getResourceUrl(bucketName, fileName);
			return imagePath;
		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which means your request made it "
					+ "to Amazon S3, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with S3, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
		return imagePath;
	}
}
