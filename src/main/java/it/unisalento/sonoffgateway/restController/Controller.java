package it.unisalento.sonoffgateway.restController;

import java.io.IOException;

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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.github.underscore.U;
import com.github.underscore.U.Builder;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import it.unisalento.sonoffgateway.model.User;


@RestController
public class Controller {
	private String ip = "10.3.141.130";

	private final String cmdTopic1 = "cmnd/tasmota_8231A8/POWER1";
	private final String reqToipic1 = "cmnd/tasmota_8231A8/POWER1";
	private final String statTopic1 = "stat/tasmota_8231A8/POWER1";
	private final String broker = "tcp://localhost:1883";		
	private final String authAddress = "http://"+ip+":8180/auth/realms/MyRealm/protocol/openid-connect/userinfo";
	private final String refreshAddress="http://"+ip+":8180/auth/realms/MyRealm/protocol/openid-connect/token";
	private String status1 = new String();
	private OkHttpClient client = new OkHttpClient();
	private final String INVALID_TOKEN = "Invalid token";

	
		
	@RequestMapping(value="changeStatusON/{clientId}", method = RequestMethod.POST)
	public ResponseEntity<User> changeStatusON(@PathVariable("clientId") String clientId, @RequestBody User user){
		try {
			user = checkToken(user); //LANCIA UN ECCEZIONE SE IL TOKEN NON E' PIU' VALIDO E NON PUO' ESSERE REFRESHATO
			MqttClient client = connectToBroker(cmdTopic1, clientId);
			MqttMessage message = new MqttMessage("ON".getBytes());
			System.out.println("Trying to change status to ON...");
			client.publish(cmdTopic1, message);
			client.disconnect(100);
			System.out.println("Client " + client.getClientId() + " disconnected succesfully");
			client.close();	
			return new ResponseEntity<>(user, HttpStatus.OK);
			
		}catch (Exception e) {
			if(e.getMessage().equals(INVALID_TOKEN)) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			else {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}	
	}
	
	@RequestMapping(value="changeStatusOFF/{clientId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<User> changeStatusOFF(@PathVariable("clientId") String clientId, @RequestBody User user){
		
		try {
			user = checkToken(user); //LANCIA UN ECCEZIONE SE IL TOKEN NON E' PIU' VALIDO E NON PUO' ESSERE REFRESHATO
			MqttClient client = connectToBroker(cmdTopic1, clientId);
			MqttMessage message = new MqttMessage("OFF".getBytes());
			System.out.println("Trying to change status to ON...");
			client.publish(cmdTopic1, message);
			client.disconnect(100);
			System.out.println("Client " + client.getClientId() + " disconnected succesfully");
			client.close();	
			return new ResponseEntity<>(user, HttpStatus.OK);
			
		}catch (Exception e) {
			if(e.getMessage().equals(INVALID_TOKEN)) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			else {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}	
	}
	
	@RequestMapping(value="getStatus1/{clientId}", method = RequestMethod.POST)
	public ResponseEntity<String> getStatus1(@PathVariable("clientId") String clientId, @RequestBody User user){
		try {
			user = checkToken(user);
			status1 = "";
			MqttClient client = connectToBroker(statTopic1, clientId);;
			System.out.println("Trying to subscribe to "+statTopic1);
			client.subscribe(statTopic1, new IMqttMessageListener() {
				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					status1 = new String(message.getPayload());
					System.out.println("Getted status: " + status1);
					client.disconnect();
				}
			});
			MqttMessage message = new MqttMessage();
			message.setPayload("".getBytes());
			System.out.println("Trying to get status...");
			client.publish(reqToipic1, message);	//BLOCKING
			while(client.isConnected());
			System.out.println("Client " + client.getClientId() + " disconnected succesfully");
			client.close();
			String s = status1;
			
			Builder builder = U.objectBuilder()
					.add("status", s);
			
			if(user!=null) {
				builder
					.add("user", U.objectBuilder()
					.add("username", user.getUsername())
					.add("role", user.getRole())
					.add("token", user.getToken())
					.add("refreshToken", user.getRefreshToken()));
			}
			String retValue = builder.toJson();
			return new ResponseEntity<>(retValue, HttpStatus.OK); //OTTENGO LO STATO DI POWER1
		}
		catch (MqttException e) {
			System.out.println("Something went wrong while getting status!\n" + e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} 
		catch (Exception e) {
			if(e.getMessage().equals(INVALID_TOKEN)) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			else {
				e.printStackTrace();
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
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
				if(topic.equals(cmdTopic1)) {
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
				if(response.isSuccessful()) {
					return null;
				}
				else {
					return executeRefresh(user);
				}
			} 
		 catch (IOException e) {
				e.printStackTrace();
				throw e;
			}
	}
	
	private User executeRefresh(User user) throws Exception {
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
	    		user.setToken(json.get("access_token").toString());
	    		user.setRefreshToken(json.get("refresh_token").toString());  
		    	return user;
	    	}
	    	else {
	    		throw new Exception(INVALID_TOKEN);	  
	    	}
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	 
}
