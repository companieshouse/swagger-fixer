package uk.gov.ch.tools.swagger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Small application to patch some of the badly formed Swagger 1.2 specifications to make them well
 * formed. The initial draft is based on the insolvency spec json
 */
public class Fix1_2 {

    private static ThreadLocal<File> lastFile = new ThreadLocal<>();
    private ISource source;

    public static void main(String[] args) {
        Fix1_2 fixer = new Fix1_2();
        fixer.parseArgs(args);
        fixer.fixFiles();
    }

    private static String readFile(final File path) {
        try {
            final byte[] encoded = Files.readAllBytes(path.toPath());
            return new String(encoded, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            System.err.println(ex.getLocalizedMessage());
            ex.printStackTrace();
            return null;
        }
    }

    static JsonNode fixJson(File file) {
//        final File file = lastFile.get() == null ? new File(fName)
//                : new File(lastFile.get().getParent(), fName);
        String jsonContent = readFile(file);
        if (lastFile.get() == null) {
            lastFile.set(file.getAbsoluteFile());
        }
        final JsonNodeExtractor ne = new JsonNodeExtractor();
        return ne.convertJson(jsonContent);
    }

    private void fixFiles() {
        source.getInputFiles().forEach(f -> fixContent(f));
    }

    private void fixContent(final File fName) {
        final JsonNode fixedNode = fixJson(fName);
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            final String fixedString = objectMapper.writeValueAsString(fixedNode);
            final byte[] bytes = fixedString.getBytes(StandardCharsets.UTF_8);
            final String outDir = source.getOutputDir();
            Path outPath;
            if (outDir.isEmpty()) {
                outPath = Paths.get(fName + ".fixed.json");
                Files.write(outPath, bytes);
            } else {
                outPath = Paths.get(outDir).resolve(fName.toPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseArgs(String[] args) {
        ArgPackager argPackager = new ArgPackager(args);
        source = argPackager.getSource();
    }

}

