package com.fondova.finance.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import com.fondova.finance.App;
import com.fondova.finance.BuildConfig;
import com.fondova.finance.FlavorConstants;
import com.fondova.finance.R;
import com.fondova.finance.api.socket.ApiService;
import com.fondova.finance.config.AppConfig;
import com.fondova.finance.enums.QuoteType;
import com.fondova.finance.persistance.AppStorageInterface;
import com.fondova.finance.repo.NewsRepository;
import com.fondova.finance.repo.QuotesRepository;
import com.fondova.finance.repo.TextsRepository;
import com.fondova.finance.sync.QuoteSyncItem;
import com.fondova.finance.vo.NewsCategory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EmailSupportHelper {

    private TextsRepository textsRepository;
    private NewsRepository newsRepository;
    private ApiService apiService;
    private NewsCategoryQueryBuilder newsCategoryQueryBuilder;
    private App app;
    private AppStorageInterface appStorage;
    private AppConfig appConfig;

    private static final String DEBUG_FILENAME = "_DebugLog.txt";
    private static final String DIAGNOSTICS_FILENAME = "_Android.txt";
    @Inject
    QuotesRepository quotesRepository;

    @Inject
    public EmailSupportHelper(App app,
                              AppConfig appConfig,
                              TextsRepository textsRepository,
                              NewsRepository newsRepository,
                              ApiService apiService,
                              AppStorageInterface appStorage,
                              NewsCategoryQueryBuilder newsCategoryQueryBuilder) {
        this.app = app;
        this.appConfig = appConfig;
        this.textsRepository = textsRepository;
        this.newsRepository = newsRepository;
        this.apiService = apiService;
        this.newsCategoryQueryBuilder = newsCategoryQueryBuilder;
        this.appStorage = appStorage;
    }

    public Intent createIntent() {
        Context context = app.getApplicationContext();
        String authority = BuildConfig.APPLICATION_ID + ".provider";

        String username = appStorage.getCredentials().username;

        String mailTo = textsRepository.getString(R.string.email_address);
        String subject = textsRepository.supportSubject(username);
        String body = textsRepository.supportBody(UUID.randomUUID().toString());
        String title = textsRepository.getString(R.string.email_financex_support);
        ArrayList<Uri> attachments = new ArrayList<>();

        File debugLogFile = createDebugLogFile();
        File diagnosticsDataLogFile = createDiagnosticsDataLog();

        if (debugLogFile != null) {
            attachments.add(FileProvider.getUriForFile(context, authority, debugLogFile));
        }

        if (diagnosticsDataLogFile != null) {
            attachments.add(FileProvider.getUriForFile(context, authority, diagnosticsDataLogFile));
        }

        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("text/plain");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mailTo});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachments);

        return Intent.createChooser(intent, title);
    }

    public boolean canSendEmail() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/html");
        final PackageManager packageManager = app.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, 0);

        return list.size() != 0;
    }

    private String createQuoteListInfo() {
        String body = getLogDivider("Quotes");
        body += "InfoQuote Data:\nRequestedSymbol, DisplaySymbol, FullyQualifiedSymbol\n";

        List<QuoteSyncItem> quotes = quotesRepository.getAllQuotes();

        if (quotes == null) {
            body += "<uninitialized>\n\n";
            return body;
        }

        for (QuoteSyncItem quote : quotes) {

            String symbol = quote.requestName;
            String displayedSymbol = quote.displayName;

            String fullyQualifiedSymbol = "";
            if (quote.type == QuoteType.LABEL) {
                fullyQualifiedSymbol = "--:--";
            }


            body += symbol + ", " + displayedSymbol + ", " + fullyQualifiedSymbol + "\n";
        }
        body += "\n\n";

        return body;
    }

    private File createDiagnosticsDataLog() {
        String body = "";

        body += createQuoteListInfo();
        if (appConfig.showNewsTab()) {
            body += createNewsSearchInfo();
        }
        body += createDiagnosticsInfo();

        String fileName = String.format("%s%s", textsRepository.getString(R.string.app_name), DIAGNOSTICS_FILENAME);
        return createFile(fileName, body);
    }

    private String createDiagnosticsInfo() {

        String body = getLogDivider("Diagnostics");
        ActivityManager actManager = (ActivityManager) app.getSystemService(
                Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        long totalMemory = memInfo.totalMem;

        long usedSize = 0;

        try {
            Runtime info = Runtime.getRuntime();
            usedSize = info.totalMemory() - info.freeMemory();
        } catch (Exception e) {
            e.printStackTrace();
        }

        long millisSinceBoot = SystemClock.elapsedRealtime();

        long days = TimeUnit.MILLISECONDS.toDays(millisSinceBoot);
        millisSinceBoot -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millisSinceBoot);
        millisSinceBoot -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millisSinceBoot);
        millisSinceBoot -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millisSinceBoot);

        String uptime = days + " Days, " + hours + " Hours, " + minutes + " minutes, " + seconds
                + " seconds";

        String username = appStorage.getCredentials().username;

        // start device info data
        body += "\ngit commit hash, " + BuildConfig.GIT_COMMIT_HASH;
        body += "\nbundle_identifier, " + BuildConfig.APPLICATION_ID;
        body += "\nbundle_version, " + BuildConfig.VERSION_CODE;
        body += "\nbundle_short_version, " + BuildConfig.VERSION_NAME;
        body += "\nshinobi_charts_info, " + com.shinobicontrols.charts.BuildConfig.INFO;
        body += "\ncurrent_username, " + username;
        body += "\nserver_connected, " + !apiService.isDisconnected();
        body += "\nquote_server_address, " + FlavorConstants.DTN_BASE_API;
        body += "\ndevice_system_version, " + Build.VERSION.RELEASE;
        body += "\nphysical_memory, " + humanReadableByteCount(totalMemory, true);
        body += "\napp_memory_used, " + humanReadableByteCount(usedSize, true);
        body += "\nsystem_uptime, " + uptime;
        body += "\ndevice_model, " + Build.MANUFACTURER;
        body += "\ndevice_name, " + Build.MODEL;
        body += "\n\n";
        return body;
    }

    private String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    private File createDebugLogFile() {
        if (!canWriteAndRead()) return null;

        String path = app.getExternalFilesDir(null).getPath() + File.separator;
        String fileName = String.format("%s%s", textsRepository.getString(R.string.app_name), DEBUG_FILENAME);
        File file = new File(path, fileName);

        try {
            int pid = android.os.Process.myPid();
            String[] command = {"logcat", "-t", "90000", "*:I"};
            Process process = Runtime.getRuntime().exec(command);

            InputStream in = process.getInputStream();
            FileOutputStream out = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    private String getLogDivider(String title) {
        String output = "\n";
        output += "=========================================================================\n";
        output += "                        " + title + "\n";
        output += "=========================================================================\n";
        output += "\n";
        return output;
    }

    private String createNewsSearchInfo() {
        String body = getLogDivider("News Categories");
        body += "Name, Query, Keywords\n";
        List<NewsCategory> categories = newsRepository.getNewsCategories();

        if (categories == null) {
            body += "<uninitialized>\n\n";
            return body;
        }
        for (NewsCategory newsCategory : newsRepository.getNewsCategories()) {
            String name = newsCategory.name;
            String query = newsCategory.query;
            List<String> keywordsList = newsCategoryQueryBuilder.decompileQuery(query);
            String keywords = TextUtils.join(", ", keywordsList);

            body += name + ", " + query + ", " + keywords + "\n";
        }
        body += "\n\n";

        return body;
    }

    private File createFile(String filename, String body) {
        if (!canWriteAndRead()) return null;

        String path = app.getExternalFilesDir(null).getPath() + File.separator;
        File file = new File(path, filename);

        try {
            FileWriter writer = new FileWriter(file);
            writer.write(body);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    private boolean canWriteAndRead() {
        return isExternalStorageReadable() && isExternalStorageWritable();
    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }
}
