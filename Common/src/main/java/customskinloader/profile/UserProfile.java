package customskinloader.profile;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.authlib.properties.Property;
import customskinloader.utils.TextureUtil;
import org.apache.commons.lang3.StringUtils;

import customskinloader.profile.ModelManager0.Model;

/**The instance to storage user's profile in memory temporarily.
 * In this manner, it could be easier to pass profile in program.
 * @since 13.1
 */
public class UserProfile {
    
    /**
     * Direct url for skin.
     * @since 13.1
     */
    public String skinUrl=null;
    
    /**
     * Model for skin.
     * default/slim
     * @since 13.1
     */
    public String model=null;
    
    /**
     * Direct url for cape.
     * @since 13.1
     */
    public String capeUrl=null;
    
    /**
     * Direct url for elytra.
     * @since 14.5
     */
    public String elytraUrl=null;
    
    public void put(Model model,String url){
        if(model==null||StringUtils.isEmpty(url))
            return;
        switch(model){
        case SKIN_DEFAULT:
            this.skinUrl=url;
            this.model="default";
            return;
        case SKIN_SLIM:
            this.skinUrl=url;
            this.model="slim";
            return;
        case CAPE:
            this.capeUrl=url;
            return;
        case ELYTRA:
            this.elytraUrl = url;
        }
    }
    
    /**
     * Get parsed String to output the instance.
     */
    @Override
    public String toString(){
        return toString(0);
    }
    public String toString(long expiry){
        return "(SkinUrl: "+skinUrl+
                " , Model: "+model+
                " , CapeUrl: "+capeUrl+
                (StringUtils.isBlank(elytraUrl)?" ":" , ElytraUrl: "+elytraUrl)+
                (expiry==0?"":(" , Expiry: "+expiry))+")";
    }

    public List<Property> toProperties() {
        return Lists.newArrayList(
            new Property("skinUrl"  , nullToValue(this.skinUrl  )),
            new Property("model"    , nullToValue(this.model    )),
            new Property("capeUrl"  , nullToValue(this.capeUrl  )),
            new Property("elytraUrl", nullToValue(this.elytraUrl))
        );
    }

    public static UserProfile fromProperties(Collection<Property> properties) {
        UserProfile profile = new UserProfile();
        if (properties != null) {
            for (Property property : properties) {
                switch ((String) TextureUtil.AuthlibField.PROPERTY_NAME.get(property)) {
                    case "skinUrl"  : profile.skinUrl   = nullToValue(TextureUtil.AuthlibField.PROPERTY_VALUE.get(property)); break;
                    case "model"    : profile.model     = nullToValue(TextureUtil.AuthlibField.PROPERTY_VALUE.get(property)); break;
                    case "capeUrl"  : profile.capeUrl   = nullToValue(TextureUtil.AuthlibField.PROPERTY_VALUE.get(property)); break;
                    case "elytraUrl": profile.elytraUrl = nullToValue(TextureUtil.AuthlibField.PROPERTY_VALUE.get(property)); break;
                }
            }
        }
        return profile;
    }

    private static String nullToValue(String value) {
        return value == null ? "null" : value.equals("null") ? null : value;
    }
    
    /**
     * Check if the instance is empty.
     * @return status (true - empty)
     */
    public boolean isEmpty(){
        return StringUtils.isEmpty(skinUrl) &&  StringUtils.isEmpty(capeUrl) &&  StringUtils.isEmpty(elytraUrl);
    }
    /**
     * Check if the instance is full(Both skin and cape).
     * @return status (true - full)
     * @since 14.7
     */
    public boolean isFull(){
        return StringUtils.isNoneBlank(skinUrl,capeUrl);
    }
    public boolean hasSkinUrl(){
        return StringUtils.isNotEmpty(skinUrl);
    }
    public void mix(UserProfile profile){
        if(profile==null)
            return;
        if(StringUtils.isEmpty(this.skinUrl)) {
            this.skinUrl = profile.skinUrl;
            this.model = profile.model;
        }
        if(StringUtils.isEmpty(this.capeUrl))
            this.capeUrl=profile.capeUrl;
        if(StringUtils.isEmpty(this.elytraUrl))
            this.elytraUrl=profile.elytraUrl;
    }
}
