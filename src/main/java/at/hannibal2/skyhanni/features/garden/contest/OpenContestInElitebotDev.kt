package at.hannibal2.skyhanni.features.garden.contest

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.cleanName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.getSBMonthByName
import at.hannibal2.skyhanni.utils.NumberUtil.formatNumber
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.asTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import io.github.moulberry.notenoughupdates.util.SkyBlockTime
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object OpenContestInElitebotDev {

    private val config get() = SkyHanniMod.feature.garden.eliteWebsite

    private val EARLIEST_CONTEST: SimpleTimeMark = SkyBlockTime(year = 100, month = 6, day = 18).asTimeMark()

    private const val ELITEBOT_DOMAIN: String = "https://elitebot.dev"
    private const val ELITEBOT_CONTESTS: String = "$ELITEBOT_DOMAIN/contests"
    private const val ELITEBOT_UPCOMING: String = "$ELITEBOT_CONTESTS/upcoming"
    private const val ELITEBOT_RECORDS_SUFFIX: String = "records"

    private val elitebotDevRepoGroup = RepoPattern.group("garden.contest.elitebot")

    private val calendarDatePattern by elitebotDevRepoGroup.pattern(
        "date.chestanditem",
        "(?<sbTime>(?<month>(?:Early |Late )?(?:Winter|Spring|Summer|Autumn|Fall))(?: (?<date>\\d+)(?:nd|rd|th|st))?, Year (?<year>[\\d,.]+))"
    )
    private val contestsPattern by elitebotDevRepoGroup.pattern(
        "contests.loreline",
        "((?:§.)+(?<crop>[\\S ]+)+ Contests?)"
    )
    private val dayPattern by elitebotDevRepoGroup.pattern(
        "day.item",
        "Day (?<day>[\\d.,]+)"
    )
    private val jacobsFarmingContestPattern by elitebotDevRepoGroup.pattern(
        "contest.loreline",
        "(?:§.)*(?:[\\S ]+)?\\d+:\\d+ [ap]m(?:-|[\\S ]+)\\d+:\\d+ [ap]m: (?:§.)*Jacob's Farming Contest(?:§.)*(?: \\((?:§.)*(?:\\d+[ywhm] )*\\d+s(?:§.)*\\)| \\((?:§.)*[\\S ]+(?:§.)*\\))?"
    )
    private val dateStringPattern by elitebotDevRepoGroup.pattern(
        "date.string.command",
        "(?<sbTime>(?<month>(?:Early |Late )?(?:Winter|Spring|Summer|Autumn|Fall))?(?: (?<date>\\d+)(?:nd|rd|th|st)?)?(?:,? )?Year (?<year>[\\d,.]+))"
    )
    private val dateNumberPattern by elitebotDevRepoGroup.pattern(
        "date.number.command",
        "(?<one>\\d+[ymd]) (?<two>\\d+[ymd]) (?<three>\\d+[ymd])"
    )

    @SubscribeEvent
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!isEnabled()) return
        if (!config.eliteWebsiteKeybind.isKeyHeld()) return
        if (event.gui !is GuiChest) return
        val item = event.slot?.stack ?: return
        val chestName = InventoryUtils.openInventoryName()
        val itemName = item.cleanName()
        val firstLoreLine = item.getLore().first()
        if (itemName == "Upcoming Contests" && chestName == "Jacob's Farming Contests" && firstLoreLine == "§8Schedule") {
            LorenzUtils.chat("§aOpening the upcoming contests page on EliteWebsite.")
            OSUtils.openBrowser(ELITEBOT_UPCOMING)
            return
        }
        val useItemName = chestName == "Your Contests" && contestsPattern.matches(firstLoreLine)
        val useChestName = jacobsFarmingContestPattern.matches(firstLoreLine)
        val theString = if (useItemName) itemName else if (useChestName) chestName else return
        calendarDatePattern.matchMatcher(theString) {
            val origYearString = group("year")
            val origMonthString = group("month")
            var origDayString = group("date") ?: ""
            var sbDate = group("sbTime") ?: ""
            if (useChestName) {
                dayPattern.matchMatcher(itemName) {
                    origDayString = group("day")
                    sbDate = "$origMonthString $origDayString, Year $origYearString"
                }
            }
            openContest(origYearString.formatNumber(), getSBMonthByName(origMonthString), origDayString.formatNumber().toInt(), sbDate)
        }
    }
    private fun sendUsageMessagesCalendarDate() {
        LorenzUtils.chat("§cUsage: /shopencontest §b[case-sensitive month name] §b[day] §cYear <year number>")
        LorenzUtils.chat("Parameters colored like §bthis §eare optional.")
    }

    private fun sendUsageMessagesNumbers(argsAsOneString: String) {
        LorenzUtils.chat("§cUsage example: /shelitebotdevcontest <month number>m <day number>d <year number>y")
        LorenzUtils.chat("All parameters are required, but they can be entered in any order (as long as you include the correct suffix).")
        if (argsAsOneString == "") return
        LorenzUtils.chat("You entered: $argsAsOneString")
    }

    fun openFromCommandString(args: Array<String>) {
        if (!LorenzUtils.inSkyBlock) return
        if (!isEnabled()) {
            LorenzUtils.chat("You have disabled opening past farming contests on EliteWebsite. Visit your config to enable this.")
            return
        }
        if (args.isEmpty()) {
            sendUsageMessagesCalendarDate()
            return
        }
        val calendarDateString = args.joinToString(" ")
        if (dateStringPattern.matches(calendarDateString)) {
            dateStringPattern.matchMatcher(calendarDateString) {
                val sbTime = group("sbTime") ?: ""
                val yearString = group("year") ?: ""
                val monthString = group("month") ?: ""
                val dayString = group("date") ?: ""
                if (sbTime.isEmpty() || yearString.isEmpty() || !(calendarDateString.contains("Year"))) {
                    sendUsageMessagesCalendarDate()
                    return
                } else if (dayString.isEmpty() && monthString.isEmpty() && yearString.isNotEmpty()) {
                    openContest(yearString.formatNumber(), sbDate = "Year $yearString")
                } else if (dayString.isEmpty() && monthString.isNotEmpty() && yearString.isNotEmpty()) {
                    openContest(yearString.formatNumber(), getSBMonthByName(monthString.replace(" Fall", " Autumn")), sbDate = "$monthString, Year $yearString")
                } else if (dayString.isNotEmpty() && monthString.isNotEmpty() && yearString.isNotEmpty()) {
                    openContest(yearString.formatNumber(), getSBMonthByName(monthString.replace(" Fall", " Autumn")), dayString.formatNumber().toInt(), calendarDateString, true)
                } else {
                    LorenzUtils.chat("§cIf you're reading this inside Minecraft, something went wrong with parsing your calendar date string. Please copy your original input below and report this bug on the SkyHanni Discord server.")
                    LorenzUtils.chat(calendarDateString)
                }
            }
        } else {
            LorenzUtils.chat("You entered $calendarDateString, which could not be read correctly.")
            sendUsageMessagesCalendarDate()
        }
    }

    private fun openContest(yearLong: Long, month: Int = -1, day: Int = -1, sbDate: String, fromCommand: Boolean = false) {
        val year = yearLong.toInt()
        if (month == -1 && day == -1 && SkyBlockTime(year).passesCalendarDateSanityCheck()) {
            LorenzUtils.chat("Opening the year-specfic contests page for $sbDate.")
            OSUtils.openBrowser("$ELITEBOT_CONTESTS/$year/$ELITEBOT_RECORDS_SUFFIX")
        } else if (day == -1 && SkyBlockTime(year, month).passesCalendarDateSanityCheck()) {
            LorenzUtils.chat("Opening the contests page for $sbDate.")
            OSUtils.openBrowser("$$ELITEBOT_CONTESTS/$year/$month")
        } else if (SkyBlockTime(year, month, day).passesCalendarDateSanityCheck()) {
            val onExactDate = if (fromCommand) "nearest to" else "for"
            LorenzUtils.chat("Opening the contests page $onExactDate $sbDate.")
            OSUtils.openBrowser("$$ELITEBOT_CONTESTS/$year/$month/$day")
        }
    }

    fun openFromCommandNumbers(args: Array<String>) {
        if (!LorenzUtils.inSkyBlock) return
        if (!isEnabled()) {
            LorenzUtils.chat("You have disabled opening past farming contests on EliteWebsite. Visit your config to enable this.")
            return
        }
        val argsAsOneString = args.joinToString(" ")
        if (args.size != 3 || args.isEmpty()) {
            sendUsageMessagesNumbers(argsAsOneString)
            return
        }
        if (dateNumberPattern.matches(argsAsOneString)) {
            dateNumberPattern.matchMatcher(argsAsOneString) {
                val timeUnitsStrings: List<String> = listOf<String>(group("one") ?: "", group("two") ?: "", group("three") ?: "")
                if (timeUnitsStrings.any { it.isEmpty() }) {
                    sendUsageMessagesNumbers(argsAsOneString)
                    return
                }
                val timeUnitsInts: MutableList<Int> = mutableListOf<Int>(0, 0, 0)
                for (timeUnit in timeUnitsStrings) {
                    val lastLetter = timeUnit.takeLast(1)
                    if (lastLetter == "y") timeUnitsInts[0] = timeUnit.removeSuffix(lastLetter).toInt()
                    else if (lastLetter == "m") timeUnitsInts[1] = timeUnit.removeSuffix(lastLetter).toInt()
                    else if (lastLetter == "d") timeUnitsInts[2] = timeUnit.removeSuffix(lastLetter).toInt()
                }
                if (timeUnitsInts.any { it == 0 }) {
                    sendUsageMessagesNumbers(argsAsOneString)
                    return
                }
                openContest(timeUnitsInts[0].toLong(), timeUnitsInts[1], timeUnitsInts[2], "${SkyBlockTime.monthName(timeUnitsInts[1])} ${timeUnitsInts[2]}, Year ${timeUnitsInts[0]}", true)
            }
        } else {
            sendUsageMessagesNumbers(argsAsOneString)
            return
        }
    }

    private fun SkyBlockTime.passesCalendarDateSanityCheck(): Boolean = this.asTimeMark() >= EARLIEST_CONTEST
    private fun isEnabled() = config.enabled
}
