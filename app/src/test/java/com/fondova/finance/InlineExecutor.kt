package com.fondova.finance

import java.util.concurrent.Executor

class InlineExecutor: Executor {
    var executeCount: Int = 0
    override fun execute(runnable: Runnable?) {
        executeCount += 1
        runnable?.run()
    }

}
