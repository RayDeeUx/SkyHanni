package at.hannibal2.skyhanni.config.features.inventory.stacksize;

import com.google.gson.annotations.Expose;
import io.github.moulberry.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.moulberry.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StackSizeMenuConfig {
    private final String stackSizeConfigDesc = "Showing various pieces of information as a stack size for these menu items.\nSome values may be truncated percentages or §a✔§r§7s.\n§c§lWARNING§r§c: If you have any respect for your FPS, §4§l§oplease don't enable everything at once§r§c!";

    @Expose
    @ConfigOption(
        name = "Tryhard",
        desc = stackSizeConfigDesc
    )
    @ConfigEditorDraggableList
    public List<PlayerTryhard> playerTryhard = new ArrayList<>(Arrays.asList(
        PlayerTryhard.MENU_NAVIGATION,
        PlayerTryhard.ACCESSORY_BAG_UTILS,
        PlayerTryhard.EVENT_COUNTDOWN_ABBV
    ));

    public enum PlayerTryhard {
        MENU_NAVIGATION("§bMenu Pagination + Sorting/Filtering Abbvs"), // §b(Note: AH/Abiphones have their seperate sorting/filtering abbv configs.)
        RNG_METER_ODDS("§bRNG Meter Drop Odds"), // (Abbvs)
        COMMUNITY_ESSENCE_UPGRADES("§bCommunity + Essence Shops Upgrade Tiers"), // (#)
        SELECTED_TAB("§bSelected Tab"), //§b(§a⬇§bs in Community Shop, §a➡§bs in Auction + Bazaar)
        FAME_RANK_BITS("§bFame Rank, Fame Count, Bits Available"), // (Abbvs)
        BOOSTER_COOKIE_DURATION("§bBooster Cookie Duration"), //§b[highest unit of time only: Xy ➡ Xd ➡ Xh ➡ etc...]
        ACTIVE_POTION_COUNT("§bActive Potion Effects"), // (#)
        ACCESSORY_BAG_UTILS("§bAccessory Bag Utils"),
        EVENT_COUNTDOWN_ABBV("§bEvents \"Start(ing) in\" Countdowns"), //§b[highest unit of time only: Xy ➡ Xd ➡ Xh ➡ etc...]
        SKYBLOCK_ACHIEVEMENT_POINTS("§bSkyBlock Achievements Points"), // (%)
        ;

        final String str;
        PlayerTryhard(String str) { this.str = str; }
        @Override public String toString() { return str; }
    }
}
