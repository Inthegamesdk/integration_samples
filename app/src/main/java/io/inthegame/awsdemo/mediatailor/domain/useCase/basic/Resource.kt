package io.inthegame.awsdemo.mediatailor.domain.useCase.basic

sealed class Resource<out T> {
    data class Loading<T>(val data: T?) : Resource<T>()
    data class Success<T>(val value: T) : Resource<T>()
    data class Failure(val throwable: Exception) : Resource<Nothing>()

    companion object {
        inline fun <reified T> Resource<T>.asSuccessful() = (this as? Success)?.value
        fun Resource<Boolean>.asSuccessful(defaultValue: Boolean = false) =
            (this as? Success)?.value ?: defaultValue

        inline fun <reified T> Resource<T>.asError() = (this as? Failure)?.throwable
    }
}