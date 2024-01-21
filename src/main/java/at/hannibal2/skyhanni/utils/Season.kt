package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.StringUtils.matchMatcher
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

enum class Season(
    val season: String,
    val abbreviatedPerk: String,
    val perk: String,
) {

    SPRING("§dSpring", "§6+25☘", "§7Gain §6+25☘ Farming Fortune§7."),
    SUMMER("§6Summer", "§3+20☯", "§7Gain §3+20☯ Farming Wisdom§7."),
    AUTUMN("§eAutumn", "§a15%+§4ൠ", "§4Pests §7spawn §a15% §7more often."),
    WINTER("§9Winter", "§a5%+§cC", "§7Visitors give §a5% §7more §cCopper."),
    ;

    fun getPerk(abbreviate: Boolean): String = if (abbreviate) abbreviatedPerk else perk
    fun getSeason(abbreviate: Boolean): String = if (abbreviate) season.take(4) else season

    companion object {

        private val seasonPattern by RepoPattern.pattern(
            "season.skyblocktime",
            "(?:Early |Late )?(?<season>Spring|Summer|Autumn|Winter)"
        )

        fun getSeasonByName(input: String): Season? {
            seasonPattern.matchMatcher(input) {
                return entries.find { it.season.endsWith(group("season")) }
            }
            return null
        }
    }

}
