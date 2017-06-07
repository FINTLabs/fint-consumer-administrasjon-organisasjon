package no.fint.consumer;

import com.github.springfox.loader.EnableSpringfox;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Info;
import no.fint.audit.EnableFintAudit;
import no.fint.events.annotations.EnableFintEvents;
import no.fint.relations.annotations.EnableFintRelations;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFintRelations
@EnableFintEvents
@EnableFintAudit
@EnableScheduling
@EnableSpringfox(@Info(title = "FINT Consumer Organisasjon", version = "${fint.version}",
        extensions = {@Extension(name = "x-logo",
                properties = {@ExtensionProperty(name = "url", value = "/images/logo.png")}
        )}
))
@SpringBootApplication
public class Application {
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
