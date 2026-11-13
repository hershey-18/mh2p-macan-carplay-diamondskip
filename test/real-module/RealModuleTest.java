import java.lang.reflect.*;
import java.util.*;

public class RealModuleTest {
    static Class<?> c(String n) throws Exception { return Class.forName(n); }
    static Object proxy(String iface, InvocationHandler h) throws Exception {
        return Proxy.newProxyInstance(RealModuleTest.class.getClassLoader(), new Class<?>[]{ c(iface) }, h);
    }
    static Object alloc(String cls) throws Exception {
        Field f = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe"); f.setAccessible(true);
        Object u = f.get(null);
        return u.getClass().getMethod("allocateInstance", Class.class).invoke(u, c(cls));
    }
    static void set(Object o, String owner, String field, Object v) throws Exception {
        Field f = c(owner).getDeclaredField(field); f.setAccessible(true); f.set(o, v);
    }
    public static class Log extends de.audi.atip.log.LogChannel {
        public void log(int a, String s, Object o1, Object o2, Object o3, Object o4, Object o5, long l1, long l2, long l3, int i, Throwable t) { System.out.println("  porsche log: " + s); }
        public void log(int a, int b, Object o1, Object o2, Object o3, Object o4, Object o5, long l1, long l2, long l3, int i, Throwable t) {}
        public void log(int a, String s) { System.out.println("  porsche log: " + s); }
    }

    public static void main(String[] args) throws Exception {
        Map<String, Object> slots = new HashMap<>();
        de.audi.app.bap.app.TestSlot tm = new de.audi.app.bap.app.TestSlot("TerminalMode", new Log()), tone = new de.audi.app.bap.app.TestSlot("Tone", new Log());
        Object realTone = proxy("de.audi.atip.interapp.combi.bap.audio.CombiBAPServiceToneListener", (p, m, a) -> m.getName().equals("toString") ? "PorscheRealTone" : null);
        tone.listener = realTone;
        slots.put(((Enum<?>) null == null ? (String) c("de.audi.atip.start.AppId").getMethod("name").invoke(c("de.audi.atip.start.AppId").getField("TerminalMode").get(null)) : ""), tm);
        slots.put((String) c("de.audi.atip.start.AppId").getMethod("name").invoke(c("de.audi.atip.start.AppId").getField("Tone").get(null)), tone);
        Object gmap = proxy("de.audi.atip.utils.generics.GMap", (p, m, a) -> m.getName().equals("get") ? slots.get(a[0]) : null);

        Object module = alloc("de.audi.app.combi.bap.app.audio.CombiModuleAudio");
        set(module, "de.audi.app.bap.fw.AbstractBAPModule", "appConnectors", gmap);

        Object player = proxy("de.audi.app.terminalmode.smartphone.IPlayerModificationListener", (p, m, a) -> { System.out.println("  carplay." + m.getName() + Arrays.toString(a)); return null; });
        Object logger = proxy("de.audi.app.terminalmode.ITerminalLogger", (p, m, a) -> null);
        final Object[] registered = new Object[2];
        Object sm = proxy("de.audi.app.terminalmode.osgi.IServiceManager", (p, m, a) -> {
            if (m.getName().equals("registerService")) { registered[0] = ((Class<?>) a[0]).getName(); registered[1] = a[1]; return null; }
            return null; });
        Object handler = c("de.audi.app.terminalmode.hmi.DiamondCarPlaySkip").getConstructors()[0].newInstance(logger, sm, player);

        Object ctx = proxy("org.osgi.framework.BundleContext", (p, m, a) -> m.getName().equals("getService") ? handler : m.getName().equals("ungetService") ? Boolean.TRUE : null);
        Object ref = proxy("org.osgi.framework.ServiceReference", (p, m, a) -> null);
        Object mgr = alloc("de.audi.app.combi.bap.app.audio.ServiceManagerAudio");
        set(mgr, "de.audi.app.bap.fw.AbstractBAPModuleServiceManager", "module", module);
        set(mgr, "de.audi.app.bap.fw.AbstractBAPModuleServiceManager", "bundleContext", ctx);
        for (Class<?> k = mgr.getClass(); k != null; k = k.getSuperclass())
            for (Field f : k.getDeclaredFields()) if (f.getType().getName().equals("de.audi.atip.log.LogChannel")) { f.setAccessible(true); f.set(mgr, new Log()); }

        System.out.println("Porsche's ServiceManagerAudio.addingService(handler):");
        mgr.getClass().getMethod("addingService", c("org.osgi.framework.ServiceReference")).invoke(mgr, ref);
        System.out.println("CarPlay slot now: " + (tm.listener == handler ? "OUR HANDLER" : String.valueOf(tm.listener)));
        System.out.println("Tone slot now: " + tone.listener);

        System.out.println("Porsche's CombiModuleAudio.getTerminalModeServiceListener() via real connector code:");
        set(module, "de.audi.app.combi.bap.app.audio.CombiModuleAudio", "appConnectorTerminalMode", null);
        System.out.println("Porsche's ServiceManagerAudio.removedService(handler):");
        mgr.getClass().getMethod("removedService", c("org.osgi.framework.ServiceReference"), Object.class).invoke(mgr, ref, handler);
        System.out.println("CarPlay slot after removal: " + tm.listener + " | Tone slot: " + tone.listener);
    }
}
