package dk.nelind.papyrus;

import dk.nelind.papyrus.mappings.MojmapProvider;
import dk.nelind.papyrus.mappings.YarnProvider;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.loader.impl.lib.mappingio.MappingVisitor;
import net.fabricmc.loader.impl.lib.mappingio.format.tiny.Tiny2FileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;

public class Papyrus implements PreLaunchEntrypoint {
    public static final Logger LOGGER = LoggerFactory.getLogger("Papyrus");

	@Override
	public void onPreLaunch() {
        LOGGER.info("Injecting extra mappings.");

        try {
            this.injectMappings(new YarnProvider().getMappingFile());
            this.injectMappings(new MojmapProvider().getMappingFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LOGGER.info(
            "Injecting done. Available mapping namespaces now are: {}",
            FabricLoader.getInstance().getMappingResolver().getNamespaces()
        );
    }

    private void injectMappings(File mappingsFile) throws IOException {
        try (
            Reader mappingsFileReader = new InputStreamReader(new GZIPInputStream(new FileInputStream(mappingsFile)))
        ) {
            MappingVisitor launcherMappings = (MappingVisitor) FabricLauncherBase.getLauncher().getMappingConfiguration().getMappings();
            Tiny2FileReader.read(mappingsFileReader, launcherMappings);
        }
    }

    public static String getGameVersion() {
        return FabricLoader
            .getInstance()
            .getModContainer("minecraft")
            .orElseThrow(() -> new NoSuchElementException("Couldn't get Minecraft mod container! This should be impossible!"))
            .getMetadata()
            .getVersion()
            .getFriendlyString();
    }

    public static Path getCacheDirectory() {
        return FabricLoader
            .getInstance()
            .getGameDir()
            .resolve("cache/papyrus");
    }

    public static String getPhysicalSide() {
        return FabricLoader
            .getInstance()
            .getEnvironmentType()
            .toString()
            .toLowerCase();
    }
}