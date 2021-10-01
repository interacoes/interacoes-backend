package org.dspace.app.rest.iiif;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.iiif.service.CacheEvictService;
import org.dspace.app.rest.iiif.service.util.CacheBeanLocator;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Consumer;
import org.dspace.event.Event;

public class IIIFEventConsumer implements Consumer {

    private static Logger log = org.apache.logging.log4j.LogManager.getLogger(IIIFEventConsumer.class);

    // Gets the service bean.
    CacheEvictService cacheEvictService = CacheBeanLocator.getCacheEvictService();

    // When true all entries will be cleared from cache.
    boolean clearAll = false;

    // Collects modified items for individual removal from cache.
    private Set<DSpaceObject> toEvictFromManifestCache = new HashSet<>();


    @Override
    public void initialize() throws Exception {

    }

    @Override
    public void consume(Context ctx, Event event) throws Exception {
        int st = event.getSubjectType();
        if (!(st == Constants.BUNDLE || st == Constants.ITEM || st == Constants.BITSTREAM)) {
            return;
        }
        DSpaceObject subject = event.getSubject(ctx);
        int et = event.getEventType();

        if (et == Event.DELETE || et == Event.REMOVE) {
            log.warn("IIIF event consumer cannot remove a single item from the cache when " +
                "a bundle is deleted. The entire manifests cache will be emptied.");
            clearAll = true;
        }

        if (st == Constants.BUNDLE) {
            if ((et == Event.ADD || et == Event.MODIFY || et == Event.MODIFY_METADATA || et == Event.REMOVE || et == Event.DELETE) && subject != null) {
                // set subject to be the parent Item.
                subject = ((Bundle) subject).getItems().get(0);
                if (log.isDebugEnabled()) {
                    log.debug("Transforming Bundle event into Item event for "
                        + subject.getID());
                }
            } else {
                return;
            }
        }

        if (st == Constants.BITSTREAM) {
            if (et == Event.DELETE || et == Event.REMOVE) {
                log.warn("IIIF event consumer cannot remove a single item from the cache when " +
                    "a bitstream is deleted. The entire manifests cache will be emptied.");
                clearAll = true;
            }

            if ((et == Event.ADD || et == Event.MODIFY_METADATA  ) && subject != null) {
                // set subject to be the parent Item.
                Bundle bundle = ((Bitstream) subject).getBundles().get(0);
                subject = bundle.getItems().get(0);
                if (log.isDebugEnabled()) {
                    log.debug("Transforming Bitstream event into Item event for "
                        + subject.getID());
                }
            }
            else {
                return;
            }
        }

        if (st == Constants.ITEM && et == Event.ADD) {
            // nothing to evict from cache.
            return;
        }

        switch (et) {
            case Event.ADD:
                toEvictFromManifestCache.add(subject);
            case Event.MODIFY:
                toEvictFromManifestCache.add(subject);
            case Event.MODIFY_METADATA:
                toEvictFromManifestCache.add(subject);
            case Event.REMOVE:
                toEvictFromManifestCache.add(subject);
            case Event.DELETE:
                toEvictFromManifestCache.add(subject);
        }
    }

    @Override
    public void end(Context ctx) throws Exception {
        if (clearAll) {
            cacheEvictService.evictAllCacheValues();
        }
        else {
            for (DSpaceObject dso : toEvictFromManifestCache) {
                UUID uuid = dso.getID();
                cacheEvictService.evictSingleCacheValue(uuid.toString());
            }
        }
        clearAll = false;
        toEvictFromManifestCache.clear();
    }

    @Override
    public void finish(Context ctx) throws Exception {

    }

}
