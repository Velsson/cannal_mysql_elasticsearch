package com.veelur.sync.elasticsearch.event;

import com.alibaba.otter.canal.protocol.CanalEntry.Entry;

/**
 * @author Veelur
 * @version 1.0
 */
public class VerInsertCanalEvent extends CanalEvent {
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public VerInsertCanalEvent(Entry source) {
        super(source);
    }

}
