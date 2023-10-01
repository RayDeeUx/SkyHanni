package at.hannibal2.skyhanni.features.chat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.ChatManager
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.StringUtils.matchMatcher
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class EveryoneHatesDonExpresso {
    private val donExpressoRegex = ".*(§.)?\\[NPC\\] (§.)?Don Expresso(§.)?: (§.)?.*(§.)?.*".toPattern()
    private var isDonExpresso = false

    @SubscribeEvent
    fun onChatMessage(event: LorenzChatEvent) {
        if (!LorenzUtils.onHypixel || !SkyHanniMod.feature.chat.everyoneHatesDonExpresso) return

        donExpressoRegex.matchMatcher(event.message) {
            ChatManager.retractMessage(startLineComponent, "donExpresso")
            isDonExpresso = true
        }

        if (isDonExpresso) {
            event.blockedReason = "don_expresso"
        }
    }
}