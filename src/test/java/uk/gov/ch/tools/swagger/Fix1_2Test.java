package uk.gov.ch.tools.swagger;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Fix1_2Test {

    @BeforeEach
    void setUp() {
    }

    @Test
    void main() throws Exception {
        Path output = Files.createTempDirectory(null);
        output.toFile().deleteOnExit();
        Fix1_2.main("-i", "/Users/dclarke/dev/api.ch.gov.uk-specifications/swagger-1.2/spec/admin",
                "-o", output.toString());
    }

    @Test
    void fixJson() {
    }
}