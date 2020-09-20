package customskinloader.fabric;

import java.util.Objects;

public class MinecraftVersion {
    public String id;
    public String name;
    public String release_target;
    public int world_version;
    public int protocol_version;
    public int pack_version;
    public String build_time;
    public boolean stable;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MinecraftVersion that = (MinecraftVersion) o;
        return world_version == that.world_version &&
            protocol_version == that.protocol_version &&
            pack_version == that.pack_version &&
            stable == that.stable &&
            Objects.equals(id, that.id) &&
            Objects.equals(name, that.name) &&
            Objects.equals(release_target, that.release_target) &&
            Objects.equals(build_time, that.build_time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, release_target, world_version, protocol_version, pack_version, build_time, stable);
    }

    @Override
    public String toString() {
        return "MinecraftVersion{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", release_target='" + release_target + '\'' +
            ", world_version=" + world_version +
            ", protocol_version=" + protocol_version +
            ", pack_version=" + pack_version +
            ", build_time='" + build_time + '\'' +
            ", stable=" + stable +
            '}';
    }
}
