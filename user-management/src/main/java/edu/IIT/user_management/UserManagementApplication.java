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
//		SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
//		String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
//		System.out.println("Generated Base64 Encoded Secret Key: " + base64Key);

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
