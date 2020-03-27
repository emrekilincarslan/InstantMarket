package com.fondova.finance.diagnostics

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.drive.Drive
import com.fondova.finance.App
import com.fondova.finance.R
import com.fondova.finance.api.quote.QuoteService
import com.fondova.finance.db.NewsDao
import com.fondova.finance.db.QuoteDao
import com.fondova.finance.news.service.GoogleNewsListService
import com.fondova.finance.news.service.NewsListService
import com.fondova.finance.news.service.OnNewsListResponseListener
import com.fondova.finance.persistance.AppStorage
import com.fondova.finance.ui.SessionAwareActivity
import com.fondova.finance.vo.NewsCategory
import com.fondova.finance.workspace.Workspace
import com.fondova.finance.workspace.WorkspaceGroup
import com.fondova.finance.workspace.WorkspaceQuoteType
import com.fondova.finance.workspace.service.EmptyResponseListener
import com.fondova.finance.workspace.service.GoogleWorkspaceService
import com.fondova.finance.workspace.service.OnDefaultWorkspaceReceivedListener
import com.fondova.finance.workspace.service.WorkspaceService
import kotlinx.android.synthetic.main.activity_diagnostics.*
import javax.inject.Inject

class DiagnosticsActivity: SessionAwareActivity() {

    @Inject
    lateinit var newsListService: NewsListService

    @Inject
    lateinit var workspaceService: WorkspaceService

    @Inject
    lateinit var legacyQuoteDao: QuoteDao

    @Inject
    lateinit var legacyNewsDao: NewsDao

    @Inject
    lateinit var appStorage: AppStorage

    @Inject
    lateinit var quoteService: QuoteService

    private val GOOGLE_REQUEST_CODE = 10001

    private var quoteDownloadPending = false
    private var newsDownloadPending = false

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, DiagnosticsActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.ChartTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diagnostics)
        setActionBar(header_view)
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)

        App.getAppComponent().inject(this)


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) {
            return
        }
        if (requestCode == GOOGLE_REQUEST_CODE) {
            getDriveData()
        }
    }

    private fun getDriveData() {
        if (quoteDownloadPending) {
            quoteDownloadPending = false
            val driveWorkspaceService = GoogleWorkspaceService(this)
            driveWorkspaceService.fetchDefaultWorkspace(object : OnDefaultWorkspaceReceivedListener {
                override fun onWorkspaceDataReceived(workspaces: List<Workspace>, default: Workspace?, error: String?) {
                    compareQuotes(default)
                }
            })
        }

        if (newsDownloadPending) {
            newsDownloadPending = false
            val driveNewsListService = GoogleNewsListService(this)
            driveNewsListService.fetchNewsList(object : OnNewsListResponseListener {
                override fun onNewsListResponse(news: List<NewsCategory>?) {
                    compareNews(news)
                }
            })

        }
    }

    private fun compareNews(news: List<NewsCategory>?) {
        if (news == null) {
            showNoDataMessage()
            return
        }
        if (areEqual(news, newsListService.currentNewsList() ?: emptyList())) {
            showDataEqualMessage()
            return
        }

        val items = mutableListOf<DriveItem>(DriveItem("NEWS", true))
        val newsCategories = news ?: emptyList()
        for (category in newsCategories) {
            items.add(DriveItem(category.name, false))
        }
        DrivePreviewDialog(this, items) {
            newsListService.saveNewsList(news)
        }.show()
    }

    private fun showDataEqualMessage() {
        AlertDialog.Builder(this, R.style.ChartDialog)
                .setMessage(R.string.pull_google_drive_is_equal_message)
                .setPositiveButton(R.string.ok, null)
                .create()
                .show()
    }

    private fun showNoDataMessage() {
        AlertDialog.Builder(this, R.style.ChartDialog)
                .setMessage(R.string.google_drive_empty_message)
                .setPositiveButton(R.string.ok, null)
                .create()
                .show()
    }

    private fun compareQuotes(quotes: Workspace?) {
        if (quotes == null) {
            showNoDataMessage()
            return
        }
        if (areEqual(quotes, workspaceService.currentWorkspace())) {
            showDataEqualMessage()
            return
        }

        var items = mutableListOf<DriveItem>()
        val groups = quotes?.getGroups() ?: emptyList<WorkspaceGroup>()
        for (group in groups) {
            items.add(DriveItem(group.getDisplayName() ?: "null", true))
            val quotes = group.getListOfQuotes()
            for (quote in quotes) {
                items.add(DriveItem(quote.getValue() ?: "null", false))
            }
        }
        DrivePreviewDialog(this, items) {
            quoteService.unwatchAll()
            quoteService.reset()
            appStorage.setWorkspace(quotes)
            for (group in groups) {
                for (item in group.getListOfQuotes()) {
                    quoteService.watchQuote(item.getValue() ?: "null", item.getType()?.toLowerCase() == WorkspaceQuoteType.EXPRESSION)
                }
            }
            workspaceService.saveWorkspace(quotes, object : EmptyResponseListener {
                override fun onResponse() {

                }
            })
        }.show()
    }

    fun pullGoogleDriveQuotes(view: View) {
        quoteDownloadPending = true
        showGoogleLogin()
    }

    fun pullGoogleDriveNews(view: View) {
        newsDownloadPending = true
        showGoogleLogin()
    }


    private fun showGoogleLogin() {
        val signInClient = getGoogleSignInClient()
        val intent = signInClient.signInIntent
        startActivityForResult(intent, GOOGLE_REQUEST_CODE)
    }

    private fun getGoogleSignInClient(): GoogleSignInClient {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Drive.SCOPE_FILE)
                .build()
        return GoogleSignIn.getClient(this, options)
    }


    override fun onNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun areEqual(workspace: Workspace?, other: Workspace?): Boolean {
        if ((workspace == null && other != null) || (workspace != null && other == null)) {
            return false
        }
        if (workspace == null && other == null) {
            return true
        }

        val leftGroups = workspace?.getGroups() ?: emptyList<WorkspaceGroup>()
        val rightGroups = other?.getGroups() ?: emptyList<WorkspaceGroup>()
        if (leftGroups.size != rightGroups.size) {
            return false
        }
        for (index in leftGroups.indices) {
            if (leftGroups[index].getDisplayName() != rightGroups[index].getDisplayName()) {
                return false
            }
            val leftQuotes = leftGroups[index].getListOfQuotes()
            val rightQuotes = rightGroups[index].getListOfQuotes()
            if (leftQuotes.size != rightQuotes.size) {
                return false
            }
            for (quoteIndex in leftQuotes.indices) {
                if (leftQuotes[quoteIndex].getValue() != rightQuotes[quoteIndex].getValue()) {
                    return false
                }
            }
        }
        return true
    }

    fun areEqual(news: List<NewsCategory>, other: List<NewsCategory>): Boolean {
        for (index in news.indices) {
            if (news[index].name != other[index].name || news[index].query != other[index].query) {
                return false
            }
        }
        return true
    }

}
