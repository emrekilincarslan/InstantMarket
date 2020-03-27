package com.fondova.finance.util

import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties

fun <T: Any> injectNewData(oldValue: T?, newValue: T): T {
    var mutableValue = oldValue ?: newValue

    val allMembers = mutableValue.javaClass.kotlin.declaredMemberProperties

    for (member in allMembers) {

        if (member.visibility != KVisibility.PUBLIC) {
            continue
        }
        val fieldName = member.name
        val field = mutableValue.javaClass.getDeclaredField(fieldName)
        val fieldValue = field.get(newValue)

        if (fieldValue != null) {
            field.set(mutableValue, fieldValue)
        }
    }
    return  mutableValue
}
