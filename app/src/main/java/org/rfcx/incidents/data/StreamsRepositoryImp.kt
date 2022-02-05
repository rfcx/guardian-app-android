package org.rfcx.incidents.data

import io.reactivex.Single
import org.rfcx.incidents.data.interfaces.StreamsRepository
import org.rfcx.incidents.data.local.AlertDb
import org.rfcx.incidents.data.local.CachedEndpointDb
import org.rfcx.incidents.data.local.StreamDb
import org.rfcx.incidents.data.remote.streams.Endpoint
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
        if (forceRefresh || !cachedEndpointDb.hasCachedEndpoint(cacheKey(projectId))) {
            return refreshFromAPI(projectId)
        }
        return getFromLocalDB(projectId)
    }

    private fun refreshFromAPI(projectId: String): Single<List<Stream>> {
        return endpoint.getStreams(projects = listOf(projectId)).observeOn(postExecutionThread.scheduler).flatMap { rawStreams ->
            rawStreams.forEach {
                streamDb.insertOrUpdate(it)
                // eventDb.insertAlert(it.toEvent())
            }
            cachedEndpointDb.updateCachedEndpoint(cacheKey(projectId))
            getFromLocalDB(projectId)
        }
    }

    private fun getFromLocalDB(projectId: String): Single<List<Stream>> {
        return Single.just(streamDb.getStreamsByProject(projectId))
    }

    private fun cacheKey(projectId: String): String {
        return "GetStreams-${projectId}"
    }
}
