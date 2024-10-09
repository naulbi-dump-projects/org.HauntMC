package hauntmc.HauntBungeeCore.data.config.temp;

public class DataConfig {

    private boolean useMaintance;

    public DataConfig(boolean useMaintance) {
        this.useMaintance = useMaintance;
    }

    public boolean isUseMaintance() {
        return useMaintance;
    }

    public void setUseMaintance(boolean value) {
        this.useMaintance = value;
    }
}
