package com.zerofake.product;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class ProductServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductServiceApplication.class, args);
	}

	@Bean
	public CommandLineRunner dropCheckConstraint(JdbcTemplate jdbcTemplate) {
		return args -> {
			try {
				jdbcTemplate.execute("ALTER TABLE products DROP CONSTRAINT IF EXISTS products_blockchain_status_check;");
				System.out.println("Successfully dropped products_blockchain_status_check constraint.");
			} catch (Exception e) {
				System.err.println("Failed to drop constraint: " + e.getMessage());
			}
		};
	}

}
