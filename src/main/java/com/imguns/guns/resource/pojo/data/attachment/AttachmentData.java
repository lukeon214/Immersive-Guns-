package com.imguns.guns.resource.pojo.data.attachment;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

public class AttachmentData {
    @SerializedName("silence")
    @Nullable
    private Silence silence;

    @SerializedName("weight")
    private float weight = 0;

    @SerializedName("ads_addend")
    private float adsAddendTime = 0;

    @SerializedName("extended_mag_level")
    private int extendedMagLevel = 0;

    @SerializedName("inaccuracy_addend")
    private float inaccuracyAddend = 0;

    @SerializedName("damage")
    private float damageAddend = 0;

    @SerializedName("recoil_modifier")
    @Nullable
    private RecoilModifier recoilModifier = null;

    @SerializedName("melee")
    @Nullable
    private MeleeData meleeData = null;

    @Nullable
    public Silence getSilence() {
        return silence;
    }

    public float getWeight() {
        return weight;
    }

    public int getExtendedMagLevel() {
        return extendedMagLevel;
    }

    public float getAdsAddendTime() {
        return adsAddendTime;
    }

    public float getInaccuracyAddend() {
        return inaccuracyAddend;
    }

    public float getDamageAddend() {
        return damageAddend;
    }

    @Nullable
    public RecoilModifier getRecoilModifier() {
        return recoilModifier;
    }

    @Nullable
    public MeleeData getMeleeData() {
        return meleeData;
    }
}
