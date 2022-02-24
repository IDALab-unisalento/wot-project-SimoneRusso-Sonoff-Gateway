package it.unisalento.sonoffgateway.restController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import it.unisalento.sonoffbackend.restController.Controller;
import net.bytebuddy.utility.RandomString;


@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = Controller.class)
public class ControllerTest {
	@Autowired
	private MockMvc mockMvc;
	String clientId;
	
	@BeforeEach
	void initEnv() {
		clientId = RandomString.make(15);
	}
	
	@Test
	void changeStatusOFFTest() {
			try {
				mockMvc.perform(get("changeStatusOFF/"+clientId)
						.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	@Test
	void changeStatusONTest() {
			try {
				mockMvc.perform(get("changeStatusON/"+clientId)
						.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	@Test
	void getStatusTest() {
			try {
				mockMvc.perform(get("getStatus/"+clientId)
						.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
}
