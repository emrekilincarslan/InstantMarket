package com.fondova.finance.util

interface TaskRunner {

    fun run(task: () -> Unit)
    fun cancel()
}