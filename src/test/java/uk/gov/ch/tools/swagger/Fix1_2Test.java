package uk.gov.ch.tools.swagger;

import java.io.File;
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
        String outputName = new File("target/fixed-1.2").getCanonicalPath();
        final String workingDir = new File("../../dev/api.ch.gov.uk-specifications/swagger-1.2")
                .getCanonicalPath();
        Fix1_2.main("-w", workingDir, "-i", "spec", "spec/admin",
                "-o", outputName);
    }

    @Test
    void fixJson() {
    }
}