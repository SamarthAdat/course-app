package com.samarth.courseapp.data.remote

import android.content.res.AssetManager
import com.samarth.courseapp.core.network.NetworkMonitor
import com.samarth.courseapp.data.remote.dto.CourseDto
import com.samarth.courseapp.data.remote.dto.CoursesResponseDto
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * [CourseApi] served from a JSON file bundled in `assets`, standing in for a real backend.
 *
 * It behaves like a network call rather than a local file read, which is what makes the offline
 * requirement actually demonstrable:
 *  - it costs [LATENCY_MS], so the loading state is real;
 *  - it checks [NetworkMonitor] first and throws [NoConnectionException] when the device is
 *    offline, so switching on airplane mode genuinely fails the request;
 *  - it honours [MockApiConfig] so the failure path can be triggered on demand in debug builds.
 */
class AssetCourseApi @Inject constructor(
    private val assets: AssetManager,
    private val json: Json,
    private val networkMonitor: NetworkMonitor,
    private val mockApiConfig: MockApiConfig,
    private val ioDispatcher: CoroutineDispatcher,
) : CourseApi {

    override suspend fun getCourses(): List<CourseDto> = withContext(ioDispatcher) {
        delay(LATENCY_MS)

        if (!networkMonitor.isCurrentlyOnline()) throw NoConnectionException()
        if (mockApiConfig.forceFailure.value) {
            throw ServerException("Simulated HTTP 500 from the mock course service")
        }

        val body = runCatching { assets.open(COURSES_ASSET).bufferedReader().use { it.readText() } }
            .getOrElse { throw ServerException("Course catalog unreachable", it) }

        runCatching { json.decodeFromString<CoursesResponseDto>(body).courses }
            .getOrElse { throw ServerException("Malformed course catalog payload", it) }
    }

    private companion object {
        const val COURSES_ASSET = "courses.json"
        const val LATENCY_MS = 900L
    }
}
