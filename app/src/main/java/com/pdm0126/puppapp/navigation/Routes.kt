package com.pdm0126.puppapp.navigation

import kotlinx.serialization.Serializable

sealed class Route {
    @Serializable
    data object Login      : Route()
    @Serializable
    data object Register   : Route()
    @Serializable
    data object Orders     : Route()
    @Serializable
    data object NewOrder   : Route()
    @Serializable
    data class  OrderDetail(val orderId: String) : Route()
    @Serializable
    data object Menu       : Route()
    @Serializable
    data object History    : Route()
}
