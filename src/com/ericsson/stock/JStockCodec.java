package com.ericsson.stock;

import com.apama.iaf.plugin.AbstractEventCodec;
import com.apama.iaf.plugin.CodecException;
import com.apama.iaf.plugin.CodecStatus;
import com.apama.iaf.plugin.EventCodecProperty;
import com.apama.iaf.plugin.EventTransport;
import com.apama.iaf.plugin.NormalisedEvent;
import com.apama.iaf.plugin.NormalisedEventIterator;
import com.apama.iaf.plugin.SemanticMapper;
import com.apama.iaf.plugin.SemanticMapperException;
import com.apama.iaf.plugin.TimestampConfig;
import com.apama.iaf.plugin.TransportException;
import com.apama.util.Logger;
import com.apama.util.TimestampSet;

public class JStockCodec extends AbstractEventCodec {

    private final Logger LOGGER;
    private EventTransport eventTransport;
    private SemanticMapper semanticMapper;
    private int encoded;
    private int decoded;

    public JStockCodec(String name, EventCodecProperty[] properties, TimestampConfig timestampConfig) throws CodecException {
        super(name, properties, timestampConfig);
        LOGGER = Logger.getLogger(name);
        updateProperties(properties, timestampConfig);
        LOGGER.info("String Codec Plugin v1.1 initialized");
    }

    public void updateProperties(EventCodecProperty[] properties, TimestampConfig timestampConfig) throws CodecException {
        LOGGER.info("Updating properties");

        // set properties to defaults

        this.timestampConfig = timestampConfig;

        LOGGER.info("Configuration updated successfully");
    } // updateProperties()

    @Override
    public void addEventTransport(String arg0, EventTransport transport) throws CodecException {
        eventTransport = transport;

    }

    /*
     * @see com.apama.iaf.plugin.AbstractEventCodec#flushUpstream()
     */
    public void flushUpstream() throws CodecException {
        // nothing to do here
    } // flushUpstream()

    /*
     * @see com.apama.iaf.plugin.AbstractEventCodec#flushDownstream()
     */
    public void flushDownstream() throws CodecException {
        // nothing to flush
    } // flushDownstream()

    /*
     * @see com.apama.iaf.plugin.AbstractEventCodec#cleanup()
     */
    public void cleanup() throws CodecException {
        // nothing to do here
    } // cleanup()

    @Override
    public int getAPIVersion() {
        return API_VERSION;
    }

    public CodecStatus getStatus() {
        return new CodecStatus("OK", decoded, encoded);
    } // getStatus()

    public int getCapabilities() {
        return 0;
    } // getCapabilities()

    @Override
    public void removeEventTransport(String arg0) throws CodecException {
        eventTransport = null;

    }

    @Override
    public void sendNormalisedEvent(NormalisedEvent event, TimestampSet timestamps) throws CodecException, TransportException {
        if (event instanceof NormalisedEvent && eventTransport != null) {
            String record = createRecord((NormalisedEvent) event);
            eventTransport.sendTransportEvent(record, new com.apama.util.TimestampSet());
            encoded++;
            LOGGER.debug("Sent record to transport: " + record);
        } // if
    }

    @Override
    public void sendTransportEvent(Object event, TimestampSet timestamps) throws CodecException, SemanticMapperException {
        if (event instanceof NormalisedEvent && semanticMapper != null) {
            semanticMapper.sendNormalisedEvent((NormalisedEvent) event, timestamps);
            decoded++;
            LOGGER.debug("Sent normalised event to semantic mapper: " + (NormalisedEvent) event);
        }

    }

    @Override
    public void setSemanticMapper(SemanticMapper mapper) throws CodecException {
        semanticMapper = mapper;

    }

    public String createRecord(NormalisedEvent event) {
        StringBuffer buf = new StringBuffer();
        NormalisedEventIterator i = ((NormalisedEvent) event).first();

        while (i.valid()) {
            buf.append(i.key());
            if (i.value() != null) {
                buf.append(" ");
                buf.append(i.value());
            }

            buf.append(" ");
            i.next();
        } // while
        buf.append(" ");

        return buf.toString();
    } // createRecord()

}
