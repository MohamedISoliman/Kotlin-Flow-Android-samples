package com.morihacky.android.rxjava.retrofit

import com.morihacky.android.rxjava.BuildConfig
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GithubApi {
    /** See https://developer.github.com/v3/repos/#list-contributors  */
    @GET("/repos/{owner}/{repo}/contributors")
    suspend fun contributors(
            @Path("owner") owner: String?, @Path("repo") repo: String?,
            @Query("client_id") clientId: String = BuildConfig.CLIENT_ID,
            @Query("client_secret") clientSecret: String = BuildConfig.CLIENT_SECRET
    ): List<Contributor>


    @GET("/repos/{owner}/{repo}/contributors")
    fun contributorsRx(@Path("owner") owner: String?, @Path("repo") repo: String?): Observable<List<Contributor>>


    /** See https://developer.github.com/v3/users/  */
    @GET("/users/{user}")
    suspend fun user(
            @Path("user") user: String?,
            @Query("client_id") clientId: String = BuildConfig.CLIENT_ID,
            @Query("client_secret") clientSecret: String = BuildConfig.CLIENT_SECRET
    ): User

    /** See https://developer.github.com/v3/users/  */
    @GET("/users/{user}")
    fun userRx(@Path("user") user: String?): Observable<User>

    /** See https://developer.github.com/v3/users/  */
    @GET("/users/{user}")
    suspend fun getUser(@Path("user") user: String?): User

    /** See https://developer.github.com/v3/users/  */
    @GET("/users/{user}")
    fun getUserRx(@Path("user") user: String?): User
}