package de.audi.app.terminalmode.hmi;

import de.audi.app.terminalmode.ITerminalLogger;
import de.audi.app.terminalmode.osgi.IServiceManager;
import de.audi.app.terminalmode.smartphone.IPlayerModificationListener;
import de.audi.atip.interapp.combi.bap.audio.CombiBAPServiceAudio;
import de.audi.atip.interapp.combi.bap.audio.CombiBAPServiceTerminalModeListener;
import de.audi.atip.interapp.combi.bap.audio.CombiBAPServiceToneListener;
import de.audi.atip.interapp.combi.bap.audio.CombiBAPSourceChangeService;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.osgi.framework.ServiceReference;

public class DiamondCarPlaySkip implements CombiBAPServiceTerminalModeListener, CombiBAPServiceToneListener {

    private static final String PREFIX = "[DiamondCarPlaySkip] ";
    private static final String TM_SERVICE = "de.audi.atip.interapp.combi.bap.audio.CombiBAPServiceTerminalMode";
    private static final int LOG_INFO = 1000000;
    private static final int MAX_STEPS = 5;
    private static final long SEEK_REPLY_DELAY_MS = 200;
    private static final int SWITCH_SOURCE_FAILED = 1;

    private final ITerminalLogger logger;
    private final IServiceManager serviceManager;
    private final IPlayerModificationListener player;

    public DiamondCarPlaySkip(ITerminalLogger logger, IServiceManager serviceManager,
                              IPlayerModificationListener player) {
        this.logger = logger;
        this.serviceManager = serviceManager;
        this.player = player;
    }

    public void skip(int count) {
        boolean ok = false;
        try {
            boolean forward = count >= 0;
            int steps = count < 0 ? -count : count;
            if (steps == 0) {
                steps = 1;
            }
            if (steps > MAX_STEPS) {
                steps = MAX_STEPS;
            }
            log(this.logger, concat("cluster skip received, count=", count));
            for (int i = 0; i < steps; i++) {
                this.player.skipButton(forward, false);
                this.player.skipButton(forward, true);
            }
            log(this.logger, forward ? "sent CarPlay skip forward" : "sent CarPlay skip backward");
            ok = true;
        } catch (Throwable t) {
            log(this.logger, concat("skip failed: ", t));
        }
        reportSkipResult(ok);
    }

    private void reportSkipResult(final boolean ok) {
        withClusterService(new ClusterCall() {
            public void call(Object service) {
                ((CombiBAPServiceAudio) service).skipResult(ok);
                log(logger, ok ? "reported skip result: success" : "reported skip result: failed");
            }
        }, "skipResult");
    }

    interface ClusterCall {
        void call(Object service);
    }

    private void withClusterService(ClusterCall action, String what) {
        try {
            Class serviceClass = Class.forName(TM_SERVICE);
            ServiceReference[] refs = this.serviceManager.getServiceReferences(serviceClass);
            if (refs == null || refs.length == 0) {
                log(this.logger, concat("cluster service not found for ", what));
                return;
            }
            try {
                Object service = this.serviceManager.getService(refs[0]);
                if (service instanceof CombiBAPServiceAudio) {
                    action.call(service);
                } else {
                    log(this.logger, concat("cluster service has unexpected type for ", what));
                }
            } finally {
                this.serviceManager.releaseService(refs[0]);
            }
        } catch (Throwable t) {
            log(this.logger, concat(concat(what, " failed: "), t));
        }
    }

    public void startSeek(int direction) {
        log(this.logger, concat("startSeek not supported for CarPlay, reporting seek stopped: ", direction));
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(SEEK_REPLY_DELAY_MS);
                } catch (InterruptedException e) {
                }
                reportSeekStopped();
            }
        }, "DiamondCarPlaySkip-seek");
        t.setDaemon(true);
        t.start();
    }

    public void cancelSeek() {
        log(this.logger, "cancelSeek, reporting seek stopped");
        reportSeekStopped();
    }

    private void reportSeekStopped() {
        withClusterService(new ClusterCall() {
            public void call(Object service) {
                ((CombiBAPServiceAudio) service).updateSeekStatus(false);
            }
        }, "updateSeekStatus");
    }

    public void setActiveSourceState(final int a, final int b) {
        log(this.logger, concat("setActiveSourceState echoed back: ", a));
        withClusterService(new ClusterCall() {
            public void call(Object service) {
                ((CombiBAPServiceAudio) service).updateActiveSourceState(a, b);
            }
        }, "updateActiveSourceState");
    }

    public void selectListEntry(int entry) {
        log(this.logger, concat("ignored selectListEntry ", entry));
    }

    public void selectListEntryPresetList(int entry) {
        log(this.logger, concat("ignored selectListEntryPresetList ", entry));
    }

    public void selectListEntryFavoriteList(int entry) {
        log(this.logger, concat("ignored selectListEntryFavoriteList ", entry));
    }

    public void selectListEntrySiriusAlertList(int entry) {
        log(this.logger, concat("ignored selectListEntrySiriusAlertList ", entry));
    }

    public void selectListEntryLastStationList(int entry) {
        log(this.logger, concat("ignored selectListEntryLastStationList ", entry));
    }

    public void setPreferredList(final int list) {
        log(this.logger, concat("setPreferredList echoed back: ", list));
        withClusterService(new ClusterCall() {
            public void call(Object service) {
                ((CombiBAPServiceAudio) service).updatePreferredList(list);
            }
        }, "updatePreferredList");
    }

    public void getNextListPos(int a, int b) {
        log(this.logger, concat("ignored getNextListPos ", a));
    }

    public void switchSource(int sourceType, int slot, int partition) {
        log(this.logger, concat("switchSource not supported for CarPlay, reporting failure: ", sourceType));
        withClusterService(new ClusterCall() {
            public void call(Object service) {
                if (service instanceof CombiBAPSourceChangeService) {
                    ((CombiBAPSourceChangeService) service).switchSourceResult(SWITCH_SOURCE_FAILED);
                }
            }
        }, "switchSourceResult");
    }

    public void activateSource(int source) {
        log(this.logger, concat("ignored activateSource ", source));
    }

    public void requestCoverArt(long id, int size) {
        log(this.logger, "ignored requestCoverArt");
    }

    public void setMuteState(boolean muted) {
        log(this.logger, "ignored setMuteState");
    }

    public void setVolume(int a, int b) {
        log(this.logger, "ignored setVolume");
    }

    private static final String SELF = "de.audi.app.terminalmode.hmi.DiamondCarPlaySkip";
    private static final String[] LISTENER_INTERFACES = new String[] {
        "de.audi.atip.interapp.combi.bap.audio.CombiBAPServiceTerminalModeListener",
        "de.audi.atip.interapp.combi.bap.audio.CombiBAPServiceToneListener"
    };

    static String compatibilityProblem() {
        try {
            Class self = Class.forName(SELF);
            for (int i = 0; i < LISTENER_INTERFACES.length; i++) {
                Class iface = Class.forName(LISTENER_INTERFACES[i]);
                if (!iface.isAssignableFrom(self)) {
                    return concat("handler does not implement ", LISTENER_INTERFACES[i]);
                }
                Method[] methods = iface.getMethods();
                for (int m = 0; m < methods.length; m++) {
                    Method impl;
                    try {
                        impl = self.getMethod(methods[m].getName(), methods[m].getParameterTypes());
                    } catch (NoSuchMethodException e) {
                        return concat("missing method ", methods[m].getName());
                    }
                    if (Modifier.isAbstract(impl.getModifiers()) || impl.getDeclaringClass() != self
                            || impl.getReturnType() != methods[m].getReturnType()) {
                        return concat("listener method not handled: ", methods[m].getName());
                    }
                }
            }
            Class player = Class.forName("de.audi.app.terminalmode.smartphone.IPlayerModificationListener");
            player.getMethod("skipButton", new Class[] { Boolean.TYPE, Boolean.TYPE });
            Class audio = Class.forName("de.audi.atip.interapp.combi.bap.audio.CombiBAPServiceAudio");
            audio.getMethod("skipResult", new Class[] { Boolean.TYPE });
            audio.getMethod("updateSeekStatus", new Class[] { Boolean.TYPE });
            audio.getMethod("updateActiveSourceState", new Class[] { Integer.TYPE, Integer.TYPE });
            audio.getMethod("updatePreferredList", new Class[] { Integer.TYPE });
            Class tm = Class.forName(TM_SERVICE);
            if (!audio.isAssignableFrom(tm)) {
                return "CarPlay cluster service has a different type";
            }
            return null;
        } catch (Throwable t) {
            return String.valueOf(t);
        }
    }

    static String concat(String text, int value) {
        StringBuffer sb = new StringBuffer(text);
        sb.append(value);
        return sb.toString();
    }

    static String concat(String text, Object value) {
        StringBuffer sb = new StringBuffer(text);
        sb.append(String.valueOf(value));
        return sb.toString();
    }

    static void log(ITerminalLogger logger, String message) {
        try {
            StringBuffer sb = new StringBuffer(PREFIX);
            sb.append(message);
            logger.hmi().log(LOG_INFO, sb.toString());
        } catch (Throwable t) {
        }
    }
}
