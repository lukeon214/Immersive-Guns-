package com.imguns.guns.config.util;

import com.google.common.collect.Maps;
import com.imguns.guns.config.sync.SyncConfig;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeadShotAABBConfigRead {
    private static final Map<Identifier, Box> AABB_CHECK = Maps.newHashMap();
    // 书写格式：touhou_little_maid:maid [-0.5, 1.0, -0.5, 0.5, 1.5, 0.5]
    // 生物 ID + 碰撞箱
    private static final Pattern REG = Pattern.compile("^([a-z0-9_.-]+:[a-z0-9/._-]+)\s*?\\[([-+]?[0-9]*\\.?[0-9]+),\s*?([-+]?[0-9]*\\.?[0-9]+),\s*?([-+]?[0-9]*\\.?[0-9]+),\s*?([-+]?[0-9]*\\.?[0-9]+),\s*?([-+]?[0-9]*\\.?[0-9]+),\s*?([-+]?[0-9]*\\.?[0-9]+),*?\s*?]");

    public static void init() {
        AABB_CHECK.clear();
        List<String> configData = SyncConfig.HEAD_SHOT_AABB.get();
        for (String text : configData) {
            addCheck(text);
        }
    }

    public static void addCheck(String text) {
        Matcher matcher = REG.matcher(text);
        if (matcher.find()) {
            Identifier id = new Identifier(matcher.group(1));
            double x1 = Double.parseDouble(matcher.group(2));
            double y1 = Double.parseDouble(matcher.group(3));
            double z1 = Double.parseDouble(matcher.group(4));
            double x2 = Double.parseDouble(matcher.group(5));
            double y2 = Double.parseDouble(matcher.group(6));
            double z2 = Double.parseDouble(matcher.group(7));
            Box aabb = new Box(x1, y1, z1, x2, y2, z2);
            AABB_CHECK.put(id, aabb);
        }
    }

    public static void clearAABB() {
        AABB_CHECK.clear();
    }

    public static Box getAABB(Identifier id) {
        return AABB_CHECK.get(id);
    }
}
