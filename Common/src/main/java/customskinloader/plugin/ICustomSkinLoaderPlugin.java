package customskinloader.plugin;

import java.util.List;

import customskinloader.config.SkinSiteProfile;
import customskinloader.loader.ProfileLoader;

public interface ICustomSkinLoaderPlugin {
    /**
     * @return return a non-null value if need to add a new profile loader, otherwise return null.
     */
    ProfileLoader.IProfileLoader getProfileLoader();

    /**
     * @return the default implementations which import by {@link ICustomSkinLoaderPlugin#getProfileLoader()}.
     */
    List<IDefaultProfile> getDefaultProfiles();

    interface IDefaultProfile {
        /**
         * @return the name of {@link SkinSiteProfile#name}.
         */
        String getName();

        /**
         * @return the lower the number, the first to load.
         */
        int getPriority();

        /**
         * Complete the {@link SkinSiteProfile} from config and write to config.
         */
        void updateSkinSiteProfile(SkinSiteProfile ssp);
    }
}
