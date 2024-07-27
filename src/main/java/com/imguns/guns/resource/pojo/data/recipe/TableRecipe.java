package com.imguns.guns.resource.pojo.data.recipe;

import com.google.gson.annotations.SerializedName;
import com.imguns.guns.crafting.GunSmithTableIngredient;
import com.imguns.guns.crafting.GunSmithTableResult;

import java.util.List;

public class TableRecipe {
    @SerializedName("materials")
    private List<GunSmithTableIngredient> materials;

    @SerializedName("result")
    private GunSmithTableResult result;

    public List<GunSmithTableIngredient> getMaterials() {
        return materials;
    }

    public GunSmithTableResult getResult() {
        return result;
    }
}
