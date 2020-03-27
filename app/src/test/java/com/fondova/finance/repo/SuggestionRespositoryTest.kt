package com.fondova.finance.repo

import android.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.*
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.whenever
import com.fondova.finance.workspace.service.WorkspaceService
import com.fondova.finance.config.AppConfig
import com.fondova.finance.db.KeyValueDao
import com.fondova.finance.persistance.AppStorage
import com.fondova.finance.persistance.KeyValueStorage
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class SuggestionRespositoryTest {

    @Rule
    fun rule(): TestRule { return InstantTaskExecutorRule() }

    private lateinit var testObject: SuggestionRepository

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
        whenever(mockAppConfig.useStockSettings()).thenReturn(false)

        mockAppStorage = AppStorage(keyValueStorage,
                encryptionService,
                keyValueDao,
                mockTextsRepository,
                Gson(),
                mockAppConfig,
                mockWorkspaceService)
        testObject = SuggestionRepository(mockAppStorage)
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

    @Test
    fun limitSearchHistory() {

        val fullHistory = mutableListOf("one",
                "two",
                "three",
                "four",
                "five",
                "six",
                "seven",
                "eight",
                "nine",
                "ten")
        mockAppStorage.setSearchHistory(fullHistory)

        testObject.saveSearchQuery("eleven")

        val expectedHistory = mutableListOf("eleven",
                "one",
                "two",
                "three",
                "four",
                "five",
                "six",
                "seven",
                "eight",
                "nine")
        assertEquals(expectedHistory, mockAppStorage.getSearchHistory())

    }

    @Test
    fun preventDuplicatesAndMovesRepeatedQueryToTop() {
        val fullHistory = mutableListOf("one",
                "two",
                "three",
                "four",
                "five",
                "six",
                "seven",
                "eight",
                "nine",
                "ten")
        mockAppStorage.setSearchHistory(fullHistory)

        testObject.saveSearchQuery("nine")

        val expectedHistory = mutableListOf("nine",
                "one",
                "two",
                "three",
                "four",
                "five",
                "six",
                "seven",
                "eight",
                "ten"
                )
        assertEquals(expectedHistory, mockAppStorage.getSearchHistory())
    }

}