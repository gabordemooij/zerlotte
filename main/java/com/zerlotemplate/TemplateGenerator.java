package com.zerlotemplate;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TemplateGenerator {

    public static void main(String[] args) throws IOException {
        Path templateDir = Path.of(args[0]);
        Path outputDir = Path.of(args[2]);
        Files.createDirectories(outputDir);
        String classPathPrefix = args[1];
        Files.walk(templateDir)
                .filter(p -> p.toString().endsWith(".html"))
                .forEach(p -> {
                    Templateable template = Template.from( new ClassPathResource( classPathPrefix + "/" + p.getFileName()) );
                    try {
                        template.generateClasses(outputDir);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
