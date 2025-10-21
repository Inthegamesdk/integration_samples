package io.inthegame.awsdemo.mediatailor.domain.useCase.basic

/**
 * Executes business logic synchronously or asynchronously using Coroutines.
 */
abstract class NotSuspendableUseCase<in P, R> {

    /** Executes the use case asynchronously and returns a [Result].
     *
     * @return a [Result].
     *
     * @param parameters the input parameters to run the use case with
     */
    operator fun invoke(parameters: P): R =
    // Moving all use case's executions to the injected dispatcher
    // In production code, this is usually the Default dispatcher (background thread)
        // In tests, this becomes a TestCoroutineDispatcher
        execute(parameters)


    @Throws(RuntimeException::class)
    protected abstract fun execute(parameters: P): R
}