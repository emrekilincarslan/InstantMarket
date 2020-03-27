package com.fondova.finance.workspace.service

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.drive.Drive
import com.google.android.gms.drive.DriveFile
import com.google.android.gms.drive.DriveId
import com.google.android.gms.drive.query.Filters
import com.google.android.gms.drive.query.Query
import com.google.android.gms.drive.query.SearchableField
import com.google.gson.annotations.SerializedName
import com.fondova.finance.sync.NewsCategorySyncItem
import com.fondova.finance.sync.QuoteSyncItem
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class GoogleDriveService private constructor() {

    companion object {
        private var instance: GoogleDriveService? = null

        val shared: GoogleDriveService
            get() {
                if (instance == null) {
                    instance = GoogleDriveService()
                }
                return instance!!
            }

    }

    private val TAG = "GoogleDriveService"
    private val fileName = "FinanceX_Data.txt"

    private var client: GoogleApiClient? = null
    private var data: String? = null


    fun connectToGoogleClient(context: Context, didConnect: (apiClient: GoogleApiClient?, connected: Boolean) -> Unit): GoogleApiClient? {
        if (client == null) {
            client = GoogleApiClient.Builder(context)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                        override fun onConnected(p0: Bundle?) {
                            didConnect(client, true)
                        }

                        override fun onConnectionSuspended(p0: Int) {
                            didConnect(client, false)
                        }
                    })
                    .addOnConnectionFailedListener {
                        didConnect(client, false)
                    }
                    .build()
        }
        if (data != null) {
            didConnect(client, true)
        } else {
            client?.connect()
        }
        return client
    }

    fun getDriveId(apiClient: GoogleApiClient): DriveId? {
        val query = Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, fileName))
                .addFilter(Filters.eq(SearchableField.TRASHED, false))
                .build()
        val result = Drive.DriveApi.query(apiClient, query).await()

        if (!result.status.isSuccess) {
            Log.e(TAG, "Could not retrieve file: ${fileName}")
        }

        val metadataBuffer = result.metadataBuffer
        val count = metadataBuffer.count
        for (metadata in metadataBuffer) {
            if (metadata.title.contains(fileName)) {
                return metadata.driveId
            }
        }
        return null

    }

    fun readFromDrive(apiClient: GoogleApiClient): String? {
        if (data != null) {
            return data
        }
        if (!apiClient.isConnected) {
            return null
        }
        val status = Drive.DriveApi.requestSync(apiClient).await()
        if (!status.isSuccess) {
            Log.d(TAG, "Drive unable to sync")
            return null
        }
        val driveId = getDriveId(apiClient)
        val driveFile = driveId?.asDriveFile()
        val result = driveFile?.open(apiClient, DriveFile.MODE_READ_ONLY, null)?.await()

        if (result == null || !result.status.isSuccess) {
            Log.e(TAG, "Unable to read file")
            return null
        }

        val contents = result.driveContents
        val reader = BufferedReader(InputStreamReader(contents.inputStream))

        var output = ""
        var line: String? = reader.readLine()
        try {
            while (line != null) {
                output += line
                line = reader.readLine()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        data = output
        return output
    }

}

class GoogleDriveData {
    @SerializedName("quotes")
    var quotes: List<QuoteSyncItem>? = null

    @SerializedName("news_categories")
    var news: List<NewsCategorySyncItem>? = null
}