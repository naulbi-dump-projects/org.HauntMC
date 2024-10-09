package hauntmc.HubCorePlugin.utils.config.manager;

import java.io.*;
import hauntmc.HubCorePlugin.*;
import com.google.common.base.*;
import org.bukkit.configuration.file.*;

public class ConfigManager {

    private File file;
    private Main plugin;
    private FileConfiguration configuration;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
    }

    public ConfigManager(FileConfiguration configuration) {
        setDefaultFiles(configuration, null);
    }

    public ConfigManager(FileConfiguration configuration, File file) {
        setDefaultFiles(configuration, file);
    }

    public ConfigManager(Main plugin, FileConfiguration configuration, File file) {
        this.plugin = plugin;
        setDefaultFiles(configuration, file);
    }

    public ConfigManager(File file) {
        setDefaultFiles(YamlConfiguration.loadConfiguration(file), file);
    }

    public ConfigManager(InputStream stream){
        setDefaultFiles(YamlConfiguration.loadConfiguration(new InputStreamReader(stream)), null);
    }

    public FileConfiguration getConfiguration() {
        return configuration;
    }

    public File getFile() {
        return file;
    }

    public void save() throws Exception{
        Preconditions.checkArgument(file != null, "File is Null");
        configuration.save(file);
    }

    public void saveDefaultConfig() {
        Preconditions.checkArgument(plugin != null, "Main class plugin is Null");
        plugin.saveDefaultConfig();
        setDefaultFiles(plugin.getConfig(), new File(plugin.getDataFolder(), "config.yml"));
    }

    public void setDefaultFiles(FileConfiguration configuration, File file) {
        this.configuration = configuration;
        this.file = file;
    }

}