package at.helpch.papibot.core.storage.file;

import at.helpch.papibot.PapiBotBootstrap;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// ------------------------------
// Copyright (c) PiggyPiglet 2018
// https://www.piggypiglet.me
    // ------------------------------
@Singleton
public final class GFile {
    @Inject private Gson gson;

    @Getter private final Map<String, Map<String, Object>> itemMaps = new ConcurrentHashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger("GFile");

    /**
     * Generates a physical file and populates it with embedded content.
     * @param name The name of the file, will be stored and accessible via a map.
     * @param externalPath Path the file will be generated at.
     * @param internalPath Path to the embedded content.
     */
    public void make(String name, String externalPath, String internalPath) {
        name = name.substring(0, name.lastIndexOf('.'));
        File file = new File(externalPath);

        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();

                if (file.createNewFile()) {
                    if (at.helpch.papibot.core.utils.file.FileUtils.exportResource(PapiBotBootstrap.class.getResourceAsStream(internalPath), externalPath)) {
                        insertIntoMap(name, file);

                        LOGGER.info(name + " successfully created & loaded.");
                    } else {
                        LOGGER.error(name + " creation failed.");
                    }
                } else {
                    LOGGER.error(name + " creation failed.");
                }
            } else {
                insertIntoMap(name, file);

                LOGGER.info(name + " successfully loaded.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isJson(File file) {
        return file.getPath().endsWith(".json");
    }

    @SuppressWarnings("unchecked")
    private void insertIntoMap(String name, File file) {
        final Map<String, Object> populatorMap = new HashMap<>();

        try {
            String fileContent = FileUtils.readFileToString(file, "UTF-8");

            if (isJson(file)) {
                populatorMap.put("file-configuration", new FileConfiguration(gson.fromJson(fileContent, LinkedTreeMap.class)));
            } else {
                populatorMap.put("file-content", fileContent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        populatorMap.put("file", file);
        itemMaps.put(name, populatorMap);
    }

    /**
     * Get a FileConfiguration instance for the GFile specified.
     * @param name The name of the GFile to be retrieved.
     * @return Returns a FileConfiguration instance of the referenced GFile.
     */
    public FileConfiguration getFileConfiguration(String name) {
        Object item = itemMaps.get(name).get("file-configuration");

        if (item instanceof FileConfiguration) {
            return (FileConfiguration) item;
        }

        return new FileConfiguration(new HashMap<>());
    }
}
