package ru.without_title.queue_project;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class QueueProjectApplication {

	@PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));
    }
	public static void main(String[] args) {
		SpringApplication.run(QueueProjectApplication.class, args);
	}

}
