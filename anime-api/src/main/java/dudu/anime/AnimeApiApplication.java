package dudu.anime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableWebFlux
public class AnimeApiApplication {

	public static void main(String[] args) {
		System.out.println(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("Megumin123"));
		SpringApplication.run(AnimeApiApplication.class, args);
	}

}
