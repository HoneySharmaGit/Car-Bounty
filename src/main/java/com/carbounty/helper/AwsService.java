package com.carbounty.helper;

import java.io.File;
import java.io.FileOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;

@Service
public class AwsService {

	private final String bucketName = "carBountry";

	@Autowired
	private AmazonS3 s3Client;

	public String uploadImage(MultipartFile image) throws Exception {
		String imageName = image.getOriginalFilename() + System.currentTimeMillis();
		File file = new File(imageName);
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(image.getBytes());
			fos.flush();
			fos.close();

			// uploading image on AWS
			return uploadImageInAmazonS3(bucketName, imageName, file);
		} catch (Exception e) {
			throw e;
		}
	}

	private String uploadImageInAmazonS3(String bucketName, String imageName, File file) throws Exception {
		s3Client.putObject(new PutObjectRequest(bucketName, imageName, file));
		return s3Client.getUrl(bucketName, imageName).toString();
	}

}
