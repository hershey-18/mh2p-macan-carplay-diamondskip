package de.audi.app.terminalmode.hmi;

import de.audi.app.terminalmode.ITerminalLogger;
import de.audi.app.terminalmode.ITerminalModeComponent;
import de.audi.app.terminalmode.osgi.IServiceManager;
import de.audi.app.terminalmode.osgi.IServiceTracker;
import de.audi.app.terminalmode.smartphone.IPlayerModificationListener;
import de.audi.app.terminalmode.smartphone.ISmartphoneProperties;
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
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(JOKER_SERVICE);
        }
        this.jokerKeyApiTracker = this.serviceManager.createServiceTracker(jokerServiceClass,
                createOriginalJokerKeyListener());
        this.jokerKeyApiTracker.open();

        registerDiamondSkip();
    }

    public void deinit() {
        if (this.jokerKeyApiTracker != null) {
            this.jokerKeyApiTracker.close();
            this.jokerKeyApiTracker = null;
        }
        unregisterDiamondSkip();
    }

    private org.osgi.util.tracker.ServiceTrackerCustomizer createOriginalJokerKeyListener() {
        try {
            Class listenerClass = Class.forName(JOKER_LISTENER);
            Class[] types = new Class[] {
                Class.forName("de.audi.app.terminalmode.hmi.TMSkipOverJokerkey"),
                Class.forName("de.audi.app.terminalmode.osgi.IServiceManager"),
                Class.forName("de.audi.app.terminalmode.smartphone.ISmartphoneProperties"),
                Class.forName("de.audi.app.terminalmode.smartphone.IPlayerModificationListener"),
                Class.forName("de.audi.app.terminalmode.ITerminalLogger")
            };
            Object[] args = new Object[] {
                this, this.serviceManager, this.smartphoneProperties,
                this.playerModificationListener, this.logger
            };
            return (org.osgi.util.tracker.ServiceTrackerCustomizer)
                    listenerClass.getConstructor(types).newInstance(args);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new NoClassDefFoundError(JOKER_LISTENER);
        }
    }

    private void registerDiamondSkip() {
        try {
            if (this.diamondRegistration != null) {
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
