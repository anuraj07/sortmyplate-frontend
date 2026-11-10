package io.sortmyplate.network

// iOS Simulator shares localhost directly with the host Mac
actual fun getApiBaseUrl(): String = "http://localhost:8000/v1"
