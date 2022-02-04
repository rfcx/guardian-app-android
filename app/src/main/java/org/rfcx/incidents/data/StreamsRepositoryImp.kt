package org.rfcx.incidents.data

import io.reactivex.Single
import org.rfcx.incidents.data.interfaces.StreamsRepository
import org.rfcx.incidents.data.local.AlertDb
import org.rfcx.incidents.data.local.CachedEndpointDb
import org.rfcx.incidents.data.local.StreamDb
import org.rfcx.incidents.data.remote.streams.Endpoint
import org.rfcx.incidents.data.remote.streams.toEvent
import org.rfcx.incidents.domain.executor.PostExecutionThread
import org.rfcx.incidents.entity.Stream

class StreamsRepositoryImp(
    private val endpoint: Endpoint,
    private val streamDb: StreamDb,
    private val eventDb: AlertDb,
    private val cachedEndpointDb: CachedEndpointDb,
    private val postExecutionThread: PostExecutionThread
) : StreamsRepository {
    override fun get(projectId: String, forceRefresh: Boolean): Single<List<Stream>> {
        if (forceRefresh || !cachedEndpointDb.hasCachedEndpoint("GetProjects")) {
            return refreshFromAPI()
        }
        return getFromLocalDB()
    }

    private fun refreshFromAPI(): Single<List<Stream>> {
        return endpoint.getStreams().observeOn(postExecutionThread.scheduler).flatMap { rawStreams ->
            rawStreams.forEach {
                streamDb.insertOrUpdate(it)
                eventDb.insertAlert(it.toEvent())
            }
            cachedEndpointDb.updateCachedEndpoint("GetProjects")
            getFromLocalDB()
        }
    }

    private fun getFromLocalDB(): Single<List<Stream>> {
        return Single.just(streamDb.getAll())
    }
}
