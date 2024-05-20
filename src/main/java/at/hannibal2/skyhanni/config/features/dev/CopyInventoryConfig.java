package at.hannibal2.skyhanni.config.features.dev;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

public class CopyInventoryConfig {

    @Expose
    @ConfigOption(name = "Copy Chest Name", desc = "Copies the chest name on key press in a GUI to clipboard.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    public int copyChestName = Keyboard.KEY_NONE;

    @Expose
    @ConfigOption(name = "Copy Chest Data", desc = "Copies everything about the currently open container.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    public int copyEntireChest = Keyboard.KEY_NONE;

    @Expose
    @ConfigOption(name = "Copy Inventory Data", desc = "Copies everything about the player's inventory.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    public int copyPlayerInventory = Keyboard.KEY_NONE;

    @Expose
    @ConfigOption(name = "Include Unnamed Items", desc = "When copying chest or player inventory data, include items with blank/empty names.")
    @ConfigEditorBoolean
    public boolean includeUnnamedItems = false;

    @Expose
    @ConfigOption(name = "Include Null Slots", desc = "When copying chest or player inventory data, include empty item slots.")
    @ConfigEditorBoolean
    public boolean includeNullSlots = false;

    @Expose
    @ConfigOption(name = "Include Armor", desc = "When copying player inventory data, include items in your armor slots.")
    @ConfigEditorBoolean
    public boolean includeArmor = false;

    @Expose
    @ConfigOption(name = "Copy Item Data", desc = "Copies item NBT data on key press in a GUI to clipboard.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    public int copyItemData = Keyboard.KEY_NONE;

    @Expose
    @ConfigOption(name = "Copy Compressed Item Data", desc = "Copies compressed item NBT data on key press in a GUI to clipboard.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    public int copyItemDataCompressed = Keyboard.KEY_NONE;

    @Expose
    @ConfigOption(name = "Copy RNG Meter", desc = "Copies internal names and maxed XP needed from RNG meter inventories as json to clipboard.")
    @ConfigEditorBoolean
    public boolean copyRngMeter = false;

    @Expose
    @ConfigOption(name = "Copy Bestiary Data", desc = "Copies the bestiary data from the inventory as json to clipboard.")
    @ConfigEditorBoolean
    public boolean copyBestiaryData = false;
}
