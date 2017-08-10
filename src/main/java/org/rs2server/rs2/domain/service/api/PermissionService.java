package org.rs2server.rs2.domain.service.api;

import org.rs2server.rs2.model.player.Player;

import javax.annotation.Nonnull;
import java.util.EnumSet;

/**
 * Represents a permission service.
 * @author Twelve
 */
public interface PermissionService {

    enum PlayerPermissions {
        PLAYER,
        MODERATOR,
        ADMINISTRATOR,
        IRON_MAN,
        ULTIMATE_IRON_MAN,
		HARDCORE_IRON_MAN,//SHIT
//		SHIT_1,
//		SHIT_2,
//		SHIT_3,
//		SHIT_4,
//		PVP,//SHIT_5
        HELPER
        ;

        public static PlayerPermissions of(int forumRights) {
            switch (forumRights) {
                case 1:
                    return MODERATOR;
                case 2:
                    return ADMINISTRATOR;
//                case 9:
//                	return PVP;
                case 11:
                    return HELPER;
                default:
                    return PLAYER;
            }
        }
    }

    PlayerPermissions getHighestPermission(@Nonnull Player player);

    void give(@Nonnull Player player, @Nonnull PlayerPermissions permission);

    void remove(@Nonnull Player player, @Nonnull PlayerPermissions permission);

    boolean is(@Nonnull Player player, @Nonnull PlayerPermissions permission);

    boolean isAny(@Nonnull Player player, @Nonnull PlayerPermissions... permissions);

    boolean isAny(@Nonnull Player player, @Nonnull EnumSet<PlayerPermissions> permissions);

    boolean isNot(@Nonnull Player player, @Nonnull PlayerPermissions permissions);

    boolean isNotAny(@Nonnull Player player, @Nonnull PlayerPermissions... permissions);

    boolean isNotAny(@Nonnull Player player, @Nonnull EnumSet<PlayerPermissions> permissions);
}
