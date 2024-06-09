package dk.nelind.papyrus.mappings;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import dk.nelind.papyrus.Papyrus;
import net.fabricmc.mappingio.adapter.MappingDstNsReorder;
import net.fabricmc.mappingio.adapter.MappingNsRenamer;
import net.fabricmc.mappingio.format.tiny.Tiny1FileReader;
import net.fabricmc.mappingio.format.tiny.Tiny2FileWriter;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class YarnProvider implements MappingProvider {
    private final File UPDATE_FILE;

    public YarnProvider() {
        this.UPDATE_FILE = Papyrus.getCacheDirectory().resolve("yarn-updated-last").toFile();
    }

    @Override
    public File getMappingFile() throws IOException {
        boolean shouldUpdate;
        if (UPDATE_FILE.exists()) {
            try (InputStream updateFileStream = new FileInputStream(UPDATE_FILE)) {
                Instant yarnLastUpdated = Instant.ofEpochSecond(
                    Long.parseLong(new String(updateFileStream.readAllBytes(), StandardCharsets.UTF_8))
                );
                shouldUpdate = ChronoUnit.DAYS.between(yarnLastUpdated, Instant.now()) >= 2;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            shouldUpdate = true;
        }
        File[] cachedMappingsFiles = Papyrus.getCacheDirectory().toFile().listFiles(
            (dir, name) -> name.contains("yarn_")
        );

        if (cachedMappingsFiles == null || cachedMappingsFiles.length == 0 || shouldUpdate) {
            return this.downloadMappings();
        }

        Arrays.sort(cachedMappingsFiles);
        return cachedMappingsFiles[cachedMappingsFiles.length - 1];
    }

    private File downloadMappings() throws IOException {
        File mappingsFile = Papyrus.getCacheDirectory().resolve(
            "yarn_" + this.getYarnVersion() + ".tiny.gz"
        ).toFile();
        mappingsFile.getParentFile().mkdirs();
        mappingsFile.createNewFile();

        try (
            Writer fileWriter = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(mappingsFile)));
            FileWriter updateFileWriter = new FileWriter(this.UPDATE_FILE, false);
        ) {
            Tiny1FileReader.read(
                new InputStreamReader(new GZIPInputStream(this.getMavenURL().openStream())),
                new MappingNsRenamer(
                    new MappingDstNsReorder(
                        new Tiny2FileWriter(fileWriter, true),
                        "yarn"
                    ),
                    Map.of("named", "yarn")
                )
            );
            updateFileWriter.write(Long.toString(System.currentTimeMillis() / 1000L));

            return mappingsFile;
        }
    }

    private URL getMavenURL() throws IOException {
        try {
            String yarnVersion = this.getYarnVersion();
            return new URI(
                "https://maven.fabricmc.net/net/fabricmc/yarn/" + yarnVersion + "/yarn-" + yarnVersion + "-tiny.gz"
            ).toURL();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private String getYarnVersion() throws IOException {
        try {
            URL yarnVersionsURL = new URI("https://meta.fabricmc.net/v2/versions/yarn/" + Papyrus.getGameVersion()).toURL();
            HttpsURLConnection yarnVersionsConn = (HttpsURLConnection) yarnVersionsURL.openConnection();
            yarnVersionsConn.setRequestMethod("GET");
            yarnVersionsConn.connect();
            JsonArray yarnVersions = JsonParser.parseString(
                new String(yarnVersionsConn.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
            ).getAsJsonArray();
            return yarnVersions.get(0).getAsJsonObject().get("version").getAsString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
