package it.unisalento.sonoffgateway.restController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.github.underscore.U;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import model.User;

@RestController
public class Controller {

	private List<String> cmdTopic = Arrays.asList("cmnd/tasmota_8231A8/POWER1", "cmnd/tasmota_8231A8/POWER2", "cmnd/tasmota_8231A8/POWER3");
	private String reqToipic = "cmnd/tasmota_8231A8/STATUS11";
	private String statTopic = "stat/tasmota_8231A8/STATUS11";
	private String broker = "tcp://localhost:1883";		
	//TODO:indirizzi ip
	//STUDIUM
	//private String authAddress = "http://10.20.72.9:8180/auth/realms/master/protocol/openid-connect/userinfo";
	//CASA
	//private String authAddress = "http://192.168.1.100:8180/auth/realms/master/protocol/openid-connect/userinfo";
	//HOTSPOT
	private String authAddress = "http://172.20.10.4:8180/auth/realms/MyRealm/protocol/openid-connect/userinfo";
	String refreshAddress="http://172.20.10.4:8180/auth/realms/MyRealm/protocol/openid-connect/token";

	String status = new String();
	OkHttpClient client = new OkHttpClient();

	
		
	@RequestMapping(value="changeStatusON/{clientId}/{input}", method = RequestMethod.POST)
	public ResponseEntity<User> changeStatusON(@PathVariable("clientId") String clientId,  @PathVariable("token") String token, @PathVariable("input") int input, @org.springframework.web.bind.annotation.RequestBody User user){
		User tempUser;
		try {
			tempUser = checkToken(user);
		} catch (Exception e1) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		try {
			MqttClient client = connectToBroker(cmdTopic.get(input-1), clientId);
			MqttMessage message = new MqttMessage("ON".getBytes());
			System.out.println("Trying to change status to ON...");
			client.publish(cmdTopic.get(input-1), message);	//BLOCKING
			client.disconnect(100);
			System.out.println("Client " + client.getClientId() + " disconnected succesfully");
			client.close();
			return new ResponseEntity<>(tempUser, HttpStatus.OK);
		}catch (Exception e) {
			System.out.println("Something went wrong while changing status!\n" + e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
	}
	@RequestMapping(value="changeStatusOFF/{clientId}/{input}", method = RequestMethod.POST)
	public ResponseEntity<User> changeStatusOFF(@PathVariable("clientId") String clientId, @PathVariable("input") int input, @org.springframework.web.bind.annotation.RequestBody User user){
		User tempUser;
		try {
			tempUser = checkToken(user);
		} catch (Exception e1) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		try {
			MqttClient client = connectToBroker(cmdTopic.get(input-1), clientId);
			MqttMessage message = new MqttMessage("OFF".getBytes());
			System.out.println("Trying to change status to OFF...");
			client.publish(cmdTopic.get(input-1), message); //BLOCKING
			client.disconnect(100);
			System.out.println("Client " + client.getClientId() + " disconnected succesfully");
			client.close();
			return new ResponseEntity<>(tempUser, HttpStatus.OK);

		}catch (Exception e) {
			System.out.println("Something went wrong while changing status!\n" + e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
	}
	
	@RequestMapping(value="getStatus/{clientId}/{input}", method = RequestMethod.POST)
	public ResponseEntity<String> getStatus(@PathVariable("clientId") String clientId, @PathVariable("input") int input, @org.springframework.web.bind.annotation.RequestBody User user){
		User tempUser;
		try {
			tempUser = checkToken(user);
		} catch (Exception e1) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		try {
			int pos= input+7;
			status = "";
			MqttClient client = connectToBroker(statTopic, clientId);;
			System.out.println("Trying to subscribe to "+statTopic);
			client.subscribe(statTopic, new IMqttMessageListener() {
				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					System.out.println(message);
					status = new String(message.getPayload(), StandardCharsets.UTF_8).split(",")[pos].split(":")[1];
					System.out.println("Getted status: " + status);
					client.disconnect();
				}
			});
			MqttMessage message = new MqttMessage();
			message.setPayload("0".getBytes());
			System.out.println("Trying to get status...");
			client.publish(reqToipic, message);	//BLOCKING
			while(client.isConnected());
			System.out.println("Client " + client.getClientId() + " disconnected succesfully");
			client.close();
			String s = status;
			int lenght = status.length();
			s = status.substring(1, lenght-1);
			
			JSONObject jsonObj = new JSONObject();
			if(tempUser!=null) {
				String sToJ = U.objectBuilder()
			            .add("user", U.objectBuilder()
			                            .add("username", tempUser.getUsername())
			                            .add("role", tempUser.getRole())
			                            .add("token", tempUser.getToken())
			                            .add("refreshToken", tempUser.getRefreshToken())
			                            		)
			            .add("status", s)
			            .toJson();
				return new ResponseEntity<String>(sToJ, HttpStatus.OK);

			}
			return new ResponseEntity<String>(s, HttpStatus.OK);
		}catch (MqttException e) {
			System.out.println("Something went wrong while getting status!\n" + e.getMessage());
			return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		
	}
	
	private MqttClient connectToBroker(String topic, String clientId) throws MqttException {
		MqttClient client = new MqttClient(broker, clientId, new MemoryPersistence());
		client.setCallback(new MqttCallbackExtended() {
			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
			}
			
			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {
				if(topic.equals(cmdTopic)) {
					System.out.println("State changed!");
				}
				else {
					System.out.println("Getting status...");
				}
			}
			
			@Override
			public void connectionLost(Throwable cause) {
			}
			
			@Override
			public void connectComplete(boolean reconnect, String serverURI) {
				System.out.println("Connected succesfully!");
			}
		});
		MqttConnectOptions opt = new MqttConnectOptions();
		opt.setCleanSession(true);
		System.out.println("Connceting to broker at: " + broker);
		client.connect(opt);
		return client;
	}
	
	
	private User checkToken(User user) throws Exception {
		Request request = new Request.Builder()
				.url(authAddress)
				.header("Content-Type", "application/json")
				.header("Authorization","Bearer "+ user.getToken())
				.get()
				.build();
		Response response;
		try {
			response = client.newCall(request).execute();
			if (response.isSuccessful()) {
				return null;
			}
			else {
				user = executeRefresh(user);
				return user; //EXPECTED
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		throw new Exception();
	}
	
	public User executeRefresh(User user) throws Exception{
		com.squareup.okhttp.RequestBody requestBody = new FormEncodingBuilder()
	    	     .add("grant_type", "refresh_token")
	    	     .add("refresh_token", user.getRefreshToken())
	    	     .add("client_id", "backend")
	    	     .add("client_secret", "eLFYzBFFDlJrA9dTmNPnkTwhiipyB8x8")
	    	     .build();
	    
	    Request request = new Request.Builder()
	    		.url(refreshAddress)
	    		.post(requestBody)
	            .build();
	    Response response;
	    try {
	    	response = client.newCall(request).execute();
	    	if(response.isSuccessful()) {
	    		JSONParser parser = new JSONParser();  
	    		JSONObject json = (JSONObject) parser.parse(response.body().string());  
	    		System.out.println(json);
	    		user.setToken(json.get("access_token").toString());
	    		user.setRefreshToken(json.get("refresh_token").toString());  
		    	return user;
	    	}
	    	throw new Exception();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	 
}
