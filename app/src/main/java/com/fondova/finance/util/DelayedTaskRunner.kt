package com.fondova.finance.util

import java.util.*

class DelayedTaskRunner(private val milliseconds: Long): TaskRunner {
    constructor(seconds: Int): this(milliseconds = 1000 * seconds.toLong())

    private var timer: Timer? = null

    override fun run(task: () -> Unit) {
        timer?.cancel()
        timer = Timer()
        timer?.schedule(object: TimerTask() {
            override fun run() {
                task()
            }
        }, milliseconds)
    }

    fun run(runnable: Runnable) {
        run {
            runnable.run()
        }
    }

    override fun cancel() {
        timer?.cancel()
        timer = null
    }

}