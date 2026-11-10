package io.sortmyplate.network

// Android Emulator maps 10.0.2.2 directly to the host Mac's localhost (127.0.0.1)
actual fun getApiBaseUrl(): String = "http://10.0.2.2:8000/v1"
