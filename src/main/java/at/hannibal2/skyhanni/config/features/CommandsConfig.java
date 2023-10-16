package at.hannibal2.skyhanni.config.features;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.moulberry.moulconfig.annotations.Accordion;
import io.github.moulberry.moulconfig.annotations.ConfigEditorBoolean;
import io.github.moulberry.moulconfig.annotations.ConfigEditorKeybind;
import io.github.moulberry.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

public class CommandsConfig {

    @ConfigOption(name = "Tab Complete", desc = "")
    @Accordion
    @Expose
    public TabCompleteConfig tabComplete = new TabCompleteConfig();

    public static class TabCompleteConfig {

        @Expose
        @ConfigOption(name = "Warps", desc = "Tab complete the warp-point names when typing §e/warp <TAB>§7.")
        @ConfigEditorBoolean
        @FeatureToggle
        public boolean warps = true;

        @Expose
        @ConfigOption(name = "Island Players", desc = "Tab complete other players on the same island.")
        public boolean islandPlayers = true;

        @Expose
        @ConfigOption(name = "Friends", desc = "Tab complete friends from your friends list.")
        @ConfigEditorBoolean
        @FeatureToggle
        public boolean friends = true;

        @Expose
        @ConfigOption(name = "Only Best Friends", desc = "Only Tab Complete best friends.")
        @ConfigEditorBoolean
        @FeatureToggle
        public boolean onlyBestFriends = false;

        @Expose
        @ConfigOption(name = "Party", desc = "Tab complete Party Members.")
        @ConfigEditorBoolean
        @FeatureToggle
        public boolean party = true;

        @Expose
        @ConfigOption(name = "VIP Visits", desc = "Tab complete the visit to special users with cake souls on it.")
        @ConfigEditorBoolean
        @FeatureToggle
        public boolean vipVisits = true;

        @Expose
        @ConfigOption(name = "/gfs Sack", desc = "Tab complete §e/gfs §7sack items.")
        @ConfigEditorBoolean
        @FeatureToggle
        public boolean gfsSack = true;
    }

    @ConfigOption(name = "Fandom Wiki for §e/wiki", desc = "")
    @Accordion
    @Expose
    public FandomWikiCommmandConfig fandomWiki = new FandomWikiCommmandConfig();

    public static class FandomWikiCommmandConfig {

        @Expose
        @ConfigOption(name = "Use Fandom", desc = "Use Fandom wiki (§ehypixel-skyblock.fandom.com§7) instead of the Hypixel wiki (§ewiki.hypixel.net§7).")
        @ConfigEditorBoolean
        @FeatureToggle
        public boolean useFandomWiki = false;

        @Expose
        @ConfigOption(name = "Fandom Wiki Key", desc = "Search for an item on Fandom Wiki with this keybind.\n§4For optimal experiences, do §lNOT§r §4bind this to a mouse button.")
        @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
        public int fandomWikiKeybind = Keyboard.KEY_NONE;
    }

    @Expose
    @ConfigOption(name = "Party transfer", desc = "Allows §e/pt <player> §7as alias for §e/party transfer§7.\n" +
            "§7SkyBlock command §e/pt §7to check the play time still works.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean usePartyTransferAlias = true;

    @Expose
    @ConfigOption(name = "Replace Warp Is", desc = "Adds §e/warp is §7alongside §e/is§7. Idk why. Ask §cKaeso")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean replaceWarpIs = false;
}
