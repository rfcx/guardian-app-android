/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rfcx.incidents.data.remote.common

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class Result<out R> {

    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val throwable: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$throwable]"
            is Loading -> "Loading"
        }
    }
}

inline fun <T> Result<T>.success(
    success: (T) -> Unit,
    noinline fail: ((Throwable) -> Unit)? = null,
    noinline loading: (() -> Unit)? = null
) {
    when (this) {
        is Result.Success<T> -> {
            success.invoke(data)
        }
        is Result.Error -> {
            fail?.invoke(throwable)
        }
        is Result.Loading -> {
            loading?.invoke()
        }
    }
}
