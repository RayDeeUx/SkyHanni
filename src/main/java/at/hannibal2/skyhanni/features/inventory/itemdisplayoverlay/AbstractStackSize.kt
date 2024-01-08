package at.hannibal2.skyhanni.features.inventory.itemdisplayoverlay

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.features.inventory.InventoryConfig
import at.hannibal2.skyhanni.events.RenderItemTipEvent
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import at.hannibal2.skyhanni.utils.repopatterns.RepoPatternGroup
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

abstract class AbstractStackSize {
    val configItemStackSize: InventoryConfig get() = SkyHanniMod.feature.inventory
    @SubscribeEvent
    open fun onRenderItemTip(event: RenderItemTipEvent) {
        event.stackTip = getStackTip(event.stack)
    }
    abstract fun getStackTip(item: ItemStack): String
    val itemStackSizeGroup: RepoPatternGroup = RepoPattern.group("itemstacksize")
    val greenCheckmark: String = "§a✔"
    val bigRedCross: String = "§c§l✖"
}