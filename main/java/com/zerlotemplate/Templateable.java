package com.zerlotemplate;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Generic interface for a Zerlotemplate class.
 * A Templateable can be used dynamically and statically.
 * It works on an HTML/XML document file containing cut/paste/slots regions,
 * identified by a comment marker. In dynamic mode, upon construction,
 * the document is parsed and cut regions are extracted from the document.
 * To obtain a copy of a cut region, use the getter. To paste a copy of a cut region,
 * use the paste method. To inject plain text (will be encoded) into a slot
 * use the setter. In static mode, the cut regions are converted into
 * classes for typesafety, compile-time template checking and autodiscovery.
 */
public interface Templateable {

        /**
         * Obtain a copy of a cut region in dynamic mode.
         * Pass the ID of the cut region of this template that you wish to obtain
         * a copy of. Only direct subregions are available.
         *
         * @param id
         * @return
         */
        public Templateable get(String id);

        /**
         * Paste a templateable at the location of the paste marker.
         * In dynamic mode, the type check is bypassed.
         *
         * @param id
         * @param snippet
         * @return
         */
        public Templateable paste(String id, Templateable snippet);

        /**
         * Injects the string value into the slot. The string value that you pass
         * will be escaped / encoded according to the output document settings.
         *
         * @param slotID ID of the slot
         * @param value  Value to inject into the slot marker, replacing all slots
         * @return
         */
        public Templateable set(String slotID, String value);

        /**
         * Copies the template, this will create a copy of the template.
         * The string contents of original will be copied so that changing
         * the string contents of one instance of the template will not affect the other.
         * Note that other properties will not be copied, just referenced.
         * So it's not allowed to change slots, paste-regions and cut-regions
         * on an instance basis (this should not be attempted).
         *
         * @return Templateable
         */
        public Templateable copy() ;

        /**
         * Returns the proposed ClassName for this template.
         *
         * @return String
         */
        public String getClassName();

        /**
         * Returns a string for a proposed class implementation for this cut region.
         *
         * @return String
         */
        public String generateClass() ;

        /**
         * Generates a set of classes mirroring the HTML-Document region structure.
         * This should be called from a generator that is hooked up to
         * Maven or Gradle.
         *
         * @param outputDir where to store the class files
         * @throws IOException Exception
         */
        public void generateClasses(Path outputDir) throws IOException;

        /**
         * Returns a string decribing the contents of this cut region for inspection.
         * Contrary to toString() this does not render the template but just provides
         * a brief summary of the object.
         *
         * @return
         */
        public String info();
}
