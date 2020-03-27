package com.fondova.finance.persistance

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.fondova.finance.api.model.Credentials
import com.fondova.finance.db.KeyValueDao
import com.fondova.finance.ui.chart.detail.ChartInterval
import com.fondova.finance.ui.chart.detail.ChartStyle
import com.fondova.finance.vo.NewsCategory
import javax.inject.Inject
import javax.inject.Singleton
import com.fondova.finance.workspace.service.EmptyResponseListener
import com.fondova.finance.workspace.service.WorkspaceService
import com.fondova.finance.config.AppConfig
import com.fondova.finance.workspace.Workspace
import com.fondova.finance.repo.*
import com.fondova.finance.vo.User
import com.fondova.finance.workspace.WorkspaceFactory


inline fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object : com.google.gson.reflect.TypeToken<T>() {}.type)

interface AppStorageInterface {
    fun getRefreshRateAsInt(): Int
    fun getWorkspace(): Workspace
    fun setWorkspace(workspace: Workspace)
    fun getWorkspaceList(): List<Workspace>
    fun setWorkspaceList(list: List<Workspace>)
    fun getCredentials(): Credentials
    fun setCredentials(credentials: Credentials)
    fun getShouldRememberCredentials(): Boolean
    fun setShouldRememberCredentials(value: Boolean)
    fun getAcceptedEula(): Boolean
    fun hasDefaultWorkspace(): Boolean
    fun clearCachedWorkspaceData()
    fun getWorkspaceLiveData(): LiveData<Workspace>
    fun getDidMigrateGoogleQuoteList(): Boolean
    fun setDidMigrateGoogleQuoteList(didMigrate: Boolean)
    fun getNewsCategories(): List<NewsCategory>
}

@Singleton
class AppStorage @Inject constructor(val keyValueStorage: KeyValueStorage,
                                     val encryptionService: EncryptionService,
                                     val legacyKeyValueDao: KeyValueDao,
                                     val textsRepository: TextsRepository,
                                     val gson: Gson,
                                     val appConfig: AppConfig,
                                     val workspaceService: WorkspaceService): AppStorageInterface {

    private val KEY_USERNAME = "user.username"
    private val KEY_PASSWORD = "user.password"
    private val KEY_REMEMBER_CREDENTIALS = "user.remember_me"
    private val KEY_REFRESH_RATE = "user.refresh_rate"
    private val KEY_DEFAULT_SYMBOLS_INSERTED = "app.default_symbols_inserted"
    private val KEY_CHART_STYLE = "app.chart.style"
    private val KEY_CHART_INTERVAL = "app.chart.interval"
    private val KEY_DEFAULT_NEWS_INSERTED = "app.default_news_inserted"
    private val KEY_NEWS_CATEGORIES = "app.news_categories"
    private val KEY_SEARCH_HISTORY = "app.search_history"
    private val KEY_USER = "app.user"
    private val KEY_ACCEPTED_EULA = "user.accepted_eula"
    private val KEY_SYNC_WITH_DRIVE = "app.sync_with_drive"
    private val KEY_ASKED_SYNC_WITH_DRIVE = "app.asked_sync_with_drive"
    private val KEY_WORKSPACE_EXPANDED_STATE = "app.workspace.expanded_state"
    private val KEY_DID_MIGRATE_GOOGLE_QUOTE_LIST = "com.stock.financex.did_migrate_google_quote_list"


    @JvmField
    var isUserLogoutForced: Boolean = false
    @JvmField
    var shouldUserBeConnected: Boolean = false
    @JvmField
    var isAppInBackground: Boolean = false
    @JvmField
    val quoteListLiveData = MutableLiveData<Workspace>()
    @JvmField
    var workspaceList: List<Workspace> = emptyList()

    private var workspace: Workspace? = null

    override fun getWorkspaceList(): List<Workspace> {
        return workspaceList
    }

    override fun setWorkspaceList(list: List<Workspace>) {
        workspaceList = list
    }

    override fun getAcceptedEula(): Boolean {
        return keyValueStorage.get(KEY_ACCEPTED_EULA)?.toBoolean() ?: false
    }

    fun setAcceptedEula(value: Boolean) {
        keyValueStorage.set(KEY_ACCEPTED_EULA, value.toString())
    }

    fun getSyncWithDrive(): Boolean {
        if (appConfig.useStockSettings()) {
            return false
        }
        return keyValueStorage.get(KEY_SYNC_WITH_DRIVE)?.toBoolean() ?: false
    }

    fun setSyncWithDrive(value: Boolean) {
        keyValueStorage.set(KEY_SYNC_WITH_DRIVE, value.toString())
    }

    fun getAskedSyncWithDrive(): Boolean {
        return keyValueStorage.get(KEY_ASKED_SYNC_WITH_DRIVE)?.toBoolean() ?: false
    }

    fun setAskedSyncWithDrive(value: Boolean) {
        keyValueStorage.set(KEY_ASKED_SYNC_WITH_DRIVE, value.toString())
    }

    fun getUser(): User? {
        val json = keyValueStorage.get(KEY_USER)
        if (json.isNullOrEmpty()) {
            return null
        }
        return gson.fromJson(json, User::class.java)
    }

    fun setUser(value: User?) {
        if (value == null) {
            keyValueStorage.delete(KEY_USER)
            return
        }
        val json = gson.toJson(value)
        keyValueStorage.set(KEY_USER, json)
    }

    fun getSearchHistory(): MutableList<String> {
        val json = keyValueStorage.get(KEY_SEARCH_HISTORY) ?: ""
        var searchHistory = gson.fromJson<MutableList<String>>(json) ?: mutableListOf()
        return searchHistory
    }

    fun setSearchHistory(searchHistory: List<String>) {
        val json = gson.toJson(searchHistory)
        keyValueStorage.set(KEY_SEARCH_HISTORY, json)
    }

    override fun getNewsCategories(): List<NewsCategory> {
        val json = keyValueStorage.get(KEY_NEWS_CATEGORIES) ?: ""
        var newsCategories = gson.fromJson<List<NewsCategory>>(json) ?: emptyList()

        return newsCategories
    }

    override fun getCredentials(): Credentials {
        return Credentials.create(getUsername(), getPassword())
    }

    override fun setCredentials(credentials: Credentials) {
        setUsername(credentials.username)
        setPassword(credentials.password)
    }

    fun getUsername(): String {
        var encryptedUsername = keyValueStorage.get(KEY_USERNAME)
        if (encryptedUsername.isNullOrEmpty()) {
            return legacyKeyValueDao.getKeyValue(KEY_USERNAME)?.value ?: ""
        }
        return encryptionService.decrypt(KEY_USERNAME, encryptedUsername)
    }

    fun setUsername(value: String) {
        keyValueStorage.set(KEY_USERNAME, encryptionService.encrypt(KEY_USERNAME, value))
    }

    fun getPassword(): String {
        var encryptedPassword = keyValueStorage.get(KEY_PASSWORD)
        if (encryptedPassword.isNullOrEmpty()) {
            encryptedPassword = legacyKeyValueDao.getKeyValue(KEY_PASSWORD)?.value ?: ""
        }
        return encryptionService.decrypt(KEY_PASSWORD, encryptedPassword)
    }

    fun setPassword(value: String) {
        keyValueStorage.set(KEY_PASSWORD, encryptionService.encrypt(KEY_PASSWORD, value))
    }

    override fun getShouldRememberCredentials(): Boolean {
        var stringValue = keyValueStorage.get(KEY_REMEMBER_CREDENTIALS)
        if (stringValue.isNullOrEmpty()) {
            stringValue = legacyKeyValueDao.getKeyValue(KEY_REMEMBER_CREDENTIALS)?.value
        }

        return stringValue?.toBoolean() ?: false
    }

    override fun setShouldRememberCredentials(value: Boolean) {
        keyValueStorage.set(KEY_REMEMBER_CREDENTIALS, value.toString())
    }

    fun getRefreshRate(): String {
        val refreshRate = keyValueStorage.get(KEY_REFRESH_RATE)
        if (refreshRate.isNullOrEmpty()) {
            return textsRepository.refreshRate5SecondsString()
        }
        return refreshRate!!
    }

    fun setRefreshRate(value: String) {
        keyValueStorage.set(KEY_REFRESH_RATE, value)
    }

    override fun getRefreshRateAsInt(): Int {
        val refreshRate = getRefreshRate()
        if (refreshRate == textsRepository.refreshRate5SecondsString()) {
            return 5
        } else if (refreshRate == textsRepository.refreshRate30SecondsString()) {
            return 30
        } else if (refreshRate == textsRepository.refreshRate60SecondsString()) {
            return 60
        } else if (refreshRate == textsRepository.refreshRate5MinutesString()) {
            return 60 * 5
        }
        return 0
    }

    fun isManualRefreshEnabled(): Boolean {
        return getRefreshRate().equals(textsRepository.refreshRateOffString())
    }


    fun getAreDefaultSymbolsInserted(): Boolean {
        return keyValueStorage.get(KEY_DEFAULT_SYMBOLS_INSERTED)?.toBoolean() ?: false
    }

    fun setAreDefaultSymbolsInserted(value: Boolean) {
        keyValueStorage.set(KEY_DEFAULT_SYMBOLS_INSERTED, value.toString())
    }

    fun getChartStyle(): Int {
        return keyValueStorage.get(KEY_CHART_STYLE)?.toIntOrNull() ?: ChartStyle.BAR
    }

    fun setChartStyle(value: Int) {
        keyValueStorage.set(KEY_CHART_STYLE, value.toString())
    }

    fun getChartInterval(): Int {
        return keyValueStorage.get(KEY_CHART_INTERVAL)?.toIntOrNull() ?: ChartInterval.DAY
    }

    fun setChartInterval(value: Int) {
        keyValueStorage.set(KEY_CHART_INTERVAL, value.toString())
    }

    fun getIsDefaultNewsInserted(): Boolean {
        return keyValueStorage.get(KEY_DEFAULT_NEWS_INSERTED)?.toBoolean() ?: false
    }

    fun setIsDefaultNewsInserted(value: Boolean) {
        keyValueStorage.set(KEY_DEFAULT_NEWS_INSERTED, value.toString())
    }

    fun clearAll() {
        isUserLogoutForced = false
        shouldUserBeConnected = false
        isAppInBackground = false
        keyValueStorage.clearAll()
    }

    override fun getWorkspace(): Workspace {
        return workspace ?: WorkspaceFactory().emptyWorkspace()
    }

    fun updateAndSaveWorkspace(workspace: Workspace) {
        setWorkspace(workspace)
        workspaceService.saveWorkspace(workspace, object: EmptyResponseListener {
            override fun onResponse() {
                quoteListLiveData.value = workspace
            }
        })
    }


    override fun setWorkspace(workspace: Workspace) {
        this.workspace = workspace
    }

    fun getWorkspaceExpandedState(workspaceId: String): List<Boolean> {
        val json = keyValueStorage.get("$KEY_WORKSPACE_EXPANDED_STATE:$workspaceId")
        if (json.isNullOrEmpty()) {
            return emptyList()
        }
        return gson.fromJson<List<Boolean>>(json!!)
    }

    fun setWorkspaceExpandedState(workspaceId: String, expandedState: List<Boolean>) {
        val json = gson.toJson(expandedState)
        if (json == null) {
            return
        }
        keyValueStorage.set("$KEY_WORKSPACE_EXPANDED_STATE:$workspaceId", json)
    }


    override fun getWorkspaceLiveData(): LiveData<Workspace> {
        return quoteListLiveData
    }

    override fun hasDefaultWorkspace(): Boolean {
        return quoteListLiveData.value != null
    }

    override fun clearCachedWorkspaceData() {
        quoteListLiveData.value = null
    }

    override fun getDidMigrateGoogleQuoteList(): Boolean {
        return keyValueStorage.get(KEY_DID_MIGRATE_GOOGLE_QUOTE_LIST)?.toBoolean() ?: false
    }

    override fun setDidMigrateGoogleQuoteList(didMigrate: Boolean) {
        keyValueStorage.set(KEY_DID_MIGRATE_GOOGLE_QUOTE_LIST, didMigrate.toString())
    }
}