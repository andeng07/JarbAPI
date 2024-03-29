package me.centauri07.jarbapi.ticket.member

/**
 * @author Centauri07
 */
data class TicketMember(
    val memberId: Long,
    val memberType: TicketMemberType,
    val permissions: MutableList<String>
)