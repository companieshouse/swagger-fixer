package uk.gov.ch.tools.swagger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Source implements ISource {

    private String outputDir = null;
    private Collection<File> inputs;
    private String extension = ".json";

    Source() {
        inputs = Collections.emptySet();
    }

    @Override
    public String getOutputDir() {

        return outputDir;
    }

    @Override
    public Collection<File> getInputFiles() {
        return inputs;
    }

    @Override
    public void setInputFiles(final Collection<String> inputFiles) throws IOException {
        final Set<File> files = new LinkedHashSet<>();
        for (final String file : inputFiles) {
            final File path = Paths.get(file).toFile();
            if (!path.exists()) {
                throw new IOException("Input path '" + path.getAbsolutePath() + "' not found");
            }
            if (path.isDirectory()) {
                final boolean fileList = files.addAll(getInputFilesInDir(path)
                        .collect(Collectors.toList()));
            } else {
                files.add(path);
            }
        }
        inputs = files;
    }

    private Stream<File> getInputFilesInDir(final File path) {
        final String[] list = path.list();
        final Stream<File> files = Arrays.stream(Objects.requireNonNull(list))
                .filter(f -> f.endsWith(extension))
                .map(pathname -> new File(path, pathname))
                .filter(File::isFile);
        return files;
    }

    @Override
    public void setOuputDir(final String oDir) throws IOException {
        File path = Paths.get(oDir).toFile();
        if (path.exists()) {
            if ((!path.isDirectory()) || (!path.canWrite())) {
                throw new IOException("No writeable directory at '" + path.getAbsolutePath()
                        + "' so output not possible");
            }
        } else {
            System.out.println("Creating output directory");
            if (!path.mkdir()) {
                throw new IOException(
                        "Unable to create output directory '" + path.getAbsolutePath() + "'");
            }
        }
    }

}
