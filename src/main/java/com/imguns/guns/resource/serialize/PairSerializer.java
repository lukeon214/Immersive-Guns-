package com.imguns.guns.resource.serialize;

import com.google.gson.*;
import net.minecraft.util.JsonHelper;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Type;

public class PairSerializer implements JsonDeserializer<Pair<Float, Float>>, JsonSerializer<Pair<Float, Float>> {
    @Override
    public Pair<Float, Float> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonArray()) {
            JsonArray array = json.getAsJsonArray();
            if (array.size() < 2) {
                throw new JsonSyntaxException("Expected " + json + " length of the array must >= 2");
            }
            JsonElement leftElement = array.get(0);
            JsonElement rightElement = array.get(1);
            float left = JsonHelper.asFloat(leftElement, "(array i=0)");
            float right = JsonHelper.asFloat(rightElement, "(array i=1)");
            if (left > right) {
                throw new JsonSyntaxException("Expected " + json + " left must <= right");
            }
            return Pair.of(left, right);
        } else {
            throw new JsonSyntaxException("Expected " + json + " to be a Pair because it's not an array");
        }
    }

    @Override
    public JsonElement serialize(Pair<Float, Float> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray array = new JsonArray(2);
        array.set(0, new JsonPrimitive(src.getLeft()));
        array.set(1, new JsonPrimitive(src.getRight()));
        return array;
    }
}
