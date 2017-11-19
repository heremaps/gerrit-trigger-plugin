package com.sonyericsson.hudson.plugins.gerrit.trigger.hudsontrigger;

import com.sonyericsson.hudson.plugins.gerrit.trigger.hudsontrigger.data.GerritProject;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * xxx.
 */
final class CacheProxy {
    private static final CacheProxy proxy = new CacheProxy();
    private final Map<String, List<GerritProject>> cache = new HashMap<String, List<GerritProject>>();
    private final Map<String, Long> ttl = new HashMap<String, Long>();

    /**
     * xxx.
     */
    private CacheProxy() {
    }

    /**
     * xxx.
     *
     * @param url ooo
     * @return ooo
     * @throws IOException ooo
     * @throws ParseException ooo
     */
    List<GerritProject> fetchThroughCache(String url) throws IOException, ParseException {
        if (cache.containsKey(url) && !isExpired(url)) {
            return cache.get(url);
        }

        List<GerritProject> gerritProjects = GerritDynamicUrlProcessor.fetch(url);
        ttl.put(url, System.currentTimeMillis());
        cache.put(url, gerritProjects);

        return gerritProjects;
    }

    /**
     * xxx.
     *
     * @return xxx
     */
    static CacheProxy getProxy() {
        return proxy;
    }

    /**
     * xxx.
     *
     * @param url xxx
     * @return xxx
     */
    private boolean isExpired(String url) {
        Long lastTimeUpdated = ttl.get(url);
        if (lastTimeUpdated == null) {
            lastTimeUpdated = System.currentTimeMillis();
        }
        long updateInterval = GerritTriggerTimer.getInstance().calculateAverageDynamicConfigRefreshInterval();
        return System.currentTimeMillis() - lastTimeUpdated > updateInterval;
    }
}
