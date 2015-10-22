package com.nuclearfurnace.oscilloscope.utility;

import com.netflix.turbine.internal.JsonUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class HystrixMetricsTransformer {
    private static final ArrayList<String> keysToRemove;
    private static final HashMap<String, String> keysToReplace;

    static {
        keysToRemove = new ArrayList<>();
        keysToRemove.add("propertyValue_metricsRollingStatisticalWindowInMilliseconds");
        keysToRemove.add("propertyValue_circuitBreakerRequestVolumeThreshold");
        keysToRemove.add("propertyValue_circuitBreakerSleepWindowInMilliseconds");
        keysToRemove.add("propertyValue_circuitBreakerErrorThresholdPercentage");
        keysToRemove.add("propertyValue_executionIsolationSemaphoreMaxConcurrentRequests");
        keysToRemove.add("propertyValue_executionIsolationThreadInterruptOnTimeout");
        keysToRemove.add("propertyValue_executionIsolationThreadPoolKeyOverride");
        keysToRemove.add("propertyValue_executionIsolationThreadTimeoutInMilliseconds");
        keysToRemove.add("propertyValue_executionTimeoutInMilliseconds");
        keysToRemove.add("propertyValue_requestCacheEnabled");
        keysToRemove.add("propertyValue_requestLogEnabled");
        keysToRemove.add("propertyValue_fallbackIsolationSemaphoreMaxConcurrentRequests");
        keysToRemove.add("TypeAndName");
        keysToRemove.add("InstanceKey");

        keysToReplace = new HashMap<>();
        keysToReplace.put("propertyValue_circuitBreakerForceOpen", "circuitBreaker_forcedOpen");
        keysToReplace.put("propertyValue_circuitBreakerForceClosed", "circuitBreaker_forcedClosed");
        keysToReplace.put("propertyValue_circuitBreakerEnabled", "circuitBreaker_enabled");
        keysToReplace.put("isCircuitBreakerOpen", "circuitBreaker_open");
        keysToReplace.put("propertyValue_executionIsolationStrategy", "execution_isolationStrategy");
    }

    public static String toPrunedJson(Map<String, Object> event) {
        Map<String, Object> newEvent = new HashMap<>(event);

        for(String keyToRemove : keysToRemove) {
            newEvent.remove(keyToRemove);
        }

        for(Map.Entry<String, String> keyToReplace : keysToReplace.entrySet()) {
            Object val = newEvent.remove(keyToReplace.getKey());
            if(val != null) {
                newEvent.put(keyToReplace.getValue(), val);
            }
        }

        return JsonUtility.mapToJson(newEvent);
    }
}
