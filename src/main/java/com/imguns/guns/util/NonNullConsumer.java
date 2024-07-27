package com.imguns.guns.util;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface NonNullConsumer<T> {
    void accept(@NotNull T var1);
}
