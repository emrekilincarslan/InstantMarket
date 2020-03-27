package com.fondova.finance.util

import java.io.File

class JsonFileReader {

    fun getJsonFromFile(filename: String): String? {
        val classLoader = this.javaClass.classLoader
        val url = classLoader.getResource(filename)
        val file = File(url.path)
        val json = file.readText()
        return json
    }

}