package hauntmc.HauntBungeeCore.data.config;

public enum PermissionConfiguration {
    COMMAND("bungeecore.command");

    private String permission;

    private PermissionConfiguration(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }

}
