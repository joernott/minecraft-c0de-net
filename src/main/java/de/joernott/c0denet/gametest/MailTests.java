package de.joernott.c0denet.gametest;

import de.joernott.c0de.blockentity.TriggerBlockEntity;
import de.joernott.c0de.content.C0deBlocks;
import de.joernott.c0de.content.C0deItems;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.item.ValueItem;
import de.joernott.c0de.library.ModLibraries;
import de.joernott.c0de.sheet.SheetValidation;
import de.joernott.c0denet.core.http.NetError;
import de.joernott.c0denet.library.MailLibrary;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * GameTests of c0de_net/mail (tasks 8.6 to 8.8): the library is registered, and a program gets the errors of a blocked
 * port and of a mail server that can't be reached. The calls go through the stand-in for the client
 * ({@link LocalClients}), so Angus Mail runs in the game. Sending and reading mail is tested against GreenMail in
 * core and runner.
 */
public final class MailTests {
    private MailTests() {}

    private static final String MAIL = """
            PROGRAM postbox
            NOOP
            INCLUDE c0de_net/mail
            NOOP
            FUNCTION run
                RESULT blocked INTEGER
                RESULT unreachable INTEGER
                RESULT unselected INTEGER
            BEGIN
                VARIABLE problem ERROR
                VARIABLE smtp STRUCT c0de_net/mail/SMTP_SERVER
                VARIABLE imap STRUCT c0de_net/mail/IMAP_SERVER
                ASSIGN smtp.server "127.0.0.1" ;
                ASSIGN smtp.port 25 ;
                ASSIGN smtp.from "steve@example.com" ;
                CALL c0de_net/mail/SMTP_SEND problem
                    PARAMETER server smtp ;
                    PARAMETER to "alex@example.com" ;
                ASSIGN blocked problem.code ;
                ASSIGN imap.server "127.0.0.1" ;
                ASSIGN imap.port 1 ;
                ASSIGN imap.security "none" ;
                ASSIGN imap.user "steve" ;
                ASSIGN imap.timeout 5 ;
                CALL c0de_net/mail/IMAP_LIST problem
                    PARAMETER server imap ;
                ASSIGN unreachable problem.code ;
                CALL c0de_net/mail/IMAP_EXPUNGE problem
                    PARAMETER server imap ;
                ASSIGN unselected problem.code ;
            END FUNCTION
            """;

    /** The library c0de_net/mail is registered with c0de. */
    public static void libraryIsRegistered(GameTestHelper helper) {
        helper.assertTrue(
                ModLibraries.registry().find(MailLibrary.NAME).isPresent(),
                "The library " + MailLibrary.NAME + " is not registered");
        helper.succeed();
    }

    /**
     * A program gets PERMISSION_DENIED for port 25, REQUEST_FAILED for a server that can't be reached and
     * INVALID_ARGUMENT for IMAP_EXPUNGE without a selected folder.
     */
    public static void programsGetMailErrors(GameTestHelper helper) {
        ServerLevel c0de = helper.getLevel().getServer().getLevel(C0deDimension.LEVEL);
        SheetBuilder sheet = new SheetBuilder(c0de, 602).place(MAIL);
        SheetValidation.validate(c0de, 602);
        LocalClients.install();
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        LocalClients.add(player.getUUID());
        helper.setBlock(BlockPos.ZERO, C0deBlocks.TRIGGER_BLOCK.get());
        TriggerBlockEntity trigger = (TriggerBlockEntity) TestCompat.blockEntity(helper, BlockPos.ZERO);
        trigger.configure(player, "postbox", "run", 20);
        for (int i = 0; i < 3; i++) {
            trigger.input().setItem(i, new ItemStack(C0deItems.VARIABLE_ITEM.get()));
        }
        trigger.start();
        helper.assertTrue(trigger.isRunning(), "The process didn't start: " + trigger.status());
        TestCompat.await(helper, () -> !trigger.isRunning(), () -> {
            try {
                helper.assertTrue(trigger.exitCode() == 0, "Exit code " + trigger.exitCode() + " " + trigger.error());
                Map<String, String> results = new HashMap<>();
                for (int i = 0; i < trigger.output().getContainerSize(); i++) {
                    ValueItem.Contents result = ValueItem.Contents.of(trigger.output().getItem(i));
                    results.put(result.name(), result.value());
                }
                boolean right = String.valueOf(NetError.PERMISSION_DENIED.code()).equals(results.get("blocked"))
                        && String.valueOf(NetError.REQUEST_FAILED.code()).equals(results.get("unreachable"))
                        && String.valueOf(NetError.INVALID_ARGUMENT.code()).equals(results.get("unselected"));
                helper.assertTrue(right, "The results are " + results);
            } finally {
                LocalClients.remove(player.getUUID());
                sheet.clear();
                SheetValidation.validate(c0de, 602);
            }
            helper.succeed();
        });
    }
}
