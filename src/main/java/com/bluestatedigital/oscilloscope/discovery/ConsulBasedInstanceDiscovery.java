package com.bluestatedigital.oscilloscope.discovery;

import com.ecwid.consul.v1.ConsistencyMode;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.catalog.model.CatalogService;
import com.netflix.turbine.discovery.Instance;
import com.netflix.turbine.discovery.InstanceDiscovery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsulBasedInstanceDiscovery implements InstanceDiscovery
{
    @Override
    public Collection<Instance> getInstanceList() throws Exception
    {
        Collection<Instance> instances = new ArrayList<>();
        ConsulClient client = new ConsulClient();

        Response<Map<String, List<String>>> response = client.getCatalogServices(new QueryParams(ConsistencyMode.DEFAULT));
        for(Map.Entry<String, List<String>> service : response.getValue().entrySet())
        {
            if(service.getKey().endsWith("-hystrix"))
            {
                String clusterName = service.getKey().replace("-hystrix", "");
                Response<List<CatalogService>> serviceResponse = client.getCatalogService(service.getKey(), new QueryParams(ConsistencyMode.DEFAULT));
                instances.addAll(serviceResponse.getValue()
                        .stream()
                        .map(node -> new Instance(String.format("%s:%d", node.getServiceAddress(), node.getServicePort()), clusterName, true))
                        .collect(Collectors.toList()));
            }
        }

        return instances;
    }

    public Collection<String> getClusterList() throws Exception
    {
        Collection<String> clusters = new ArrayList<>();
        ConsulClient client = new ConsulClient();

        Response<Map<String, List<String>>> response = client.getCatalogServices(new QueryParams(ConsistencyMode.DEFAULT));
        response.getValue().entrySet()
                .stream()
                .filter(service -> service.getKey().endsWith("-hystrix"))
                .forEach(service -> {
                    String clusterName = service.getKey().replace("-hystrix", "");
                    clusters.add(clusterName);
                });

        return clusters;
    }
}
