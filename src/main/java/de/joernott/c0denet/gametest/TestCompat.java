package de.joernott.c0denet.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.entity.BlockEntity;

/** The APIs of the GameTests that differ between the Minecraft versions, as in the GameTests of c0de. */
final class TestCompat {
    private TestCompat() {}

    /** The most ticks {@link #await} waits: less than the timeout of the HTTP tests. */
    static final int MAX_WAIT = 1800;

    /** The most milliseconds {@link #await} waits. */
    static final long MAX_MILLIS = 30_000;

    /**
     * Runs the checks of a test as soon as the condition holds, at the latest after {@link #MAX_MILLIS} or {@link
     * #MAX_WAIT} ticks. Requests take real time, and a GameTest server with little to do may run its ticks faster than
     * 20 per second, so the limit is a time, not a number of ticks.
     */
    static void await(GameTestHelper helper, java.util.function.BooleanSupplier condition, Runnable checks) {
        await(helper, condition, checks, System.currentTimeMillis() + MAX_MILLIS, MAX_WAIT);
    }

    private static void await(
            GameTestHelper helper,
            java.util.function.BooleanSupplier condition,
            Runnable checks,
            long deadline,
            int ticksLeft) {
        if (condition.getAsBoolean() || ticksLeft <= 0 || System.currentTimeMillis() > deadline) {
            checks.run();
        } else {
            helper.runAfterDelay(1, () -> await(helper, condition, checks, deadline, ticksLeft - 1));
        }
    }

    /** The block entity at a position relative to the test. */
    static BlockEntity blockEntity(GameTestHelper helper, BlockPos pos) {
        //? if >=1.21.5 {
        /*return helper.getBlockEntity(pos, BlockEntity.class);
        *///?} else {
        return helper.getBlockEntity(pos);
        //?}
    }
}
