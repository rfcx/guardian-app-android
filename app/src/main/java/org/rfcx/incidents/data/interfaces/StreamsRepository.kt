package org.rfcx.incidents.data.interfaces

import io.reactivex.Single
import org.rfcx.incidents.entity.Stream

interface StreamsRepository {
    fun get(projectId: String, forceRefresh: Boolean): Single<List<Stream>>
}