package com.fondova.finance.api.session

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import com.fondova.finance.AppExecutors
import com.fondova.finance.api.Resource
import com.fondova.finance.api.Status
import com.fondova.finance.api.auth.AuthService
import com.fondova.finance.api.auth.AuthenticationResponseListener
import com.fondova.finance.api.model.Credentials
import com.fondova.finance.api.model.login.LoginResponse
import com.fondova.finance.api.quote.QuoteService
import com.fondova.finance.api.socket.WebsocketService
import com.fondova.finance.api.socket.WebsocketServiceListener
import com.fondova.finance.config.AppConfig
import com.fondova.finance.db.NewsDao
import com.fondova.finance.db.QuoteDao
import com.fondova.finance.news.service.*
import com.fondova.finance.workspace.Workspace
import com.fondova.finance.workspace.WorkspaceFactory
import com.fondova.finance.workspace.WorkspaceQuoteType
import com.fondova.finance.persistance.AppStorageInterface
import com.fondova.finance.repo.DefaultNewsRepository
import com.fondova.finance.repo.DefaultSymbolsRepository
import com.fondova.finance.util.DelayedTaskRunner
import com.fondova.finance.vo.NewsCategory
import com.fondova.finance.workspace.service.*
import java.io.IOException

class WebsocketSessionService(val websocketService: WebsocketService,
                              val authService: AuthService,
                              val networkConnectivityService: NetworkConnectivityService,
                              val workspaceService: WorkspaceService,
                              val newsListService: NewsListService,
                              val quoteService: QuoteService,
                              val defaultSymbolsRepository: DefaultSymbolsRepository,
                              val appExecutors: AppExecutors,
                              val appConfig: AppConfig,
                              val quoteDao: QuoteDao,
                              val legacyNewsDao: NewsDao,
                              val appStorage: AppStorageInterface): SessionService, WebsocketServiceListener, AuthenticationResponseListener, ConnectivityListener {

    private var hasWifi = networkConnectivityService.hasWifi()
    private var hasCellular = networkConnectivityService.hasCellular()
    private var sessionStatusLiveData = MutableLiveData<SessionStatus>()
    private var isAuthenticating = false
    private val tag = "WebsocketSessionService"
    private var retryCount = 0
    private val maxRetries = 3
    private var listener: AuthenticationResponseListener? = null
    private val disconnectTimeout = 30
    private val reconnectTimer: DelayedTaskRunner = DelayedTaskRunner(10)
    private val autoLogoutTimer: DelayedTaskRunner = DelayedTaskRunner(disconnectTimeout)
    private var activityCount = 0
    private var credentials: Credentials? = null
    private var workspaceSelector: WorkspaceSelector = object : WorkspaceSelector {
        override fun selectWorkspace(legacy: Workspace?, current: Workspace?, listener: DidSelectWorkspaceListener) {
            val workspace = legacy ?: defaultSymbolsRepository.createDefaultWorkspace()
            listener.didSelectWorkspace(workspace)
        }
    }
    private var context: Context? = null
    private var sessionStatus: SessionStatus = SessionStatus.disconnected
        set(value) {
            field = value
            appExecutors.mainThread().execute {
                Log.i(tag, "Setting Session Status: $value")
                sessionStatusLiveData.value = value
            }
        }

    init {
        networkConnectivityService.registerConnectivityChangeListener(this)
        websocketService.addListener(this)
    }

    override fun setWorkspaceSelector(context: Context, workspaceSelector: WorkspaceSelector) {
        this.workspaceSelector = workspaceSelector
        this.context = context
    }

    override fun getSessionStatusLiveData(): LiveData<SessionStatus> {
        return sessionStatusLiveData
    }

    override fun authenticate(credentials: Credentials, listener: AuthenticationResponseListener?) {
        this.credentials = credentials
        this.listener = listener
        val lowercasedUsername = credentials.username.toLowerCase()
        isAuthenticating = true
        Log.i(tag, "Setting status to user logging in")
        sessionStatus = SessionStatus.userLoggingIn
        Log.i(tag, "Authenticating $lowercasedUsername")
        if (websocketService.isConnected()) {
            Log.e(tag, "Login error, setting status to login failure")
            sessionStatus = SessionStatus.loginFailure
            websocketService.disconnect()
            val errorMessage = "Unable to establish connection, please check your internet connection and try again."
            val errorTitle = "Login Failure"
            listener?.onAuthenticationResponse(Resource.error(errorTitle, errorMessage))
            return
        }
        authService.authenticate(credentials, this)

    }

    override fun reconnect(listener: AuthenticationResponseListener?) {
        reconnect(true, listener)
    }

    override fun logout(userInitiated: Boolean) {
        Log.i(tag, "Logging Out")
        if (userInitiated) {
            Log.i(tag, "User initiated, setting status to user logged out")
            sessionStatus = SessionStatus.userLoggedOut
        } else {
            Log.i(tag, "Not user initiated, setting status to logged out")
            sessionStatus = SessionStatus.loggedOut
        }
        websocketService.disconnect()
    }

    override fun handleMessage(message: String): Boolean {
        return false // This does not handle messages directly
    }

    override fun onConnected(websocketService: WebsocketService) {
        reconnectTimer.cancel()
        Log.i(tag, "onConnected, setting status to user connected")
        sessionStatus = SessionStatus.connected
    }

    override fun onDisconnected(websocketService: WebsocketService, code: Int, reason: String, closedByServer: Boolean) {
        Log.i(tag, "Websocket Disconnected(reason: $reason, closedByServer: $closedByServer)")
        Log.i(tag, "Session Status: $sessionStatus")

        if (sessionStatus == SessionStatus.seatbump) {
            Log.i(tag, "seatbumped")
            return
        } else if (code == 4000) {
            Log.i(tag, "was code 4000 setting staus to login Failure")
            sessionStatus = SessionStatus.loginFailure
        } else if (sessionStatus == SessionStatus.connected) {
            Log.i(tag, "Was connected, setting status to connection lost")
            sessionStatus = SessionStatus.connectionLost
        } else if (sessionStatus == SessionStatus.userLoggingIn) {
            Log.i(tag, "Was logging in, setting status to login failure")
            sessionStatus = SessionStatus.loginFailure
        }

        if (sessionStatus.shouldReconnect() != true) {
            return
        }

        if (retryCount < maxRetries) {
            retryCount += 1
            Log.i(tag, "Disconnected, NOT user initiated. Will try to reconnect")

            reconnectTimer.run {
                Log.i(tag, "Trying reconnect count: $retryCount")
                reconnect(null)

            }
        } else {
            Log.e(tag, "Max reconnect retries - disconnecting without retry")
            // show error
            reconnectTimer.cancel()
            Log.i(tag, "setting status to user logged out")

            sessionStatus = SessionStatus.userLoggedOut
            retryCount = 0
        }
    }

    override fun onSocketError(websocketService: WebsocketService, exception: IOException) {
        if (exception.message?.toLowerCase()?.contains("broken pipe") == true
                || exception.message?.toLowerCase()?.contains("connection reset by peer") == true) {
            Log.e(tag, "Socket error, server pipe broken, setting status to connection lost")
            sessionStatus = SessionStatus.connectionLost
            websocketService.disconnect()
        }
    }

    override fun onErrorMessage(message: String) {
        // Don't care
    }


    private fun handleLoginErrors(reason: String?) {
        if (reason == "Not Ready") {
            Log.e(tag, "api not ready for responses, setting status to connection lost")

            sessionStatus = SessionStatus.connectionLost

            websocketService.disconnect()
            return
        }

        if (reason == "Forced Logout") {
            Log.i(tag, "forced logout, setting status to seatbump")
            sessionStatus = SessionStatus.seatbump
        } else {
            Log.e(tag, "unknown login error, setting status to disconnected")
            sessionStatus = SessionStatus.disconnected
        }
        websocketService.disconnect()
    }

    override fun onAuthenticationResponse(response: Resource<LoginResponse>) {
        if (response.status == Status.ERROR) {
            handleLoginErrors(response.title)
            listener?.onAuthenticationResponse(response)
            return
        }
        Log.i(tag, "Authentication complete, setting status to connected")
        sessionStatus = SessionStatus.connected

        val cachedWorkspace = appStorage.getWorkspaceLiveData().value
        if (cachedWorkspace != null) {
            watchQuotes(cachedWorkspace)
            listener?.onAuthenticationResponse(response)
            return
        }

        if (workspaceService is StockCloudWorkspaceService) {
            workspaceService.username = credentials?.username?.toLowerCase() ?: ""
            workspaceService.password = credentials?.password ?: ""
        }
        if (newsListService is StockCloudNewsListService) {
            newsListService.username = credentials?.username?.toLowerCase() ?: ""
            newsListService.password = credentials?.password ?: ""
        }
        var workingNewsListService: NewsListService = newsListService
        val workingContext = context
        if (workingContext != null && appConfig.useGoogleDrive()) {
            val legacyNewsListService = LegacyNewsListService(legacyNewsDao, appExecutors)
            workingNewsListService = AggregateNewsListService(appStorage, legacyNewsListService, newsListService)
        }
        workingNewsListService.fetchNewsList(object : OnNewsListResponseListener {
            override fun onNewsListResponse(news: List<NewsCategory>?) {
                if (news == null) {
                    val defaultNews = DefaultNewsRepository().createDefaultNewsCategories()
                    workingNewsListService.saveNewsList(defaultNews)
                }
                workspaceService.fetchDefaultWorkspace(object : OnDefaultWorkspaceReceivedListener {
                    override fun onWorkspaceDataReceived(workspaces: List<Workspace>, default: Workspace?, error: String?) {

                        if (error != null) {
                            listener?.onAuthenticationResponse(Resource.error<LoginResponse>("REST Error", "Unable to fetch Workspace"))
                        } else {
                            defaultWorkspaceReceived(workspaces, default, response)
                        }
                    }
                })
            }
        })

    }

    fun defaultWorkspaceReceived(workspaces: List<Workspace>, default: Workspace?, response: Resource<LoginResponse>) {
        var currentContext = context
        var stockWorkspace = default
        if (currentContext != null) {
            fetchLegacyWorkspace(stockWorkspace, response)
        } else {
            saveAndWatchQuotes(workspaces, default ?: WorkspaceFactory().emptyWorkspace())
            listener?.onAuthenticationResponse(response)
        }
    }

    private fun fetchLegacyWorkspace(stockWorkspace: Workspace?, response: Resource<LoginResponse>) {
        val legacyWorkspaceService = LegacyWorkspaceService(quoteDao, defaultSymbolsRepository)
        legacyWorkspaceService.fetchDefaultWorkspace(object : OnDefaultWorkspaceReceivedListener {
            override fun onWorkspaceDataReceived(workspaces: List<Workspace>, default: Workspace?, error: String?) {
                if (stockWorkspace != null && default != null) {
                    workspaceSelector.selectWorkspace(default, stockWorkspace, object : DidSelectWorkspaceListener {
                        override fun didSelectWorkspace(workspace: Workspace) {
                            saveAndWatchQuotes(listOf(workspace), workspace)
                            workspaceService.saveWorkspace(workspace, object : EmptyResponseListener {
                                override fun onResponse() {
                                    appStorage.setDidMigrateGoogleQuoteList(true)
                                    listener?.onAuthenticationResponse(response)
                                }
                            })
                        }
                    })
                    return
                }

                if (default != null) {
                    saveAndWatchQuotes(listOf(default), default)
                    workspaceService.saveWorkspace(default, object : EmptyResponseListener {
                        override fun onResponse() {
                            appStorage.setDidMigrateGoogleQuoteList(true)
                            listener?.onAuthenticationResponse(response)
                        }
                    })
                    return
                }

                val defaultWorkspace = defaultSymbolsRepository.createDefaultWorkspace()
                saveAndWatchQuotes(listOf(defaultWorkspace), defaultWorkspace)
                listener?.onAuthenticationResponse(response)
            }
        })
    }

    fun saveAndWatchQuotes(workspaces: List<Workspace>, default: Workspace) {
        appStorage.setWorkspaceList(workspaces)
        appStorage.setWorkspace(default)
        watchQuotes(default)

    }

    fun watchQuotes(default: Workspace) {
        for (group in default.getGroups()) {
            for (quote in group.getListOfQuotes()) {
                val symbol = quote.getValue() ?: ""
                val isExpression = quote.getType()?.toLowerCase() == WorkspaceQuoteType.EXPRESSION
                if (!symbol.isEmpty()) {
                    quoteService.watchQuote(symbol, isExpression)
                }
            }
        }
    }


    override fun appWillEnterBackground() {
        if (websocketService.isConnected()) {
            Log.i(tag, "Entering background, setting status to disconnected No Retry")
            sessionStatus = SessionStatus.disconnectedNoRetry

            websocketService.disconnect()
        }
    }

    override fun appWillEnterForeground() {
        if (sessionStatus != SessionStatus.connected && sessionStatus != SessionStatus.userLoggedOut) {
            reconnect(null)
        }
    }

    private fun reconnect(userInitiated: Boolean, listener: AuthenticationResponseListener?) {

        if (credentials == null) {
            return
        }

        if (userInitiated) {
            Log.i(tag, "disconnect was user initiated, setting status to logged out")
            sessionStatus = SessionStatus.loggedOut

            if (listener != null && listener != this) {
                this.listener = listener
            }

        }

        if (!sessionStatus.shouldReconnect()) {
            Log.i(tag, "Do not reconnect, sessionStatus: $sessionStatus")
            return
        }
        Log.i(tag, "Reconnecting, sessionStatus: $sessionStatus")
        sessionStatus = SessionStatus.connecting
        authService.reconnect(this)

    }

    override fun incrementActivityCount() {
        Log.i(tag, "Activity appears, cancelling any running auto logout timers")
        autoLogoutTimer.cancel()
        activityCount += 1
        if (activityCount > 0 && !websocketService.isConnected() && sessionStatus != SessionStatus.seatbump && sessionStatus != SessionStatus.userLoggedOut && sessionStatus != SessionStatus.connecting) {
            Log.i(tag, "Auto-Reconnect....")
            reconnect(true, null)
        }
        Log.i(tag, "Activity Count Incrmented: $activityCount")
    }

    override fun decrementActivityCount() {
        activityCount -= 1
        Log.i(tag, "Activity Count Decrmented: $activityCount")
        if (activityCount < 1 && websocketService.isConnected()) {
            Log.i(tag, "No activities running, starting disconnect timer for $disconnectTimeout seconds")
            autoLogoutTimer.run {
                Log.i(tag, "Disconnecting websocket, setting status to disconnected")
                sessionStatus = SessionStatus.disconnectedNoRetry

                websocketService.disconnect()
            }
        }
    }

    override fun isAppVisible(): Boolean {
        return activityCount > 0
    }


    override fun onConnectivityChanged(isConnectedToWifi: Boolean, isConnectedToCellular: Boolean) {
        Log.i(tag, "isConnectedToWifi: $isConnectedToWifi")
        Log.i(tag, "isConnectedToCell: $isConnectedToCellular")
        val connectionLost = (hasWifi && !isConnectedToWifi) || (hasCellular && !isConnectedToCellular)
        hasWifi = isConnectedToWifi
        hasCellular = isConnectedToCellular
        if (sessionStatus == SessionStatus.connected && connectionLost) {
            Log.d(tag, "Setting status to connection lost")
            sessionStatus = SessionStatus.connectionLost
            quoteService.reset()
        }
        if (networkConnectivityService.isNetworkAvailable() && activityCount > 0) {
            reconnect(false, null)
        }
    }

    override fun clearCache() {
        workspaceService.clearCache()
        newsListService.clearCache()
        appStorage.clearCachedWorkspaceData()
    }

}