package com.fondova.finance.util.ui

fun String.fromCamelCaseToSpaces(): String {
    val pattern = "(?<=[^A-Z])(?=[A-Z])"
    var regex = Regex(pattern)
    return this.replace(regex, " ")
}