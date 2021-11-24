package it.unisalento.sonoffgateway;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
@SpringBootApplication
public class SonoffGatewayApplication{

	public static void main(String[] args) throws Exception {
		SpringApplication app = new SpringApplication(SonoffGatewayApplication.class);
        app.setDefaultProperties(Collections
          .singletonMap("server.port", "8081"));
        app.run(args);	        
        try {
        	System.out.println("Connecting to Firebase");
        	FileInputStream serviceAccount = new FileInputStream("./sonoff-66f45-firebase-adminsdk-uyqwv-6627f88f88.json");
        	FirebaseOptions options = FirebaseOptions.builder()
        			.setCredentials(GoogleCredentials.fromStream(serviceAccount))
        			.setProjectId("sonoff-66f45")
        			.build();
        	FirebaseApp.initializeApp(options);
        }catch (Exception e) {
        	System.out.println("Something went wrong with Firebase conf file!\n" + e.getMessage());
        	throw e;
		}
        System.out.println("Connected succefully to Firebase");        
		File file = new File("./tokens.json");  
		file.createNewFile();
        connectAndSubscribeMqtt();
        
        
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
    				List<String> tokens = new ArrayList<>();
    				Firestore db = FirestoreClient.getFirestore();
    	        	ApiFuture<QuerySnapshot> future = db.collection("tokens").get(); 
    	        	List<QueryDocumentSnapshot> documents = future.get().getDocuments();
    	        	for (DocumentSnapshot document : documents) {
    	        		  System.out.println(document.getId() + " => " + document.getString("token"));
    	        		  tokens.add(document.getString("token"));
    	        		}
    	        	
    				Notification.Builder builder = Notification.builder();
    				MulticastMessage notMess = MulticastMessage.builder()
    						.setNotification(builder.build())
    						.putData("title", "Cambio di stato")
    						.putData("body", status)
    				        .addAllTokens(tokens)
    				        .build();
    				System.out.println("Sending notification...");
    				BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(notMess);
    				System.out.println(response.getSuccessCount()+"/"+tokens.size() + " messages were sent successfully");

				}
    		});
			System.out.println("Subscribed succesfully to: " +statTopic);
    		
        }catch (Exception e) {
		}
		
	}
}