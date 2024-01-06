package at.hannibal2.skyhanni.features.inventory.itemdisplayoverlay.menu

import at.hannibal2.skyhanni.config.features.inventory.stacksize.StackSizeMenuConfig
import at.hannibal2.skyhanni.events.RenderItemTipEvent
import at.hannibal2.skyhanni.features.inventory.itemdisplayoverlay.AbstractMenuStackSize
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.cleanName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import at.hannibal2.skyhanni.utils.StringUtils.matchMatcher
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class MenuItemDisplayOverlayFarming : AbstractMenuStackSize() {
    // private val genericPercentPattern = ".* (§.)?(?<percent>[0-9]+)(\\.[0-9]*)?(§.)?%".toPattern()
    private val totallingCountResourceLoreLinePattern by RepoPattern.pattern(("itemstacksize.farming.totallingcountresource.loreline"), (".*(§.)Totalling ((§.)+)(?<resourceCount>[\\w]+) (?<resourceType>[ \\w]+)(§.)\\..*"))
    private val jacobFarmingContestMedalInventoryLoreLinePattern by RepoPattern.pattern(("itemstacksize.farming.jacobfarmingcontestmedalinventory.loreline"), ("(?<colorCode>§.)(?<bold>§l)+(?<medal>[\\w]+) (§.)*(m|M)edals: (§.)*(?<count>[\\w]+)"))
    private val nextVisitorCountdownLoreLinePattern by RepoPattern.pattern(("itemstacksize.farming.nextvisitorcountdown.loreline"), ("(§.)*Next Visitor: (§.)*(?<time>[\\w]+)(m|s).*"))
    private val insertResourceFromLocationItemNamePattern by RepoPattern.pattern(("itemstacksize.farming.insertresourcefromlocation.itemname"), ("Insert (?<resource>[\\w]+) from (?<inventorySacks>[\\w]+)"))
    private val visitorLogbookNPCRarityLoreLinePattern by RepoPattern.pattern(("itemstacksize.farming.visitorlogbooknpcrarity.loreline"), ("§.§(L|l)(UNCOMMON|RARE|LEGENDARY|SPECIAL|MYTHIC)"))
    private val visitorMilestonePercentProgressLoreLinePattern by RepoPattern.pattern(("itemstacksize.farming.visitormilestonepercentprogress.loreline"), ("(§.)*Progress to Tier (?<tier>[\\w]+):.* (§.)*(?<percent>[0-9]+)(\\.[0-9]*)?(§.)?%"))

    @SubscribeEvent
    override fun onRenderItemTip(event: RenderItemTipEvent) {
        super.onRenderItemTip(event)
    }

    override fun getStackTip(item: ItemStack): String {
        if (configMenuStackSize.farming.isEmpty()) return ""
        val itemName = item.cleanName()
        val stackSizeConfig = configMenuStackSize.farming
        val chestName = InventoryUtils.openInventoryName()

        if (stackSizeConfig.contains(StackSizeMenuConfig.Farming.JACOBS_MEDALS) && ((chestName == "Jacob's Farming Contests") && itemName == ("Claim your rewards!"))) {
            var result = ""
            for (line in item.getLore()) {
                jacobFarmingContestMedalInventoryLoreLinePattern.matchMatcher(line) {
                    result = "$result${group("colorCode")}${group("count")}"
                }
            }
            return result
        }

        if (stackSizeConfig.contains(StackSizeMenuConfig.Farming.VISITORS_LOGBOOK_COUNTDOWN) && ((chestName == "Visitor's Logbook") && itemName == ("Logbook"))) {
            for (line in item.getLore()) {
                nextVisitorCountdownLoreLinePattern.matchMatcher(line) {
                    return group("time")
                }
            }
        }

        if (stackSizeConfig.contains(StackSizeMenuConfig.Farming.VISITOR_MILESTONES) && (chestName == "Visitor Milestones")) {
            val lore = item.getLore()
            if (lore.isNotEmpty()) {
                for (line in lore) {
                    visitorMilestonePercentProgressLoreLinePattern.matchMatcher(line) {
                        return group("percent").convertPercentToGreenCheckmark()
                    }
                }
            }
        }

        if (stackSizeConfig.contains(StackSizeMenuConfig.Farming.VISITOR_NPC_RARITIES) && (chestName == "Visitor's Logbook")) {
            val lore = item.getLore()
            for (line in lore) {
                visitorLogbookNPCRarityLoreLinePattern.matchMatcher(line) {
                    return line.take(5)
                }
            }
        }

        if (stackSizeConfig.contains(StackSizeMenuConfig.Farming.COMPOSTER_INSERT_ABBV)) {
            // (chestName.contains("Composter"))
            // (itemName.contains("Insert ") && itemName.contains(" from "))
            if (chestName == "Composter") {
                insertResourceFromLocationItemNamePattern.matchMatcher(itemName) {
                    val lore = item.getLore()
                    // §7Totalling §2§240k Fuel§7.
                    // Totalling 40k Fuel.
                    for (line in lore) {
                        totallingCountResourceLoreLinePattern.matchMatcher(line) {
                            return group("resourceCount")
                        }
                    }
                }
            }
        }

        return ""
    }
}
