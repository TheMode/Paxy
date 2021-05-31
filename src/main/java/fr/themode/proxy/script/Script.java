package fr.themode.proxy.script;

import fr.themode.proxy.utils.FileUtils;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.io.File;

public class Script {

    private final ScriptExecutor executor = new ScriptExecutor();

    private final String name;
    private final String fileString;

    private boolean loaded;
    private volatile Context context;

    public Script(String name, String fileString) {
        this.name = name;
        this.fileString = fileString;
    }

    public Script(String name, File file) {
        this(name, FileUtils.readFile(file));
    }

    public void load() {
        if (loaded)
            return;
        this.loaded = true;

        final Source source = Source.create("js", fileString);
        assert source != null;
        this.context = createContext(executor);
        this.context.eval(source);
    }

    public void unload() {
        if (!loaded)
            return;
        this.loaded = false;
        this.context.close();
    }

    public ScriptExecutor getExecutor() {
        return executor;
    }

    public String getFileString() {
        return fileString;
    }

    public String getName() {
        return name;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public Context getContext() {
        return context;
    }

    private static Context createContext(ScriptExecutor executor) {
        HostAccess hostAccess = HostAccess.newBuilder(HostAccess.ALL).build();
        Context context = Context.newBuilder("js")
                // Allows foreign object prototypes
                .allowExperimentalOptions(true)
                // Allows native js methods to be used on foreign (java) objects.
                // For example, calling Array.prototype.filter on java lists.
                .option("js.experimental-foreign-object-prototype", "true")
                .allowHostAccess(hostAccess)
                .build();
        Value bindings = context.getBindings("js");
        bindings.putMember("proxy", executor);
        return context;
    }
}

