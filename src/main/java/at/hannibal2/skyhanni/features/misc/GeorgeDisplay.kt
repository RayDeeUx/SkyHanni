package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryOpenEvent
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.cleanName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.enumJoinToPattern
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getPriceOrNull
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.StringUtils.allLettersFirstUppercase
import at.hannibal2.skyhanni.utils.StringUtils.matchMatcher
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class GeorgeDisplay {

    private val config get() = SkyHanniMod.feature.misc.pets.george

    private val SEPARATOR = ";"

    private val neededPetPattern by RepoPattern.pattern(
        "george.tamingcap.needed.pet.loreline",
        "(?i) +(?<fullThing>(?<tierColorCodes>§.)*(?<tier>${enumJoinToPattern<LorenzRarity>{it.rawName.lowercase()}}) (?<pet>[\\S ]+))"
    )

    private var display = listOf<Renderable>()

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryOpenEvent) {
        if (!isEnabled()) return
        if (event.inventoryName != "Offer Pets") return
        val stack = event.inventoryItems[41] ?: return
        if (stack.cleanName() != "+1 Taming Level Cap") return
        display = listBuilding(stack.getLore())
    }

    private fun listBuilding(lore: List<String>): MutableList<Renderable> {
        val updateList: MutableList<Renderable> = mutableListOf(
            Renderable.string("§d§lTaming 60 Cost: §r§d(${
                if (config.otherRarities) "cheapest" else "exact"
            } rarity)")
        )
        var totalCost = 0.0
        for (line in lore) {
            neededPetPattern.matchMatcher(line) {
                val origTierString = group("tier") ?: ""
                val tier = LorenzRarity.entries.find { it.name == origTierString.uppercase() }!!.id
                val origPetString = group("pet") ?: ""
                val pet = origPetString.uppercase().replace(" ", "_").removePrefix("FROST_")
                val petPrices: MutableList<Double> = mutableListOf()
                val petPriceOne = "$pet;$tier".asInternalName().getPriceOrNull() ?: -1.0
                petPrices.add(petPriceOne)
                if (config.otherRarities || petPriceOne == -1.0) {
                    val lowerTier = "$pet$SEPARATOR${tier - 1}".asInternalName().getPriceOrNull() ?: Double.MAX_VALUE
                    petPrices.add(lowerTier)
                    if (tier != 5) {
                        val higherTier = "$pet$SEPARATOR${tier + 1}".asInternalName().getPriceOrNull() ?: Double.MAX_VALUE
                        petPrices.add(higherTier)
                    }
                }
                val petPrice = petPrices.min()
                val tierUsed = when (petPrices.indexOf(petPrice)) {
                    1 -> tier - 1
                    2 -> tier + 1
                    else -> tier
                }
                val displayPetString = if (tierUsed == tier) group("fullThing") else {
                    "${LorenzRarity.entries.find { it.id == tierUsed }!!.formattedName} $origPetString"
                }
                if (petPrice != -1.0) {
                    totalCost += petPrice
                    updateList.add(Renderable.clickAndHover(
                        text = " §7- $displayPetString§7: §6${petPrice.addSeparators()} coins",
                        tips = listOf(
                            "§aClick to run §e/ahs ] $origPetString §ato find it on the Auction House.",
                            "§aNotes: §eSet the rarity filter yourself. §cBooster Cookie required!"
                        ),
                        onClick = { LorenzUtils.sendCommandToServer("ahs ] $origPetString") }
                    ))
                } else {
                    updateList.add(Renderable.clickAndHover(
                        text = " §7- $displayPetString§7: §eNot on AH; view its Wiki article here.",
                        tips = listOf("§4Click to run §e/wiki $pet §4to view how to obtain it."),
                        onClick = { LorenzUtils.sendCommandToServer("wiki $pet") }
                    ))
                }
            }
        }
        updateList.add(Renderable.string("§dTotal cost §7(§6Lowest BIN§7): §6${totalCost.addSeparators()} coins"))
        if (config.otherRarities) updateList.add(Renderable.string("§c§lDisclaimer:§r§c Total does not include costs to upgrade via Kat."))
        return updateList
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (InventoryUtils.openInventoryName() != "Offer Pets") return
        config.position.renderRenderables(display, posLabel = "George Display")
    }

    private fun isEnabled() = config.enabled && LorenzUtils.inSkyBlock
}
