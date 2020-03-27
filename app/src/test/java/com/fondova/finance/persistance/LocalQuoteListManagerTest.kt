package com.fondova.finance.persistance

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import com.fondova.finance.workspace.service.EmptyResponseListener
import com.fondova.finance.workspace.service.WorkspaceService
import com.fondova.finance.config.AppConfig
import com.fondova.finance.db.KeyValueDao
import com.fondova.finance.workspace.instantmarket.IMWorkspace
import com.fondova.finance.workspace.instantmarket.IMWorkspaceGroup
import com.fondova.finance.workspace.instantmarket.IMWorkspaceQuote
import com.fondova.finance.repo.EncryptionService
import com.fondova.finance.repo.TextsRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class LocalQuoteListManagerTest {

    @Rule
    fun rule(): TestRule { return InstantTaskExecutorRule() }

    lateinit var testObject: LocalQuoteListManager

    @Mock
    lateinit var encryptionService: EncryptionService
    @Mock
    lateinit var keyValueDao: KeyValueDao
    @Mock
    lateinit var keyValueStorage: KeyValueStorage
    @Mock
    lateinit var mockTextsRepository: TextsRepository
    @Mock
    lateinit var mockAppConfig: AppConfig
    @Mock
    lateinit var mockWorkspaceService: WorkspaceService

    var keyValueMap: MutableMap<String, String> = mutableMapOf()

    lateinit var mockAppStorage: AppStorage

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        setupMockKeyValueStorage()
        setupMockWorkspaceService()
        whenever(mockAppConfig.useStockSettings()).thenReturn(false)
        mockAppStorage = AppStorage(keyValueStorage,
                encryptionService,
                keyValueDao,
                mockTextsRepository,
                Gson(), mockAppConfig,
                mockWorkspaceService)
        testObject = LocalQuoteListManager(mockAppStorage)
    }

    fun setupMockKeyValueStorage() {
        whenever(keyValueStorage.set(Mockito.anyString(), Mockito.anyString())).thenAnswer {
            keyValueMap.set(it.arguments[0] as String, it.arguments[1] as String)
        }

        whenever(keyValueStorage.get(Mockito.anyString())).thenAnswer {
            keyValueMap[it.arguments[0] as String]
        }
        whenever(keyValueStorage.clearAll()).thenAnswer {
            keyValueMap.clear()
        }

    }


    fun setupMockWorkspaceService() {
       whenever(mockWorkspaceService.saveWorkspace(any(), any<EmptyResponseListener>())).thenAnswer {
            var listener = it.arguments[1] as EmptyResponseListener
           listener.onResponse()
       }
    }

    @Test
    fun insertQuotes() {
        testObject.insertQuote("AAPL", "AAPL", "Symbol", 0, 0)

        var workspaceGroup = testObject.getWorkspace().getGroups().first()
        assertEquals(2, testObject.getQuoteCount())
        assertEquals("AAPL", workspaceGroup.getListOfQuotes().get(0).getValue())

        testObject.insertQuote("GOOG", "GOOG", "Symbol", 0, 1)

        workspaceGroup = testObject.getWorkspace().getGroups().first()
        assertEquals(3, testObject.getQuoteCount())
        assertEquals("AAPL", workspaceGroup.getListOfQuotes().get(0).getValue())
        assertEquals("GOOG", workspaceGroup.getListOfQuotes().get(1).getValue())
    }

    @Test
    fun getAllSymbols() {
        val quote1 = IMWorkspaceQuote()
        quote1.setValue("AAPL")
        quote1.setType("Symbol")

        val quote2 = IMWorkspaceQuote()
        quote2.setValue("GOOG")
        quote2.setType("Symbol")

        val group = IMWorkspaceGroup()
        group.setDisplayName("Monkey")
        group.setListOfQuotes(mutableListOf(quote1, quote2))

        val workspace = IMWorkspace()
        workspace.setGroups(mutableListOf(group))


        testObject.setWorkspace(workspace)

        assertEquals(2, testObject.getAllSymbols().size)
        assertEquals("AAPL", testObject.getAllSymbols().get(0).getValue())
        assertEquals("GOOG", testObject.getAllSymbols().get(1).getValue())
    }

    @Test
    fun deleteQuote() {
        val quote1 = IMWorkspaceQuote()
        quote1.setValue("AAPL")
        quote1.setType("Symbol")

        val quote2 = IMWorkspaceQuote()
        quote2.setValue("GOOG")
        quote2.setType("Symbol")

        val quote3 = IMWorkspaceQuote()
        quote3.setValue("MSFT")
        quote3.setType("Symbol")

        val group = IMWorkspaceGroup()
        group.setDisplayName("Group")
        group.setListOfQuotes(mutableListOf(quote1, quote2, quote3))

        val workspace = IMWorkspace()
        workspace.setGroups(mutableListOf(group))

        testObject.setWorkspace(workspace)

        assertEquals(4, testObject.getQuoteCount())

        testObject.deleteSymbol(0, 1)

        var workspaceGroup = testObject.getWorkspace().getGroups().first()
        assertEquals(3, testObject.getQuoteCount())
        assertEquals("AAPL", workspaceGroup.getListOfQuotes().get(0).getValue())
        assertEquals("MSFT", workspaceGroup.getListOfQuotes().get(1).getValue())

    }



    @Test
    fun insertEmptyLabelIfNeeded() {
        mockAppStorage.setWorkspace(IMWorkspace(mutableMapOf()))

        testObject.insertQuote("AAPL", "AAPL", "Symbol", 0, 0)

        var workspaceGroup = testObject.getWorkspace().getGroups().first()
        assertEquals(2, testObject.getQuoteCount())
        assertEquals("UNASSIGNED", workspaceGroup.getDisplayName())
        assertEquals("AAPL", workspaceGroup.getListOfQuotes().get(0).getValue())
        assertEquals("symbol", workspaceGroup.getListOfQuotes().get(0).getType()?.toLowerCase())

    }
}