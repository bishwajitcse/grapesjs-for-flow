package com.lausntech.grapesjs.demo;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.aura.Aura;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demo/test application for the GrapesJS Flow addon. Run this class's
 * {@code main} method from your IDE, or via
 * {@code mvn org.codehaus.mojo:exec-maven-plugin:java -Dexec.mainClass=com.lausntech.grapesjs.demo.Application -Dexec.classpathScope=test},
 * then open http://localhost:8081.
 *
 * @see com.lausntech.grapesjs.GrapesJsEditor
 */
@SpringBootApplication
@StyleSheet(Aura.STYLESHEET)
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
