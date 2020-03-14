package com.morihacky.android.rxjava.retrofit

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GithubService {
    @JvmStatic
    fun createGithubService(): GithubApi {
        val builder = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.github.com")

        val client = OkHttpClient.Builder()
                .addInterceptor(
                        HttpLoggingInterceptor().also {
                            it.level = HttpLoggingInterceptor.Level.BODY
                        }
                )
                .build()
        builder.client(client)

        return builder.build().create(GithubApi::class.java)
    }
}