package at.hannibal2.skyhanni.mixins.hooks

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI.isBarn
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI.name
import at.hannibal2.skyhanni.utils.StringUtils.matchMatcher
import net.minecraft.client.gui.FontRenderer

// TODO USE SH-REPO
private val piggyPattern = "Piggy: (?<coins>.*)".toPattern()

fun drawString(
    instance: FontRenderer,
    text: String,
    x: Int,
    y: Int,
    color: Int,
) = replaceString(text)?.let {
    instance.drawString(it, x, y, color)
} ?: 0

fun replaceString(text: String): String? {
    if (SkyHanniMod.feature.misc.hideScoreboardNumbers && text.startsWith("§c") && text.length <= 4) {
        return null
    }
    if (SkyHanniMod.feature.misc.hidePiggyScoreboard) {
        piggyPattern.matchMatcher(text) {
            val coins = group("coins")
            return "Purse: $coins"
        }
    }

    if (SkyHanniMod.feature.garden.plotNameInScoreboard && GardenAPI.inGarden()) {
        if (text.contains("⏣")) {
            val plot = GardenPlotAPI.getCurrentPlot()
            val hasPests = text.contains("ൠ")
            val pestSuffix = if (hasPests) {
                val pests = text.last().digitToInt()
                val color = if (pests >= 4) "§c" else "§6"
                " §7(${color}${pests}ൠ§7)"
            } else ""
            val name = plot?.let {
                if (it.isBarn()) "§aThe Barn" else {
                    val namePrefix = if (hasPests) "" else "§aPlot §7- "
                    "$namePrefix§b" + it.name
                }
            } ?: "§aGarden §coutside"
            return " §7⏣ $name$pestSuffix"
        }
    }

    if (SkyHanniMod.feature.misc.colorMonthNames) {
        listOf(
            "Early Spring" to "§d",
            "Spring" to "§d",
            "Late Spring" to "§d",
            "Early Summer" to "§6",
            "Summer" to "§6",
            "Late Summer" to "§6",
            "Early Autumn" to "§e",
            "Autumn" to "§e",
            "Late Autumn" to "§e",
            "Early Winter" to "§9",
            "Winter" to "§9",
            "Late Winter" to "§9"
        ).forEach {
            if (text.trim().startsWith(it.first)) return it.second + text
        }
    }

    return text
}
