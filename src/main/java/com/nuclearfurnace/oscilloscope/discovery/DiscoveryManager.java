package com.nuclearfurnace.oscilloscope.discovery;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DiscoveryManager implements Managed {
    private final Logger logger = LoggerFactory.getLogger(DiscoveryManager.class);

    private final Timer providerRefreshTimer = new Timer();
    private final ConcurrentHashMap<String, ClusterProvider> providers = new ConcurrentHashMap<>();
    private final DynamicStringProperty rawProviderNames =
            DynamicPropertyFactory.getInstance().getStringProperty("oscilloscope.discovery.providers", "");

    public void start() {
        // Fucking closures.
        DiscoveryManager self = this;

        providerRefreshTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() { self.refreshProviders(); }
        }, 0, 30 * 1000);

        logger.info("Discovery manager started!");
    }


    public void stop() {
        providerRefreshTimer.cancel();

        logger.info("Discovery manager stopped!");
    }

    public void refreshProviders() {
        Set<String> existingProviderNames = new HashSet<>(providers.keySet());

        String unsplitProviderNames = rawProviderNames.get();
        if(unsplitProviderNames == null) {
            logger.info("No providers configured.  Leaving registered providers untouched.");
            return;
        }

        // Add our new providers.
        Set<String> newProviderNames = new HashSet<>(Arrays.asList(unsplitProviderNames.split(",")));
        for(String newProviderName : newProviderNames) {
            // Don't recreate providers we already have.
            if(providers.containsKey(newProviderName)) {
                continue;
            }

            ClusterProvider provider = ProviderRegistry.getProvider(newProviderName);
            if(provider == null) {
                // Log something about a non-existent provider here.
                logger.error("Provider '{}' provided but does not have a registered cluster provider!", newProviderName);
                continue;
            }

            logger.info("Registered new provider '{}'!", newProviderName);

            providers.put(newProviderName, provider);
        }

        // Now remove all of the providers that we have but aren't in our list.  Not thrilled that
        // this is modifying `existingProviderNames` in situ but whatever.
        existingProviderNames.removeAll(newProviderNames);
        for(String providerNameToDelete : existingProviderNames) {
            providers.remove(providerNameToDelete);
        }
    }

    public List<Cluster> getClusters() {
        Map<String, ClusterProvider> providersClone = new HashMap<>(providers);

        return Observable.from(providersClone.values())
                .flatMap(provider -> provider.getClusters())
                .toList()
                .toBlocking()
                .last();
    }

    public ClusterProvider getProvider(String providerName) {
        return providers.get(providerName);
    }
}
