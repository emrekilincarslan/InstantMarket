package com.fondova.finance.sync;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.fondova.finance.App;
import com.fondova.finance.AppExecutors;
import com.fondova.finance.config.AppConfig;
import com.fondova.finance.persistance.AppStorage;
import com.fondova.finance.repo.NewsRepository;
import com.fondova.finance.repo.QuotesRepository;
import com.fondova.finance.repo.ValuesRepository;
import com.fondova.finance.vo.NewsCategory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class SyncManager implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "SyncManager";
    private static final String FILE_NAME = "FinanceX_Data.txt";
    public static final String QUOTES = "quotes";
    public static final String NEWS = "news_categories";

    private QuotesRepository quotesRepository;
    private ValuesRepository valuesRepository;
    private NewsRepository newsRepository;
    private AppExecutors appExecutors;
    private Context context;
    private Gson gson;
    private GoogleDriveListener listener;
    private GoogleApiClient googleApiClient;
    private List<QuoteSyncItem> cloudQuotes;
    private List<NewsCategorySyncItem> cloudNewsCategories;
    private DriveId driveId;
    private AppStorage appStorage;
    private AppConfig appConfig;

    private boolean connectingToDrive = false;

    @Inject
    public SyncManager(QuotesRepository quotesRepository,
                       ValuesRepository valuesRepository,
                       NewsRepository newsRepository,
                       AppExecutors appExecutors,
                       App context,
                       AppStorage appStorage,
                       @Named("gsonSyncDeserializer") Gson gson,
                       AppConfig appConfig) {
        this.quotesRepository = quotesRepository;
        this.valuesRepository = valuesRepository;
        this.newsRepository = newsRepository;
        this.appExecutors = appExecutors;
        this.context = context;
        this.gson = gson;
        this.appStorage = appStorage;
        this.appConfig = appConfig;
    }

    // ---------------------------------------------------------------------------------------------
    // Public
    // ---------------------------------------------------------------------------------------------
    public boolean isConnectingToDrive() {
        return connectingToDrive;
    }

    public void clearListener() {
        this.listener = null;
    }

    public void setListener(GoogleDriveListener listener) {
        this.listener = listener;
    }

    public void uploadLocalData() {
        if (!appConfig.useGoogleDrive() || !appStorage.getSyncWithDrive()){
            return;
        }

        Log.d(TAG, "upload local quotes to drive");
        appExecutors.syncThread().execute(() -> {
            // first delete the old file
            DriveId driveId = getDriveId();
            Log.d(TAG, "found DriveId: " + driveId);
            if (driveId != null) {
                DriveFile oldFile = driveId.asDriveFile();
                Status deleteStatus = oldFile.delete(googleApiClient).await();

                if (!deleteStatus.isSuccess()) {
                    Log.e(TAG, "Unable to delete app data.");
                }
            }

            // then upload new one
            DriveApi.DriveContentsResult result = Drive.DriveApi.newDriveContents(
                    googleApiClient).await();

            if (!result.getStatus().isSuccess()) {
                Log.d(TAG, "onResult: Error while trying to create new file contents");
                return;
            }
            final DriveContents driveContents = result.getDriveContents();

            // write content to DriveContents
            OutputStream outputStream = driveContents.getOutputStream();
            Writer writer = new OutputStreamWriter(outputStream);
            try {
                writer.write(getLocalDataAsString());
                writer.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }

            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(FILE_NAME)
                    .setMimeType("text/plain")
                    .build();

            // create a file in the app folder
            DriveFolder.DriveFileResult fileUpload = Drive.DriveApi.getRootFolder(googleApiClient)
                    .createFile(googleApiClient, changeSet, driveContents).await();

            if (!fileUpload.getStatus().isSuccess()) {
                Log.d(TAG, "Error while trying to create the file");
                return;
            }

            Log.d(TAG, "Created a file with content: "
                    + fileUpload.getDriveFile().getDriveId().toString());

            this.driveId = fileUpload.getDriveFile().getDriveId();
        });
    }

    // ---------------------------------------------------------------------------------------------
    // Private
    // ---------------------------------------------------------------------------------------------

    private void readDataFromDrive() {
        appExecutors.syncThread().execute(() -> {

            Status requestSync = Drive.DriveApi.requestSync(googleApiClient).await();
            Log.d(TAG, "requestSync: " + requestSync.isSuccess());

            if (!requestSync.isSuccess()) {
                if (listener != null) {
                    Log.d(TAG, "using cached data. Treat it like there is no difference");
                    listener.noDifference();
                    return;
                }
            }


            DriveId driveId = getDriveId();
            Log.d(TAG, "found DriveId: " + driveId);
            if (driveId == null) {
                checkIfDataIsTheSameAsLocal(null);
                return;
            }

            DriveFile file = driveId.asDriveFile();
            DriveApi.DriveContentsResult result = file.open(googleApiClient,
                    DriveFile.MODE_READ_ONLY, null).await();

            if (!result.getStatus().isSuccess()) {
                // display an error saying file can't be opened
                return;
            }

            DriveContents contents = result.getDriveContents();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(contents.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            String cloudData = builder.toString();
            Log.d(TAG, "cloud data: " + cloudData);
            checkIfDataIsTheSameAsLocal(cloudData);
        });
    }

    private DriveId getDriveId() {
        if (driveId != null) {
            return driveId;
        }

        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, FILE_NAME))
                .addFilter(Filters.eq(SearchableField.TRASHED, false))
                .build();

        DriveApi.MetadataBufferResult result = Drive.DriveApi.query(googleApiClient, query).await();

        if (!result.getStatus().isSuccess()) {
            Log.d(TAG, "Problem while retrieving files");
        }

        MetadataBuffer metadataBuffer = result.getMetadataBuffer();

        int count = metadataBuffer.getCount();
        Log.d(TAG, "found files count: " + count);

        if (count > 0) {
            for (int i = 0; i < count; i++) {
                Metadata metadata = metadataBuffer.get(i);

                Log.d(TAG, metadata.getTitle());

                if (metadata.getTitle().contains(FILE_NAME)) {
                    driveId = metadata.getDriveId();

                    return driveId;
                }
            }
        }

        return null;
    }

    private void checkIfDataIsTheSameAsLocal(String cloudData) {
        cloudQuotes = new ArrayList<>();
        cloudNewsCategories = new ArrayList<>();

        List<QuoteSyncItem> localQuotes = quotesRepository.getAllQuotes();
        List<NewsCategory> localNewsCategories = newsRepository.getNewsCategories();

        if (cloudData == null && localQuotes.isEmpty() && localNewsCategories.isEmpty()) {
            // no local or cloud data. Insert default
            if (listener != null) {
                Log.d(TAG, "noCloudOrLocalData no cloud or local data");
                listener.noCloudOrLocalData();
                return;
            }
        }

        if (cloudData == null && (!localQuotes.isEmpty() || !localNewsCategories.isEmpty())) {
            appExecutors.mainThread().execute(() -> {
                if (listener != null) {
                    Log.d(TAG, "hasDifference no cloud data");
                    listener.hasDifference();
                }
            });
            return;
        }

        HashMap<String, Object> cloud = convertCloudDataToHashmap(cloudData);

        cloudQuotes = (List<QuoteSyncItem>) cloud.get(QUOTES);
        cloudNewsCategories = (List<NewsCategorySyncItem>) cloud.get(NEWS);

        if (cloudQuotes.isEmpty() && localQuotes.isEmpty() && localNewsCategories.isEmpty()
                && cloudNewsCategories.isEmpty()) {

            // no local or cloud data. Insert default
            if (listener != null) {
                Log.d(TAG, "noCloudOrLocalData");
                listener.noCloudOrLocalData();
                return;
            }
        }

        if (cloudData != null && localQuotes.isEmpty() && localNewsCategories.isEmpty()) {
            if (!appStorage.getAreDefaultSymbolsInserted()) {
                Log.d(TAG, "no local data, has cloud data and first time login");
                appExecutors.mainThread().execute(
                        () -> listener.hasCloudDataAndNoLocalAndFirstTimeLogin());
                return;
            }
        }

        if (cloudData.contentEquals(getLocalDataAsString())) {
            if (listener != null) {
                Log.d(TAG, "noDifference between cloud and local data");
                listener.noDifference();
            }
        } else {
            appExecutors.mainThread().execute(() -> {
                if (listener != null) {
                    Log.d(TAG, "hasDifference between cloud and local");
                    listener.hasDifference();
                }
            });
        }
    }

    private String getLocalDataAsString() {
        List<QuoteSyncItem> localQuotes = quotesRepository.getAllQuotes();
        List<NewsCategorySyncItem> localNewsCategories = convertNewsCategoryToNewsCategorySyncItem(
                newsRepository.getNewsCategories());

        Map<String, Object> data = new HashMap<>();

        data.put(QUOTES, localQuotes);
        data.put(NEWS, localNewsCategories);

        String json = gson.toJson(data);
        Log.d(TAG, "getLocalDataAsString: " + json);
        return json;
    }

    private HashMap<String, Object> convertCloudDataToHashmap(String cloudData) {
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        HashMap<String, Object> stringObjectHashMap = gson.fromJson(cloudData, type);
        Log.d(TAG, "convertCloudDataToHashmap: " + stringObjectHashMap);
        return stringObjectHashMap;
    }

    private List<NewsCategorySyncItem> convertNewsCategoryToNewsCategorySyncItem(
            List<NewsCategory> categories) {
        List<NewsCategorySyncItem> categorySyncItems = new ArrayList<>(categories.size());
        for (NewsCategory category : categories) {
            categorySyncItems.add(
                    new NewsCategorySyncItem(category.name, category.query, category.keywords,
                            category.order, category.isQuoteRelated));
        }

        return categorySyncItems;
    }

    // ---------------------------------------------------------------------------------------------
    // Drive Api callbacks
    // ---------------------------------------------------------------------------------------------
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: ");
        connectingToDrive = false;

        driveId = null;
        readDataFromDrive();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.d(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            listener.showDriveApiError(result);
            return;
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization
        // dialog is displayed to the user.if(dri)
        listener.startResolutionForResult(result);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "GoogleApiClient connection suspended");
    }


    // ---------------------------------------------------------------------------------------------
// Sync callback interfaces
// ---------------------------------------------------------------------------------------------
    public interface GoogleDriveListener {
        void hasDifference();

        void noDifference();

        void noCloudOrLocalData();

        void hasCloudDataAndNoLocalAndFirstTimeLogin();

        void showDriveApiError(ConnectionResult result);

        void startResolutionForResult(ConnectionResult result);
    }

    public interface GoogleDriveDownloadListener {
        void onDone();
    }
}
