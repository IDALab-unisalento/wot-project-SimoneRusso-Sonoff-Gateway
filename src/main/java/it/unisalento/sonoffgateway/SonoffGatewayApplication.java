package it.unisalento.sonoffgateway;

import java.nio.charset.StandardCharsets;
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

@SpringBootApplication
public class SonoffGatewayApplication{

	public static void main(String[] args) throws Exception {
		SpringApplication app = new SpringApplication(SonoffGatewayApplication.class);
        app.setDefaultProperties(Collections
          .singletonMap("server.port", "8081"));
        app.run(args);	             
        //connectAndSubscribeMqtt();
        
        
	}

	private static void connectAndSubscribeMqtt() {
		String broker = "tcp://localhost:1883";		
		String statTopic = "stat/tasmota_8231A8/POWER1";
		String clientId = "notificationChannel";
        try {
        	MqttClient client = new MqttClient(broker, clientId, new MemoryPersistence());
    		client.setCallback(new MqttCallbackExtended() {
				
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
						client.reconnect();
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
    		client.connect(opt);
    		client.subscribe(statTopic, new IMqttMessageListener() {
    			@Override
    			public void messageArrived(String topic, MqttMessage message) throws Exception {
    				String status = new String(message.getPayload(), StandardCharsets.UTF_8);
    				System.out.println("A change of status occured, status: " + status);
				}
    		});
			System.out.println("Subscribed succesfully to: " +statTopic);
    		
        }catch (Exception e) {
		}
		
	}
}
