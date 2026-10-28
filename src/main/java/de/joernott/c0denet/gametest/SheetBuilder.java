package de.joernott.c0denet.gametest;

import de.joernott.c0de.blockentity.PropertiesBlockEntity;
import de.joernott.c0de.compat.ChunkTickets;
import de.joernott.c0de.content.C0deBlocks;
import de.joernott.c0de.core.line.Block;
import de.joernott.c0de.core.line.Line;
import de.joernott.c0de.core.line.Notation;
import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.sheet.LevelSheet;
import de.joernott.c0de.sheet.SheetOwners;
import de.joernott.c0de.sheet.SheetWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;

/**
 * Places programs written in the notation of the examples on sheets of the c0de dimension, for GameTests. A copy of
 * the SheetBuilder of the GameTests of c0de, which is not public. The tests of c0de net use the sheets from 600 on,
 * because the GameTests of c0de run in the same world.
 */
final class SheetBuilder {
    private final ServerLevel level;
    private final int sheet;
    private final List<BlockPos> placed = new ArrayList<>();

    /** Loads the chunks of the first 96 lines of the sheet. */
    SheetBuilder(ServerLevel level, int sheet) {
        this.level = level;
        this.sheet = sheet;
        // The mock players of earlier runs, which may have claimed the sheet, don't exist any more
        SheetOwners.get(level).release(level.getServer(), sheet);
        int chunkX = SheetGeometry.absoluteX(sheet, 0) >> 4;
        for (int chunkZ = 0; chunkZ < 6; chunkZ++) {
            for (ChunkPos chunk : List.of(new ChunkPos(chunkX, chunkZ), new ChunkPos(chunkX + 1, chunkZ))) {
                ChunkTickets.addTemporary(level, chunk);
                level.getChunk(chunk.x, chunk.z);
            }
        }
    }

    /** Places the blocks of a program and sets their properties. */
    SheetBuilder place(String program) {
        for (Line line : Notation.parse(program).lines()) {
            for (Line.Cell cell : line.cells()) {
                place(cell.block(), cell.x(), line.z());
            }
        }
        return this;
    }

    /** Places one block and sets its properties. */
    BlockPos place(Block block, int x, int z) {
        BlockPos pos = pos(x, z);
        level.setBlockAndUpdate(pos, C0deBlocks.get(SheetWriter.type(block)).defaultBlockState());
        placed.add(pos);
        SheetWriter.properties(block).ifPresent(properties -> {
            PropertiesBlockEntity entity = (PropertiesBlockEntity) level.getBlockEntity(pos);
            Optional<String> problem = entity.apply(properties, level);
            if (problem.isPresent()) {
                throw new IllegalArgumentException(block + " at " + x + ", " + z + ": " + problem.get());
            }
        });
        return pos;
    }

    BlockPos pos(int x, int z) {
        return LevelSheet.codePos(sheet, x, z);
    }

    /** Removes all blocks of the first lines of the sheet, also those that were not placed by this builder. */
    void clearLines(int lines) {
        for (int z = 0; z < lines; z++) {
            for (int x = SheetGeometry.CODE_MIN_X; x <= SheetGeometry.CODE_MAX_X; x++) {
                if (!level.getBlockState(pos(x, z)).isAir()) {
                    level.setBlockAndUpdate(pos(x, z), Blocks.AIR.defaultBlockState());
                }
            }
        }
        placed.clear();
    }

    /** Removes all placed blocks. */
    void clear() {
        placed.forEach(pos -> level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState()));
        placed.clear();
    }
}
