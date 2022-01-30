package org.rfcx.incidents.data.remote.usertouch

import io.reactivex.Single
import org.rfcx.incidents.data.remote.domain.SingleUseCase
import org.rfcx.incidents.data.remote.domain.executor.PostExecutionThread
import org.rfcx.incidents.data.remote.domain.executor.ThreadExecutor

class CheckUserTouchUseCase(
    private val userTouchRepository: UserTouchRepository,
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread
) : SingleUseCase<Nothing?, Boolean>(threadExecutor, postExecutionThread) {
    override fun buildUseCaseObservable(params: Nothing?): Single<Boolean> {
        return userTouchRepository.checkUser()
    }
}
