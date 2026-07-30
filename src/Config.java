import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

    private final Properties properties;

    @SuppressWarnings("ConvertToTryWithResources")
    public Config(String filePath) throws IOException {
        properties = new Properties();
        FileInputStream fis = new FileInputStream(filePath);
        properties.load(fis);
        fis.close();
    }

    // -------------------------------
    // Basic Site Information
    // -------------------------------
    /*
    only the config file is allowed to read the properties file directly
    */
    public int getSiteId() {
        return Integer.parseInt(properties.getProperty("site.id"));
    }

    public String getSiteName() {
        return properties.getProperty("site.name");
    }

    public String getHost() {
        return properties.getProperty("host");
    }

    public int getPort() {
        return Integer.parseInt(properties.getProperty("port"));
    }

    // -------------------------------
    // Coordinator
    // -------------------------------
    public int getCoordinatorId() {
        return Integer.parseInt(properties.getProperty("coordinator.id"));
    }

    // -------------------------------
    // Network Information
    // -------------------------------
    public String getHost(int siteId) {
        return properties.getProperty("site" + siteId + ".host");
    }

    public int getPort(int siteId) {
        return Integer.parseInt(properties.getProperty("site" + siteId + ".port"));
    }

    // -------------------------------
    // Total Number of Universities
    // -------------------------------
    public int getTotalSites() {
        return Integer.parseInt(properties.getProperty("total.sites"));
    }
}