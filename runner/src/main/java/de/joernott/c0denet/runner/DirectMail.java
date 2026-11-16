package de.joernott.c0denet.runner;

import de.joernott.c0de.core.library.NativeCall;
import de.joernott.c0de.core.library.NativeResult;
import de.joernott.c0denet.core.http.NetException;
import de.joernott.c0denet.core.mail.MailAnswer;
import de.joernott.c0denet.core.mail.MailClient;
import de.joernott.c0denet.core.mail.MailOperation;
import de.joernott.c0denet.core.mail.MailRequest;
import de.joernott.c0denet.library.MailLibrary;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Runs the calls of c0de_net/mail in the program runner: directly with {@link MailClient}, and the cursor waits until
 * the call is done. In the game, the client of the player who runs the process does them.
 */
final class DirectMail {
    private DirectMail() {}

    static NativeResult send(
            NativeCall call,
            MailOperation operation,
            Supplier<MailRequest> request,
            Function<MailAnswer, NativeResult> result) {
        try {
            return result.apply(MailClient.run(request.get()));
        } catch (NetException e) {
            return MailLibrary.error(e);
        }
    }
}
