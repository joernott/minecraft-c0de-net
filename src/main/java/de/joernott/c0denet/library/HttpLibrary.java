package de.joernott.c0denet.library;

import de.joernott.c0de.core.library.Library;
import de.joernott.c0de.core.library.NativeFunction;
import de.joernott.c0de.core.value.BooleanValue;
import de.joernott.c0de.core.value.HashValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.StructDefinition;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0denet.core.http.HttpConnection;
import de.joernott.c0denet.core.http.HttpMethod;
import de.joernott.c0denet.core.http.NetResponse;
import de.joernott.c0denet.http.PendingRequests;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * The library {@code c0de_net/http} (docs/concept.md): the struct type HTTP_CONNECTION and a function per HTTP
 * method, HTTP_GET to HTTP_PUT. The functions send their request through the client of the player who runs the
 * process ({@link PendingRequests}) and wait for the answer.
 */
public final class HttpLibrary implements Library {
    public static final String NAME = "c0de_net/http";

    private static final Type HASH_OF_STRINGS = new Type.Hash(Type.Str.UNLIMITED);

    /** The base information of the requests to one server. */
    public static final StructDefinition HTTP_CONNECTION = new StructDefinition(
            NAME + "/HTTP_CONNECTION",
            List.of(
                    element("use_ssl", Type.Simple.BOOLEAN),
                    element("server", Type.Str.UNLIMITED),
                    element("port", Type.Simple.INTEGER),
                    element("base_endpoint", Type.Str.UNLIMITED),
                    element("user", Type.Str.UNLIMITED),
                    element("password", Type.Str.UNLIMITED),
                    element("skip_ssl_validation", Type.Simple.BOOLEAN),
                    element("proxy", Type.Str.UNLIMITED),
                    element("headers", HASH_OF_STRINGS),
                    element("timeout", Type.Simple.INTEGER)));

    /** The results of every function. */
    public static final List<String> RESULTS = List.of("result", "status", "headers");

    private static final Predicate<Value> A_CONNECTION =
            value -> value instanceof StructValue struct && struct.definition().equals(HTTP_CONNECTION);
    private static final Predicate<Value> A_STRING = value -> value instanceof StringValue;

    private static StructDefinition.Element element(String name, Type type) {
        return new StructDefinition.Element(name, type, Map.of());
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Map<String, StructDefinition> types() {
        return Map.of("HTTP_CONNECTION", HTTP_CONNECTION);
    }

    @Override
    public Map<String, NativeFunction> functions() {
        Map<String, NativeFunction> functions = new LinkedHashMap<>();
        for (HttpMethod method : HttpMethod.values()) {
            String name = "HTTP_" + method.name();
            List<NativeFunction.Parameter> parameters = method.sendsData()
                    ? List.of(connection(), endpoint(), data())
                    : List.of(connection(), endpoint());
            functions.put(name, new NativeFunction(name, parameters, RESULTS, call -> PendingRequests.call(call, method)));
        }
        return functions;
    }

    private static NativeFunction.Parameter connection() {
        return NativeFunction.Parameter.required("connection", NAME + "/HTTP_CONNECTION", A_CONNECTION);
    }

    private static NativeFunction.Parameter endpoint() {
        return NativeFunction.Parameter.withDefault("endpoint", "STRING", A_STRING, new StringValue(""));
    }

    private static NativeFunction.Parameter data() {
        return NativeFunction.Parameter.withDefault("data", "STRING", A_STRING, new StringValue(""));
    }

    /** The values of a struct HTTP_CONNECTION. */
    public static HttpConnection connection(StructValue struct) {
        Map<String, String> headers = new LinkedHashMap<>();
        ((HashValue) struct.get("headers"))
                .elements()
                .forEach((name, value) -> headers.put(name, ((StringValue) value).value()));
        return new HttpConnection(
                bool(struct, "use_ssl"),
                text(struct, "server"),
                ((IntegerValue) struct.get("port")).value(),
                text(struct, "base_endpoint"),
                text(struct, "user"),
                text(struct, "password"),
                bool(struct, "skip_ssl_validation"),
                text(struct, "proxy"),
                headers,
                ((IntegerValue) struct.get("timeout")).value());
    }

    /** The results of an answer: result, status and headers. */
    public static Map<String, Value> results(NetResponse response) {
        Map<String, StringValue> headers = new LinkedHashMap<>();
        response.headers().forEach((name, value) -> headers.put(name, new StringValue(value)));
        return Map.of(
                "result", new StringValue(response.result()),
                "status", new IntegerValue(response.status()),
                "headers", new HashValue(Type.Str.UNLIMITED, headers));
    }

    private static boolean bool(StructValue struct, String element) {
        return ((BooleanValue) struct.get(element)).value();
    }

    private static String text(StructValue struct, String element) {
        return ((StringValue) struct.get(element)).value();
    }
}
