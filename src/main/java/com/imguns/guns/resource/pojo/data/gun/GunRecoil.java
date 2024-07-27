package com.imguns.guns.resource.pojo.data.gun;

import com.google.gson.annotations.SerializedName;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import org.jetbrains.annotations.Nullable;

public class GunRecoil {
    private static final SplineInterpolator INTERPOLATOR = new SplineInterpolator();

    @SerializedName("pitch")
    @Nullable
    private GunRecoilKeyFrame[] pitch;

    @SerializedName("yaw")
    @Nullable
    private GunRecoilKeyFrame[] yaw;

    public GunRecoilKeyFrame[] getPitch() {
        return pitch;
    }

    public void setPitch(@Nullable GunRecoilKeyFrame[] pitch) {
        this.pitch = pitch;
    }

    public GunRecoilKeyFrame[] getYaw() {
        return yaw;
    }

    public void setYaw(@Nullable GunRecoilKeyFrame[] yaw) {
        this.yaw = yaw;
    }

    /**
     * Returns a spline interpolation function for the vertical recoil of the camera after randomization and scaling.
     *
     * @param modifier Accessory modifications to recoil
     * @return spline interpolation function (math)
     */
    @Nullable
    public PolynomialSplineFunction genPitchSplineFunction(float modifier) {
        return getSplineFunction(pitch, modifier);
    }

    /**
     * Returns a spline interpolation function for the horizontal recoil of the camera after randomization and scaling.
     *
     * @param modifier Accessory modifications to recoil
     * @return spline interpolation function (math)
     */
    @Nullable
    public PolynomialSplineFunction genYawSplineFunction(float modifier) {
        return getSplineFunction(yaw, modifier);
    }

    private PolynomialSplineFunction getSplineFunction(GunRecoilKeyFrame[] keyFrames, float modifier) {
        if (keyFrames == null || keyFrames.length == 0) {
            return null;
        }
        double[] values = new double[keyFrames.length + 1];
        double[] times = new double[keyFrames.length + 1];
        times[0] = 0;
        values[0] = 0;
        for (int i = 0; i < keyFrames.length; i++) {
            times[i + 1] = keyFrames[i].getTime() * 1000 + 30;
        }
        for (int i = 0; i < keyFrames.length; i++) {
            float[] value = keyFrames[i].getValue();
            values[i + 1] = (value[0] + Math.random() * (value[1] - value[0])) * modifier;
        }
        return INTERPOLATOR.interpolate(times, values);
    }
}
