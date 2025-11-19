package com.example.stockgestion;

import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import ch.qos.logback.core.filter.Filter;

@SpringBootApplication
public class StockGestionApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext appContext = SpringApplication.run(StockGestionApplication.class, args);
		
		Arrays.stream(appContext.getBean(Filter.class).getClass().getAnnotations())
			.forEach(System.out::println);
	}
}