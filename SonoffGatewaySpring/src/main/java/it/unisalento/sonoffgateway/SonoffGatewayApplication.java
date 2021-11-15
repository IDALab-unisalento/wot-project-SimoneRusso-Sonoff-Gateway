package it.unisalento.sonoffgateway;

import java.util.Collections;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SonoffGatewayApplication{

	public static void main(String[] args) throws Exception {
		SpringApplication app = new SpringApplication(SonoffGatewayApplication.class);
        app.setDefaultProperties(Collections
          .singletonMap("server.port", "8081"));
        app.run(args);	        
	}	
}
