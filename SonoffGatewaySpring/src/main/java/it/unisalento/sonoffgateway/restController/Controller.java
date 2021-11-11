package it.unisalento.sonoffgateway.restController;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

	String cmdTopic = "cmnd/tasmota_8231A8/POWER1";
	String reqToipic = "cmnd/tasmota_8231A8/Power1";
	String statTopic = "stat/tasmota_8231A8/POWER1";
	
	String status = new String();
		
	@RequestMapping(value="changeStatusON", method = RequestMethod.GET)
	public ResponseEntity<Boolean> changeStatusON(){
		try {
			
			MqttClient client = connectToBroker(cmdTopic);
			
			MqttMessage message = new MqttMessage("ON".getBytes());
			
			System.out.println("Trying to change status to ON...");
			
			client.publish(cmdTopic, message);	//BLOCKING
			
			client.disconnect(100);
			System.out.println("Client " + client.getClientId() + " disconnected succesfully");
			client.close();
			
			return new ResponseEntity<Boolean>(HttpStatus.OK);

		}catch (Exception e) {
			System.out.println("Something went wrong while changing status!\n" + e.getMessage());
			return new ResponseEntity<Boolean>(HttpStatus.BAD_GATEWAY);
			}

	}
	
	@RequestMapping(value="changeStatusOFF", method = RequestMethod.GET)
	public ResponseEntity<Boolean> changeStatusOFF() throws Exception{
try {
			
			MqttClient client = connectToBroker(cmdTopic);
			
			MqttMessage message = new MqttMessage("OFF".getBytes());
			
			System.out.println("Trying to change status to OFF...");
			
			client.publish(cmdTopic, message); //BLOCKING
			
			client.disconnect(100);
			System.out.println("Client " + client.getClientId() + " disconnected succesfully");
			
			client.close();
			
			return new ResponseEntity<Boolean>(HttpStatus.OK);

		}catch (Exception e) {
			System.out.println("Something went wrong while changing status!\n" + e.getMessage());
			return new ResponseEntity<Boolean>(HttpStatus.BAD_GATEWAY);
			}
	}
	

	
	@RequestMapping(value="getStatus", method = RequestMethod.GET)
	public String getStatus() throws MqttException{
		try {
			status = "";
			MqttClient client = connectToBroker(statTopic);;

			System.out.println("Trying to subscribe to "+statTopic);
			
			client.subscribe(statTopic, new IMqttMessageListener() {
				
				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					status = new String(message.getPayload(), StandardCharsets.UTF_8);
					System.out.println("Getted status: " + status);
					client.disconnect();
				}
			});
			
			
			MqttMessage message = new MqttMessage();
			
			System.out.println("Trying to get status...");
			client.publish(reqToipic, message);	//BLOCKING
			
			
			while(client.isConnected());
			System.out.println("Client " + client.getClientId() + " disconnected succesfully");
			client.close();

			return status;
		}catch (MqttException e) {
			System.out.println("Something went wrong while getting status!\n" + e.getMessage());
			throw e;
			}
		
	}
	
	private MqttClient connectToBroker(String topic) throws MqttException {
		
		String broker = "tcp://192.168.1.67:1883";
		String clientId = "raspberrypi";
		
		
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
	
	 
}
