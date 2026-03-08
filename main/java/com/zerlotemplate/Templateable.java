package com.zerlotemplate;

import java.io.IOException;
import java.nio.file.Path;

public interface Templateable {
        public Templateable get(String id);
        public Templateable paste(String id, Templateable snippet);
        public Templateable set(String slotID, String value);
        public Templateable copy() ;
        public String getClassName();
        public String generateClass() ;
        public void generateClasses(Path outputDir) throws IOException;
        public String info();
}
