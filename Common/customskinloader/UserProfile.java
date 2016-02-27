package customskinloader;

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
	 * Get parsed String to output the instance.
	 */
	@Override
	public String toString(){
		return "(SkinUrl: "+skinUrl+" , Model: "+model+" , CapeUrl: "+capeUrl+")";
	}
	
	/**
	 * Check if the instance is empty.
	 * @return status (true - empty)
	 */
	public boolean isEmpty(){
		return skinUrl==null && model==null && capeUrl==null;
	}
}
