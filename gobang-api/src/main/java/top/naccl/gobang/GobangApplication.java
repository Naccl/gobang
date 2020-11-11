package top.naccl.gobang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GobangApplication {

	public static void main(String[] args) {
		SpringApplication.run(GobangApplication.class, args);
	}

}
