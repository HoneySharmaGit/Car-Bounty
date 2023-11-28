package com.carbounty.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/* class to demonstrate use of Drive files list API */

@Controller
@CrossOrigin("*")
public class GoogleDriveService {

//	private static final String APPLICATION_NAME = "Car Bounty Web-Application";
//
//	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
//
//	private static final String TOKENS_DIRECTORY_PATH = "tokens";
//
//	private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
//
//	private static final String CREDENTIALS_FILE_PATH = "classpath:keys/credentials.json";
//
//	private static Credential getCredentials(final HttpTransport HTTP_TRANSPORT) throws IOException {
//		JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
//		try {
//			GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory,
//					new InputStreamReader(GoogleDriveService.class.getResourceAsStream(CREDENTIALS_FILE_PATH)));
//
//			GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, jsonFactory,
//					clientSecrets, SCOPES)
//					.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH))).build();
//			Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver())
//					.authorize("user");
//			Credential credential1 = new Credential(BearerToken.authorizationHeaderAccessMethod())
//					.setAccessToken(TOKENS_DIRECTORY_PATH);
//			System.out.println("Successfully authorized: " + credential.getAccessToken());
//			return credential1;
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
//
//	public String uploadGoogleDriveFile(MultipartFile image) {
//		final NetHttpTransport HTTP_TRANSPORT;
//		try {
//			System.out.println("honeyyyyyyyyyyyyy");
//			HTTP_TRANSPORT = new NetHttpTransport();
//			Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
//					.setApplicationName(APPLICATION_NAME).build();
//			System.out.println("Drive object -> " + service);
//
//			String folderId = "1VW6rf4nL8co6TPNeUiXOXFArF-yrUSnQ";
//			String imageName = image.getOriginalFilename() + System.currentTimeMillis();
//			File fileMetaData = new File();
//			fileMetaData.setName(imageName);
//			fileMetaData.setParents(Collections.singletonList(folderId));
//
//			java.io.File file = new java.io.File(imageName);
//			FileOutputStream fos = new FileOutputStream(file);
//			fos.write(image.getBytes());
//			fos.flush();
//			fos.close();
//
//			FileContent content = new FileContent(image.getContentType(), file);
//
//			File uploadedFile = service.files().create(fileMetaData, content).setFields("id").execute();
//			System.out.println("File ID: " + uploadedFile.getId());
//			return uploadedFile.getId();
//		} catch (IOException e) {
//			e.printStackTrace();
//			return "error -> " + e.getMessage();
//		}
//	}

	private static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private static JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
	private static final String USER_IDENTIFIER_KEY = "Car Bounty Web-Application";
	@Value("${google.oauth.callback.uri}")
	private String CALLBACK_URI;
	@Value("${google.credential.folder.path}")
	private Resource credentail_folder;
	private GoogleAuthorizationCodeFlow flow;
//	private static final String CREDENTIALS_FILE_PATH = "credential.json";

	@PostConstruct
	private void init() {
		GoogleClientSecrets clientSecrets;
		try {
			clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
					new InputStreamReader(GoogleDriveService.class.getResourceAsStream("/credential.json")));
			flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
					.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(
							"C:\\Users\\HP\\eclipse-2023\\CarBounty-Web-Application\\src\\main\\resources")))
					.build();
		} catch (IOException e) {
			System.out.println("error ->" + e.getMessage());
			e.printStackTrace();
		}
	}

	@GetMapping("/")
	public String showHomePage() throws Exception {
		boolean isUserAuthenticated = false;
		Credential credential = flow.loadCredential(USER_IDENTIFIER_KEY);
		if (credential != null) {
			boolean tokenValid = credential.refreshToken();
			if (tokenValid) {
				isUserAuthenticated = true;
			}
		}
		return isUserAuthenticated ? "dashboard.html" : "index.html";
	}

	@GetMapping("/signin")
	public void doGoogleSignIn(HttpServletResponse resp) throws Exception {
		GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();
		String redirectUrl = url.setRedirectUri(CALLBACK_URI).setAccessType("offline").build();
		resp.sendRedirect(redirectUrl);
	}

	@GetMapping("/oauth")
	public String saveAuthorizationCode(HttpServletRequest req) throws Exception {
		String result = req.getParameter("code");
		if (result != null) {
			saveToken(result);
			return "dashboard.html";
		}
		return "index.html";
	}

	private void saveToken(String result) throws Exception {
		GoogleTokenResponse response = flow.newTokenRequest(result).setRedirectUri(CALLBACK_URI).execute();
		flow.createAndStoreCredential(response, USER_IDENTIFIER_KEY);
	}

	public String uploadGoogleDriveFile(MultipartFile image) {
		Credential cred;
		try {
			cred = flow.loadCredential(USER_IDENTIFIER_KEY);
			Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, cred).setApplicationName("Car Bounty")
					.build();
			String folderId = "1VW6rf4nL8co6TPNeUiXOXFArF-yrUSnQ";
			String imageName = image.getOriginalFilename() + System.currentTimeMillis();
			File fileMetaData = new File();
			fileMetaData.setName(imageName);
			fileMetaData.setParents(Collections.singletonList(folderId));

			java.io.File file = new java.io.File(imageName);
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(image.getBytes());
			fos.flush();
			fos.close();

			FileContent content = new FileContent(image.getContentType(), file);

			File uploadedFile = drive.files().create(fileMetaData, content).setFields("id").execute();
			System.out.println("File ID: " + uploadedFile.getId());
			return uploadedFile.getId();
		} catch (IOException e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

}
