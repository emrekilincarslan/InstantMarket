package com.fondova.finance.repo

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.fondova.finance.api.Resource
import com.fondova.finance.util.injectNewData
import com.fondova.finance.vo.ChartData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChartWatchRepository @Inject constructor() {

    private var _liveData: MutableLiveData<Resource<List<ChartData>>> = MutableLiveData()
    private var _workingData: MutableList<ChartData> = mutableListOf()

    fun clearData() {
        _workingData = mutableListOf()
        _liveData.value  = Resource.success(emptyList())
        addData(emptyList(), true)
    }

    fun addData(data: List<ChartData>, isStillLoading: Boolean): List<ChartData> {
        for (item in data) {
            val existingItem = _workingData.firstOrNull { it.dateTime == item.dateTime }
            if (existingItem != null) {
                val index = _workingData.indexOf(existingItem)
                _workingData[index] = injectNewData(existingItem, item)
            } else {
                _workingData.add(item)
            }
            _workingData.sortBy { it.dateTime }
        }
        if (isStillLoading) {
            _liveData.value = Resource.loading()
        } else {
            _liveData.value = Resource.success(_workingData)
        }
        return _workingData
    }

    fun subscribe() : LiveData<Resource<List<ChartData>>> {
        return _liveData
    }

    fun setError(title: String, message: String) {
        _liveData.value = Resource.error(title, message)
    }
}

