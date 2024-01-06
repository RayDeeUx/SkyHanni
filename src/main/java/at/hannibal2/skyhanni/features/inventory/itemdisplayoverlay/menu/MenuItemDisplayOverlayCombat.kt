package at.hannibal2.skyhanni.features.inventory.itemdisplayoverlay.menu

import at.hannibal2.skyhanni.config.features.inventory.stacksize.StackSizeMenuConfig
import at.hannibal2.skyhanni.events.RenderItemTipEvent
import at.hannibal2.skyhanni.features.inventory.itemdisplayoverlay.AbstractMenuStackSize
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.cleanName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.StringUtils.matchMatcher
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class MenuItemDisplayOverlayCombat : AbstractMenuStackSize() {
    private val combatSubgroup = itemStackSizeGroup.group("combat")
    
    // private val genericPercentPattern = ".* (§.)?(?<percent>[0-9]+)(\\.[0-9]*)?(§.)?%".toPattern()
    private val bestiaryChestNamePattern by combatSubgroup.pattern(("bestiary.chestname"), ("Bestiary.*"))
    private val bestiaryMilestoneItemNamePattern by combatSubgroup.pattern(("bestiarymilestone.itemname"), ("Bestiary Milestone (?<milestone>[\\w]+)"))
    private val familiesCompletedOverallProgressPercentLoreLinePattern by combatSubgroup.pattern(("familiescompletedoverallprogresspercent.loreline"), (".*(Families Completed|Overall Progress):.* (§.)?(?<percent>[0-9]+)(\\.[0-9]*)?(§.)?%.*"))
    private val slayerLevelLoreLinePattern by combatSubgroup.pattern(("slayerlevel.loreline"), ("(§.)*(?<mobType>[\\w]+) Slayer: (§.)*LVL (?<level>[\\w]+)"))
    private val slayerLevelOtherLoreLinePattern by combatSubgroup.pattern(("slayerlevelother.loreline"), ("(§.)*Current LVL: (§.)*(?<level>[\\w]+)"))
    private val combatWisdomBuffLoreLinePattern by combatSubgroup.pattern(("combatwisdombuff.loreline"), ("(§.)*Total buff: (§.)*\\+(?<combatWise>[\\w]+). Combat Wisdom"))
    private val rngMeterProgressPercentLoreLinePattern by combatSubgroup.pattern(("rngmeterprogresspercent.loreline"), (".*(§.)+Progress:.* (§.)?(?<percent>[0-9]+)(\\.[0-9]*)?(§.)?%.*"))
    private val unlockedSlayerRecipesLoreLinePattern by combatSubgroup.pattern(("unlockedslayerrecipes.loreline"), (".*(§.)*Unlocked: (§.)*(?<recipes>[\\w]+) recipes.*"))
    private val overallProgressToggleLoreLinePattern by combatSubgroup.pattern(("overallprogresstoggle.loreline"), ("(§.)*Overall Progress: (§.)*(?<status>[\\w]+)"))

    @SubscribeEvent
    override fun onRenderItemTip(event: RenderItemTipEvent) {
        super.onRenderItemTip(event)
    }

    override fun getStackTip(item: ItemStack): String {
        if (configMenuStackSize.combat.isEmpty()) return ""
        val itemName = item.cleanName()
        val stackSizeConfig = configMenuStackSize.combat
        val chestName = InventoryUtils.openInventoryName()

        if (stackSizeConfig.contains(StackSizeMenuConfig.Combat.BESTIARY_LEVEL)) {
            bestiaryChestNamePattern.matchMatcher(chestName) {
                bestiaryMilestoneItemNamePattern.matchMatcher(itemName) {
                        return group("milestone")
                }
            }
        }

        if (stackSizeConfig.contains(StackSizeMenuConfig.Combat.BESTIARY_OVERALL_FAMILY_PROGRESS)) {
            bestiaryChestNamePattern.matchMatcher(chestName) {
                val lore = item.getLore()
                if (itemName == "Toggle Families Completed Display") {
                    for (line in lore) {
                        overallProgressToggleLoreLinePattern.matchMatcher(line) {
                            if (group("status") != "SHOWN") return "§c‼‼‼"
                        }
                    }
                }
                if (itemName.isNotEmpty()) {
                    for (line in lore) {
                        familiesCompletedOverallProgressPercentLoreLinePattern.matchMatcher(line) {
                            return group("percent").convertPercentToGreenCheckmark()
                        }
                    }
                }
            }
        }

        if (stackSizeConfig.contains(StackSizeMenuConfig.Combat.SLAYER_LEVELS)) {
            val lore = item.getLore()
            if (chestName == ("Slayer")) {
                if (itemName.isNotEmpty() && lore.isNotEmpty()) {
                    for (line in lore) {
                        slayerLevelLoreLinePattern.matchMatcher(line) {
                            return group("level")
                        }
                    }
                }
            }
            if (itemName == ("Boss Leveling Rewards")) {
                for (line in lore) {
                    slayerLevelOtherLoreLinePattern.matchMatcher(line) {
                        return group("level")
                    }
                }
            }
        }

        if ((stackSizeConfig.contains(StackSizeMenuConfig.Combat.SLAYER_COMBAT_WISDOM_BUFF)) && (itemName == ("Global Combat Wisdom Buff"))) {
            for (line in item.getLore()) {
                combatWisdomBuffLoreLinePattern.matchMatcher(line) {
                    return group("combatWise")
                }
            }
        }

        if (stackSizeConfig.contains(StackSizeMenuConfig.Combat.RNG_METER_PROGRESS) && itemName == ("RNG Meter")) {
            for (line in item.getLore()) {
                rngMeterProgressPercentLoreLinePattern.matchMatcher(line) {
                    return group("percent").convertPercentToGreenCheckmark()
                }
            }
        }

        if (stackSizeConfig.contains(StackSizeMenuConfig.Combat.UNLOCKED_SLAYER_RECIPES) && itemName == ("Slayer Recipes")) {
            for (line in item.getLore()) {
                unlockedSlayerRecipesLoreLinePattern.matchMatcher(line) {
                    return group("recipes")
                }
            }
        }

        return ""
    }
}
