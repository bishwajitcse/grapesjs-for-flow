package com.lausntech.grapesjs.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.vaadin.flow.component.page.AppShellConfigurator;

/**
 * Demo/test application for the GrapesJS Flow addon. Run this class's
 * {@code main} method from your IDE, or via
 * {@code mvn org.codehaus.mojo:exec-maven-plugin:java -Dexec.mainClass=com.lausntech.grapesjs.demo.Application -Dexec.classpathScope=test},
 * then open http://localhost:8081.
 *
 * @see com.lausntech.grapesjs.GrapesJsEditor
 */
@SpringBootApplication
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
