package edu.IIT.user_management;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;

@SpringBootApplication
public class UserManagementApplication {

	public static void main(String[] args) {

		SpringApplication.run(UserManagementApplication.class, args);

	}

}

@Component
class JwtSecretLoader {

	private final String jwtSecret;

	public JwtSecretLoader(Environment env) {
		this.jwtSecret = env.getProperty("jwt.secret");

		// Decode the Base64-encoded secret key
		byte[] decodedKey = Base64.getDecoder().decode(jwtSecret);
		SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(decodedKey);

		System.out.println("Loaded JWT Secret Key Successfully!");
	}
}
