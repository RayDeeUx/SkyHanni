package at.hannibal2.skyhanni.features.inventory.itemdisplayoverlay

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.features.inventory.stacksize.StackSizeConfig.stackSizeDraggable
import at.hannibal2.skyhanni.events.RenderItemTipEvent
import at.hannibal2.skyhanni.features.inventory.itemdisplayoverlay.ItemDisplayOverlayFeatures.repoPatternFeature
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.cleanName
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.intellij.lang.annotations.Language
import java.util.regex.Matcher
import java.util.regex.Pattern

abstract class AbstractStackSize<configEnumType : stackSizeDraggable> {
    val configItemStackSize get() = SkyHanniMod.feature.inventory

    abstract val listOfEntries: List<ItemStackFeatureAbstract<configEnumType>>

    abstract fun preRequirement(item: ItemStack, itemName: String, internalName: String, chestName: String, lore: List<String>): Boolean

    @SubscribeEvent
    open fun onRenderItemTip(event: RenderItemTipEvent) {
        event.stackTip = getStackTip(event.stack)
    }

    fun getStackTip(item: ItemStack): String {
        val itemName = item.cleanName()
        val internalName = item.getInternalName()
        val chestName = InventoryUtils.openInventoryName()
        val lore = item.getLore()

        val internalNameAsString = internalName.asString()

        if (!preRequirement(item, itemName, internalNameAsString, chestName, lore)) return ""

        listOfEntries.forEach { it.getTip(itemName, internalNameAsString, chestName, item, lore)?.let { return it } }
        return ""
    }

    companion object {
        const val repoPatternPrefix = "itemStackSize."
    }

    abstract val repoPatternFeature: String

    class ItemStackFeature<configEnumType : stackSizeDraggable>(entry: configEnumType, stringType: StringType, @Language("RegExp") val patternString: String, action: (Matcher, ItemStack, List<String>) -> String) : ItemStackFeatureAbstract<configEnumType>(entry, stringType, action) {

        override val pattern by RepoPattern.pattern(repoPatternPrefix + repoPatternFeature + "." + entry.name().lowercase().replace('_', '.'), patternString)
    }

    abstract class ItemStackFeatureAbstract<configEnumType : stackSizeDraggable>(val entry: configEnumType, val stringType: StringType, val action: (Matcher, ItemStack, List<String>) -> String) {

        abstract val pattern: Pattern
        fun getTip(itemName: String, internalName: String, chestName: String, item: ItemStack, lore: List<String>): String? {
            if (!entry.isSelected()) return null
            val matcher = pattern.matcher(
                when (stringType) {
                    StringType.InternalName -> internalName
                    StringType.ChestName -> chestName
                    StringType.Item -> itemName
                }
            )
            if (!matcher.matches()) return null
            return action.invoke(matcher, item, lore)
        }
    }

    class ItemStackFeatureWithoutRepoPattern<configEnumType : stackSizeDraggable>(entry: configEnumType, stringType: StringType, override val pattern: Pattern, action: (Matcher, ItemStack, List<String>) -> String) : ItemStackFeatureAbstract<configEnumType>(entry, stringType, action)
}


enum class StringType {
    InternalName,
    ChestName,
    Item,
}



