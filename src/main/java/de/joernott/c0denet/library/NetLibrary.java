package de.joernott.c0denet.library;

import de.joernott.c0de.core.library.Library;
import de.joernott.c0de.core.library.NativeFunction;
import java.util.Map;

/**
 * The library {@code c0de_net/net} (docs/concept.md): the struct type HTTP_CONNECTION and a function per HTTP
 * method. The type and the functions follow in sprint 3.
 */
public final class NetLibrary implements Library {
    public static final String NAME = "c0de_net/net";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Map<String, NativeFunction> functions() {
        return Map.of();
    }
}
