package customskinloader.config;

import java.lang.reflect.Field;

public class SkinSiteProfile {
    //Common
    public String name;
    public String type;
    public String userAgent;

    //Mojang API
    public String apiRoot;
    public String sessionRoot;

    //Json API
    public String root;

    //Legacy
    public Boolean checkPNG;//Not suitable for local skin
    public String skin;
    public String model;
    public String cape;
    public String elytra;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{ ");
        Field[] fields = SkinSiteProfile.class.getDeclaredFields();
        boolean first = true;
        for (Field field : fields) {
            try {
                Object value = field.get(this);
                if (value == null) continue;
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append("\"").append(field.getName()).append("\": \"").append(value).append("\"");
            } catch (Exception ignored) { }
        }
        return sb.append(" }").toString();
    }
}
