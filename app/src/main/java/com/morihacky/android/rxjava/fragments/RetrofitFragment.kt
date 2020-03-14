package com.morihacky.android.rxjava.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.morihacky.android.rxjava.R
import com.morihacky.android.rxjava.retrofit.GithubApi
import com.morihacky.android.rxjava.retrofit.GithubService
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.util.*

@ExperimentalCoroutinesApi
class RetrofitFragment : BaseFragment() {
    @JvmField
    @BindView(R.id.demo_retrofit_contributors_username)
    var _username: EditText? = null

    @JvmField
    @BindView(R.id.demo_retrofit_contributors_repository)
    var _repo: EditText? = null

    @JvmField
    @BindView(R.id.log_list)
    var _resultList: ListView? = null
    private var _adapter: ArrayAdapter<String>? = null
    private lateinit var _githubService: GithubApi
    private var _disposables: CompositeDisposable? = null
    private var unbinder: Unbinder? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _githubService = GithubService.createGithubService()
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

        flow {
            _githubService.contributors(_username!!.text.toString(), _repo!!.text.toString())
                    .forEach { emit(it) }

        }
                .onStart { _adapter!!.clear() }
                .catch {
                    Timber.e(it, "woops we got an error while getting the list of contributors")
                }
                .onEach {
                    _adapter!!.add(String.format(
                            "%s has made %d contributions to %s",
                            it.login, it.contributions, _repo!!.text.toString()))
                    Timber.d(
                            "%s has made %d contributions to %s",
                            it.login, it.contributions, _repo!!.text.toString())

                }
                .onCompletion {
                    Timber.d("Retrofit call 1 completed")
                }.launchIn(this)
    }

    @FlowPreview
    @OnClick(R.id.btn_demo_retrofit_contributors_with_user_info)
    fun onListContributorsWithFullUserInfoClicked() {

        flow {
            _githubService.contributors(_username!!.text.toString(), _repo!!.text.toString())
                    .forEach { emit(it) }

        }

                .onStart { _adapter!!.clear() }
                .flatMapMerge { contributor ->
                    flowOf(_githubService.getUser(contributor.login))
                            .filter { user -> !user.name.isNullOrEmpty() && !user.email.isNullOrEmpty() }
                            .zip(flowOf(contributor)) { user, _ -> Pair(user, contributor) }
                }
                .onEach {
                    val user = it.first
                    val contributor = it.second
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
                .catch {
                    Timber.e(it, "error while getting the list of contributors along with full " + "names")
                }
                .onCompletion {
                    Timber.d("Retrofit call 2 completed ")
                }.launchIn(this)
    }
}