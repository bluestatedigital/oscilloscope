package com.nuclearfurnace.oscilloscope.discovery;

import com.nuclearfurnace.oscilloscope.discovery.ClusterProvider;
import java.util.concurrent.ConcurrentHashMap;

public final class ProviderRegistry {
    private final static ConcurrentHashMap<String, ClusterProvider> providerMap = new ConcurrentHashMap<>();

    public static void registerProvider(String providerName, ClusterProvider provider) {
        providerMap.put(providerName, provider);
    }

    public static ClusterProvider getProvider(String providerName) {
        return providerMap.get(providerName);
    }
}
