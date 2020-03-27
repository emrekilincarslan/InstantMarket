package com.fondova.finance.persistance

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import com.fondova.finance.api.model.Credentials
import com.fondova.finance.workspace.service.WorkspaceService
import com.fondova.finance.config.AppConfig
import com.fondova.finance.db.KeyValueDao
import com.fondova.finance.repo.EncryptionService
import com.fondova.finance.repo.TextsRepository
import com.fondova.finance.ui.chart.detail.ChartInterval
import com.fondova.finance.ui.chart.detail.ChartStyle
import com.fondova.finance.vo.KeyValue
import com.fondova.finance.vo.NewsCategory
import com.fondova.finance.vo.User
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations


class AppStorageTest {

    @Rule
    fun rule(): TestRule { return InstantTaskExecutorRule() }

    lateinit var testObject: AppStorage
    @Mock lateinit var encryptionService: EncryptionService
    @Mock lateinit var keyValueDao: KeyValueDao
    @Mock lateinit var keyValueStorage: KeyValueStorage
    @Mock lateinit var mockTextsRepository: TextsRepository
    @Mock lateinit var mockAppConfig: AppConfig
    @Mock lateinit var mockWorkspaceService: WorkspaceService

    var keyValueMap: MutableMap<String, String> = mutableMapOf()
    var daoMap: MutableMap<String, String> = mutableMapOf()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        whenever(mockTextsRepository.refreshRateOffString()).thenReturn("0")
        whenever(mockTextsRepository.refreshRate5SecondsString()).thenReturn("Default")
        whenever(mockAppConfig.useStockSettings()).thenReturn(false)

        setupMockKeyValueStorage()
        setupMockKeyValueDao()
        setupMockEncryptionService()

        testObject = AppStorage(keyValueStorage,
                encryptionService,
                keyValueDao,
                mockTextsRepository,
                Gson(), mockAppConfig,
                mockWorkspaceService)
    }

    fun setupMockKeyValueStorage() {
        whenever(keyValueStorage.set(any(), any())).thenAnswer {
            keyValueMap.set(it.arguments[0] as String, it.arguments[1] as String)
        }

        whenever(keyValueStorage.get(any())).thenAnswer {
            keyValueMap[it.arguments[0] as String]
        }
        whenever(keyValueStorage.clearAll()).thenAnswer {
            keyValueMap.clear()
        }
        whenever(keyValueStorage.delete(any())).thenAnswer {
            keyValueMap.remove(it.arguments[0] as String)
        }

    }

    fun setupMockKeyValueDao() {
        whenever(keyValueDao.insert(any())).thenAnswer {
            daoMap.set((it.arguments[0] as KeyValue).key, (it.arguments[0] as KeyValue).value)
        }
        whenever(keyValueDao.getKeyValue(any())).thenAnswer {
            KeyValue.create(it.arguments[0] as String, daoMap[it.arguments[0] as String])
        }

    }

    fun setupMockEncryptionService() {
        whenever(encryptionService.encrypt(any(), any())).thenAnswer {
            (it.arguments[0] as String) + "|" + (it.arguments[1] as String)
        }
        whenever(encryptionService.decrypt(any(), any())).thenAnswer {
            (it.arguments[1] as String).removePrefix((it.arguments[0] as String) + "|")
        }

    }

    private fun getTestNewsCategory(): List<NewsCategory> {
        val newsCategory = NewsCategory()
        newsCategory.id = 1
        newsCategory.isQuoteRelated = true
        newsCategory.keywords = "my,keywords,string,list"
        newsCategory.name = "CategoryName"
        newsCategory.order = 2
        newsCategory.query = "News Category Query"
        newsCategory.userId = 1234

        val newsCategoryList = listOf(newsCategory)
        return newsCategoryList
    }

    @Test
    fun getAcceptEula() {
        testObject.setAcceptedEula(false)

        assertFalse(testObject.getAcceptedEula())

        testObject.setAcceptedEula(true)

        assertTrue(testObject.getAcceptedEula())
    }

    @Test
    fun getSyncWithDrive() {
        testObject.setSyncWithDrive(false)

        assertFalse(testObject.getSyncWithDrive())

        testObject.setSyncWithDrive(true)

        assertTrue(testObject.getSyncWithDrive())

    }

    @Test
    fun getAskedSyncWithDrive() {
        testObject.setAskedSyncWithDrive(false)

        assertFalse(testObject.getAskedSyncWithDrive())

        testObject.setAskedSyncWithDrive(true)

        assertTrue(testObject.getAskedSyncWithDrive())

    }

    @Test
    fun getUser() {
        val testUser = User()
        testUser.id = 1
        testUser.info = "B"
        testUser.username = "Bob"
        testUser.whoId = 2
        testUser.wspVersion = "1.0.0"
        testUser.protocolRevision = "C"
        testUser.qsVersion = "1.0.1"
        testUser.dataSrc = "A"
        testUser.isLoggedIn = true

        testObject.setUser(testUser)

        val actualUser = testObject.getUser()

        if (actualUser == null) {
            fail("User was null")
            return
        }

        assertEquals(1, actualUser.id)
        assertEquals("B", actualUser.info)
        assertEquals(2, actualUser.whoId)
        assertEquals("Bob", actualUser.username)
        assertEquals("1.0.0", actualUser.wspVersion)
        assertEquals("C", actualUser.protocolRevision)
        assertEquals("1.0.1", actualUser.qsVersion)
        assertEquals("A", actualUser.dataSrc)
        assertTrue(actualUser.isLoggedIn)
    }

    @Test
    fun setUserToNull() {
        testObject.setUser(User())

        assertNotNull(testObject.getUser())

        testObject.setUser(null)

        assertNull(testObject.getUser())
    }

    @Test
    fun getSearchHistory() {

        val searchHistory: List<String> = listOf("first", "second", "third")

        testObject.setSearchHistory(searchHistory)

        val fetchedSearchHistory = testObject.getSearchHistory()

        assertEquals(3, fetchedSearchHistory.size)
        assertEquals("first", fetchedSearchHistory.first())
        assertEquals("second", fetchedSearchHistory[1])
        assertEquals("third", fetchedSearchHistory[2])
    }

    @Test
    fun getCredentials() {
        testObject.setCredentials(Credentials.create("Bob", "Monkey"))

        assertEquals("Bob", testObject.getCredentials().username)
        assertEquals("Monkey", testObject.getCredentials().password)

        testObject.setCredentials(Credentials.create("Mary", "Banana"))

        assertEquals("Mary", testObject.getCredentials().username)
        assertEquals("Banana", testObject.getCredentials().password)
    }

    @Test
    fun fallbackToLegacyCredentials() {
        keyValueDao.insert(KeyValue("user.username", "Bob"))
        keyValueDao.insert(KeyValue("user.password", encryptionService.encrypt("user.password", "Monkey")))
        keyValueDao.insert(KeyValue("user.remember_me", true.toString()))

        keyValueStorage.set("user.username", "")
        keyValueStorage.set("user.password", "")
        keyValueStorage.set("user.remember_me", "")

        assertEquals("Bob", testObject.getCredentials().username)
        assertEquals("Monkey", testObject.getCredentials().password)
        assertTrue(testObject.getShouldRememberCredentials())
    }

    @Test
    fun getShouldRememberCredentials() {
        testObject.setShouldRememberCredentials(false)
        assertFalse(testObject.getShouldRememberCredentials())

        testObject.setShouldRememberCredentials(true)
        assertTrue(testObject.getShouldRememberCredentials())
    }

    @Test
    fun getRefreshRate() {
        testObject.setRefreshRate("10")
        assertEquals("10", testObject.getRefreshRate())

        testObject.setRefreshRate("1")
        assertEquals("1", testObject.getRefreshRate())
    }

    @Test
    fun getAreDefaultSymbolsInserted() {
        testObject.setAreDefaultSymbolsInserted(false)
        assertFalse(testObject.getAreDefaultSymbolsInserted())

        testObject.setAreDefaultSymbolsInserted(true)
        assertTrue(testObject.getAreDefaultSymbolsInserted())
    }

    @Test
    fun getChartStyle() {
        testObject.setChartStyle(1)
        assertEquals(1, testObject.getChartStyle())

        testObject.setChartStyle(2)
        assertEquals(2, testObject.getChartStyle())
    }

    @Test
    fun chartStyleDefaultsToBar() {
        whenever(keyValueStorage.get(Mockito.anyString())).thenReturn("")

        assertEquals(ChartStyle.BAR, testObject.getChartStyle())
    }

    @Test
    fun getChartInterval() {
        testObject.setChartInterval(5)
        assertEquals(5, testObject.getChartInterval())

        testObject.setChartInterval(10)
        assertEquals(10, testObject.getChartInterval())
    }

    @Test
    fun chartIntervalDefaultsToDay() {
        whenever(keyValueStorage.get(Mockito.anyString())).thenReturn("")

        assertEquals(ChartInterval.DAY, testObject.getChartInterval())
    }

    @Test
    fun getIsDefaultNewsInserted() {
        testObject.setIsDefaultNewsInserted(false)
        assertFalse(testObject.getIsDefaultNewsInserted())

        testObject.setIsDefaultNewsInserted(true)
        assertTrue(testObject.getIsDefaultNewsInserted())
    }

    @Test
    fun clearAllPreferences() {
        testObject.isUserLogoutForced = true
        testObject.shouldUserBeConnected = true
        testObject.isAppInBackground = true
        testObject.setUsername("Bob")
        testObject.setPassword("Monkey")
        testObject.setShouldRememberCredentials(true)
        testObject.setRefreshRate("1")
        testObject.setAreDefaultSymbolsInserted(true)
        testObject.setChartStyle(ChartStyle.LINE)
        testObject.setChartInterval(ChartInterval.MONTH)
        testObject.setIsDefaultNewsInserted(true)

        testObject.clearAll()

        assertFalse(testObject.isUserLogoutForced)
        assertFalse(testObject.shouldUserBeConnected)
        assertFalse(testObject.isAppInBackground)
        assertEquals("", testObject.getUsername())
        assertEquals("", testObject.getPassword())
        assertFalse(testObject.getShouldRememberCredentials())
        assertEquals("Default", testObject.getRefreshRate())
        assertFalse(testObject.getAreDefaultSymbolsInserted())
        assertEquals(ChartStyle.BAR, testObject.getChartStyle())
        assertEquals(ChartInterval.DAY, testObject.getChartInterval())
        assertFalse(testObject.getIsDefaultNewsInserted())

    }

    @Test
    fun workspaceExpandedState() {
        testObject.setWorkspaceExpandedState("abcd", listOf(true, false, false))
        testObject.setWorkspaceExpandedState("efgh", listOf(false, true, true))

        assertEquals(listOf(true, false, false), testObject.getWorkspaceExpandedState("abcd"))
        assertEquals(listOf(false, true, true), testObject.getWorkspaceExpandedState("efgh"))
        assertEquals(emptyList<Boolean>(), testObject.getWorkspaceExpandedState("xyz"))
    }

}