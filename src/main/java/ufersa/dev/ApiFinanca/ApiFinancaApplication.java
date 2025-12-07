package ufersa.dev.ApiFinanca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ufersa.dev.ApiFinanca.config.DatabaseInitializer;

@SpringBootApplication
public class ApiFinancaApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(ApiFinancaApplication.class);
		app.addListeners(new DatabaseInitializer());
		app.run(args);
	}

}
