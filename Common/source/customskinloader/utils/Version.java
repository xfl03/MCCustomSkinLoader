package customskinloader.utils;

import org.apache.commons.lang3.StringUtils;

public class Version implements Comparable {
    private String version;
    private int sub[];

    public Version(String version) {
        if (version == null) version = "";
        this.version = version;

        //Parse Subversion
        String[] split = version.split("\\.");
        sub = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            if (StringUtils.isNumeric(split[i]))
                sub[i] = Integer.parseInt(split[i]);
        }
    }

    public static Version of(String version) {
        return new Version(version);
    }

    public static int compare(String version, String anotherVersion) {
        return Version.of(version).compareTo(anotherVersion);
    }

    @Override
    public String toString() {
        return version;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof String) o = Version.of((String) o);//Parse String version
        if (!(o instanceof Version)) throw new IllegalArgumentException(String.format("'%s' is not a Version.", o));

        Version v = (Version) o;
        int i = 0;
        //Skip same subversion
        while (i < sub.length && i < v.sub.length && sub[i] == v.sub[i]) i++;
        //Compare first non-equal number
        if (i < sub.length && i < v.sub.length)
            return (sub[i] < v.sub[i]) ? -1 : ((sub[i] == v.sub[i]) ? 0 : 1); //Integer.compare(sub[i], v.sub[i]);
        //Compare length if all subversion is same
        return Integer.signum(sub.length - v.sub.length);
    }
}
