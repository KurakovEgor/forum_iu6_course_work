package api;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by egor on 13.10.17.
 */

@SpringBootApplication
public class Application {
        public static void main(String[] args) {
            SpringApplication.run(Application.class, args);
        }

        HikariConfig config = new HikariConfig(Constants.hikariConfigPath);
        HikariDataSource ds = new HikariDataSource(config);
}
