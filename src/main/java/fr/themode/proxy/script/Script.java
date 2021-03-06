package fr.themode.proxy.script;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;

public class Script {

    private final ScriptExecutor executor = new ScriptExecutor();
    private final String fileString;

    private boolean loaded;
    private volatile Context context;

    public Script(String fileString) {
        this.fileString = fileString;
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

    private static Context createContext(ScriptExecutor executor) {
        Context context = Context.newBuilder("js")
                .allowHostAccess(HostAccess.ALL)
                .build();
        Value bindings = context.getBindings("js");
        bindings.putMember("registerOutgoing", (ProxyExecutable) arguments -> {
            executor.registerOutgoing(arguments[0].asString(), arguments[1].asString(),
                    (connectionContext, polyglotPacket) -> arguments[2].execute(connectionContext, polyglotPacket));
            return null;
        });
        bindings.putMember("registerIncoming", (ProxyExecutable) arguments -> {
            executor.registerIncoming(arguments[0].asString(), arguments[1].asString(),
                    (connectionContext, polyglotPacket) -> arguments[2].execute(connectionContext, polyglotPacket));
            return null;
        });
        return context;
    }
}

