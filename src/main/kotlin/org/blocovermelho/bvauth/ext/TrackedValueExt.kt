package org.blocovermelho.bvauth.ext

import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueList

operator fun <T> TrackedValue<T>.divAssign(value: T & Any) {
    this.setValue(value)
}

inline operator fun <reified T> TrackedValue<ValueList<T>>.divAssign(value: List<T>) {
    this.setValue(ValueList.create(value.first(), *value.toTypedArray()))
}

operator fun <T> TrackedValue<T>.unaryMinus() : T {
    return this.value()
}