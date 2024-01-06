package at.hannibal2.skyhanni.features.inventory.itemdisplayoverlay.menu

import at.hannibal2.skyhanni.config.features.inventory.stacksize.StackSizeMenuConfig
import at.hannibal2.skyhanni.events.RenderItemTipEvent
import at.hannibal2.skyhanni.features.inventory.itemdisplayoverlay.AbstractMenuStackSize
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.cleanName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.StringUtils.firstLetterUppercase
import at.hannibal2.skyhanni.utils.StringUtils.matchMatcher
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class MenuItemDisplayOverlayAbiphone : AbstractMenuStackSize() {
    private val abiphoneSubgroup = itemStackSizeGroup.group("abiphone")
    
    private val abiphoneContactsDirectoryChestNamePattern by abiphoneSubgroup.pattern(("abiphonecontactsdirectory.chestname"), ("(.*A.iphone.*|Contacts Directory)"))
    private val yourContactsLoreLinePattern by abiphoneSubgroup.pattern(("yourcontacts.loreline"), ("(§.)?Your contacts: (§.)?(?<useful>[0-9]+)(§.)?\\/(§.)?(?<total>[0-9]+).*"))
    private val isAContactItemNamePattern by abiphoneSubgroup.pattern(("isacontact.itemname"), (".*§f§.*"))
    private val upgradedAllRelaysLoreLinePattern by abiphoneSubgroup.pattern(("upgradedallrelays.loreline"), ("(§.)?Upgraded Relays: (§.).*ALL!.*"))
    private val upgradedPartialRelaysLoreLinePattern by abiphoneSubgroup.pattern(("upgradedpartialrelays.loreline"), ("(§.)?Upgraded Relays: (§.)?(?<useful>[0-9]+)(§.)?\\/(§.)?(?<total>[0-9]+).*"))
    private val selectedRingtoneLoreLinePattern by abiphoneSubgroup.pattern(("selectedringtone.loreline"), ("(§.)*Selected Ringtone: (§.)*(?<ringtone>.+)"))
    private val abiphoneMinigameStatsLoreLinePattern by abiphoneSubgroup.pattern(("abiphoneminigamestats.loreline"), ("(§.)*(?<type>.+): (§.)*(?<count>[\\w]+)"))
    private val tilerSortAbiphoneOnlyLoreLinePattern by abiphoneSubgroup.pattern(("tilersortabiphoneonly.loreline"), (".*(?<colorCode>§.)*▶.?(?<category>[\\w ]+).*"))

    @SubscribeEvent
    override fun onRenderItemTip(event: RenderItemTipEvent) {
        super.onRenderItemTip(event)
    }

    override fun getStackTip(item: ItemStack): String {
        if (configMenuStackSize.abiphone.isEmpty()) return ""
        val itemName = item.cleanName()
        val stackSizeConfig = configMenuStackSize.abiphone
        val chestName = InventoryUtils.openInventoryName()

        abiphoneContactsDirectoryChestNamePattern.matchMatcher(chestName) {
            if ((stackSizeConfig.contains(StackSizeMenuConfig.Abiphone.CONTACTS_DIRECTORY)) && (itemName == ("Contacts Directory"))) {
                for (line in item.getLore()) {
                    yourContactsLoreLinePattern.matchMatcher(line) {
                        return group("useful")
                    }
                }
            }

            if ((stackSizeConfig.contains(StackSizeMenuConfig.Abiphone.DO_NOT_DISTURB))) {
                val nameWithColor = item.name ?: return ""
                isAContactItemNamePattern.matchMatcher(nameWithColor) {
                    val lore = item.getLore()
                    for (line in lore) {
                        if (line == ("§cDo Not Disturb enabled!")) {
                            return "§c§l✖"
                        }
                    }
                }
            }

            if ((stackSizeConfig.contains(StackSizeMenuConfig.Abiphone.RELAYS_COMPLETED)) && (itemName == ("9f™ Operator Chip"))) {
                val maxRelays = "9" //edit this line whenever they add more relays
                //§7Upgraded Relays: §e1§7/§59
                //Upgraded Relays: 1/9
                for (line in item.getLore()) {
                    upgradedAllRelaysLoreLinePattern.matchMatcher(line) { return maxRelays }
                    upgradedPartialRelaysLoreLinePattern.matchMatcher(line) { return group("useful") }
                }
            }

            if ((stackSizeConfig.contains(StackSizeMenuConfig.Abiphone.SELECTED_RINGTONE)) && (itemName == ("Ringtones"))) {
                for (line in item.getLore()) {
                    selectedRingtoneLoreLinePattern.matchMatcher(line) {
                        return when (group("ringtone").split(" ").last()) {
                            "Default" -> "Def"
                            "Entertainer" -> "Ent"
                            "Notkia" -> "Nka"
                            "Techy" -> "Tec"
                            "Scrapper" -> "Scr"
                            "Elise" -> "WTF" //intentional. do not edit.
                            "Bells" -> "Jbl"
                            "Vibrate" -> "Vib"
                            else -> "?"
                        }
                    }
                }
            }

            if ((stackSizeConfig.contains(StackSizeMenuConfig.Abiphone.TIC_TAC_TOE)) && (itemName == ("Tic Tac Toe"))) {
                var finalString = ""
                for (line in item.getLore()) {
                    abiphoneMinigameStatsLoreLinePattern.matchMatcher(line) {
                        val colorCode = when (group("type")) {
                            "Wins" -> "§a"
                            "Draws" -> "§e"
                            "Losses" -> "§c"
                            else -> "§0"
                        }
                        finalString = "$finalString$colorCode${group("count")}"
                    }
                }
                return finalString
            }

            if ((stackSizeConfig.contains(StackSizeMenuConfig.Abiphone.SNAKE)) && (itemName == ("Snake"))) {
                for (line in item.getLore()) {
                    abiphoneMinigameStatsLoreLinePattern.matchMatcher(line) {
                        return group("count")
                    }
                }
            }

            if ((stackSizeConfig.contains(StackSizeMenuConfig.Abiphone.NAVIGATION)) && ((itemName == ("Filter")) || itemName == ("Sort"))) {
                for (line in item.getLore()) {
                    tilerSortAbiphoneOnlyLoreLinePattern.matchMatcher(line) {
                        return when (val placeholder = group("category").replace(" ", "").lowercase()) {
                            "alphabetical" -> "ABC"
                            "donotdisturbfirst" -> "§cDND"
                            "difficulty" -> "§aE§eM§cH"
                            "usuallocation" -> "Loc"
                            "notadded" -> "§cQA"
                            "completedquestbutnotadded" -> "§aQ§cA"
                            else -> placeholder.take(3).firstLetterUppercase()
                        }
                    }
                }
            }
        }

        return ""
    }
}
