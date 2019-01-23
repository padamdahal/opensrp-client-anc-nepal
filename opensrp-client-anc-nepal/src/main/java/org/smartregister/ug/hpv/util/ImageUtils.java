package org.smartregister.ug.hpv.util;

import org.smartregister.domain.Photo;
import org.smartregister.domain.ProfileImage;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.application.HpvApplication;

/**
 * Created by ndegwamartin on 12/04/2018.
 */

public class ImageUtils {
    public static int getProfileImageResourceIDentifier() {
        return R.drawable.ic_african_girl;
    }

    public static Photo profilePhotoByClientID(String clientEntityId) {
        Photo photo = new Photo();
        ProfileImage profileImage = HpvApplication.getInstance().getContext().imageRepository().findByEntityId(clientEntityId);
        if (profileImage != null) {
            photo.setFilePath(profileImage.getFilepath());
        } else {
            photo.setResourceId(getProfileImageResourceIDentifier());
        }
        return photo;
    }
}
