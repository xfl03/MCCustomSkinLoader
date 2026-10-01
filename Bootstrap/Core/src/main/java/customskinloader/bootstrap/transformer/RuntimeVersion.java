package customskinloader.bootstrap.transformer;

public final class RuntimeVersion {
    private final int protocolVersion;
    private final int worldVersion;

    public RuntimeVersion(int protocolVersion, int worldVersion) {
        this.protocolVersion = protocolVersion;
        this.worldVersion = worldVersion;
    }

    public int getProtocolVersion() {
        return this.protocolVersion;
    }

    public int getWorldVersion() {
        return this.worldVersion;
    }

    @Override
    public String toString() {
        return "protocol=" + this.protocolVersion + ", world=" + this.worldVersion;
    }
}
