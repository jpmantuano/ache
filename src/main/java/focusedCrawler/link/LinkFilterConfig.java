package focusedCrawler.link;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class LinkFilterConfig {

    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    static {
        yamlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @JsonProperty("global.type")
    private String type;

    @JsonProperty("global.whitelist")
    private List<String> whitelist;

    @JsonProperty("global.blacklist")
    private List<String> blacklist;

    private String fileLocation;

    public LinkFilterConfig() {
    }

    public LinkFilterConfig(String configPath) {
        this(Paths.get(configPath));
    }

    public LinkFilterConfig(Path linkFiltersPath) {
        Path linkFiltersFile;
        if (Files.isDirectory(linkFiltersPath)) {
            linkFiltersFile = linkFiltersPath.resolve("link_filters.yml");
        } else {
            linkFiltersFile = linkFiltersPath;
        }
        try {

            LinkFilterConfig linkFilterConfig = yamlMapper.readValue(linkFiltersFile.toFile(), LinkFilterConfig.class);
            init(yamlMapper.readTree(linkFiltersFile.toFile()));
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read config from file: " + linkFiltersFile.toString(), e);
        }
    }

    private void init(JsonNode linkFilters) throws IOException {
        yamlMapper.readerForUpdating(this).readValue(linkFilters);
    }

    public String getType() {
        return type;
    }

    public List<String> getWhitelist() {
        return whitelist;
    }

    public List<String> getBlacklist() {
        return blacklist;
    }

    @JsonSetter("global.type")
    public void setLinkFilterType(String type) {
        this.type = type;
    }

    @JsonSetter("global.whitelist")
    public void setWhiteList(List<String> whitelist) {
        this.whitelist = whitelist;
    }

    @JsonSetter("global.blacklist")
    public void setBlackList(List<String> blacklist) {
        this.blacklist = blacklist;
    }

    public String getFileLocation() {
        if (StringUtils.isNotEmpty(fileLocation)) {
            return fileLocation.trim();
        } else {
            return "";
        }
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }
}
