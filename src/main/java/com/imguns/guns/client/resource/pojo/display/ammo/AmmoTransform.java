package com.imguns.guns.client.resource.pojo.display.ammo;

import com.google.gson.annotations.SerializedName;
import com.imguns.guns.client.resource.pojo.TransformScale;

public class AmmoTransform {
    @SerializedName("scale")
    private TransformScale scale;

    public TransformScale getScale() {
        return scale;
    }
}
