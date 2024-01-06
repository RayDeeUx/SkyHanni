package at.hannibal2.skyhanni.features.inventory.itemdisplayoverlay.menu

import at.hannibal2.skyhanni.api.CollectionAPI
import at.hannibal2.skyhanni.config.features.inventory.stacksize.StackSizeMenuConfig
import at.hannibal2.skyhanni.events.RenderItemTipEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.inventory.itemdisplayoverlay.AbstractMenuStackSize
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.cleanName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil
import at.hannibal2.skyhanni.utils.NumberUtil.formatNumber
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimalIfNecessary
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import at.hannibal2.skyhanni.utils.StringUtils.matchMatcher
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class MenuItemDisplayOverlayPlayer : AbstractMenuStackSize() {
    private val playerGeneralSubgroup = itemStackSizeGroup.group("player.general")
    
    // private val genericPercentPattern = ((".* (§.)?(?<percent>[0-9]+)(\\.[0-9]*)?(§.)?%").toPattern())
    private val museumDonationLoreLinePattern by playerGeneralSubgroup.pattern(("museumdonation.loreline"), ("§7Items Donated: §.(?<amount>[0-9.]+).*"))
    private val skyblockLevelLoreLinePattern by playerGeneralSubgroup.pattern(("skyblocklevel.loreline"), ("§7Your SkyBlock Level: §.?\\[§.?(?<sblvl>[0-9]{0,3})§.?].*"))
    private val skillAvgLoreLinePattern by playerGeneralSubgroup.pattern(("skillavg.loreline"), ("§[0-9](?<avg>[0-9]{1,2}(\\.[0-9])?) Skill Avg\\..*"))
    private val dungeonClassLevelItemNamePattern by playerGeneralSubgroup.pattern(("dungeonclasslevel.itemname"), ("(?<class>[A-z ]+)( )(?<level>[0-9]+)"))
    private val dungeonEssenceRewardItemNamePattern by playerGeneralSubgroup.pattern(("dungeonessencereward.itemname"), ("(§.)?(?<type>[A-z]+) (Essence) (§.)?x(?<amount>[0-9]+)"))
    private val essenceCountLoreLinePattern by playerGeneralSubgroup.pattern(("essencecount.loreline"), ("(§.)?Your (?<essencetype>.+) Essence: (§.)?(?<total>(?<useful>[0-9]+)(,[0-9]+)*)"))
    private val essenceCountOtherLoreLinePattern by playerGeneralSubgroup.pattern(("essencecountother.loreline"), (".*(§.)You currently own (§.)(?<total>(?<useful>[0-9]+)(,[0-9]+)*)(§.(?<essencetype>[\\w]+))?.*"))
    private val profileIconVariantOneDisplayNamePattern by playerGeneralSubgroup.pattern(("profileiconvariantone.displayname"), ("(((§.)*(?<icon>[^A-z])? (§.)*(?<type>.+)?) ?(§.)*(?<profile>Profile: )(§.)*(?<fruit>.+))"))
    private val profileIconVariantTwoDisplayNamePattern by playerGeneralSubgroup.pattern(("profileiconvarianttwo.displayname"), ("(§.)*(Profile: )(§.)*(?<fruit>[\\w]+)"))
    private val skyblockLevelingItemNamePattern by playerGeneralSubgroup.pattern(("skyblockleveling.itemname"), (".* Leveling"))
    private val skillLevelItemNamePattern by playerGeneralSubgroup.pattern(("skilllevel.itemname"), ("(?<skillReal>([\\w]+(?<!Dungeoneering))) (?<level>[\\w]+)"))
    private val gardenLevelSkillLevelItemNamePattern by playerGeneralSubgroup.pattern(("gardenlevelskilllevel.itemname"), ("Garden Level (?<level>[\\w]+)"))
    private val collectionsChestNameItemNamePattern by playerGeneralSubgroup.pattern(("collectionschestname.itemname"), (".*Collections"))
    private val collectionLevelItemNamePattern by playerGeneralSubgroup.pattern(("collectionlevel.itemname"), (".*(§.)+(?<collection>[\\w ]+) (?<tier>[MDCLXVI]+)"))
    private val collectionsPercentLoreLinePattern by playerGeneralSubgroup.pattern(("collectionspercent.loreline"), (".*Collections .*: (§.)?(?<percent>[0-9]+)(\\.[0-9]*)?(§.)?%"))
    private val craftMoreMinionsLoreLinePattern by playerGeneralSubgroup.pattern(("craftmoreminions.loreline"), ("(§.)*Craft (§.)*(?<count>[\\w]+) (§.)*more (§.)*unique.*"))
    private val clickToViewLoreLinePattern by playerGeneralSubgroup.pattern(("clicktoview.loreline"), ("§eClick to view .*"))
    private val minionTierCraftProgressLoreLinePattern by playerGeneralSubgroup.pattern(("miniontiercraftprogress.loreline"), (".* Tier .*"))
    private val minionTierNotYetCraftedLoreLinePattern by playerGeneralSubgroup.pattern(("miniontiernotyetcrafted.loreline"), (".*§c.* Tier .*"))
    private val petsItemNamePattern by playerGeneralSubgroup.pattern(("pets.itemname"), (".*Pets.*"))
    private val petsNoPetLoreLinePattern by playerGeneralSubgroup.pattern(("petsnopet.loreline"), ("(§.)*Selected (p|P)et: (§.)*None"))
    private val petsChestNamePattern by playerGeneralSubgroup.pattern(("pets.chestname"), ("Pets.*"))
    private val yourPetScoreLoreLinePattern by playerGeneralSubgroup.pattern(("yourpetscore.loreline"), (".*(§.)*Your Pet Score: (§.)*(?<score>[\\w]+).*"))
    private val minionMenuChestNamePattern by playerGeneralSubgroup.pattern(("minionmenu.chestname"), (".* Minion .*"))
    private val quickUpgradeItemNamePattern by playerGeneralSubgroup.pattern(("quickupgrade.itemname"), ("Quick.Upgrade Minion"))
    private val youNeedXMoreMaterialsLoreLinePattern by playerGeneralSubgroup.pattern(("youneedxmorematerials.loreline"), (".*(§.)+You need (§.)*(?<needed>[\\w]+).*"))
    private val doesNotContainArrowsChestNamePattern by playerGeneralSubgroup.pattern(("doesnotcontainarrows.chestname"), ("^((?! ➜ ).)*\$"))
    private val canDisplayEssenceChestNameItemNamePattern by playerGeneralSubgroup.pattern(("candisplayessence.chestnameitemname"), (".*Essence( Guide.*| Shop)?"))
    private val museumItemNamesList = listOf(
        "Museum",
        "Rarities",
        "Armor Sets",
        "Weapons",
        "Special Items"
    )

    @SubscribeEvent
    override fun onRenderItemTip(event: RenderItemTipEvent) {
        super.onRenderItemTip(event)
    }

    override fun getStackTip(item: ItemStack): String {
        if (configMenuStackSize.player.isEmpty()) return ""
        val itemName = item.cleanName()
        val stackSizeConfig = configMenuStackSize.player
        val chestName = InventoryUtils.openInventoryName()

        if (stackSizeConfig.contains(StackSizeMenuConfig.PlayerGeneral.SKYBLOCK_LEVEL) && chestName.lowercase() == ("skyblock menu")) {
            // itemName.endsWith(" Leveling")
            skyblockLevelingItemNamePattern.matchMatcher(itemName) {
                for (line in item.getLore()) {
                    skyblockLevelLoreLinePattern.matchMatcher(line) {
                        return group("sblvl")
                    }
                }
            }
        }

        if (stackSizeConfig.contains(StackSizeMenuConfig.PlayerGeneral.SKILL_GARDEN_DUNGEON_LEVELS)) {
            if (chestName == "Your Skills" || chestName == "Dungeoneering") {
                if (item.getLore().isNotEmpty() && item.getLore().last() == ("§eClick to view!")) {
                    if (chestName == "Your Skills") {
                        if (CollectionAPI.isCollectionTier0(item.getLore()) && (itemName != ("Dungeoneering"))) return "0"
                        if (itemName.split(" ").size < 2) return "" //thanks to watchdogshelper we had to add this hotfix line
                        skillLevelItemNamePattern.matchMatcher(itemName) {
                            return "${group("level").romanToDecimalIfNecessary()}"
                        }
                    } else if (chestName == "Dungeoneering") {
                        dungeonClassLevelItemNamePattern.matchMatcher(itemName) {
                            return group("level")
                        }
                    }
                }
            } else if (((chestName == "Farming Skill") || (chestName == "Desk"))) {
               gardenLevelSkillLevelItemNamePattern.matchMatcher(itemName) {
                   if (GardenAPI.getGardenLevel() != 0) return GardenAPI.getGardenLevel().toString()
                   return group("level")
               }
            }
        }

        if (stackSizeConfig.contains(StackSizeMenuConfig.PlayerGeneral.SKILL_AVERAGE) && (chestName.lowercase() == ("skyblock menu") && (itemName == ("Your Skills")))) {
            for (line in item.getLore()) {
                skillAvgLoreLinePattern.matchMatcher(line) { return group("avg").toDouble().toInt().toString() }
            }
        }

        if (stackSizeConfig.contains(StackSizeMenuConfig.PlayerGeneral.COLLECTION_LEVELS_AND_PROGRESS)) {
            collectionsChestNameItemNamePattern.matchMatcher(chestName) {
                val lore = item.getLore()
                if (lore.isNotEmpty() && lore.last() == ("§eClick to view!")) {
                    if (CollectionAPI.isCollectionTier0(lore)) return "0"
                    item.name?.let {
                        collectionLevelItemNamePattern.matchMatcher(it) {
                            return "${group("tier").romanToDecimalIfNecessary()}"
                        }
                    }
                }
            }
            if (chestName.lowercase() == "skyblock menu") {
                if (itemName == "Collections") {
                    for (line in item.getLore()) {
                        collectionsPercentLoreLinePattern.matchMatcher(line) { return group("percent").convertPercentToGreenCheckmark() }
                    }
                }
            }
            collectionsChestNameItemNamePattern.matchMatcher(chestName) {
                collectionsChestNameItemNamePattern.matchMatcher(itemName) {
                    for (line in item.getLore()) {
                        collectionsPercentLoreLinePattern.matchMatcher(line) { return group("percent").convertPercentToGreenCheckmark() }
                    }
                }
            }
        }

        if (stackSizeConfig.contains(StackSizeMenuConfig.PlayerGeneral.CRAFTED_MINIONS) && chestName == ("Crafted Minions")) {
            val lore = item.getLore()
            if (itemName == "Information") {
                for (line in lore) {
                    craftMoreMinionsLoreLinePattern.matchMatcher(line) {
                        return group("count")
                    }
                }
            }
            if (lore.isNotEmpty()) {
                clickToViewLoreLinePattern.matchMatcher(lore.last()) {
                    var tiersToSubtract = 0
                    var totalTiers = 0
                    for (line in lore) {
                        minionTierCraftProgressLoreLinePattern.matchMatcher(line) { totalTiers++ } //§c
                        minionTierNotYetCraftedLoreLinePattern.matchMatcher(line) { tiersToSubtract++ }
                    }
                    return "${totalTiers - tiersToSubtract}".replace("$totalTiers", "§a✔")
                }
            }
        }

        if (stackSizeConfig.contains(StackSizeMenuConfig.PlayerGeneral.MUSEUM_PROGRESS) && chestName == ("Your Museum") && museumItemNamesList.contains(itemName)) {
            for (line in item.getLore()) {
                museumDonationLoreLinePattern.matchMatcher(line) { return group("amount").toDouble().toInt().toString().convertPercentToGreenCheckmark() }
            }
        }

        if (stackSizeConfig.contains(StackSizeMenuConfig.PlayerGeneral.PROFILE_ICON) && chestName == ("Profile Management")) {
            profileIconVariantOneDisplayNamePattern.matchMatcher(itemName) { return group("icon") }
            profileIconVariantTwoDisplayNamePattern.matchMatcher(itemName) { return "©" }
            if (itemName == "Locked profile slot") {
                return "§c┏┓"
            }
        }

        if (stackSizeConfig.contains(StackSizeMenuConfig.PlayerGeneral.PET_SCORE_STATUS)) {
            if ((chestName.lowercase() == "skyblock menu")) {
                petsItemNamePattern.matchMatcher(itemName) {
                    for (line in item.getLore()) {
                        petsNoPetLoreLinePattern.matchMatcher(line) { return "§c§l✖" }
                    }
                }
            }
            petsChestNamePattern.matchMatcher(chestName) {
                if (itemName == ("Pet Score Rewards") && item.getLore().isNotEmpty()) {
                    yourPetScoreLoreLinePattern.matchMatcher(item.getLore().last()) {
                        return group("score")
                    }
                }
            }
        }

        if (stackSizeConfig.contains(StackSizeMenuConfig.PlayerGeneral.MINION_QUICK_UPGRADE)) {
            // one day admins are going to remove that damn hyphen in "Quick-Upgrade" and it's going to break this feature
            /*
                chestName.contains(" Minion ")
                itemName.contains("Quick") && itemName.contains("Upgrade Minion")
             */
            minionMenuChestNamePattern.matchMatcher(chestName) {
                quickUpgradeItemNamePattern.matchMatcher(itemName) {
                    val lore = item.getLore()
                    for (line in lore) {
                        youNeedXMoreMaterialsLoreLinePattern.matchMatcher(line) {
                            return group("needed")
                        }
                    }
                }
            }
        }
        // friendly note to future contribs: if you want to add more stack sizes to this category, you have to do it above this line or else it wont work. look, i don't make the rules, aight? -ery
        if (stackSizeConfig.contains(StackSizeMenuConfig.PlayerGeneral.ESSENCE_COUNTS)) {
            if (item.item != Items.skull) return ""
            if (LorenzUtils.isRewardChest()) {
                dungeonEssenceRewardItemNamePattern.matchMatcher(itemName) { return group("amount") } ?: return ""
            }
            var canDisplayEssence = false
            doesNotContainArrowsChestNamePattern.matchMatcher(chestName) {
                canDisplayEssenceChestNameItemNamePattern.matchMatcher(chestName) {
                    canDisplayEssenceChestNameItemNamePattern.matchMatcher(itemName) {
                        canDisplayEssence = true
                    }
                }
            }
            // !(chestName.contains(" ➜ ")) &&
            // (chestName.contains("Essence Shop") && itemName.contains("Essence Shop")) ||
            // (chestName.contains("Essence Guide") && itemName.endsWith(" Essence")) ||
            // (chestName.endsWith(" Essence"))
            if (canDisplayEssence) {
                val lore = item.getLore()
                var total = 0L
                loop@for (line in lore) {
                    essenceCountOtherLoreLinePattern.matchMatcher(line) {
                        total = group("total").formatNumber()
                        break@loop
                    }
                    essenceCountLoreLinePattern.matchMatcher(line) {
                        total = group("total").formatNumber()
                        break@loop
                    }
                }
                return NumberUtil.format(total)
            }
        }

        return ""
    }
}
