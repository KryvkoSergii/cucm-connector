package ua.com.smiddle;

import com.cisco.cti.util.Condition;
import com.cisco.jtapi.extensions.CiscoAddress;
import com.cisco.jtapi.extensions.CiscoCall;
import com.cisco.jtapi.extensions.CiscoJtapiPeer;
import com.cisco.jtapi.extensions.CiscoProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Description;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.telephony.*;
import javax.telephony.events.CallEv;
import javax.telephony.events.ProvEv;
import javax.telephony.events.ProvInServiceEv;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Added by A.Osadchuk on 28.09.2016 at 11:36.
 * Project: SmiddleRecording
 */
@Component("ConnectorCUCM")
@Description("Connector to Cisco Unified Call Manager.")
public class ConnectorCUCM {
    private final static String BEAN = "CUCM Connector";
    @Autowired
    private ApplicationContext context;
    @Autowired
    private Environment env;
    private CiscoProvider provider;
    private final ConcurrentMap<String, CiscoAddress> pool = new ConcurrentHashMap<>();
    private final CallObserver phoneCallObserver = eventList -> {
        for (CallEv c : eventList) {
            System.out.println(c.getCall().toString());
        }
    };
    private final AddressObserver addressObserver = addrEvs -> {
    };


    //Constructors
    public ConnectorCUCM() {
    }


    //Methods
    @PostConstruct
    private void setUp() {
        try {
            System.out.println("Connecting to CUCM....");
            //Construct CiscoJtapiPeer
            CiscoJtapiPeer peer = (CiscoJtapiPeer) JtapiPeerFactory.getJtapiPeer(null);
            try {
                //Constructing connect settings string
                String connectParams = env.getProperty("connection.ipA");
                connectParams += ";login=" + env.getProperty("connection.login");
                connectParams += ";passwd=" + env.getProperty("connection.password");
                connectParams += ";appinfo=CUCM Connector";
                //Construct provider
                provider = (CiscoProvider) peer.getProvider(connectParams);
            } catch (Exception e) {
                throw new CRMCUCMException("CiscoProvider initialization fault! " + e);
            }
            try {
                //Constructing ProviderObserver with custom condition
                final Condition condition = new Condition();
                ProviderObserver providerObserver = new ProviderObserver() {
                    public void providerChangedEvent(ProvEv[] eventList) {
                        if (eventList == null) return;
                        for (ProvEv event : eventList) {
                            if (event instanceof ProvInServiceEv) condition.set();
                        }
                    }
                };
                //Adding ProviderObserver to CiscoProvider
                provider.addObserver(providerObserver);
                condition.waitTrue();
            } catch (Exception e) {
                throw new CRMCUCMException("ProviderObserver adding fault! " + e);
            }
            updateDevices();
            System.out.println("INITIALIZED!");
        } catch (Exception e) {
            System.err.println("INITIALIZATION FAULT! " + e);
        }
    }

    @PreDestroy
    private void tierDown() {
        provider.shutdown();
    }


    public CiscoCall getCall(String ccid) throws CRMCUCMException {
        try {
            String[] ids = ccid.split("/");
            if (ids.length == 1) return provider.getCall(Integer.valueOf(ids[0]));
            return provider.getCall(Integer.valueOf(ids[0]), Integer.valueOf(ids[1]));
        } catch (Exception e) {
            throw new CRMCUCMException("Call not found for CCID=" + ccid);
        }
    }


    //Tasks
    @Scheduled(initialDelay = 20 * 60 * 1000, fixedDelay = 20 * 60 * 1000)
    public void updateDevices() {
        try {
            Address[] loadedDeviceAddresses = provider.getAddresses();
            if (loadedDeviceAddresses != null) {
                System.out.println("updateDevices: devices available=" + loadedDeviceAddresses.length);
                //Adding new devices
                List<String> loadedDeviceNames = new ArrayList<>();
                for (Address device : loadedDeviceAddresses) {
                    loadedDeviceNames.add(device.getName());
                    if (pool.containsKey(device.getName())) continue;
                    device.addCallObserver(phoneCallObserver);
                    device.addObserver(addressObserver);
                    pool.put(device.getName(), (CiscoAddress) device);
                }
                //Cleaning not existed devices
                pool.keySet().stream().filter(key -> !loadedDeviceNames.contains(key)).forEach(pool::remove);
            } else
                throw new CRMCUCMException("For user=" + env.getProperty("connection.login") + " NO available terminal phonePool!");
        } catch (CRMCUCMException e) {
            System.out.println("updateDevices: thrown " + e);
        } catch (Exception e) {
            System.out.println("updateDevices: thrown " + e);
        }
    }
}
