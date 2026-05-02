package nl.abnamro.amrotv.feature.movies.ui.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import nl.abnamro.amrotv.core.data.di.NetworkModule
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [NetworkModule::class])
object TestNetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val mockUrlInterceptor = Interceptor { chain ->
            val mockBase = MockServerHolder.url.toHttpUrl()
            val originalUrl = chain.request().url
            val rewrittenUrl = originalUrl.newBuilder()
                .scheme(mockBase.scheme)
                .host(mockBase.host)
                .port(mockBase.port)
                .build()
            chain.proceed(chain.request().newBuilder().url(rewrittenUrl).build())
        }
        return OkHttpClient.Builder()
            .addInterceptor(mockUrlInterceptor)
            .build()
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Singleton
    fun provideRetrofitBuilder(okHttpClient: OkHttpClient, json: Json): Retrofit.Builder =
        Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json; charset=UTF-8".toMediaType()))
}
