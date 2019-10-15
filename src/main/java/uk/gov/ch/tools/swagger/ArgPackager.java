package uk.gov.ch.tools.swagger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import uk.gov.ch.tools.ArgParser;

class ArgPackager {

    ISource source = new Source();

    ArgPackager(final String[] args) {
        final String[] filenames = {
                "swagger-codegen/src/main/swaggerspecifications/spec/companyOfficerList.json"
//                ,"swagger-codegen/src/main/swaggerspecifications/spec/companyAddress.json"
//               , "swagger-codegen/src/main/swaggerspecifications/spec/charges.json"
//               ,"swagger-codegen/src/main/swaggerspecifications/spec/companyProfile.json"
//                ,"swagger-codegen/src/main/swaggerspecifications/insolvency/insolvency1.2.json"
        };
        try {
            final String OUTPUT_DIR_PARAM = "-o";
            source.setInputFiles(Arrays.asList(filenames));
            ArgParser argParser = new ArgParser(args);
            if (argParser.hasAny()) {
                if (argParser.has("-i")) {
                    source.setInputFiles(argParser.get("-i"));
                }
                if (argParser.has(OUTPUT_DIR_PARAM)) {
                    final List<String> outputs = argParser.get(OUTPUT_DIR_PARAM);
                    if (outputs.size() > 1) {
                        System.err.println("If " + OUTPUT_DIR_PARAM
                                + "is specified, it must be a single directory or empty for output to the same directory");
                    } else {
                        outputs.stream()
                                .findFirst()
                                .ifPresent(s -> {
                                    try {
                                        source.setOuputDir(s);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });
                    }
                }
            }
        } catch (Exception ex) {

        }
    }

    public ISource getSource() {
        return source;
    }
}
