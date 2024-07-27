package com.imguns.guns.resource.serialize;

import com.google.gson.*;
import com.imguns.guns.crafting.GunSmithTableIngredient;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.JsonHelper;

import java.lang.reflect.Type;

public class GunSmithTableIngredientSerializer implements JsonDeserializer<GunSmithTableIngredient> {
    @Override
    public GunSmithTableIngredient deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonObject()) {
            JsonObject jsonObject = json.getAsJsonObject();
            if (!jsonObject.has("item")) {
                throw new JsonSyntaxException("Expected " + jsonObject + " must has a item member");
            }
            Ingredient ingredient = Ingredient.fromJson(jsonObject.get("item"));
            int count = 1;
            if (jsonObject.has("count")) {
                count = Math.max(JsonHelper.getInt(jsonObject, "count"), 1);
            }
            return new GunSmithTableIngredient(ingredient, count);
        } else {
            throw new JsonSyntaxException("Expected " + json + " to be a Pair because it's not an object");
        }
    }
}
