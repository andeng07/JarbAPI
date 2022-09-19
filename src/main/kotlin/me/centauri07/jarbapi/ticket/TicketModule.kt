package me.centauri07.jarbapi.ticket

import me.centauri07.jarbapi.BotApplication
import me.centauri07.jarbapi.module.DiscordModule
import me.centauri07.jarbapi.ticket.data.TicketData
import me.centauri07.jarbapi.ticket.exception.TicketTypeAlreadyExistException
import me.centauri07.jarbapi.ticket.exception.TicketTypeNotFoundException
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Category
import net.dv8tion.jda.api.entities.Member
import java.util.*

/**
 * @author Centauri07
 */
class TicketModule(
    botApplication: BotApplication,
    private val moduleData: TicketModuleData,
) : DiscordModule(botApplication, "ticket_system") {

    private val ticketTypeRegistry: MutableMap<String, TicketType<*, *>> = mutableMapOf()

    fun getTicketRegistry(): Map<String, TicketType<*, *>> = ticketTypeRegistry

    fun registerTicketType(ticketType: TicketType<*, *>) {
        if (ticketTypeRegistry.containsKey(ticketType.id)) throw TicketTypeAlreadyExistException("Ticket type with id ${ticketType.id} already exist")

        ticketTypeRegistry[ticketType.id] = ticketType
    }

    fun <T: Ticket<TD>, TD> createTicket(owner: Member, ticketTypeId: String, data: TD): T {

        val ticketId = UUID.randomUUID().toString()

        val channel = getCategory(ticketTypeId).createTextChannel("${ticketTypeId}-${owner.effectiveName}")
            .setTopic(ticketId)
            .syncPermissionOverrides()
            .complete()

        val ticketData = TicketData(ticketId, ticketTypeId, channel.idLong, mutableListOf(), data)

        channel.upsertPermissionOverride(owner).grant(Permission.VIEW_CHANNEL).queue()

        val ticketType = getType(ticketData.type) as TicketType<T, TD>

        val ticket = ticketType.fromData(ticketData)

        ticketType.insertData(ticketData)

        ticketType.insertCache(ticketData)

        ticket.initialize(owner)

        return ticket

    }

    fun getType(ticketTypeId: String): TicketType<*, *> = ticketTypeRegistry[ticketTypeId] ?: throw TicketTypeNotFoundException("Ticket type with name $ticketTypeId not found!")

    private fun getCategory(ticketType: String): Category {
        val categories = moduleData.categories.filter { it.value == ticketType }.keys.mapNotNull {
            botApplication.mainGuild.getCategoryById(it)
        }

        return categories.firstOrNull { it.channels.size < 50 } ?: botApplication.mainGuild
            .createCategory(ticketType + if (categories.isNotEmpty()) categories.count() + 1 else "")
            .complete().also {
                it.upsertPermissionOverride(botApplication.mainGuild.publicRole).deny(Permission.VIEW_CHANNEL).queue()
                moduleData.categories[it.idLong] = ticketType
            }

    }


}