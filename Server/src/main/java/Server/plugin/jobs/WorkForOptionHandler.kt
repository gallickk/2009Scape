package plugin.jobs

import GatheringJobs
import SlayingJob
import core.cache.def.impl.NPCDefinition
import core.game.interaction.OptionHandler
import core.game.node.Node
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.diary.DiaryType
import core.game.node.item.GroundItemManager
import core.game.node.item.Item
import core.plugin.InitializablePlugin
import core.plugin.Plugin
import core.tools.ItemNames
import java.util.*

/**
 * Handles the work-for actions for the NPCs
 * @author Ceikry
 */
@InitializablePlugin
class WorkForOptionHandler : OptionHandler() {
    val possibleWeaponLooks = arrayListOf(
            ItemNames.BRONZE_SCIMITAR,
            ItemNames.STEEL_SCIMITAR,
            ItemNames.RUNE_SCIMITAR,
            ItemNames.BRONZE_MACE,
            ItemNames.MITHRIL_WARHAMMER
    )
    val gatheringMap = hashMapOf<Int,List<GatheringJobs>>(
            0 to listOf(GatheringJobs.AIR_RUNE,GatheringJobs.COWHIDES,GatheringJobs.RUNE_ESS,GatheringJobs.WATER_RUNE),
            922 to listOf(GatheringJobs.ASHES,GatheringJobs.AIR_RUNE,GatheringJobs.WATER_RUNE),
            3807 to listOf(GatheringJobs.COWHIDES),
            4899 to listOf(GatheringJobs.CAKE,GatheringJobs.ANCHOVY_PIZZA,GatheringJobs.COOKED_CRAYFISH,GatheringJobs.COOKED_SALMON,GatheringJobs.COOKED_TROUT,GatheringJobs.MEAT_PIE,GatheringJobs.MEAT_PIZZA,GatheringJobs.PLAIN_PIZZA),
            4901 to listOf(GatheringJobs.RAW_CRAYFISH,GatheringJobs.RAW_SALMON,GatheringJobs.RAW_SHRIMP,GatheringJobs.RAW_TROUT),
            4902 to listOf(GatheringJobs.COPPER_ORE,GatheringJobs.TIN_ORE,GatheringJobs.COAL,GatheringJobs.IRON_ORE,GatheringJobs.SILVER_ORE,GatheringJobs.GOLD_ORE),
            4903 to listOf(GatheringJobs.SILVER_ORE),
            4904 to listOf(GatheringJobs.BRONZE_BAR,GatheringJobs.IRON_BAR,GatheringJobs.STEEL_BAR),
            4906 to listOf(GatheringJobs.LOG,GatheringJobs.OAK,GatheringJobs.WILLOW)
    )
    val typeMap = hashMapOf(
            0 to 0,
            705 to 1,
            922 to 0,
            1861 to 1,
            3807 to 0,
            4899 to 0,
            4901 to 0,
            4902 to 0,
            4903 to 0,
            4904 to 0,
            4906 to 0,
            4707 to 1
    )
    @Throws(Throwable::class)
    override fun newInstance(arg: Any?): Plugin<Any>? {
        NPCDefinition.forId(4906).handlers["option:work-for"] = this
        NPCDefinition.forId(4707).handlers["option:work-for"] = this
        NPCDefinition.forId(4904).handlers["option:work-for"] = this
        NPCDefinition.forId(4903).handlers["option:work-for"] = this
        NPCDefinition.forId(4902).handlers["option:work-for"] = this
        NPCDefinition.forId(4901).handlers["option:work-for"] = this
        NPCDefinition.forId(4899).handlers["option:work-for"] = this
        NPCDefinition.forId(3807).handlers["option:work-for"] = this
        NPCDefinition.forId(1861).handlers["option:work-for"] = this
        NPCDefinition.forId(922).handlers["option:work-for"] = this
        NPCDefinition.forId(705).handlers["option:work-for"] = this
        NPCDefinition.forId(0).handlers["option:work-for"] = this
        return this
    }

    override fun handle(player: Player, node: Node, option: String): Boolean {
        var amount = 0
        var jobId = 0

        if(player.getAttribute("jobs:id",-1) != -1){
            JobManager.rewardPlayer(player)
            return true
        }

        val type = typeMap[node.id]
        jobId = if(type == 0) {
            var job = gatheringMap[node.id]?.random()
            while(!checkRequirement(player,job)){
                job = gatheringMap[node.id]?.random()
            }
            amount = job?.getAmount() ?: 0
            job?.ordinal ?: 0
        } else {
            SlayingJob.values().random().ordinal.also { amount = SlayingJob.values()[it].getAmount() }
        }

        when(type){
            0 -> {
                val job = GatheringJobs.values()[jobId]
                player.dialogueInterpreter.sendItemMessage(job.itemId,"You are assigned to gather $amount ${Item(job.itemId).name.toLowerCase()}")
            }
            1 -> {
                val job = SlayingJob.values()[jobId]
                player.dialogueInterpreter.sendItemMessage(possibleWeaponLooks.random(),"You are assigned to kill $amount ${NPC(job.ids[0]).name.toLowerCase()}")
            }
        }

        player.setAttribute("/save:jobs:id",jobId)
        player.setAttribute("/save:jobs:amount",amount)
        player.setAttribute("/save:jobs:original_amount",amount)
        player.setAttribute("/save:jobs:type",type)

        if(node.asNpc().id == 4901) {
            player.achievementDiaryManager.finishTask(player, DiaryType.LUMBRIDGE, 0, 14)
        }
        return true
    }

    fun checkRequirement(player: Player, jobs: GatheringJobs?): Boolean{
        jobs ?: return true
        val requirement: Pair<Int,Int> = Pair(jobs.lvlReq,jobs.skill)
        if(player.skills.getLevel(requirement.second) < requirement.first){
            return false
        }
        return true
    }
}
