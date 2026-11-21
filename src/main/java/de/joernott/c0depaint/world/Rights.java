package de.joernott.c0depaint.world;

import de.joernott.c0de.C0de;
import de.joernott.c0de.compat.Compat;
import de.joernott.c0de.core.config.C0deConfig;
import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.library.ProcessHost;
import de.joernott.c0de.sheet.ProgramRegistry;
import de.joernott.c0de.sheet.SheetOwners;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.server.level.ServerLevel;

/**
 * The rights of a process, as for PLACE_BLOCK and MINE_BLOCK of c0de/minecraft (whose helpers are not public): the
 * owner of the trigger block is an administrator if their permission level is 2 or higher and the program is theirs or
 * another administrator's.
 */
final class Rights {
    private Rights() {}

    static boolean isAdministrator(ProcessHost host) {
        if (!Compat.isAdministrator(host.server(), host.owner())) {
            return false;
        }
        ServerLevel c0de = host.server().getLevel(C0deDimension.LEVEL);
        if (c0de == null) {
            return true;
        }
        OptionalInt sheet = ProgramRegistry.get(c0de).sheetOf(host.program());
        Optional<SheetOwners.Owner> author =
                sheet.isPresent() ? SheetOwners.get(c0de).owner(sheet.getAsInt()) : Optional.empty();
        return author.map(a -> a.id().equals(host.owner()) || Compat.isAdministrator(host.server(), a.id()))
                .orElse(true);
    }

    /**
     * Raises PERMISSION_DENIED unless blocks may be changed (allowBlockChanges) or the owner is an administrator. If a
     * player who is not an administrator started the process, the process crashes instead, as in c0de.
     */
    static void requireBlockChanges(ProcessHost host) {
        if (C0de.config().get(C0deConfig.ALLOW_BLOCK_CHANGES)) {
            return;
        }
        if (host.activator() != null && !Compat.isAdministrator(host.server(), host.activator())) {
            throw C0deException.crash(
                    ErrorCode.PERMISSION_DENIED,
                    "A player who is not an administrator started the process (allowBlockChanges)");
        }
        if (!isAdministrator(host)) {
            throw C0deException.of(
                    ErrorCode.PERMISSION_DENIED,
                    "RENDER_CANVAS without an input inventory needs allowBlockChanges or an administrator");
        }
    }
}
