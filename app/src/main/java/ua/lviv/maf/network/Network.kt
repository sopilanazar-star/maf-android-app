package ua.lviv.maf.network

import okhttp3.OkHttpClient

object Network {
    val client: OkHttpClient by lazy {
        OkHttpClient()
    }
}