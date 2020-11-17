package tw.waterball.judgegirl.springboot.problem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tw.waterball.judgegirl.problemservice.ScanRoot;

@ComponentScan(basePackageClasses = ScanRoot.class)
@SpringBootApplication
public class SpringBootProblemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootProblemApplication.class, args);
    }


}
