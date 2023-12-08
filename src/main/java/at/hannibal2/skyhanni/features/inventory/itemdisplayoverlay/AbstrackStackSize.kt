package at.hannibal2.skyhanni.features.inventory.itemdisplayoverlay

import at.hannibal2.skyhanni.SkyHanniMod
import net.minecraft.item.ItemStack

abstract class AbstractStackSize {

    val configItemStackSize get() = SkyHanniMod.feature.inventory

    abstract fun getStackTip(item: ItemStack): String
}

abstract class AbstractMenuStackSize : AbstractStackSize()  {
    val configMenuStackSize get() = SkyHanniMod.feature.inventory.stackSize.menu
}
