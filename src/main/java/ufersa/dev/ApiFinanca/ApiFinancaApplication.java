package ufersa.dev.ApiFinanca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ufersa.dev.ApiFinanca.config.DatabaseInitializer;
import ufersa.dev.ApiFinanca.config.DatabaseUrlConfig;

@SpringBootApplication
public class ApiFinancaApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(ApiFinancaApplication.class);
		app.addListeners(new DatabaseInitializer());
		app.addListeners(new DatabaseUrlConfig());
		app.run(args);
	}

}
