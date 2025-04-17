package edu.IIT.notification_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;

@SpringBootApplication
public class NotificationManagementApplication {

	public static void main(String[] args) {

		SpringApplication.run(NotificationManagementApplication.class, args);
	}

}
