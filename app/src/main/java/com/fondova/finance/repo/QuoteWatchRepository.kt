package com.fondova.finance.repo

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.fondova.finance.api.model.quote.QuoteWatchResponse
import com.fondova.finance.api.model.quote.setValueForKey
import com.fondova.finance.api.model.quote.update
import com.fondova.finance.util.injectNewData
import com.fondova.finance.vo.QuoteValue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteWatchRepository @Inject constructor() {


    var observables: MutableMap<String, MutableLiveData<QuoteWatchResponse>> = mutableMapOf()

    fun value(expression: String): QuoteWatchResponse? {
        return observables[expression]?.value
    }

    fun setValue(expression: String, quoteWatchResponse: QuoteWatchResponse) {
        if (observables[expression] == null) {
            val liveData = MutableLiveData<QuoteWatchResponse>()
            observables[expression] = liveData
        }
        observables[expression]?.value = quoteWatchResponse

    }

    fun subscribe(expression: String): LiveData<QuoteWatchResponse> {
        if (observables[expression] == null) {
            val liveData = MutableLiveData<QuoteWatchResponse>()
            observables[expression] = liveData
        }
        return observables[expression]!!
    }

    fun updateQuote(expression: String, quoteValue: QuoteWatchResponse) {
        var oldValue = value(expression)
        quoteValue.setValueForKey("Expression", expression)
        if (oldValue == null) {
            setValue(expression, quoteValue)
            return
        }
        val mergedValue = oldValue.update(quoteValue)
        setValue(expression, mergedValue)
    }

    fun mergeData(existingQuoteValue: QuoteValue?, newQuoteValue: QuoteValue): QuoteValue {
        return injectNewData(existingQuoteValue, newQuoteValue)
    }

    fun clearCache() {
        observables = mutableMapOf()
    }

}
