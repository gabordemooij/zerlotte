package com.zerlotemplate;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Template Generator to use in Maven/Gradle setup.
 * Only tested with Maven currently.
 */
public class TemplateGenerator {

    /**
     * Main method to generate the template classes.
     * Should be integrated with Maven.
     * Currently, uses exec-plugin but mvn generate-template-objects
     * or so would be nicer!
     *
     * Arguments
     * Argument 1: Path to template dir
     * Argument 2: Classpath prefix (sorry, I was lazy)
     * Argument 2: Path to output dir for classes
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        Path templateDir = Path.of(args[0]);
        String classPathPrefix = args[1];
        Path outputDir = Path.of(args[2]);
        Files.createDirectories(outputDir);
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
