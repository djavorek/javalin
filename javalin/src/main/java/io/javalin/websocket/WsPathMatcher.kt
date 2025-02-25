/*
 * Javalin - https://javalin.io
 * Copyright 2017 David Åse
 * Licensed under Apache 2.0: https://github.com/tipsy/javalin/blob/master/LICENSE
 */

package io.javalin.websocket

import io.javalin.config.RouterConfig
import io.javalin.router.matcher.PathParser
import io.javalin.security.RouteRole
import java.util.*

data class WsHandlerEntry(
    val type: WsHandlerType,
    val path: String,
    val routerConfig: RouterConfig,
    val wsConfig: WsConfig,
    val roles: Set<RouteRole>
) {
    private val pathParser = PathParser(path, routerConfig)
    fun matches(path: String) = pathParser.matches(path)
    fun extractPathParams(path: String) = pathParser.extractPathParams(path)
}

/**
 * Performs match operations on WebSocket paths.
 */
class WsPathMatcher {

    private val wsHandlerEntries = WsHandlerType.values()
        .associateTo(EnumMap<WsHandlerType, MutableList<WsHandlerEntry>>(WsHandlerType::class.java)) {
            it to mutableListOf()
        }

    fun add(entry: WsHandlerEntry) {
        if (wsHandlerEntries[entry.type]!!.find { it.type == entry.type && it.path == entry.path } != null) {
            throw IllegalArgumentException("Handler with type='${entry.type}' and path='${entry.path}' already exists.")
        }
        wsHandlerEntries[entry.type]!!.add(entry)
    }

    fun allEntries() = wsHandlerEntries.values.flatten()

    /** Returns all the before handlers that match the given [path]. */
    fun findBeforeHandlerEntries(path: String) = findEntries(WsHandlerType.WEBSOCKET_BEFORE, path)

    /** Returns the first endpoint handler that match the given [path], or `null`. */
    fun findEndpointHandlerEntry(path: String) = findEntries(WsHandlerType.WEBSOCKET, path).firstOrNull()

    /** Returns all the after handlers that match the given [path]. */
    fun findAfterHandlerEntries(path: String) = findEntries(WsHandlerType.WEBSOCKET_AFTER, path)

    /** Returns all the handlers of type [handlerType] that match the given [path]. */
    private fun findEntries(handlerType: WsHandlerType, path: String) =
        wsHandlerEntries[handlerType]!!.filter { entry -> entry.path == "*" || entry.matches(path) }
}
