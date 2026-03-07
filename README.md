Zerlotte
========

Zerlotte stands for Zero Logic Template (zer lo te) - and is a simple template engine for Java Spring.

Zerlotte is a hybrid between a template engine and a code generator,
turning HTML templates into type-safe, composable Java objects that mirror the HTML
structure and make building dynamic pages fluent, safe, and IDE-friendly.


Compile time errors instead of Runtime errors
---------------------------------------------

Unlike string-based templates or Thymeleaf/Handlebars, slot names and
composable template parts are compile-time methods.
Mistyping a slot or paste ID will cause a compile error (not a runtime one).
Also, if the designer removes a slot and you still try to fill it,
you will also get a compile-time error. You can also never insert the
wrong piece of HTML, the building blocks are clearly defined by types and
only composable according to the rules in the template file.

Clean Separation of content and code
------------------------------------

HTML designers can work on templates with minimal Java knowledge.
They mark regions that can be used in other regions as well as slots that
can be filled with plain text:

```
<!-- cut:book -->
<div><!-- slot:title --></div>
<!-- /cut:book -->
```

Java developers get a typed API to populate content.


Typesafety
----------

Designers can enforce certain restrictions in their templates.
For instance, a list of books that must only contain books and magazine but no 
leaflets can be written as:

```
<!-- paste:books(book,magazine) -->
```

For Java developers this means that the BookShelf has
a method pasteBook( Book ) and a method pasteBook( Magazine ) but
no way of adding a leaflet to the shelf.
The HTML becomes the source of truth.

Autocomplete driven templating
------------------------------

As a bonus, the IDE will help Java developers to explore and
understand the template and how it's meant to be used.


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
	
	// Create a new bookshop GUI
	Shop shop = new Shop(t);
	
	// Add a shelf for the books
	Shelf shelf = root.getShelf();
	
	// Get book templates
	Book book = shelf.getBook();
	Book book2 = shelf.getBook();
	
	// Set titles (fills slot), you cannot
	// set a slot that does not exist
	book.setTitle("my book");
	book2.setTitle("second book");
	
	// Put books on the shelf
	// You cannot put items on the shelf that
	// the designer does not allow
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
The architecture is not mature yet, it's just an initial draft written
in two evenings.

I just had to create a repository and push the code to get it out of my head.
This is an idea that I have had for years and it just wanted out ;-)



