package it.unisalento.sonoffgateway;

import java.util.Collections;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;


@SpringBootApplication
public class SonoffGatewayApplication{

	//private final static String ip = "10.3.141.1";
	private final static String ip = "192.168.1.177";

	public static void main(String[] args) throws Exception {
		SpringApplication app = new SpringApplication(SonoffGatewayApplication.class);
       /* app.setDefaultProperties(Collections
          .singletonMap("server.port", "8081"));*/
        app.run(args);
		connectAndSubscribeMqtt();

	}
	
	private static void connectAndSubscribeMqtt() throws Exception {
		String broker = "tcp://"+ip+":1883";		
		String PIRTopic = "stat/tasmota_8231A8/POWER2";
		String touchTopic = "stat/tasmota_8231A8/POWER3";
		String clientId = "backend";
		OkHttpClient httpClient = new OkHttpClient();	
		final String backendAddress = "http://"+ip+":8080/";


		
        try {
        	MqttClient mqttClient = new MqttClient(broker, clientId, new MemoryPersistence());
    		mqttClient.setCallback(new MqttCallbackExtended() {
				
				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					
				}
				
				@Override
				public void deliveryComplete(IMqttDeliveryToken token) {
				}
				
				@Override
				public void connectionLost(Throwable cause) {
					System.out.println("Something went wrong with connection!\n"+ cause.getMessage());
					try {
						mqttClient.reconnect();
					} catch (MqttException e) {
						e.printStackTrace();
					}
				}
				
				@Override
				public void connectComplete(boolean reconnect, String serverURI) {
					System.out.println("Connected Succesfully");
				}
    		});	
    		MqttConnectOptions opt = new MqttConnectOptions();
    		opt.setCleanSession(true);
    		System.out.println("CONNECTIONG TO BROKER " + broker);
    		mqttClient.connect(opt);
    		mqttClient.subscribe(touchTopic, new IMqttMessageListener() {
    			@Override
    			public void messageArrived(String topic, MqttMessage message) throws Exception {
    				Request request;
    				String event_type;

    				if(new String(message.getPayload()).equals("ON")) {
    					 event_type = "Touch sensor on";
    				}
    				else if(new String(message.getPayload()).equals("OFF")) {
   					 event_type = "Touch sensor off";
    				}
    				else {
    					event_type = "Evento sconosciuto";
    				}
    				request = new Request.Builder().url(backendAddress+"saveSensorEvent/"+event_type)
    						.get()
    						.build();
    				Response response;
    				try {
    					response = httpClient.newCall(request).execute();
    				}catch (Exception e) {
    					e.printStackTrace();
					}
    				
    			}
    		});
			System.out.println("Subscribed succesfully to: " +touchTopic);
			
			mqttClient.subscribe(PIRTopic, new IMqttMessageListener() {
    			@Override
    			public void messageArrived(String topic, MqttMessage message) throws Exception {
    				Request request;
    				String event_type;
    				
    				if(new String(message.getPayload()).equals("ON")) {
    					 event_type = "PIR sensor on";
    				}
    				else if(new String(message.getPayload()).equals("OFF")) {
   					 event_type = "PIR sensor off";
    				}
    				else {
    					event_type = "Evento sconosciuto";
    				}
    				request = new Request.Builder().url(backendAddress+"saveSensorEvent/"+event_type)
    						.get()
    						.build();
    				Response response;
    				try {
    					response = httpClient.newCall(request).execute();
    				}catch (Exception e) {
    					e.printStackTrace();
					}					
				}
    		});
			System.out.println("Subscribed succesfully to: " +PIRTopic);
    		
        }catch (Exception e) {
        	throw e;
		}
		
	}

}
