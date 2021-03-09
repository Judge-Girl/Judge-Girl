package tw.waterball.judgegirl.springboot.exam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tw.waterball.judgegirl.problemservice.ScanRoot;

@ComponentScan(basePackageClasses = {
        tw.waterball.judgegirl.springboot.ScanRoot.class,
        tw.waterball.judgegirl.examservice.ScanRoot.class})
@SpringBootApplication
public class SpringBootExamApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootExamApplication.class, args);
    }


}
