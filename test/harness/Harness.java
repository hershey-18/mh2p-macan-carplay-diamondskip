import java.lang.reflect.*;
import java.util.*;

public class Harness {
    static List<String> calls = new ArrayList<>();
    static Object registered;
    static Class<?> c(String n) throws Exception { return Class.forName(n); }

    static Object proxy(String iface, InvocationHandler h) throws Exception {
        return Proxy.newProxyInstance(Harness.class.getClassLoader(), new Class<?>[]{ c(iface) }, h);
    }

    public static void main(String[] a) throws Exception {
        Object tmService = proxy("de.audi.atip.interapp.combi.bap.audio.CombiBAPServiceTerminalMode", (p, m, args) -> {
            calls.add("cluster." + m.getName() + Arrays.toString(args)); return null; });
        Object ref = proxy("org.osgi.framework.ServiceReference", (p, m, args) -> m.getName().equals("toString") ? "ref" : null);
        Object tracker = proxy("de.audi.app.terminalmode.osgi.IServiceTracker", (p, m, args) -> {
            calls.add("tracker." + m.getName()); return null; });
        Object sm = proxy("de.audi.app.terminalmode.osgi.IServiceManager", (p, m, args) -> {
            switch (m.getName()) {
                case "createServiceTracker":
                    calls.add("createServiceTracker(" + ((Class<?>) args[0]).getName() + ", " + args[1].getClass().getName() + ")");
                    return tracker;
                case "registerService":
                    registered = args[1];
                    calls.add("registerService(" + ((Class<?>) args[0]).getName() + ", " + args[1].getClass().getName() + ")");
                    return proxy("org.osgi.framework.ServiceRegistration", (pp, mm, aa) -> null);
                case "unregisterService": calls.add("unregisterService"); return null;
                case "getServiceReferences":
                    calls.add("getServiceReferences(" + ((Class<?>) args[0]).getName() + ")");
                    Object arr = Array.newInstance(c("org.osgi.framework.ServiceReference"), 1);
                    Array.set(arr, 0, ref); return arr;
                case "getService": return tmService;
                case "releaseService": calls.add("releaseService"); return null;
            }
            return null;
        });
        Object player = proxy("de.audi.app.terminalmode.smartphone.IPlayerModificationListener", (p, m, args) -> {
            calls.add("carplay." + m.getName() + Arrays.toString(args)); return null; });
        Object logger = proxy("de.audi.app.terminalmode.ITerminalLogger", (p, m, args) -> null);
        Object props = proxy("de.audi.app.terminalmode.smartphone.ISmartphoneProperties", (p, m, args) -> null);

        Class<?> hook = c("de.audi.app.terminalmode.hmi.TMSkipOverJokerkey");
        System.out.println("hook loaded from: " + hook.getProtectionDomain().getCodeSource().getLocation());
        Object h = hook.getConstructor(c("de.audi.app.terminalmode.ITerminalLogger"), c("de.audi.app.terminalmode.osgi.IServiceManager"),
                c("de.audi.app.terminalmode.smartphone.ISmartphoneProperties"), c("de.audi.app.terminalmode.smartphone.IPlayerModificationListener"))
                .newInstance(logger, sm, props, player);
        hook.getMethod("init").invoke(h);
        Method skip = registered.getClass().getMethod("skip", int.class);
        calls.add("--- cluster sends skip(1)"); skip.invoke(registered, 1);
        calls.add("--- cluster sends skip(-1)"); skip.invoke(registered, -1);
        calls.add("--- cluster sends skip(0)"); skip.invoke(registered, 0);
        calls.add("--- cluster sends switchSource"); registered.getClass().getMethod("switchSource", int.class, int.class, int.class).invoke(registered, 1, 2, 3);
        calls.add("--- cluster sends cancelSeek"); registered.getClass().getMethod("cancelSeek").invoke(registered);
        calls.add("--- cluster sends setActiveSourceState(2,0)"); registered.getClass().getMethod("setActiveSourceState", int.class, int.class).invoke(registered, 2, 0);
        calls.add("--- cluster sends setPreferredList(3)"); registered.getClass().getMethod("setPreferredList", int.class).invoke(registered, 3);
        calls.add("--- cluster sends setVolume (tone interface)"); registered.getClass().getMethod("setVolume", int.class, int.class).invoke(registered, 1, 1);
        calls.add("--- cluster sends startSeek(0), reply expected ~200ms later"); registered.getClass().getMethod("startSeek", int.class).invoke(registered, 0);
        calls.add("    (immediately after startSeek returned)");
        Thread.sleep(500);
        hook.getMethod("deinit").invoke(h);
        for (String s : calls) System.out.println(s);
    }
}
