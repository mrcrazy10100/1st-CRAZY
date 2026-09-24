package com.example.data

import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {
    val allHistory: Flow<List<HistoryItem>> = historyDao.getAllHistory()

    suspend fun insert(expression: String, result: String) {
        historyDao.insertHistory(
            HistoryItem(
                expression = expression,
                result = result,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun delete(id: Long) {
        historyDao.deleteById(id)
    }

    suspend fun clearAll() {
        historyDao.clearAll()
    }
}
