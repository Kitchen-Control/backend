package org.luun.kitchencontrolbev1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KitchenControlBEv1Application {

    public static void main(String[] args) {
        SpringApplication.run(KitchenControlBEv1Application.class, args);
    }

}
