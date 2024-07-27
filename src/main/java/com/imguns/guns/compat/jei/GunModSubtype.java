package com.imguns.guns.compat.jei;

import com.imguns.guns.api.item.IAmmo;
import com.imguns.guns.api.item.IAmmoBox;
import com.imguns.guns.api.item.IAttachment;
import com.imguns.guns.api.item.IGun;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import net.minecraft.item.ItemStack;

public class GunModSubtype {
    public static IIngredientSubtypeInterpreter<ItemStack> getAmmoSubtype() {
        return (stack, context) -> {
            if (stack.getItem() instanceof IAmmo iAmmo) {
                return iAmmo.getAmmoId(stack).toString();
            }
            return IIngredientSubtypeInterpreter.NONE;
        };
    }

    public static IIngredientSubtypeInterpreter<ItemStack> getGunSubtype() {
        return (stack, context) -> {
            if (stack.getItem() instanceof IGun iGun) {
                return iGun.getGunId(stack).toString();
            }
            return IIngredientSubtypeInterpreter.NONE;
        };
    }

    public static IIngredientSubtypeInterpreter<ItemStack> getAttachmentSubtype() {
        return (stack, context) -> {
            if (stack.getItem() instanceof IAttachment iAttachment) {
                return iAttachment.getAttachmentId(stack).toString();
            }
            return IIngredientSubtypeInterpreter.NONE;
        };
    }

    public static IIngredientSubtypeInterpreter<ItemStack> getAmmoBoxSubtype() {
        return (stack, context) -> {
            if (stack.getItem() instanceof IAmmoBox iAmmoBox) {
                if (iAmmoBox.isAllTypeCreative(stack)) {
                    return "all_type_creative";
                }
                if (iAmmoBox.isCreative(stack)) {
                    return "creative";
                }
                return String.format("level_%d", iAmmoBox.getAmmoLevel(stack));
            }
            return IIngredientSubtypeInterpreter.NONE;
        };
    }
}
