package com.fondova.finance.di.modules;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.fondova.finance.App;
import com.fondova.finance.AppExecutors;
import com.fondova.finance.api.LogHttpClient;
import com.fondova.finance.api.auth.AuthService;
import com.fondova.finance.api.auth.WebsocketAuthService;
import com.fondova.finance.api.chart.ChartService;
import com.fondova.finance.api.chart.WebsocketChartService;
import com.fondova.finance.api.model.category.CategoriesResponse;
import com.fondova.finance.api.model.symbol.RestfulSymbolSearchResponseDeserializer;
import com.fondova.finance.api.model.symbol.SymbolSearchResponse;
import com.fondova.finance.api.model.symbol.SymbolSearchResponseDeserializer;
import com.fondova.finance.api.news.NewsService;
import com.fondova.finance.api.news.WebsocketNewsService;
import com.fondova.finance.api.quote.QuoteService;
import com.fondova.finance.api.quote.WebsocketQuoteService;
import com.fondova.finance.api.restful.StockRetrofit;
import com.fondova.finance.api.session.NetworkConnectivityService;
import com.fondova.finance.api.session.SessionService;
import com.fondova.finance.api.session.WebsocketSessionService;
import com.fondova.finance.api.socket.GzipRequestInterceptor;
import com.fondova.finance.config.AppConfig;
import com.fondova.finance.db.NewsDao;
import com.fondova.finance.db.QuoteDao;
import com.fondova.finance.news.service.StockCloudNewsListService;
import com.fondova.finance.news.service.NewsListService;
import com.fondova.finance.repo.DefaultSymbolsRepository;
import com.fondova.finance.workspace.service.StockCloudWorkspaceService;
import com.fondova.finance.workspace.service.WebsocketWorkspaceService;
import com.fondova.finance.api.socket.NeoWebSocketService;
import com.fondova.finance.api.socket.WebsocketService;
import com.fondova.finance.api.quote.QuoteWatchFieldsFactory;
import com.fondova.finance.workspace.service.WorkspaceService;
import com.fondova.finance.persistance.AppStorage;
import com.fondova.finance.persistance.AppStorageInterface;
import com.fondova.finance.repo.ChartWatchRepository;
import com.fondova.finance.repo.QuoteWatchRepository;
import com.fondova.finance.repo.TextsRepository;
import com.fondova.finance.sync.SyncResponseDeserializer;
import com.fondova.finance.util.DelayedTaskRunner;
import com.fondova.finance.util.InternetConnectivityUtils;
import com.fondova.finance.util.TaskRunner;

import java.lang.reflect.Type;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class NetworkModule {

    private static final String DTN_RESTFUL_URL = "https://ws1.stock.com/";

    @Provides
    @Singleton
    public Cache provideOkHttpCache(App application) {
        int cacheSize = 10 * 1024 * 1024; // 10mb
        return new Cache(application.getCacheDir(), cacheSize);
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(Cache cache) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addInterceptor(new GzipRequestInterceptor())
                .cache(cache)
                .build();
    }

    @Provides
    @Singleton
    public GsonConverterFactory provideGsonConverterFactory(
            @Named("gsonResftulSearchResultDeserializer") Gson gson) {
        return GsonConverterFactory.create(gson);
    }

    @Provides
    @Singleton
    public Gson provideGson() {
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .disableHtmlEscaping()
                .create();
    }

    @Named("gsonResftulSearchResultDeserializer")
    @Provides
    @Singleton
    public Gson provideGsonWithRestfulSearchDeserializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(CategoriesResponse.class, new RestfulSymbolSearchResponseDeserializer());
        return gsonBuilder.create();
    }

    @Named("gsonSearchResultDeserializer")
    @Provides
    @Singleton
    public Gson provideGsonWithSearchResultsDeserializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(SymbolSearchResponse.class, new SymbolSearchResponseDeserializer());
        return gsonBuilder.create();
    }

    @Named("gsonSyncDeserializer")
    @Provides
    @Singleton
    public Gson provideGsonWithSyncDeserializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        gsonBuilder.registerTypeAdapter(type, new SyncResponseDeserializer());
        gsonBuilder.disableHtmlEscaping();
        return gsonBuilder.create();
    }

    @Provides
    @Singleton
    public NetworkConnectivityService provideInternetConnectivityUtils(App context,
                                                                      AppExecutors appExecutors) {
        return new InternetConnectivityUtils(context, appExecutors);
    }

    @Provides
    @Singleton
    public Retrofit.Builder provideRetrofit(GsonConverterFactory gsonConverterFactory,
                                            LogHttpClient httpClient) {
        return new Retrofit.Builder()
                .client(httpClient.get())
                .addConverterFactory(gsonConverterFactory);
    }

    @Provides
    @Singleton
    public StockRetrofit.StockService provideStockService(Retrofit.Builder retrofitBuilder) {
        Retrofit retrofit = retrofitBuilder
                .baseUrl(DTN_RESTFUL_URL)
                .build();

        return retrofit.create(StockRetrofit.StockService.class);
    }

    @Provides
    @Singleton
    public StockRetrofit provideStockRetrofit(StockRetrofit.StockService service) {
        return new StockRetrofit(service);
    }

    @Provides
    @Singleton
    public WorkspaceService provideWorkspaceService(WebsocketService websocketService, AppExecutors appExecutors, AppConfig appConfig) {
        if (appConfig.useStockSettings()) {
            return new WebsocketWorkspaceService(websocketService, appExecutors);
        } else {
            return new StockCloudWorkspaceService(appExecutors);
        }
    }

    @Provides
    @Singleton
    public QuoteService provideQuoteService(WebsocketService websocketService,
                                            AppExecutors appExecutors,
                                            QuoteWatchRepository quoteWatchRepository,
                                            QuoteWatchFieldsFactory workspaceQuoteFieldsFactory,
                                            AppStorage appStorage) {
        return new WebsocketQuoteService(websocketService,
                appExecutors,
                quoteWatchRepository,
                workspaceQuoteFieldsFactory,
                appStorage);
    }

    @Provides
    @Singleton
    public ChartService provideChartService(WebsocketService websocketService,
                                            AppExecutors appExecutors,
                                            ChartWatchRepository chartWatchRepository,
                                            AppStorageInterface appStorageInterface,
                                            @Named("NetworkTimeout") TaskRunner taskRunner) {
        return new WebsocketChartService(websocketService,
                appExecutors,
                chartWatchRepository,
                appStorageInterface,
                taskRunner);
    }

    @Provides
    @Singleton
    public NewsService provideNewsService(WebsocketService websocketService,
                                          AppExecutors appExecutors) {
        return new WebsocketNewsService(websocketService, appExecutors);
    }

    @Provides
    @Singleton
    public AuthService provideAuthService(WebsocketService websocketService,
                                          AppExecutors appExecutors,
                                          AppStorageInterface appStorage,
                                          TextsRepository textsRepository) {
        return new WebsocketAuthService(websocketService, appExecutors, appStorage, textsRepository);
    }

    @Provides
    @Singleton
    public SessionService provideSessionService(WebsocketService websocketService,
                                                AuthService authService,
                                                NetworkConnectivityService networkConnectivityService,
                                                WorkspaceService workspaceService,
                                                NewsListService newsListService,
                                                DefaultSymbolsRepository defaultSymbolsRepository,
                                                QuoteService quoteService,
                                                AppExecutors appExecutors,
                                                AppConfig appConfig,
                                                QuoteDao quoteDao,
                                                NewsDao newsDao,
                                                AppStorageInterface appStorage) {
        return new WebsocketSessionService(websocketService,
                authService,
                networkConnectivityService,
                workspaceService,
                newsListService,
                quoteService,
                defaultSymbolsRepository,
                appExecutors,
                appConfig,
                quoteDao,
                newsDao,
                appStorage);
    }

    @Provides
    @Singleton
    public AppStorageInterface provideAppStorageInterface(AppStorage appStorage) {
        return appStorage;
    }

    @Provides
    @Singleton
    public WebsocketService provideWebsocketService() {
        return new NeoWebSocketService();
    }

    @Provides
    @Singleton
    public NewsListService provideNewsListService() {
        return new StockCloudNewsListService();
    }

    @Named("NetworkTimeout")
    @Provides
    public TaskRunner provideNetworkTimeoutRunner() {
        return new DelayedTaskRunner(30);
    }

}
