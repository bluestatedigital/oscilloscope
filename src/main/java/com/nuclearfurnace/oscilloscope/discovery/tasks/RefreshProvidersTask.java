package com.nuclearfurnace.oscilloscope.discovery.tasks;

import com.google.common.collect.ImmutableMultimap;
import com.nuclearfurnace.oscilloscope.discovery.DiscoveryManager;
import io.dropwizard.servlets.tasks.Task;
import java.io.PrintWriter;

public class RefreshProvidersTask extends Task {
    private final DiscoveryManager discoveryManager;

    public RefreshProvidersTask(DiscoveryManager discoveryManager) {
        super("refresh_providers");
        this.discoveryManager = discoveryManager;
    }

    @Override
    public void execute(ImmutableMultimap<String, String> immutableMultimap, PrintWriter printWriter) throws Exception {

    }
}
