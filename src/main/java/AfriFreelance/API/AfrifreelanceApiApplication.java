package AfriFreelance.API;

import AfriFreelance.API.config.LocalEnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AfrifreelanceApiApplication {

    public static void main(String[] args) {
        LocalEnvLoader.apply();
        SpringApplication.run(AfrifreelanceApiApplication.class, args);
        displayStartupInfo();
    }

    private static void displayStartupInfo() {
        System.out.println("\n" +
                "AfriFreelance Backend API démarrée !\n" +
                "API Docs: http://localhost:8082/api/v1/swagger-ui.html");
    }
}
