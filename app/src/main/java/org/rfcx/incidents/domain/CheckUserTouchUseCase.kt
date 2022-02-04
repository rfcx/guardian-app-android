package org.rfcx.incidents.domain

import io.reactivex.Single
import org.rfcx.incidents.data.interfaces.UserTouchRepository
import org.rfcx.incidents.domain.base.SingleUseCase
import org.rfcx.incidents.domain.executor.PostExecutionThread
import org.rfcx.incidents.domain.executor.ThreadExecutor

class CheckUserTouchUseCase(
    private val userTouchRepository: UserTouchRepository,
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread
) : SingleUseCase<Nothing?, Boolean>(threadExecutor, postExecutionThread) {
    override fun buildUseCaseObservable(params: Nothing?): Single<Boolean> {
        return userTouchRepository.checkUser()
    }
}
