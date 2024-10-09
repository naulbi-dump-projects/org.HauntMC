package hauntmc.HauntBungeeCore.data.config;

public enum SettingsConfiguration {
    USE_MAINTANCE(false);

    private boolean value;

    private SettingsConfiguration(boolean value) {
        this.value = value;
    }

    public boolean isValue() {
        return value;
    }

    public void setValue() {
        this.value = value;
    }
}
