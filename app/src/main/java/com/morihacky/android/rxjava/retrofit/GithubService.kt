package com.morihacky.android.rxjava.retrofit

import android.text.TextUtils
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GithubService {
    @JvmStatic
    fun createGithubService(githubToken: String?): GithubApi {
        val builder = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.github.com")
        if (!TextUtils.isEmpty(githubToken)) {
            val client = OkHttpClient.Builder()
                    .addInterceptor(
                            Interceptor { chain: Interceptor.Chain ->
                                val request = chain.request()
                                val newReq = request
                                        .newBuilder()
                                        .addHeader("Authorization", String.format("token %s", githubToken))
                                        .build()
                                chain.proceed(newReq)
                            })
                    .build()
            builder.client(client)
        }
        return builder.build().create(GithubApi::class.java)
    }
}