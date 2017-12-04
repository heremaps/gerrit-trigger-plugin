package com.sonyericsson.hudson.plugins.gerrit.trigger.hudsontrigger;

import com.sonyericsson.hudson.plugins.gerrit.trigger.hudsontrigger.data.GerritProject;
import hudson.util.TimeUnit2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * xxx.
 */
final class CacheProxy {
    private static final CacheProxy CACHE_PROXY = new CacheProxy();
    private final Map<String, List<GerritProject>> cache = new HashMap<String, List<GerritProject>>();
    private static final Logger logger = LoggerFactory.getLogger(CacheProxy.class);
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
    synchronized List<GerritProject> fetchThroughCache(String url) throws IOException, ParseException {
        if (cache.containsKey(url) && !isExpired(url)) {
            logger.debug("Get dynamic projects from cache for URL: " + url);
            return cache.get(url);
        }

        List<GerritProject> gerritProjects = GerritDynamicUrlProcessor.fetch(url);
        ttl.put(url, System.currentTimeMillis());
        cache.put(url, gerritProjects);

        logger.info("Get dynamic projects directly for URL: {}", url);

        return gerritProjects;
    }

    /**
     * xxx.
     *
     * @return xxx
     */
    static CacheProxy getCacheProxy() {
        return CACHE_PROXY;
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
        return TimeUnit2.MILLISECONDS.toSeconds(System.currentTimeMillis() - lastTimeUpdated) > updateInterval;
    }
}
