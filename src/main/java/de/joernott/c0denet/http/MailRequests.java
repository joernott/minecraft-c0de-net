package de.joernott.c0denet.http;

import de.joernott.c0de.core.library.NativeCall;
import de.joernott.c0de.core.library.NativeResult;
import de.joernott.c0de.library.ProcessHost;
import de.joernott.c0denet.core.config.NetConfig;
import de.joernott.c0denet.core.http.NetError;
import de.joernott.c0denet.core.http.NetException;
import de.joernott.c0denet.core.mail.MailAnswer;
import de.joernott.c0denet.core.mail.MailOperation;
import de.joernott.c0denet.core.mail.MailRequest;
import de.joernott.c0denet.library.MailLibrary;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The calls of c0de_net/mail in the game (docs/concept.md#the-library-c0de_netmail): like the HTTP requests, the
 * client of the player who runs the process does them, through {@link PendingRequests}. SMTP_SEND is limited to
 * maxMailsPerMinute messages per player.
 *
 * <p>Only used on the server thread.
 */
public final class MailRequests {
    /** The times (milliseconds) of the messages each player sent in the last minute. */
    private static final Map<UUID, Deque<Long>> SENT = new HashMap<>();

    private MailRequests() {}

    /** The sender of {@link MailLibrary} in the game. */
    public static NativeResult send(
            NativeCall call,
            MailOperation operation,
            Supplier<MailRequest> request,
            Function<MailAnswer, NativeResult> result) {
        return PendingRequests.run(call, MailLibrary.NAME, () -> {
            MailRequest mail = request.get();
            if (operation == MailOperation.SEND) {
                countMessage(call, PendingRequests.config().get(NetConfig.MAX_MAILS_PER_MINUTE));
            }
            return new PendingRequests.Job(
                    mail.toBytes(),
                    mail.server().timeout(),
                    mail.server().server(),
                    PendingRequests.config().get(NetConfig.MAX_FETCH_SIZE),
                    bytes -> result.apply(MailAnswer.fromBytes(bytes)));
        });
    }

    /**
     * Counts a message of the player who runs the process.
     *
     * @throws NetException MAIL_LIMIT if the player sent maxMailsPerMinute messages in the last minute
     */
    private static void countMessage(NativeCall call, int perMinute) {
        if (!(call.process().host() instanceof ProcessHost host)) {
            return;
        }
        UUID player = host.activator() != null ? host.activator() : host.owner();
        if (player == null) {
            return;
        }
        long now = System.currentTimeMillis();
        Deque<Long> times = SENT.computeIfAbsent(player, key -> new ArrayDeque<>());
        while (!times.isEmpty() && now - times.peekFirst() >= 60_000) {
            times.pollFirst();
        }
        if (times.size() >= perMinute) {
            throw new NetException(
                    NetError.MAIL_LIMIT,
                    "The player sent " + perMinute + " messages in the last minute (maxMailsPerMinute)");
        }
        times.addLast(now);
    }

    /** Forgets the counted messages, for GameTests. */
    public static void reset() {
        SENT.clear();
    }
}
