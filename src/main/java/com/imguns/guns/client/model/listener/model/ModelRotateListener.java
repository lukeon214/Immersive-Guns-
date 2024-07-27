package com.imguns.guns.client.model.listener.model;

import com.imguns.guns.api.client.animation.AnimationListener;
import com.imguns.guns.api.client.animation.ObjectAnimationChannel;
import com.imguns.guns.client.model.bedrock.ModelRendererWrapper;
import com.imguns.guns.util.math.MathUtil;
import org.joml.Quaternionf;

public class ModelRotateListener implements AnimationListener {
    private final ModelRendererWrapper rendererWrapper;

    public ModelRotateListener(ModelRendererWrapper rendererWrapper) {
        this.rendererWrapper = rendererWrapper;
    }

    @Override
    public void update(float[] values, boolean blend) {
        if (values.length == 4) {
            values = MathUtil.toEulerAngles(values);
        }
        if (blend) {
            float[] q = MathUtil.toQuaternion(values[0], values[1], values[2]);
            Quaternionf quaternion = MathUtil.toQuaternion(q);
            MathUtil.blendQuaternion(rendererWrapper.getAdditionalQuaternion(), quaternion);
        } else {
            MathUtil.toQuaternion(values[0], values[1], values[2], rendererWrapper.getAdditionalQuaternion());
        }
    }

    @Override
    public float[] initialValue() {
        return MathUtil.toQuaternion(rendererWrapper.getRotateAngleX(), rendererWrapper.getRotateAngleY(), rendererWrapper.getRotateAngleZ());
    }

    @Override
    public ObjectAnimationChannel.ChannelType getType() {
        return ObjectAnimationChannel.ChannelType.ROTATION;
    }
}
