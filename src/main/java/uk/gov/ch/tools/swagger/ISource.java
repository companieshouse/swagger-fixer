package uk.gov.ch.tools.swagger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public interface ISource {

    String getOutputDir();

    Collection<File> getInputFiles();

    void setInputFiles(final Collection<String> inputFiles) throws IOException;

    void setOutputDir(final String oDirs) throws IOException;

    void setWorkingDir(final String s) throws IOException;

    Path getWorkingDir();
}
