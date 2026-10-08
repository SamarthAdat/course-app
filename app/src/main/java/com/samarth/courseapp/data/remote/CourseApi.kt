package com.samarth.courseapp.data.remote

import com.samarth.courseapp.data.remote.dto.CourseDto

/**
 * The course catalog endpoint.
 *
 * Declared as an interface so swapping the bundled-JSON implementation for a Retrofit service is a
 * one-line change in the DI module and touches nothing else.
 */
interface CourseApi {

    /**
     * @throws NoConnectionException when the device is offline.
     * @throws ServerException when the response cannot be used.
     */
    suspend fun getCourses(): List<CourseDto>
}
