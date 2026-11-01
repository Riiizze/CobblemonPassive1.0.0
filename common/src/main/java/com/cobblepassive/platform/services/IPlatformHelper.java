package com.cobblepassive.platform.services;

import java.nio.file.Path;

public interface IPlatformHelper {

    String getPlatformName();

    boolean isModLoaded(String modId);

    Path getConfigDirectory();

    boolean isPhysicalClient();
}
