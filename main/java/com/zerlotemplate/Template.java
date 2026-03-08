package com.zerlotemplate;

import org.springframework.core.io.ClassPathResource;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Template {

    protected String ID;
    protected StringBuilder document;
    protected HashMap<String,Template> snippets = new HashMap<String, Template>();
    protected HashMap<String,List<String>> pasteareas = new HashMap<String, List<String>>();
    protected List<String> slots = new ArrayList<String>();

    private static final Pattern PATTERN_CUT = Pattern.compile(
            "<!--\\scut:(\\S+)\\s-->(.*?)<!--\\s/cut:\\1\\s-->",
            Pattern.DOTALL | Pattern.MULTILINE
    );

    private static final Pattern PATTERN_PASTE = Pattern.compile(
            "<!--\\spaste:(\\S+)\\s-->",
            Pattern.DOTALL | Pattern.MULTILINE
    );

    private static final Pattern PATTERN_SLOT = Pattern.compile(
            "<!--\\sslot:(\\S+)\\s-->",
            Pattern.DOTALL | Pattern.MULTILINE
    );

    public Template(String ID, String document) {
        this.ID = ID;
        Matcher m = Template.PATTERN_CUT.matcher(document);
        StringBuilder result = new StringBuilder();
        while(m.find()) {
            String cutID = m.group(1);
            String cutContent = m.group(2);
            this.snippets.put(cutID, new Template(cutID, cutContent));
            m.appendReplacement(result, "");
        }
        m.appendTail(result);


        m = Template.PATTERN_PASTE.matcher(result);
        while(m.find()) {
            String[] parts = m.group(1).split("\\(", 2);
            String key = parts[0];
            String valuesStr = parts[1].substring(0, parts[1].length() - 1);
            ArrayList<String> values = new ArrayList<String>(Arrays.asList(valuesStr.split(",")));
            this.pasteareas.put(key,values);
        }

        m = Template.PATTERN_SLOT.matcher(result);
        while(m.find()) {
            String slotID = m.group(1);
            if (!this.slots.contains(slotID)) {
                this.slots.add(m.group(1));
            }
        }

        this.document = result;
    }


    public static Template from(ClassPathResource path) {
        try {
            byte[] bytes = path.getInputStream().readAllBytes();
            return new Template( "Root", new String(bytes, StandardCharsets.UTF_8) );

        } catch( IOException except ) {
            throw new RuntimeException( "Unable to load template file: %s ".formatted( path.toString() ) );
        }
    }

    public Template get(String id) {
        return this.snippets.get(id).copy();
    }

    public Template paste(String id, Template snippet) {
        Matcher m = Template.PATTERN_PASTE.matcher(this.document);
        while(m.find()) {
            this.document.insert(m.start(), snippet.toString());
        }
        return this;
    }

    public Template set(String slotID, String value) {
        if (this.slots.contains(slotID)) {
            String mark = "<!-- slot:%s -->".formatted(slotID) ;
            int start = this.document.indexOf(mark);
            while(start != -1) {
                int end = start + mark.length();
                this.document.replace(
                        start,
                        end,
                        HtmlUtils.htmlEscape(value));
                start = this.document.indexOf(mark);
            }
        }
        return this;
    }

    public Template copy() {
        // avoid any parsing
        Template template = new Template(this.ID, "");
        // references are fine here, nothing will be changed
        template.document.append(this.document.toString());
        template.snippets = this.snippets;
        template.pasteareas = this.pasteareas;
        template.slots = this.slots;
        return template;
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public String getClassName() {
        String suffix = "";
        String className = capitalize(this.ID) + suffix;
        return className;
    }

    public String generateClass() {
        String suffix = "";
        String className = capitalize(this.ID) + suffix;
        String classStr = """
            package com.zerlotemplate.snippets;
            
            import com.zerlotemplate.Template;
            
            public class %s {
            
            protected Template template;
            
            public %s(Template t) {
                this.template = t;
            }
            
            public Template unbox() {
                return this.template;
            }
            
        """.formatted(className, className);
        StringBuilder classStrBuild = new StringBuilder( classStr );
        for ( String slot: this.slots ) {
            classStrBuild.append(
                    """
                    public %s set%s( String value ) {
                        this.template.set("%s", value);
                        return this;
                    }
                    """.formatted(className, capitalize(slot), slot)
            );
        }

        for( Map.Entry<String, List<String>> entry : this.pasteareas.entrySet() ) {
            for (String value:entry.getValue()) {
                classStrBuild.append(
                        """
                                public %s paste%s( %s value ) {
                                    this.template.paste("%s", value.unbox() );
                                    return this;
                                }
                        """.formatted(
                                className,
                                capitalize(entry.getKey()),
                                capitalize(value) + suffix,
                                value
                        )
                );
            }
        }

        for (Map.Entry<String, Template> entry : this.snippets.entrySet()) {
            classStrBuild.append("""
                public %s get%s() {
                    return new %s( this.template.get("%s") );
                }
            """.formatted(
                    capitalize(entry.getKey()) + suffix,
                    capitalize(entry.getKey()),
                    capitalize(entry.getKey()) + suffix,
                    entry.getKey()
            ));
        }

        classStrBuild.append("""
        
            public String toString() {
                return this.template.toString();
            }        
            
            public String info() {
                return this.template.info();
            }
            
        }
        """);
        return classStrBuild.toString();


    }

    public String info() {
        return "<Template ID=%s pasties=%s slots=%s >".formatted(
                this.ID,
                this.pasteareas.toString(),
                this.slots.toString());
    }



    public String toString() {
        return this.document.toString();
    }

    public void generateClasses(Path outputDir) throws IOException {
        String classCode = this.generateClass();
        String className = this.getClassName();
        Path outputFile = outputDir.resolve(className + ".java");
        Files.writeString(outputFile, classCode);
        for (Map.Entry<String, Template> entry : this.snippets.entrySet()) {
            entry.getValue().generateClasses(outputDir);
        }
    }


    public static void main(String[] args) throws IOException {
        Path templateDir = Path.of(args[0]);
        Path outputDir = Path.of(args[2]);
        Files.createDirectories(outputDir);
        String classPathPrefix = args[1];
        Files.walk(templateDir)
                .filter(p -> p.toString().endsWith(".html"))
                .forEach(p -> {
                    Template template = Template.from( new ClassPathResource( classPathPrefix + "/" + p.getFileName()) );
                    try {
                        template.generateClasses(outputDir);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

}
