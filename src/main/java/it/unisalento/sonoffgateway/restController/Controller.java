package it.unisalento.sonoffgateway.restController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

@RestController
public class Controller {

	private String cmdTopic = "cmnd/tasmota_8231A8/POWER1";
	private String reqToipic = "cmnd/tasmota_8231A8/STATUS11";
	private String statTopic = "stat/tasmota_8231A8/STATUS11";
	private String broker = "tcp://localhost:1883";		
	private String authAddress = "http://192.168.1.100:8180/auth/realms/master/protocol/openid-connect/userinfo";
	String status = new String();
	OkHttpClient client = new OkHttpClient();

	
		
	@RequestMapping(value="changeStatusON/{clientId}/{token}", method = RequestMethod.GET)
	public ResponseEntity<Boolean> changeStatusON(@PathVariable("clientId") String clientId,  @PathVariable("token") String token){
		if(!checkToken(token)) {
			return new ResponseEntity<Boolean>(HttpStatus.UNAUTHORIZED);
		}
		try {
			MqttClient client = connectToBroker(cmdTopic, clientId);
			MqttMessage message = new MqttMessage("ON".getBytes());
			System.out.println("Trying to change status to ON...");
			client.publish(cmdTopic, message);	//BLOCKING
			client.disconnect(100);
			System.out.println("Client " + client.getClientId() + " disconnected succesfully");
			client.close();
			return new ResponseEntity<Boolean>(HttpStatus.OK);
		}catch (Exception e) {
			System.out.println("Something went wrong while changing status!\n" + e.getMessage());
			return new ResponseEntity<Boolean>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
	}
	@RequestMapping(value="changeStatusOFF/{clientId}/{token}", method = RequestMethod.GET)
	public ResponseEntity<Boolean> changeStatusOFF(@PathVariable("clientId") String clientId, @PathVariable("token") String token){
		if(!checkToken(token)) {
			return new ResponseEntity<Boolean>(HttpStatus.UNAUTHORIZED);
		}
		try {
			MqttClient client = connectToBroker(cmdTopic, clientId);
			MqttMessage message = new MqttMessage("OFF".getBytes());
			System.out.println("Trying to change status to OFF...");
			client.publish(cmdTopic, message); //BLOCKING
			client.disconnect(100);
			System.out.println("Client " + client.getClientId() + " disconnected succesfully");
			client.close();
			return new ResponseEntity<Boolean>(HttpStatus.OK);
		}catch (Exception e) {
			System.out.println("Something went wrong while changing status!\n" + e.getMessage());
			return new ResponseEntity<Boolean>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
	}
	
	@RequestMapping(value="getStatus/{clientId}/{token}", method = RequestMethod.GET)
	public ResponseEntity<String> getStatus(@PathVariable("clientId") String clientId, @PathVariable("token") String token){
		if(!checkToken(token)) {
			return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);
		}
		try {
			status = "";
			MqttClient client = connectToBroker(statTopic, clientId);;
			System.out.println("Trying to subscribe to "+statTopic);
			client.subscribe(statTopic, new IMqttMessageListener() {
				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					status = new String(message.getPayload(), StandardCharsets.UTF_8).split(",")[8].split(":")[1];
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
			return new ResponseEntity<String>(s, HttpStatus.OK); //OTTENGO LO STATO DI POWER1
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
	
	private Boolean checkToken(String token) {
		Request request = new Request.Builder()
			      .url(authAddress)
			      .header("Content-Type", "application/json")
			      .header("Authorization","Bearer "+ token)
			      .get()
			      .build();
		 Response response;
		 try {
				response = client.newCall(request).execute();
				if (response.isSuccessful()) {
					return true;
				}
				return false;
			} 
		 catch (IOException e) {
				e.printStackTrace();
			}
		return null;
	}
	
	 
}
