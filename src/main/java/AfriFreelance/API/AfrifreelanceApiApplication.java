package AfriFreelance.API;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class  AfrifreelanceApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AfrifreelanceApiApplication.class, args);
        displayStartupInfo();
    }

    private static void displayStartupInfo() {
        System.out.println("\n" +
                "Afri-freelance Backend API démarrée !\n" +
                "API Docs: http://localhost:8080/api/swagger-ui.html");
    }
}
