package com.garon.gmdb.utils

import com.google.gson.annotations.SerializedName
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import okhttp3.Headers
import retrofit2.Response
import timber.log.Timber

class ApiCallTransformer<T>(
    private val geekErrorMessage: String
) : ObservableTransformer<Response<T>, ApiCallStateResult> {

    private companion object {
        const val HTTP_BODY_CODE = 200
    }

    override fun apply(upstream: Observable<Response<T>>): ObservableSource<ApiCallStateResult> = upstream
        .map { response ->
            when {
                response.isSuccessful -> {
                    if (response.code() == HTTP_BODY_CODE && response.body() != null) {
                        ApiSuccessState(response.body(), response.headers())
                    } else {
                        ApiSuccessState(Unit, response.headers())
                    }
                }
                else -> {
                    val errorBody = response.errorBody()

                    return@map if (errorBody != null && errorBody === ApiErrorState::class.java)
                        TODO("Parse json error.")
                    else
                        ApiErrorState(-1, geekErrorMessage, response.code())
                }
            }
        }
        .onErrorReturn {
            Timber.e("${it.printStackTrace()}. Api call failed.")
            ApiErrorState(-1, geekErrorMessage)
        }
}

interface ApiCallStateResult

data class ApiErrorState(
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("status_message") val statusMessage: String,
    val httpCode: Int = 0
) : ApiCallStateResult

object ApiInProgressState : ApiCallStateResult

data class ApiSuccessState<T>(
    val content: T,
    val headers: Headers
) : ApiCallStateResult
