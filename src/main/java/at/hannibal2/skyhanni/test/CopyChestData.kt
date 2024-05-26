package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.test.command.CopyItemCommand.grabItemData
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.OSUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object CopyChestData {
    private val config get() = SkyHanniMod.feature.dev.debug.copyInventory
    private val mc = Minecraft.getMinecraft()
    private const val HOTBAR_SIZE = 9
    private const val INVENTORY_SIZE = 27
    private const val DEBUG_SUFFIX: String = "data copied into the clipboard! §lMake sure to save it into a .txt file; these tend to get quite long."
    @SubscribeEvent
    fun onKeybind(event: GuiScreenEvent.KeyboardInputEvent.Post) {
        if (event.gui !is GuiContainer) return
        when {
            config.copyEntireChest.isKeyHeld() -> {
                if (event.gui is GuiChest) {
                    copyChestData(chest = (event.gui as GuiChest).inventorySlots.inventorySlots)
                }
            }
            config.copyPlayerInventory.isKeyHeld() -> {
                copyInventoryData(inventory = if (config.includeArmor) (mc.thePlayer.inventory.mainInventory + mc.thePlayer.inventory.armorInventory) else mc.thePlayer.inventory.mainInventory)
            }
            config.copyChestName.isKeyHeld() -> {
                if (InventoryUtils.openInventoryName().isNotEmpty()) {
                    OSUtils.copyToClipboard(InventoryUtils.openInventoryName())
                    ChatUtils.chat("Chest name copied to clipboard.")
                }
            }
        }
    }
    private fun copyInventoryData(inventory: Array<ItemStack?>) {
        val copyList = mutableListOf<String>(
            "relevant config:",
            "includeNullSlots: ${config.includeNullSlots}",
            "includeUnnamedItems: ${config.includeUnnamedItems}",
            "includeArmor: ${config.includeArmor}",
            "",
            "your inventory is below.",
            "",
            "your hotbar:",
            ""
        )
        for ((i, stack) in inventory.withIndex()) {
            if (i == HOTBAR_SIZE) copyList.addAll(
                listOf(
                    "note: hotbar ends here",
                    "the rest of your inventory:",
                    "",
                    ""
                )
            )
            if (i == HOTBAR_SIZE + INVENTORY_SIZE && config.includeArmor) copyList.addAll(listOf("note: the rest of your inventory data ends here", "your armor:", "", ""))
            if (stack == null) {
                if (config.includeNullSlots) copyList.addAll(
                    listOf(
                        "(slot $i is null)",
                        "",
                        ""
                    )
                )
                continue
            }
            if (stack.displayName.isNotBlank() || config.includeUnnamedItems) {
                copyList.add("slot $i:")
                copyList.addAll(stack.getStackInfo())
            }
        }
        OSUtils.copyToClipboard(copyList.joinToString("\n"))
        ChatUtils.chat("Inventory $DEBUG_SUFFIX")
    }
    private fun copyChestData(chest: List<Slot>) {
        val copyList = mutableListOf<String>(
            "relevant config:",
            "includeNullSlots: ${config.includeNullSlots}",
            "includeUnnamedItems: ${config.includeUnnamedItems}",
            "includeArmor: ${config.includeArmor}",
            "",
            "chest name: '${InventoryUtils.openInventoryName()}'",
            ""
        )
        for (slot in chest) {
            val stack = slot.stack
            if (stack == null) {
                if (config.includeNullSlots) copyList.addAll(listOf("(there is nothing inside slot ${slot.slotIndex}; it is null)", "", ""))
                continue
            }
            if (stack in mc.thePlayer.inventory.mainInventory) break
            if ((stack.displayName.isNotEmpty() && stack.displayName.isNotBlank()) || config.includeUnnamedItems) {
                copyList.add("slot ${slot.slotIndex}:")
                copyList.addAll(stack.getStackInfo())
            }
        }
        OSUtils.copyToClipboard(copyList.joinToString("\n"))
        ChatUtils.chat("Chest $DEBUG_SUFFIX")
    }
    private fun ItemStack.getStackInfo(): List<String> {
        val itemStack = this
        return buildList {
            addAll(
                listOf<String>(
                    "{",
                    "  stackSize: ${itemStack.stackSize}",
                    "    isStackable: ${itemStack.isStackable}",
                    "    item data:",
                    "      ["
                )
            )
            addAll(grabItemData(itemStack))
            addAll(
                listOf<String>(
                    "   ]",
                    "}",
                    ""
                )
            )
        }
    }
}
