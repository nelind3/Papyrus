package dk.nelind.papyrus.mappings;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dk.nelind.papyrus.Papyrus;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.proguard.ProGuardFileReader;
import net.fabricmc.mappingio.format.tiny.Tiny2FileWriter;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

public class MojmapProvider implements MappingProvider {
    private final File MAPPINGS_FILE;

    public MojmapProvider() {
        this.MAPPINGS_FILE = Papyrus.getCacheDirectory().resolve(
            "mojmap_" + Papyrus.getGameVersion() + "_" + Papyrus.getPhysicalSide() + ".tiny.gz"
        ).toFile();
    }

    @Override
    public File getMappingFile() throws IOException {
        if (!this.MAPPINGS_FILE.exists()) {
            return this.downloadMappings();
        }

        return MAPPINGS_FILE;
    }

    private File downloadMappings() throws IOException {
        MAPPINGS_FILE.getParentFile().mkdirs();
        MAPPINGS_FILE.createNewFile();
        try (
            Reader mappingsReader = new InputStreamReader(this.getMappingURL().openStream());
            Writer mappingsFileWriter = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(MAPPINGS_FILE)));
        ) {
            ProGuardFileReader.read(
                mappingsReader,
                "mojmap",
                "official",
                new MappingSourceNsSwitch(
                    new Tiny2FileWriter(mappingsFileWriter, true),
                    "official"
                )
            );
        }

        return MAPPINGS_FILE;
    }

    private URL getMappingURL() throws IOException {
        try {
            final URL[] versionMetadataUrl = new URL[1];
            URL versionManifestURL = new URI("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json").toURL();
            HttpsURLConnection manifestConn = (HttpsURLConnection) versionManifestURL.openConnection();
            manifestConn.setRequestMethod("GET");
            manifestConn.connect();
            JsonObject versionManifest = JsonParser.parseString(
                new String(manifestConn.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
            ).getAsJsonObject();
            versionManifest.get("versions").getAsJsonArray().forEach((e) -> {
                if (e.getAsJsonObject().get("id").getAsString().equals(Papyrus.getGameVersion())) {
                    try {
                        versionMetadataUrl[0] = new URI(e.getAsJsonObject().get("url").getAsString()).toURL();
                    } catch (MalformedURLException | URISyntaxException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });

            HttpsURLConnection metadataConn = (HttpsURLConnection) versionMetadataUrl[0].openConnection();
            metadataConn.setRequestMethod("GET");
            metadataConn.connect();
            JsonObject versionMetadata = JsonParser.parseString(
                new String(metadataConn.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
            ).getAsJsonObject();

            return new URI(versionMetadata
                .get("downloads")
                .getAsJsonObject()
                .get(Papyrus.getPhysicalSide() + "_mappings")
                .getAsJsonObject()
                .get("url")
                .getAsString()
            ).toURL();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
