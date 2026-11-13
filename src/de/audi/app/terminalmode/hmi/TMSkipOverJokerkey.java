package de.audi.app.terminalmode.hmi;

import de.audi.app.terminalmode.ITerminalLogger;
import de.audi.app.terminalmode.ITerminalModeComponent;
import de.audi.app.terminalmode.osgi.IServiceManager;
import de.audi.app.terminalmode.osgi.IServiceTracker;
import de.audi.app.terminalmode.smartphone.IPlayerModificationListener;
import de.audi.app.terminalmode.smartphone.ISmartphoneProperties;
import java.lang.reflect.Constructor;
import org.osgi.framework.ServiceRegistration;

public class TMSkipOverJokerkey implements ITerminalModeComponent {

    private static final String LOGCLASS = "TMSkipOverJokerkey";
    private static final String JOKER_SERVICE = "de.audi.app.car.api.services.ICarJokerKeyService";
    private static final String JOKER_LISTENER = "de.audi.app.terminalmode.hmi.TMSkipOverJokerkey$JokerKeyListener";
    private static final String REGISTER_AS = "de.audi.atip.interapp.combi.bap.audio.CombiBAPServiceToneListener";

    private final ITerminalLogger logger;
    private final IServiceManager serviceManager;
    private final ISmartphoneProperties smartphoneProperties;
    private final IPlayerModificationListener playerModificationListener;
    private volatile IServiceTracker jokerKeyApiTracker;
    private volatile ServiceRegistration diamondRegistration;

    public TMSkipOverJokerkey(ITerminalLogger logger, IServiceManager serviceManager,
                              ISmartphoneProperties smartphoneProperties,
                              IPlayerModificationListener playerModificationListener) {
        this.serviceManager = serviceManager;
        this.logger = logger;
        this.smartphoneProperties = smartphoneProperties;
        this.playerModificationListener = playerModificationListener;
    }

    public void init() {
        Class jokerServiceClass;
        try {
            jokerServiceClass = Class.forName(JOKER_SERVICE);
            org.osgi.util.tracker.ServiceTrackerCustomizer original = createOriginalJokerKeyListener();
            if (original != null) {
                this.jokerKeyApiTracker = this.serviceManager.createServiceTracker(jokerServiceClass, original);
                this.jokerKeyApiTracker.open();
            } else {
                DiamondCarPlaySkip.log(this.logger, "original joker key listener not available, skipped");
            }
        } catch (Throwable t) {
            this.jokerKeyApiTracker = null;
            DiamondCarPlaySkip.log(this.logger, DiamondCarPlaySkip.concat("original joker key listener not started: ", t));
        }

        registerDiamondSkip();
    }

    public void deinit() {
        try {
            if (this.jokerKeyApiTracker != null) {
                this.jokerKeyApiTracker.close();
            }
        } catch (Throwable t) {
            DiamondCarPlaySkip.log(this.logger, DiamondCarPlaySkip.concat("closing joker key tracker failed: ", t));
        }
        this.jokerKeyApiTracker = null;
        unregisterDiamondSkip();
    }

    private org.osgi.util.tracker.ServiceTrackerCustomizer createOriginalJokerKeyListener() throws Exception {
        Class listenerClass = Class.forName(JOKER_LISTENER);
        Object[] available = new Object[] {
            this, this.serviceManager, this.smartphoneProperties,
            this.playerModificationListener, this.logger
        };
        try {
            Class[] types = new Class[] {
                Class.forName("de.audi.app.terminalmode.hmi.TMSkipOverJokerkey"),
                Class.forName("de.audi.app.terminalmode.osgi.IServiceManager"),
                Class.forName("de.audi.app.terminalmode.smartphone.ISmartphoneProperties"),
                Class.forName("de.audi.app.terminalmode.smartphone.IPlayerModificationListener"),
                Class.forName("de.audi.app.terminalmode.ITerminalLogger")
            };
            return asCustomizer(listenerClass.getConstructor(types).newInstance(available));
        } catch (NoSuchMethodException e) {
            DiamondCarPlaySkip.log(this.logger, "original joker key listener has a different constructor, trying to match it");
        }
        Constructor[] constructors = listenerClass.getDeclaredConstructors();
        for (int c = 0; c < constructors.length; c++) {
            Object[] args = matchArguments(constructors[c].getParameterTypes(), available);
            if (args != null) {
                constructors[c].setAccessible(true);
                return asCustomizer(constructors[c].newInstance(args));
            }
        }
        return null;
    }

    private static Object[] matchArguments(Class[] types, Object[] available) {
        Object[] args = new Object[types.length];
        boolean[] used = new boolean[available.length];
        for (int i = 0; i < types.length; i++) {
            for (int a = 0; a < available.length; a++) {
                if (!used[a] && available[a] != null && types[i].isInstance(available[a])) {
                    args[i] = available[a];
                    used[a] = true;
                    break;
                }
            }
            if (args[i] == null) {
                return null;
            }
        }
        return args;
    }

    private static org.osgi.util.tracker.ServiceTrackerCustomizer asCustomizer(Object o) {
        if (o instanceof org.osgi.util.tracker.ServiceTrackerCustomizer) {
            return (org.osgi.util.tracker.ServiceTrackerCustomizer) o;
        }
        return null;
    }

    private void registerDiamondSkip() {
        try {
            if (this.diamondRegistration != null) {
                return;
            }
            String problem = DiamondCarPlaySkip.compatibilityProblem();
            if (problem != null) {
                DiamondCarPlaySkip.log(this.logger,
                        DiamondCarPlaySkip.concat("not registering, this PCM software does not match: ", problem));
                return;
            }
            Class listenerClass = Class.forName(REGISTER_AS);
            DiamondCarPlaySkip handler = new DiamondCarPlaySkip(this.logger, this.serviceManager,
                    this.playerModificationListener);
            this.diamondRegistration = this.serviceManager.registerService(listenerClass, handler,
                    IServiceManager.EMPTY_PARAMETERS);
            DiamondCarPlaySkip.log(this.logger, "registered CarPlay cluster skip handler");
        } catch (Throwable t) {
            this.diamondRegistration = null;
            DiamondCarPlaySkip.log(this.logger, DiamondCarPlaySkip.concat("registration failed: ", t));
        }
    }

    private void unregisterDiamondSkip() {
        try {
            if (this.diamondRegistration != null) {
                this.serviceManager.unregisterService(this.diamondRegistration);
                DiamondCarPlaySkip.log(this.logger, "unregistered CarPlay cluster skip handler");
            }
        } catch (Throwable t) {
            DiamondCarPlaySkip.log(this.logger, DiamondCarPlaySkip.concat("unregister failed: ", t));
        }
        this.diamondRegistration = null;
    }
}
