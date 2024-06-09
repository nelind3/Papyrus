package dk.nelind.papyrus.mappings;

import dk.nelind.papyrus.Papyrus;

import java.io.File;
import java.io.IOException;

public interface MappingProvider {
    /**
     * Gets a {@link File} to a gzipped tiny v2 mappings file containing mappings from official (obfuscated) to the provided mappings.
     * The file should be located in the directory provided by {@link Papyrus#getCacheDirectory()}
     * and should be named in a mapping and version specific way.
     * @return {@link File} of the mappings file.
     */
    File getMappingFile() throws IOException;
}
