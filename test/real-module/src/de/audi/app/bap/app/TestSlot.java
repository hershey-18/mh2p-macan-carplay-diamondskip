package de.audi.app.bap.app;

public class TestSlot extends AbstractAppConnector {
    public Object listener;
    final String name;
    public TestSlot(String n, de.audi.atip.log.LogChannel log) { super(log); name = n; }
    public void setAppServiceListener(de.audi.atip.interapp.bap.BAPServiceListener l) { listener = l; System.out.println("  slot " + name + " <- " + (l == null ? "null" : l.getClass().getName())); }
    public void addAppServiceListener(de.audi.atip.interapp.bap.BAPServiceListener l) { setAppServiceListener(l); }
    public de.audi.atip.interapp.bap.BAPServiceListener getAppServiceListener() { return (de.audi.atip.interapp.bap.BAPServiceListener) listener; }
    public de.audi.atip.utils.generics.GList getAllAppServiceListeners() { return null; }
}
