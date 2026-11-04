package com.cobblepassive.platform;

import com.cobblepassive.platform.services.IPlatformHelper;
import java.util.ServiceLoader;

public final class Services {

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    public static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz, clazz.getClassLoader())
                .findFirst()
                .or(() -> ServiceLoader.load(clazz).findFirst())
                .orElseThrow(() -> new NullPointerException("Failed to load service implementation for: " + clazz.getName()));
    }

    private Services() {}
}
