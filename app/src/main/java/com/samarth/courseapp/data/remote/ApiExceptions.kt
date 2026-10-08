package com.samarth.courseapp.data.remote

import java.io.IOException

/** Device has no usable network; the request never left the handset. */
class NoConnectionException : IOException("No network connection available")

/** The endpoint answered with something unusable (bad status, unparseable body). */
class ServerException(message: String, cause: Throwable? = null) : IOException(message, cause)
