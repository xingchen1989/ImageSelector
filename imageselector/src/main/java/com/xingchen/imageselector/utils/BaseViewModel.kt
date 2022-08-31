package com.xingchen.imageselector.utils

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cqss.doctor.network.NetResponse
import com.cqss.doctor.network.RepositoryCenter
import kotlinx.coroutines.launch

enum class ApiStatus { LOADING, ERROR, DONE }

open class BaseViewModel : ViewModel() {
    // The external immutable LiveData for the request status
    val status: MutableLiveData<ApiStatus> = MutableLiveData()

    inline fun <reified T : Any?> requestSilent(
        crossinline apiBlock: suspend () -> NetResponse<T>,
        noinline onSuccess: ((T) -> Unit)? = null,
        noinline onError: ((Any?) -> Unit)? = null,
    ) {
        viewModelScope.launch {
            RepositoryCenter.launchRequest(
                apiBlock,
                onSuccess,
                onError
            )
        }
    }

    inline fun <reified T : Any?> requestObvious(
        crossinline apiBlock: suspend () -> NetResponse<T>,
        noinline onSuccess: ((T) -> Unit)? = null,
        noinline onError: ((Any?) -> Unit)? = null,
    ) {
        viewModelScope.launch {
            status.value = ApiStatus.LOADING
            RepositoryCenter.launchRequest(
                apiBlock,
                onSuccess,
                onError
            ) { status.value = ApiStatus.DONE }
        }
    }
}