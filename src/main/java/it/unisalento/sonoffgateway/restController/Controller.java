package it.unisalento.sonoffgateway.restController;

import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import exception.InvalidTokenEx;
import it.unisalento.sonoffgateway.model.Credential;
import it.unisalento.sonoffgateway.model.User;


@RestController
public class Controller {
	//private final String backendAddress = "http://10.3.141.1:8080/";
	private final String backendAddress = "http://192.168.1.177:8080/";


	private OkHttpClient client = new OkHttpClient();
	
		
	@SuppressWarnings("unchecked")
	@RequestMapping(value="changeStatusON/{clientId}", method = RequestMethod.POST)
	public ResponseEntity<User> changeStatusON(@PathVariable("clientId") String clientId, @RequestBody User user){
		com.squareup.okhttp.MediaType JSON = com.squareup.okhttp.MediaType.parse("application/json; charset=utf-8");
		JSONObject jsonObj = new JSONObject();
		
		jsonObj.put("username", user.getUsername());
		jsonObj.put("role", user.getRole());
		jsonObj.put("token", user.getToken());
		jsonObj.put("refreshToken", user.getRefreshToken());
		
		com.squareup.okhttp.RequestBody body = com.squareup.okhttp.RequestBody.create(JSON, jsonObj.toString());
		
		Request request = new Request.Builder().url(backendAddress+"changeStatusON/"+clientId)
				.post(body)
				.build();
		
		Response response;
		try {
			response = client.newCall(request).execute();
			if(response.isSuccessful()) {
				JSONParser parser = new JSONParser();  
				String stringBody = response.body().string();
				if(!stringBody.isEmpty()) {
					JSONObject json = (JSONObject) parser.parse(stringBody);  
					user.setToken(json.get("token").toString());
					user.setRefreshToken(json.get("refreshToken").toString());
					return new ResponseEntity<>(user, HttpStatus.valueOf(response.code()));
				}
				return new ResponseEntity<>(new User(), HttpStatus.valueOf(response.code()));

			}
			return new ResponseEntity<>(HttpStatus.valueOf(response.code()));
			
		} 
		catch (IOException e) {
			e.getStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} 
		catch (ParseException e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value="changeStatusOFF/{clientId}", method = RequestMethod.POST)
	public ResponseEntity<User> changeStatusOFF(@PathVariable("clientId") String clientId, @RequestBody User user){
		com.squareup.okhttp.MediaType JSON = com.squareup.okhttp.MediaType.parse("application/json; charset=utf-8");
		JSONObject jsonObj = new JSONObject();
		
		jsonObj.put("username", user.getUsername());
		jsonObj.put("role", user.getRole());
		jsonObj.put("token", user.getToken());
		jsonObj.put("refreshToken", user.getRefreshToken());
		
		com.squareup.okhttp.RequestBody body = com.squareup.okhttp.RequestBody.create(JSON, jsonObj.toString());
		
		Request request = new Request.Builder().url(backendAddress+"changeStatusOFF/"+clientId)
				.post(body)
				.build();
		
		Response response;
		try {
			response = client.newCall(request).execute();
			if(response.isSuccessful()) {
				JSONParser parser = new JSONParser();  
				String stringBody = response.body().string();
				if(!stringBody.isEmpty()) {
					JSONObject json = (JSONObject) parser.parse(stringBody);  
					user.setToken(json.get("token").toString());
					user.setRefreshToken(json.get("refreshToken").toString());
					return new ResponseEntity<>(user, HttpStatus.valueOf(response.code()));
				}
				return new ResponseEntity<>(new User(), HttpStatus.valueOf(response.code()));

			}
			return new ResponseEntity<>(HttpStatus.valueOf(response.code()));
			
		} 
		catch (IOException e) {
			e.getStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} 
		catch (ParseException e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value="getStatus/{clientId}", method = RequestMethod.POST)
	public ResponseEntity<String> getStatus(@PathVariable("clientId") String clientId, @RequestBody User user) throws ParseException, InvalidTokenEx, IOException{
		com.squareup.okhttp.MediaType JSON = com.squareup.okhttp.MediaType.parse("application/json; charset=utf-8");
		JSONObject jsonObj = new JSONObject();
		
		jsonObj.put("username", user.getUsername());
		jsonObj.put("role", user.getRole());
		jsonObj.put("token", user.getToken());
		jsonObj.put("refreshToken", user.getRefreshToken());
		
		com.squareup.okhttp.RequestBody body = com.squareup.okhttp.RequestBody.create(JSON, jsonObj.toString());
		
		Request request = new Request.Builder().url(backendAddress+"getStatus/"+clientId)
				.post(body)
				.build();
		
		Response response;
		try {
			response = client.newCall(request).execute();
			if(response.isSuccessful()) {
				JSONParser parser = new JSONParser();
				JSONObject jsonResp = (JSONObject) parser.parse(response.body().string());
				return new ResponseEntity<>(jsonResp.toString(), HttpStatus.valueOf(response.code()));
			}
			return new ResponseEntity<>(HttpStatus.valueOf(response.code()));

		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (ParseException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value="getTouchSensorState/{clientId}", method = RequestMethod.POST)
	public ResponseEntity<String> getTouchSensorState(@PathVariable("clientId") String clientId, @RequestBody User user) throws ParseException, InvalidTokenEx, IOException{
		com.squareup.okhttp.MediaType JSON = com.squareup.okhttp.MediaType.parse("application/json; charset=utf-8");
		JSONObject jsonObj = new JSONObject();
		
		jsonObj.put("username", user.getUsername());
		jsonObj.put("role", user.getRole());
		jsonObj.put("token", user.getToken());
		jsonObj.put("refreshToken", user.getRefreshToken());
		
		com.squareup.okhttp.RequestBody body = com.squareup.okhttp.RequestBody.create(JSON, jsonObj.toString());
		
		Request request = new Request.Builder().url(backendAddress+"getTouchSensorState/"+clientId)
				.post(body)
				.build();
		
		Response response;
		try {
			response = client.newCall(request).execute();
			if(response.isSuccessful()) {
				JSONParser parser = new JSONParser();
				JSONObject jsonResp = (JSONObject) parser.parse(response.body().string());
				return new ResponseEntity<>(jsonResp.toString(), HttpStatus.valueOf(response.code()));
			}
			return new ResponseEntity<>(HttpStatus.valueOf(response.code()));

		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (ParseException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "auth", method = RequestMethod.POST)
	public ResponseEntity<User> authentication(@RequestBody Credential credential) {
		com.squareup.okhttp.MediaType JSON = com.squareup.okhttp.MediaType.parse("application/json; charset=utf-8");
		JSONObject jsonObj = new JSONObject();
		
		jsonObj.put("username", credential.getUsername());
		jsonObj.put("password", credential.getPassword());

		com.squareup.okhttp.RequestBody body = com.squareup.okhttp.RequestBody.create(JSON, jsonObj.toString());
		
		Request request = new Request.Builder().url(backendAddress+"auth")
				.post(body)
				.build();
		
		Response response;

		try {
			response = client.newCall(request).execute();
			if(response.isSuccessful()) {
				JSONParser parser = new JSONParser();
				JSONObject jsonResp = (JSONObject) parser.parse(response.body().string());
				User user = new User();
				user.setUsername(jsonResp.get("username").toString());
				user.setRole(jsonResp.get("role").toString());
				user.setToken(jsonResp.get("token").toString());
				user.setRefreshToken(jsonResp.get("refreshToken").toString());
				return new ResponseEntity<>(user, HttpStatus.valueOf(response.code()));
			}
			return new ResponseEntity<>(HttpStatus.valueOf(response.code()));

		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (ParseException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@SuppressWarnings("unchecked")
	@PostMapping("createUser/{username}/{password}/{userRole}")
	public ResponseEntity<User> createUser(@PathVariable("username") String username, @PathVariable("password") String password, @PathVariable("userRole") String userRole, @RequestBody User user) {
		com.squareup.okhttp.MediaType JSON = com.squareup.okhttp.MediaType.parse("application/json; charset=utf-8");
		JSONObject jsonObj = new JSONObject();
		
		jsonObj.put("username", user.getUsername());
		jsonObj.put("token", user.getToken());
		jsonObj.put("refreshToken", user.getRefreshToken());
		jsonObj.put("role", user.getRole());

		com.squareup.okhttp.RequestBody body = com.squareup.okhttp.RequestBody.create(JSON, jsonObj.toString());
		
		Request request = new Request.Builder().url(backendAddress+"createUser/"+username+"/"+password+"/"+userRole)
				.post(body)
				.build();
		
		Response response;
		try {
			response = client.newCall(request).execute();
			if(response.isSuccessful()) {
				JSONParser parser = new JSONParser();  
				String stringBody = response.body().string();
				if(!stringBody.isEmpty()) {
					JSONObject json = (JSONObject) parser.parse(stringBody);  
					user.setToken(json.get("token").toString());
					user.setRefreshToken(json.get("refreshToken").toString());
					return new ResponseEntity<>(user, HttpStatus.valueOf(response.code()));
				}
				return new ResponseEntity<>(new User(), HttpStatus.valueOf(response.code()));

			}
			return new ResponseEntity<>(HttpStatus.valueOf(response.code()));
			
		} 
		catch (IOException e) {
			e.getStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} 
		catch (ParseException e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
	@SuppressWarnings("unchecked")
	@PostMapping("getEventLog")
	public ResponseEntity<String> getEventLog(@RequestBody User user){
		com.squareup.okhttp.MediaType JSON = com.squareup.okhttp.MediaType.parse("application/json; charset=utf-8");
		JSONObject jsonObj = new JSONObject();
		
		jsonObj.put("username", user.getUsername());
		jsonObj.put("token", user.getToken());
		jsonObj.put("refreshToken", user.getRefreshToken());
		jsonObj.put("role", user.getRole());

		com.squareup.okhttp.RequestBody body = com.squareup.okhttp.RequestBody.create(JSON, jsonObj.toString());
		
		Request request = new Request.Builder().url(backendAddress+"getEventLog/")
				.post(body)
				.build();
		
		Response response;
		try {
			response = client.newCall(request).execute();
			if(response.isSuccessful()) {
				JSONParser parser = new JSONParser();
				JSONObject jsonResp = (JSONObject) parser.parse(response.body().string());
				return new ResponseEntity<>(jsonResp.toString(), HttpStatus.valueOf(response.code()));
			}
			else {
				return new ResponseEntity<>(HttpStatus.valueOf(response.code()));
			}
		}catch (ParseException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
