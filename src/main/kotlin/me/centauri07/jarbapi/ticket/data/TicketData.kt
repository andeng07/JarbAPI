package me.centauri07.jarbapi.ticket.data

import me.centauri07.jarbapi.ticket.member.TicketMember
import me.centauri07.jarbapi.ticket.member.TicketMemberType
import org.bson.codecs.pojo.annotations.BsonId

/**
 * @author Centauri07
 */
data class TicketData<T>(
    @BsonId val ticketId: String,
    val type: String,
    var channelRef: Long,
    val members: MutableList<TicketMember>,
    val ticketTypeData: T
)