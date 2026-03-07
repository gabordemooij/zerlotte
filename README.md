Zerlotte
========

Zerlotte stands for Zero Logic Template (zer lo te) - and is a simple template engine for Java Spring.

Zerlotte is a hybrid between a template engine and a code generator,
turning HTML templates into type-safe, composable Java objects that mirror the HTML
structure and make building dynamic pages fluent, safe, and IDE-friendly.


Type safety
-----------

Unlike string-based templates or Thymeleaf/Handlebars, slot names are compile-time methods.
Mistyping a slot or paste ID will cause a compile error.

Fluent and readable code
------------------------

You can chain setters and paste snippets naturally.
Makes template building feel like native Java programming, instead of string manipulation.

Separation of content and code:
-------------------------------

HTML designers can work on templates with minimal Java knowledge.
Java developers get a typed API to populate content.

Recursive snippets:
-------------------

Snippets can contain other snippets.
The generated API mirrors the hierarchy of your HTML, making nested templates easy to use.

Example
=======

```
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title><!-- slot:title --></title>
</head>
<body>
    <p>
        Welcome to my bookshop!
        <br>
        Books:
        <br>
    </p>
    <!-- cut:shelf -->
    <table>
        <!-- cut:book -->
        <tr>
            <td>
                <!-- slot:title -->
            </td>
        </tr>
        <!-- /cut:book -->
        <!-- cut:magazine -->
        <tr>
            <td>
                <!-- slot:title -->
            </td>
        </tr>
        <!-- /cut:magazine -->
        <!-- paste:books(book,magazine) -->
    </table>
    <!-- /cut:shelf -->
    <!-- paste:shelves(shelf) -->
</body>
</html>
```

Hook the template generator up to maven like this:

```
<build>
		<plugins>
			<plugin>
				<groupId>org.codehaus.mojo</groupId>
				<artifactId>exec-maven-plugin</artifactId>
				<version>3.1.0</version>
				<executions>
					<execution>
						<phase>process-classes</phase>
						<goals>
							<goal>java</goal>
						</goals>
						<configuration>
							<mainClass>com.zerlotemplate.Template</mainClass>
							<arguments>
								<argument>${project.basedir}/src/main/resources/templates</argument>
								<argument>templates</argument>
								<argument>target/generated-sources/templates/com/zerlotemplate/snippets</argument>
							</arguments>
						</configuration>
					</execution>
				</executions>
			</plugin>
			<plugin>
				<groupId>org.codehaus.mojo</groupId>
				<artifactId>build-helper-maven-plugin</artifactId>
				<version>3.5.0</version>
				<executions>
					<execution>
						<id>add-source</id>
						<phase>generate-sources</phase>
						<goals>
							<goal>add-source</goal>
						</goals>
						<configuration>
							<sources>
								<source>target/generated-sources/templates</source>
							</sources>
						</configuration>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>
```

Java code (Spring boot):

```
import com.zerlotemplate.snippets.*;

...

public ResponseEntity<Object> bookshop() {
		Template t = Template.from(new ClassPathResource("templates/bookshop.html"));
		Shop shop = new Shop(t);
		Shelf shelf = root.getShelf();
		Book book = shelf.getBook();
		Book book2 = shelf.getBook();
		book.setTitle("my book");
		book2.setTitle("second book");
		shelf.pasteBooks(book);
		shelf.pasteBooks(book2);
		root.pasteShelves(shelf);
		String html = root.toString();
		return ResponseEntity.ok()
				.header("Content-Type", "text/html")
				.body(html);
	}
```

Note that this is a version 0.1/draft of the project.
I just had to create a repository and push the code to get it out of my head.
This is an idea that I have had for years and it just wanted out ;-)



