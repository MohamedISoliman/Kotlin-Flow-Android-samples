package com.morihacky.android.rxjava.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.morihacky.android.rxjava.R
import com.morihacky.android.rxjava.retrofit.Contributor
import com.morihacky.android.rxjava.retrofit.GithubApi
import com.morihacky.android.rxjava.retrofit.GithubService
import com.morihacky.android.rxjava.retrofit.User
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*

class RetrofitFragment : Fragment() {
    @BindView(R.id.demo_retrofit_contributors_username)
    var _username: EditText? = null

    @BindView(R.id.demo_retrofit_contributors_repository)
    var _repo: EditText? = null

    @BindView(R.id.log_list)
    var _resultList: ListView? = null
    private var _adapter: ArrayAdapter<String>? = null
    private var _githubService: GithubApi? = null
    private var _disposables: CompositeDisposable? = null
    private var unbinder: Unbinder? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val githubToken = resources.getString(R.string.github_oauth_token)
        _githubService = GithubService.createGithubService(githubToken)
        _disposables = CompositeDisposable()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.fragment_retrofit, container, false)
        unbinder = ButterKnife.bind(this, layout)
        _adapter = ArrayAdapter(activity, R.layout.item_log, R.id.item_log, ArrayList())
        //_adapter.setNotifyOnChange(true);
        _resultList!!.adapter = _adapter
        return layout
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbinder!!.unbind()
    }

    override fun onDestroy() {
        super.onDestroy()
        _disposables!!.dispose()
    }

    @OnClick(R.id.btn_demo_retrofit_contributors)
    fun onListContributorsClicked() {
        _adapter!!.clear()
        _disposables!!.add( //
                _githubService
                        .contributors(_username!!.text.toString(), _repo!!.text.toString())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(
                                object : DisposableObserver<List<Contributor?>?>() {
                                    override fun onComplete() {
                                        Timber.d("Retrofit call 1 completed")
                                    }

                                    override fun onError(e: Throwable) {
                                        Timber.e(e, "woops we got an error while getting the list of contributors")
                                    }

                                    override fun onNext(contributors: List<Contributor>) {
                                        for (c in contributors) {
                                            _adapter!!.add(String.format(
                                                    "%s has made %d contributions to %s",
                                                    c.login, c.contributions, _repo!!.text.toString()))
                                            Timber.d(
                                                    "%s has made %d contributions to %s",
                                                    c.login, c.contributions, _repo!!.text.toString())
                                        }
                                    }
                                }))
    }

    @OnClick(R.id.btn_demo_retrofit_contributors_with_user_info)
    fun onListContributorsWithFullUserInfoClicked() {
        _adapter!!.clear()
        _disposables!!.add(
                _githubService
                        .contributors(_username!!.text.toString(), _repo!!.text.toString())
                        .flatMap { source: List<Contributor?>? -> Observable.fromIterable(source) }
                        .flatMap { contributor: Contributor ->
                            val _userObservable = _githubService
                                    .user(contributor.login)
                                    .filter { user: User -> !TextUtils.isEmpty(user.name) && !TextUtils.isEmpty(user.email) }
                            Observable.zip(_userObservable, Observable.just(contributor), BiFunction<User, Contributor, android.util.Pair<User, Contributor>> { first: User?, second: Contributor? -> Pair(first, second) })
                        }
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(
                                object : DisposableObserver<android.util.Pair<User?, Contributor?>?>() {
                                    override fun onComplete() {
                                        Timber.d("Retrofit call 2 completed ")
                                    }

                                    override fun onError(e: Throwable) {
                                        Timber.e(
                                                e,
                                                "error while getting the list of contributors along with full " + "names")
                                    }

                                    override fun onNext(pair: android.util.Pair<User, Contributor>) {
                                        val user = pair.first
                                        val contributor = pair.second
                                        _adapter!!.add(String.format(
                                                "%s(%s) has made %d contributions to %s",
                                                user.name,
                                                user.email,
                                                contributor.contributions,
                                                _repo!!.text.toString()))
                                        _adapter!!.notifyDataSetChanged()
                                        Timber.d(
                                                "%s(%s) has made %d contributions to %s",
                                                user.name,
                                                user.email,
                                                contributor.contributions,
                                                _repo!!.text.toString())
                                    }
                                }))
    }
}