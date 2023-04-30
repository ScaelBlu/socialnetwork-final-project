package socialnetwork;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SocialnetworkApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialnetworkApplication.class, args);
	}

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Social Network API")
						.version("1.0.0")
						.description("""
									In this project, I have implemented a 3-layered REST API with two entities
									and a MariaDB database connection through Spring Data JPA. The application
									represents a simple photo-sharing social network like Instagram, where users
									can connect with each other and share simple images in JPG/JPEG or PNG format."""));
	}
}
