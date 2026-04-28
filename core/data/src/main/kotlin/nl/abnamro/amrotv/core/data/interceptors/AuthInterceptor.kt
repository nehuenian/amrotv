package nl.abnamro.amrotv.core.data.interceptors

import nl.abnamro.amrotv.core.buildconfig.BuildConfigProvider
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

internal class AuthInterceptor @Inject constructor(
    private val buildConfigProvider: BuildConfigProvider,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${buildConfigProvider.tmdbReadAccessToken}")
            .build()
        return chain.proceed(request)
    }
}