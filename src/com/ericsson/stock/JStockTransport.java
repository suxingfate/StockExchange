package com.ericsson.stock;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import com.apama.iaf.plugin.AbstractEventTransport;
import com.apama.iaf.plugin.EventDecoder;
import com.apama.iaf.plugin.EventTransportProperty;
import com.apama.iaf.plugin.NormalisedEvent;
import com.apama.iaf.plugin.TimestampConfig;
import com.apama.iaf.plugin.TransportException;
import com.apama.iaf.plugin.TransportStatus;
import com.apama.util.Logger;
import com.apama.util.TimestampSet;

public class JStockTransport extends AbstractEventTransport {

    /** Logging support. */
    private final Logger LOGGER;
    private Runnable reader;
    private Thread readerThread;
    private EventDecoder eventDecoder;
    private int cycles;
    private boolean running = false;
    private volatile long received = 0;
    private volatile long sent = 0;
    private static final int DEFAULT_CYCLES = 1;
    private String prefix = "http://hq.sinajs.cn/list=";

    private String[] stockList = new String[] { "sh600000", "sh600004", "sh600005", "sh600006", "sh600007", "sh600008", "sh600009", "sh600010", "sh600011",
            "sh600012", "sh600015", "sh600016", "sh600017", "sh600018", "sh600019", "sh600020", "sh600021", "sh600022", "sh600023", "sh600026", "sh600027",
            "sh600028", "sh600029", "sh600030", "sh600031", "sh600033", "sh600035", "sh600036", "sh600037", "sh600038", "sh600039", "sh600048", "sh600050",
            "sh600051", "sh600052", "sh600053", "sh600054", "sh600055", "sh600056", "sh600057", "sh600058", "sh600059", "sh600060", "sh600061", "sh600062",
            "sh600063", "sh600064", "sh600066", "sh600067", "sh600068", "sh600069", "sh600070", "sh600071", "sh600073", "sh600076", "sh600077", "sh600078",
            "sh600079", "sh600080", "sh600081", "sh600082", "sh600083", "sh600084", "sh600085", "sh600086", "sh600088", "sh600089", "sh600090", "sh600093",
            "sh600094", "sh600095", "sh600096", "sh600097", "sh600098", "sh600099", "sh600100", "sh600101", "sh600103", "sh600104", "sh600105", "sh600106",
            "sh600107", "sh600108", "sh600109", "sh600110", "sh600111", "sh600112", "sh600113", "sh600114", "sh600115", "sh600116", "sh600117", "sh600118",
            "sh600119", "sh600120", "sh600121", "sh600122", "sh600123", "sh600125", "sh600126", "sh600127", "sh600128", "sh600129", "sh600130", "sh600131",
            "sh600132", "sh600133", "sh600135", "sh600136", "sh600137", "sh600138", "sh600139", "sh600141", "sh600143", "sh600145", "sh600146", "sh600148",
            "sh600149", "sh600150", "sh600151", "sh600152", "sh600153", "sh600155", "sh600156", "sh600157", "sh600158", "sh600159", "sh600160", "sh600161",
            "sh600162", "sh600163", "sh600165", "sh600166", "sh600167", "sh600168", "sh600169", "sh600170", "sh600171", "sh600172", "sh600173", "sh600175",
            "sh600176", "sh600177", "sh600178", "sh600179", "sh600180", "sh600182", "sh600183", "sh600184", "sh600185", "sh600186", "sh600187", "sh600188",
            "sh600189", "sh600190", "sh600191", "sh600192", "sh600193", "sh600195", "sh600196" };

    public JStockTransport(String name, EventTransportProperty[] properties, TimestampConfig timestampConfig) throws TransportException {
        super(name, properties, timestampConfig);
        LOGGER = Logger.getLogger(name);

        // set up the runnable which will read the input file

        reader = new Runnable() {

            public void run() {
                TimestampSet timestamps = new TimestampSet();
                int cyclesDone = 0;
                String line;

                HttpURLConnection connection;
                InputStream inputStream;
                Reader inputreader;
                // while (running && (cycles < 0 || cyclesDone < cycles)) {
                while (running) {
                    try {

                        int i = 0;
                        while (i < stockList.length) {
                            i++;
                            // proxy setting start
                            InetSocketAddress proxyInet = new InetSocketAddress("www-proxy.ao.ericsson.se", 8080);
                            Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyInet);
                            // proxy setting end

                            URL url = new URL(prefix + stockList[i]); // HttpURLConnection connection = (HttpURLConnection)
                                                                      // url.openConnection(proxy);
                            // connection = (HttpURLConnection) url.openConnection();
                            connection = (HttpURLConnection) url.openConnection(proxy);

                            connection.connect();
                            inputStream = connection.getInputStream();

                            inputreader = new InputStreamReader(inputStream, "gbk");
                            BufferedReader bufferedReader = new BufferedReader(inputreader);
                            String str = null;
                            StringBuffer sb = new StringBuffer();
                            while ((str = bufferedReader.readLine()) != null) {
                                sb.append(str);
                            }
                            inputreader.close();
                            connection.disconnect();

                            String s = sb.substring(21);
                            String[] arr = s.split(",");

                            String stockName = arr[0];
                            String stockOpenPrice = arr[1];
                            String stockBasePrice = arr[2];
                            String stockPrice = arr[3];
                            // System.out.println("stock info: " + arr[0] + arr[3]);

                            NormalisedEvent event = new NormalisedEvent();
                            event.addQuick("stockName", stockName);
                            event.addQuick("stockOpenPrice", stockOpenPrice);
                            event.addQuick("stockBasePrice", stockBasePrice);
                            event.addQuick("stockPrice", stockPrice);
                            eventDecoder.sendTransportEvent(event, timestamps);

                            Thread.yield();
                        }
                        Thread.sleep(10000);

                        // increment cycle count if not doing infinite loop

                        if (cycles > 0) {
                            cyclesDone++;
                            if (cyclesDone < cycles) {
                                LOGGER.info("Cycling... " + (cycles - cyclesDone) + " cycle(s) remaining");
                            } // if
                        } // if
                    } catch (Exception e) {
                    }
                }
            } // run()

        }; // reader

        updateProperties(properties, timestampConfig);

        LOGGER.info("File Transport Plugin v1.1 initialized");
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        try {
            // proxy setting start
            InetSocketAddress proxyInet = new InetSocketAddress("www-proxy.ao.ericsson.se", 8080);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyInet);
            // proxy setting end

            URL url = new URL("http://hq.sinajs.cn/list=sh600151");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
            // HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream inputStream = connection.getInputStream();

            Reader reader = new InputStreamReader(inputStream, "gbk");
            BufferedReader bufferedReader = new BufferedReader(reader);
            String str = null;
            StringBuffer sb = new StringBuffer();
            while ((str = bufferedReader.readLine()) != null) {
                sb.append(str);
            }
            reader.close();
            connection.disconnect();

            String s = sb.substring(21);
            String[] arr = s.split(",");
            String stockName = arr[0];
            String stockPrice = arr[3];
            System.out.println("stock info: " + arr[0] + arr[3]);
        } catch (Exception ex) {

        }
    }

    public void flushUpstream() throws TransportException {
        // don't do anything here

    } // flushUpstream()

    /*
     * @see com.apama.iaf.plugin.AbstractEventTransport#flushDownstream()
     */
    public void flushDownstream() throws TransportException {
        // we flush on each event so nothing to do here

    } // flushDownstream()

    @Override
    public void addEventDecoder(String arg0, EventDecoder decoder) throws TransportException {
        eventDecoder = decoder;

    }

    @Override
    public void cleanup() throws TransportException {
        // TODO Auto-generated method stub

    }

    @Override
    public int getAPIVersion() {
        return API_VERSION;
    }

    @Override
    public TransportStatus getStatus() {

        return new TransportStatus("Finished input = " + (readerThread != null ? readerThread.isAlive() : false), received, sent);
    }

    @Override
    public void removeEventDecoder(String arg0) throws TransportException {
        eventDecoder = null;

    }

    @Override
    public void sendTransportEvent(Object arg0, TimestampSet arg1) throws TransportException {
        // do nothing here
        LOGGER.info("sendTransportEvent at JStockTransport");

    }

    @Override
    public void start() throws TransportException {
        LOGGER.debug("Starting");
        // kick off a new Thread to read from the input file

        readerThread = new Thread(reader);
        running = true;
        readerThread.start();

        LOGGER.force("Started successfully");

    }

    @Override
    public void updateProperties(EventTransportProperty[] properties, TimestampConfig timestampConfig) throws TransportException {
        LOGGER.info("Updating properties");

        // set properties to defaults
        cycles = 100;

        this.timestampConfig = timestampConfig;

        LOGGER.info("Configuration updated successfully");
    }

    @Override
    public void stop() throws TransportException {
        running = false;
        LOGGER.debug("Stopping");

        try {
            readerThread.join();
        } // try

        catch (InterruptedException ie) {
        } // catch
        LOGGER.force("Stopped successfully");
    }

}
