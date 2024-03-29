package me.centauri07.jarbapi.ticket

/**
 * @author Centauri07
 */

data class TicketModuleData(
    val categories: MutableMap<Long, TicketCategoryList>
)

data class TicketCategoryList(
    val categories: MutableMap<Long, String>
)