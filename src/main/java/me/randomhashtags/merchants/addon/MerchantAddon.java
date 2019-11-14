package me.randomhashtags.merchants.addon;

import me.randomhashtags.merchants.util.universal.UVersionable;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public abstract class MerchantAddon implements UVersionable {
    protected File file;
    protected YamlConfiguration yml;
    public void load(File file) {
        if(!file.exists()) {
        }
        this.file = file;
        yml = YamlConfiguration.loadConfiguration(file);
    }
    public File getFile() { return file; }
    public YamlConfiguration getYaml() { return yml; }
    public String getYamlName() { return file.getName().split("\\.yml")[0]; }
}
