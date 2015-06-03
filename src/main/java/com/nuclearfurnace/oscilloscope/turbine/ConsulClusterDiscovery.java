package com.nuclearfurnace.oscilloscope.turbine;

import com.ecwid.consul.v1.ConsistencyMode;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;

import java.util.*;

public class ConsulClusterDiscovery implements ClusterDiscovery {
    @Override
    public Collection<String> getClusters() {
        HashSet<String> clusters = new HashSet<>();

        try {
            ConsulClient client = new ConsulClient();
            Response<Map<String, List<String>>> response = client.getCatalogServices(new QueryParams(ConsistencyMode.DEFAULT));
            for(Map.Entry<String, List<String>> entry : response.getValue().entrySet()) {
                if(entry.getValue().contains("hystrix")) {
                    clusters.add(entry.getKey());
                }
            }
        } catch(Exception e) {
        }

        return clusters;
    }
}
