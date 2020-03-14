package com.morihacky.android.rxjava.retrofit

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path

interface GithubApi {
    /** See https://developer.github.com/v3/repos/#list-contributors  */
    @GET("/repos/{owner}/{repo}/contributors")
    suspend fun contributors(@Path("owner") owner: String?, @Path("repo") repo: String?): List<Contributor>


    @GET("/repos/{owner}/{repo}/contributors")
    fun contributorsRx(@Path("owner") owner: String?, @Path("repo") repo: String?): Observable<List<Contributor>>


    /** See https://developer.github.com/v3/users/  */
    @GET("/users/{user}")
    suspend fun user(@Path("user") user: String?): User

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