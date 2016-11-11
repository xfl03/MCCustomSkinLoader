package customskinloader.profile;

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
			this.capeUrl=url;
			return;
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
		return "(SkinUrl: "+skinUrl+" , Model: "+model+" , CapeUrl: "+capeUrl+" , ElytraUrl: "+elytraUrl+(expiry==0?"":(" , Expiry: "+expiry))+")";
	}
	
	/**
	 * Check if the instance is empty.
	 * @return status (true - empty)
	 */
	public boolean isEmpty(){
		return StringUtils.isEmpty(skinUrl) &&  StringUtils.isEmpty(capeUrl) &&  StringUtils.isEmpty(elytraUrl);
	}
	public boolean hasSkinUrl(){
		return StringUtils.isNotEmpty(skinUrl);
	}
}
