package com.imguns.guns.client.model.listener.constraint;

import com.imguns.guns.api.client.animation.AnimationListener;
import com.imguns.guns.api.client.animation.AnimationListenerSupplier;
import com.imguns.guns.api.client.animation.ObjectAnimationChannel;
import com.imguns.guns.client.model.bedrock.BedrockPart;
import com.imguns.guns.client.resource.pojo.model.BonesItem;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import static com.imguns.guns.client.model.BedrockAnimatedModel.CONSTRAINT_NODE;

public class ConstraintObject implements AnimationListenerSupplier {
    public Vector3f translationConstraint = new Vector3f(0, 0, 0);
    public Vector3f rotationConstraint = new Vector3f(0, 0, 0);
    /**
     * When the camera's node is the root, node is empty.
     */
    public BedrockPart node;
    /**
     * When the camera's node is not the root, bonesItem is empty.
     */
    public BonesItem bonesItem;

    @Nullable
    @Override
    public AnimationListener supplyListeners(String nodeName, ObjectAnimationChannel.ChannelType type) {
        if (!nodeName.equals(CONSTRAINT_NODE)) {
            return null;
        }
        if (type.equals(ObjectAnimationChannel.ChannelType.ROTATION)) {
            return new ConstraintRotateListener(this);
        }
        if (type.equals(ObjectAnimationChannel.ChannelType.TRANSLATION)) {
            return new ConstraintTranslateListener(this);
        }
        return null;
    }
}
