package de.joernott.c0denet.library;

import de.joernott.c0de.core.eval.Variable;
import de.joernott.c0de.core.library.Library;
import de.joernott.c0de.core.library.NativeCall;
import de.joernott.c0de.core.library.NativeFunction;
import de.joernott.c0de.core.library.NativeResult;
import de.joernott.c0de.core.value.ArrayValue;
import de.joernott.c0de.core.value.BooleanValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.StructDefinition;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0de.core.value.TimeValue;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0denet.core.config.NetConfig;
import de.joernott.c0denet.core.http.NetError;
import de.joernott.c0denet.core.http.NetException;
import de.joernott.c0denet.core.mail.MailAnswer;
import de.joernott.c0denet.core.mail.MailOperation;
import de.joernott.c0denet.core.mail.MailRequest;
import de.joernott.c0denet.core.mail.MailServer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The library {@code c0de_net/mail} (docs/concept.md#the-library-c0de_netmail): the struct types SMTP_SERVER,
 * IMAP_SERVER, MAIL and MAILBOX, SMTP_SEND and the IMAP functions. How the calls are run is given to it: in the game
 * through the client of the player who runs the process, in the program runner of c0de directly. This class doesn't
 * use Minecraft, so the runner library compiles it too.
 */
public final class MailLibrary implements Library {
    public static final String NAME = "c0de_net/mail";

    /**
     * Runs a call: makes the request (which may throw a NetException, the error of the call), runs it and turns the
     * answer into the result.
     */
    public interface Sender {
        NativeResult send(
                NativeCall call,
                MailOperation operation,
                Supplier<MailRequest> request,
                Function<MailAnswer, NativeResult> result);
    }

    private final Supplier<NetConfig> config;
    private final Sender sender;

    public MailLibrary(Supplier<NetConfig> config, Sender sender) {
        this.config = config;
        this.sender = sender;
    }

    private static final Type TEXT = Type.Str.UNLIMITED;
    private static final Type TEXTS = new Type.Array(Type.Str.UNLIMITED);
    private static final Type NUMBERS = new Type.Array(Type.Simple.INTEGER);
    private static final Map<String, String> SENSITIVE = Map.of("security", "sensitive");

    /** A server that accepts mail for sending. */
    public static final StructDefinition SMTP_SERVER = new StructDefinition(
            NAME + "/SMTP_SERVER",
            List.of(
                    element("server", TEXT),
                    element("port", Type.Simple.INTEGER),
                    element("security", TEXT),
                    element("user", TEXT),
                    new StructDefinition.Element("password", TEXT, SENSITIVE),
                    element("from", TEXT),
                    element("timeout", Type.Simple.INTEGER),
                    element("skip_ssl_validation", Type.Simple.BOOLEAN)));

    /** A server with mailboxes; folder is the folder IMAP_SELECT selected. */
    public static final StructDefinition IMAP_SERVER = new StructDefinition(
            NAME + "/IMAP_SERVER",
            List.of(
                    element("server", TEXT),
                    element("port", Type.Simple.INTEGER),
                    element("security", TEXT),
                    element("user", TEXT),
                    new StructDefinition.Element("password", TEXT, SENSITIVE),
                    element("timeout", Type.Simple.INTEGER),
                    element("skip_ssl_validation", Type.Simple.BOOLEAN),
                    element("folder", TEXT)));

    /** A message IMAP_FETCH read. */
    public static final StructDefinition MAIL = new StructDefinition(
            NAME + "/MAIL",
            List.of(
                    element("uid", Type.Simple.INTEGER),
                    element("from", TEXT),
                    element("to", TEXTS),
                    element("cc", TEXTS),
                    element("subject", TEXT),
                    element("date", Type.Simple.TIME),
                    element("text", TEXT),
                    element("flags", TEXTS),
                    element("size", Type.Simple.INTEGER)));

    /** The status of a folder. */
    public static final StructDefinition MAILBOX = new StructDefinition(
            NAME + "/MAILBOX",
            List.of(
                    element("name", TEXT),
                    element("messages", Type.Simple.INTEGER),
                    element("recent", Type.Simple.INTEGER),
                    element("unseen", Type.Simple.INTEGER),
                    element("uid_next", Type.Simple.INTEGER),
                    element("uid_validity", Type.Simple.INTEGER)));

    private static final Predicate<Value> A_STRING = value -> value instanceof StringValue;
    private static final Predicate<Value> A_BOOLEAN = value -> value instanceof BooleanValue;
    private static final Predicate<Value> STRINGS = value ->
            value instanceof ArrayValue array && array.elements().stream().allMatch(A_STRING);
    private static final Predicate<Value> A_STRING_OR_STRINGS = A_STRING.or(STRINGS);
    private static final Predicate<Value> UIDS = value -> value instanceof IntegerValue
            || value instanceof ArrayValue array && array.elements().stream().allMatch(e -> e instanceof IntegerValue);

    private static StructDefinition.Element element(String name, Type type) {
        return new StructDefinition.Element(name, type, Map.of());
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Map<String, StructDefinition> types() {
        Map<String, StructDefinition> types = new LinkedHashMap<>();
        types.put("SMTP_SERVER", SMTP_SERVER);
        types.put("IMAP_SERVER", IMAP_SERVER);
        types.put("MAIL", MAIL);
        types.put("MAILBOX", MAILBOX);
        return types;
    }

    /** The errors of c0de_net/mail as INTEGER constants, like {@code c0de_net/mail/MAIL_REJECTED}. */
    @Override
    public Map<String, Value> constants() {
        Map<String, Value> constants = new LinkedHashMap<>();
        for (NetError error : NetError.MAIL) {
            constants.put(error.name(), new IntegerValue(error.code()));
        }
        return constants;
    }

    @Override
    public Map<String, NativeFunction> functions() {
        Map<String, NativeFunction> functions = new LinkedHashMap<>();
        add(functions, MailOperation.SEND, List.of("result"),
                server(SMTP_SERVER),
                NativeFunction.Parameter.required("to", "STRING or ARRAY STRING", A_STRING_OR_STRINGS),
                NativeFunction.Parameter.withDefault("subject", "STRING", A_STRING, new StringValue("")),
                NativeFunction.Parameter.withDefault("text", "STRING", A_STRING, new StringValue("")),
                NativeFunction.Parameter.withDefault("cc", "ARRAY STRING", STRINGS, new ArrayValue(TEXT)),
                NativeFunction.Parameter.withDefault("bcc", "ARRAY STRING", STRINGS, new ArrayValue(TEXT)),
                NativeFunction.Parameter.withDefault("reply_to", "STRING", A_STRING, new StringValue("")));
        add(functions, MailOperation.LIST, List.of("result"),
                server(IMAP_SERVER),
                NativeFunction.Parameter.withDefault("pattern", "STRING", A_STRING, new StringValue("*")));
        add(functions, MailOperation.CREATE, List.of(), server(IMAP_SERVER), folder());
        add(functions, MailOperation.DELETE, List.of(), server(IMAP_SERVER), folder());
        add(functions, MailOperation.RENAME, List.of(),
                server(IMAP_SERVER), folder(), NativeFunction.Parameter.required("new_name", "STRING", A_STRING));
        add(functions, MailOperation.STATUS, List.of("result"),
                server(IMAP_SERVER),
                NativeFunction.Parameter.withDefault("folder", "STRING", A_STRING, new StringValue("")));
        add(functions, MailOperation.SELECT, List.of("result"),
                NativeFunction.Parameter.reference("server", "a variable of the type " + NAME + "/IMAP_SERVER"),
                NativeFunction.Parameter.withDefault("folder", "STRING", A_STRING, new StringValue("INBOX")));
        add(functions, MailOperation.SEARCH, List.of("result"),
                server(IMAP_SERVER),
                NativeFunction.Parameter.withDefault("criteria", "STRING", A_STRING, new StringValue("ALL")));
        add(functions, MailOperation.FETCH, List.of("result"),
                server(IMAP_SERVER), uids(),
                NativeFunction.Parameter.withDefault("text", "BOOLEAN", A_BOOLEAN, BooleanValue.of(true)));
        add(functions, MailOperation.STORE, List.of(),
                server(IMAP_SERVER), uids(),
                NativeFunction.Parameter.required("flags", "STRING or ARRAY STRING", A_STRING_OR_STRINGS),
                NativeFunction.Parameter.withDefault("mode", "STRING", A_STRING, new StringValue("add")));
        add(functions, MailOperation.MOVE, List.of(), server(IMAP_SERVER), uids(), folder());
        add(functions, MailOperation.EXPUNGE, List.of("result"), server(IMAP_SERVER));
        return functions;
    }

    private void add(
            Map<String, NativeFunction> functions,
            MailOperation operation,
            List<String> results,
            NativeFunction.Parameter... parameters) {
        functions.put(operation.function(), new NativeFunction(
                operation.function(),
                List.of(parameters),
                results,
                call -> sender.send(
                        call, operation, () -> request(call, operation), answer -> result(call, operation, answer))));
    }

    private static NativeFunction.Parameter server(StructDefinition type) {
        return NativeFunction.Parameter.required(
                "server", type.name(), value -> value instanceof StructValue struct && struct.definition().equals(type));
    }

    private static NativeFunction.Parameter folder() {
        return NativeFunction.Parameter.required("folder", "STRING", A_STRING);
    }

    private static NativeFunction.Parameter uids() {
        return NativeFunction.Parameter.required("uids", "INTEGER or ARRAY INTEGER", UIDS);
    }

    /** The request of a call, checked against the settings of the server. */
    MailRequest request(NativeCall call, MailOperation operation) {
        NetConfig settings = config.get();
        if (!settings.get(NetConfig.ENABLE_MAIL)) {
            throw new NetException(NetError.MAIL_DISABLED, "Mail is switched off on this server (enableMail is false)");
        }
        StructValue struct = serverStruct(call, operation);
        MailServer server = MailServer.of(
                operation.imap(),
                text(struct, "server"),
                integer(struct, "port"),
                text(struct, "security"),
                text(struct, "user"),
                text(struct, "password"),
                operation.imap() ? "" : text(struct, "from"),
                operation.imap() ? text(struct, "folder") : "",
                integer(struct, "timeout"),
                ((BooleanValue) struct.get("skip_ssl_validation")).value(),
                settings.get(NetConfig.MAIL_TIMEOUT));
        if (server.port() == 25 && !settings.get(NetConfig.ALLOW_PORT_25)) {
            throw new NetException(
                    NetError.PERMISSION_DENIED,
                    "Port 25 is blocked on this server (allowPort25 is false); use the submission port of your mail"
                            + " provider, e.g. 465 or 587");
        }
        Map<String, String> texts = new LinkedHashMap<>();
        Map<String, List<String>> lists = new LinkedHashMap<>();
        List<Long> uids = List.of();
        boolean withText = false;
        switch (operation) {
            case SEND -> {
                List<String> to = strings(call.argument("to"));
                lists.put("to", to);
                lists.put("cc", strings(call.argument("cc")));
                lists.put("bcc", strings(call.argument("bcc")));
                texts.put("subject", argument(call, "subject"));
                texts.put("text", argument(call, "text"));
                texts.put("reply_to", argument(call, "reply_to"));
                int recipients = lists.values().stream().mapToInt(List::size).sum();
                if (recipients > settings.get(NetConfig.MAX_RECIPIENTS)) {
                    throw new NetException(
                            NetError.MAIL_LIMIT,
                            recipients + " recipients are more than " + settings.get(NetConfig.MAX_RECIPIENTS)
                                    + " (maxRecipients)");
                }
                int size = texts.get("text").getBytes(StandardCharsets.UTF_8).length
                        + texts.get("subject").getBytes(StandardCharsets.UTF_8).length;
                if (size > settings.get(NetConfig.MAX_MAIL_SIZE)) {
                    throw new NetException(
                            NetError.MAIL_LIMIT,
                            "The message has more than " + settings.get(NetConfig.MAX_MAIL_SIZE) + " bytes (maxMailSize)");
                }
            }
            case LIST -> texts.put("pattern", argument(call, "pattern"));
            case CREATE, DELETE -> texts.put("folder", argument(call, "folder"));
            case RENAME -> {
                texts.put("folder", argument(call, "folder"));
                texts.put("new_name", argument(call, "new_name"));
            }
            case STATUS -> {
                String folder = argument(call, "folder").isEmpty() ? server.folder() : argument(call, "folder");
                if (folder.isEmpty()) {
                    throw new NetException(
                            NetError.INVALID_ARGUMENT, "IMAP_STATUS needs a folder, or a folder selected with IMAP_SELECT");
                }
                texts.put("folder", folder);
            }
            case SELECT -> texts.put("folder", argument(call, "folder"));
            case SEARCH -> texts.put("criteria", argument(call, "criteria"));
            case FETCH -> {
                uids = uids(call.argument("uids"));
                withText = ((BooleanValue) call.argument("text")).value();
            }
            case STORE -> {
                uids = uids(call.argument("uids"));
                lists.put("flags", strings(call.argument("flags")));
                texts.put("mode", argument(call, "mode"));
            }
            case MOVE -> {
                uids = uids(call.argument("uids"));
                texts.put("folder", argument(call, "folder"));
            }
            case EXPUNGE -> {}
        }
        if (operation.inFolder() && server.folder().isEmpty()) {
            throw new NetException(
                    NetError.INVALID_ARGUMENT, operation.function() + " needs a selected folder: call IMAP_SELECT first");
        }
        return new MailRequest(
                operation,
                server,
                texts,
                lists,
                uids,
                withText,
                new MailRequest.Limits(
                        settings.get(NetConfig.MAX_MAIL_SIZE),
                        settings.get(NetConfig.MAX_RECIPIENTS),
                        settings.get(NetConfig.MAX_FETCH_SIZE),
                        settings.get(NetConfig.MAX_FETCH_MESSAGES)));
    }

    /** The struct of the connection: for IMAP_SELECT the variable passed by reference. */
    private static StructValue serverStruct(NativeCall call, MailOperation operation) {
        Value value = operation == MailOperation.SELECT
                ? call.reference("server").map(Variable::value).orElse(null)
                : call.argument("server");
        StructDefinition expected = operation.imap() ? IMAP_SERVER : SMTP_SERVER;
        if (!(value instanceof StructValue struct) || !struct.definition().equals(expected)) {
            throw new NetException(
                    NetError.INVALID_ARGUMENT, operation.function() + " needs a variable of the type " + expected.name());
        }
        return struct;
    }

    /** The results of an answer; IMAP_SELECT also stores the folder in the variable of the server. */
    static NativeResult result(NativeCall call, MailOperation operation, MailAnswer answer) {
        Map<String, Value> results = new LinkedHashMap<>();
        switch (operation) {
            case SEND -> results.put("result", new StringValue(answer.text()));
            case LIST -> results.put("result", texts(answer.names()));
            case STATUS -> results.put("result", mailbox(answer.mailbox()));
            case SELECT -> {
                results.put("result", mailbox(answer.mailbox()));
                call.reference("server").ifPresent(variable -> {
                    StructValue server = ((StructValue) variable.value()).copy();
                    server.set("folder", new StringValue(answer.mailbox().name()));
                    variable.set(server);
                });
            }
            case SEARCH -> results.put("result", numbers(answer.uids()));
            case FETCH -> {
                List<Value> messages = new ArrayList<>();
                for (MailAnswer.Message message : answer.messages()) {
                    messages.add(message(message));
                }
                results.put("result", new ArrayValue(new Type.Struct(MAIL), messages));
            }
            case EXPUNGE -> results.put("result", new IntegerValue(answer.count()));
            case CREATE, DELETE, RENAME, STORE, MOVE -> {}
        }
        return NativeResult.of(results);
    }

    /** An error of c0de net as the error of a call. */
    public static NativeResult error(NetException e) {
        return NativeResult.error(e.error().code(), e.getMessage());
    }

    private static StructValue mailbox(MailAnswer.Mailbox mailbox) {
        StructValue struct = new StructValue(MAILBOX);
        struct.set("name", new StringValue(mailbox.name()));
        struct.set("messages", new IntegerValue(mailbox.messages()));
        struct.set("recent", new IntegerValue(mailbox.recent()));
        struct.set("unseen", new IntegerValue(mailbox.unseen()));
        struct.set("uid_next", new IntegerValue(mailbox.uidNext()));
        struct.set("uid_validity", new IntegerValue(mailbox.uidValidity()));
        return struct;
    }

    private static StructValue message(MailAnswer.Message message) {
        StructValue struct = new StructValue(MAIL);
        struct.set("uid", new IntegerValue(message.uid()));
        struct.set("from", new StringValue(message.from()));
        struct.set("to", texts(message.to()));
        struct.set("cc", texts(message.cc()));
        struct.set("subject", new StringValue(message.subject()));
        struct.set("date", new TimeValue(message.date()));
        struct.set("text", new StringValue(message.text()));
        struct.set("flags", texts(message.flags()));
        struct.set("size", new IntegerValue(message.size()));
        return struct;
    }

    private static ArrayValue texts(List<String> texts) {
        return new ArrayValue(TEXT, texts.stream().map(StringValue::new).toList());
    }

    private static ArrayValue numbers(List<Long> numbers) {
        return new ArrayValue(Type.Simple.INTEGER, numbers.stream().map(IntegerValue::new).toList());
    }

    private static List<String> strings(Value value) {
        if (value instanceof StringValue text) {
            return text.value().isBlank() ? List.of() : List.of(text.value());
        }
        List<String> strings = new ArrayList<>();
        for (Value element : ((ArrayValue) value).elements()) {
            strings.add(((StringValue) element).value());
        }
        return strings;
    }

    private static List<Long> uids(Value value) {
        if (value instanceof IntegerValue number) {
            return List.of(number.value());
        }
        List<Long> uids = new ArrayList<>();
        for (Value element : ((ArrayValue) value).elements()) {
            uids.add(((IntegerValue) element).value());
        }
        return uids;
    }

    private static String argument(NativeCall call, String name) {
        return ((StringValue) call.argument(name)).value();
    }

    private static String text(StructValue struct, String element) {
        return ((StringValue) struct.get(element)).value();
    }

    private static long integer(StructValue struct, String element) {
        return ((IntegerValue) struct.get(element)).value();
    }
}
