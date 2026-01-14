package org.blocovermelho.bvauth.ext.dsl

import net.minecraft.network.chat.Component
import org.blocovermelho.bvauth.ext.Core.toLiteral

@DslMarker
annotation class LineDsl

@LineDsl
class LineBuilder {
    var entries: MutableList<Component> = mutableListOf()

    constructor(components: List<Component>) {
        entries = components.toMutableList()
    }

    constructor() {
        entries = mutableListOf()
    }
}

fun LineBuilder.lineBreak() {
    this += "\n"
}

operator fun LineBuilder.plusAssign(literal: String) {
    entries += buildComponent { literal(literal) }
}

operator fun LineBuilder.plusAssign(component: Component) {
    entries += component
}

operator fun LineBuilder.plusAssign(components: List<Component>) {
    entries += components
}

operator fun LineBuilder.plusAssign(action: TextBuilder.() -> Unit) {
    entries += buildComponent { action() }
}

fun LineBuilder.wrap(start: String, component: Component, end: String) {
    entries += start.toLiteral()
    entries += component
    entries += end.toLiteral()
}

fun LineBuilder.build() : Component {
    val finalComponent = Component.empty()

    for (component in entries) {
        finalComponent.append(component)
        finalComponent.append(" ")
    }

    return finalComponent
}

fun buildLine(vararg components: Component) : Component {
    return LineBuilder(components.toList()).build()
}

fun buildLine(action: LineBuilder.() -> Unit) : Component {
   return LineBuilder().apply (action).build()
}