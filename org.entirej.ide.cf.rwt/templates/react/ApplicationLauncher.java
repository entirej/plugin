package org.entirej;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@ServletComponentScan(basePackages="org.entirej")
public class ApplicationLauncher extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(ApplicationLauncher.class, args);
	}
}