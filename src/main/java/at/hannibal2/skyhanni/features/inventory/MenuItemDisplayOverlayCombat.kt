package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.CollectionAPI
import at.hannibal2.skyhanni.events.RenderItemTipEvent
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.ItemUtils.cleanName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimal
import at.hannibal2.skyhanni.utils.StringUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.matchRegex
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class MenuItemDisplayOverlayCombat {
    private val genericPercentPattern = ".* (§.)?(?<percent>[0-9]+)(\.[0-9]*)?(§.)?%".toPattern()


    @SubscribeEvent
    fun onRenderItemTip(event: RenderItemTipEvent) {
        event.stackTip = getStackTip(event.stack)
    }

    private fun getTheLastWordNoColor(original: String): String {
        return original.removeColor().split(" ").last()
    }

    private fun getStackTip(item: ItemStack): String {
        if (SkyHanniMod.feature.inventory.menuItemNumberCombatAsStackSize.isEmpty()) return ""
        val itemName = item.cleanName()
        val stackSizeConfig = SkyHanniMod.feature.inventory.menuItemNumberCombatAsStackSize
        val chestName = InventoryUtils.openInventoryName()
        /*
        -------------------------------IMPORTANT------------------------------------
        
        If at *any* point someone tells you to not nest your code, do the following
        (in any order, but more importantly in the order given below):
        - tell them to "cope and seethe", especially if they cite CodeAesthetic
        - kindly remind them that the last time someone attempted this, friendships
          were almost shattered in the process
        - you will lose your sanity as you try to figure out what the fuck went wrong
          in your forays with string manipulation
        - remind them of the Single-responsibility Principle:
          https://en.wikipedia.org/wiki/Single-responsibility_principle
        - make sure you have an IDE capable of debugging within your reach
        
        This concludes the PSA. Happy writing! -Erymanthus

        PS: T'was all a joke. Just don't do stupid shit like
        ` if (!(chestName == "Visitor's Logbook")) return "" `
        and you *should* be fine for the most part.
        ----------------------------------------------------------------------------
        */

        //NOTE: IT'S String.length, NOT String.length()!

        if (stackSizeConfig.contains(0)) {
            if ((chestName.contains("Bestiary")) && !(itemName.isEmpty()) && (itemName.contains("Bestiary Milestone "))) {
                return itemName.split(" ").last()
            }
        }

        if (stackSizeConfig.contains(1)) {
            if ((chestName.contains("Bestiary")) && !(itemName.isEmpty())) {
                val lore = item.getLore()
                for (line in lore) {
                    if (line.contains("Families Completed: ") || line.contains("Overall Progress: ")) {
                        return genericPercentPattern.matchMatcher(line.replace(" (MAX!)","")) { group("percent").replace("100", "§a✔") } ?: ""
                    }
                }
            }
        }

        if (stackSizeConfig.contains(2)) {
            if ((chestName.contains("Slayer")) && !(itemName.isEmpty())) {
                val lore = item.getLore()
                for (line in lore) {
                    if (line.contains(" Slayer: ")) {
                        return getTheLastWordNoColor(line)
                    }
                }
            }
            if (itemName.contains("Boss Leveling Rewards")) {
                val lore = item.getLore()
                for (line in lore) {
                    if (line.contains("Current LVL: ")) {
                        return getTheLastWordNoColor(line)
                    }
                }
            }
        }

        if ((stackSizeConfig.contains(3)) && (itemName.contains("Global Combat Wisdom"))) {
            for (line in item.getLore()) {
                if (line.contains("Total buff")) {
                    return "§3" + line.removeColor().replace("Total buff: +","").replace("☯ Combat Wisdom", "")
                }
            }
        }

        if (stackSizeConfig.contains(4) && itemName.contains("RNG Meter")) {
            for (line in item.getLore()) {
                if (line.contains("Progress: ")) {
                    return genericPercentPattern.matchMatcher(line) { group("percent").replace("100", "§a✔") } ?: ""
                }
            }
        }

        return ""
    }
}