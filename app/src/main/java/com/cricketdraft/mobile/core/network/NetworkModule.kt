package com.cricketdraft.mobile.core.network

import com.cricketdraft.mobile.BuildConfig
import com.cricketdraft.mobile.admin.data.AdminApi
import com.cricketdraft.mobile.auth.data.AuthApi
import com.cricketdraft.mobile.auth.data.ProfileApi
import com.cricketdraft.mobile.captain.data.CaptainMatchApi
import com.cricketdraft.mobile.core.security.TokenStore
import com.cricketdraft.mobile.draft.data.DraftApi
import com.cricketdraft.mobile.audience.data.PublicApi
import com.cricketdraft.mobile.reports.data.ReportsApi
import com.cricketdraft.mobile.scoring.data.ScoringApi
import com.cricketdraft.mobile.superadmin.data.SuperAdminApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import okhttp3.MediaType.Companion.toMediaType
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenStore: TokenStore): Interceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("X-Client", "cricket-draft-android")
            .apply { tokenStore.read()?.let { addHeader("Authorization", "Bearer $it") } }
            .build()
        chain.proceed(request)
    }

    @Provides
    @Singleton
    fun provideOkHttp(authInterceptor: Interceptor): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            }
        }
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides @Singleton fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)
    @Provides @Singleton fun providePublicApi(retrofit: Retrofit): PublicApi = retrofit.create(PublicApi::class.java)
    @Provides @Singleton fun provideReportsApi(retrofit: Retrofit): ReportsApi = retrofit.create(ReportsApi::class.java)
    @Provides @Singleton fun provideDraftApi(retrofit: Retrofit): DraftApi = retrofit.create(DraftApi::class.java)
    @Provides @Singleton fun provideProfileApi(retrofit: Retrofit): ProfileApi = retrofit.create(ProfileApi::class.java)
    @Provides @Singleton fun provideCaptainMatchApi(retrofit: Retrofit): CaptainMatchApi = retrofit.create(CaptainMatchApi::class.java)
    @Provides @Singleton fun provideScoringApi(retrofit: Retrofit): ScoringApi = retrofit.create(ScoringApi::class.java)
    @Provides @Singleton fun provideAdminApi(retrofit: Retrofit): AdminApi = retrofit.create(AdminApi::class.java)
    @Provides @Singleton fun provideSuperAdminApi(retrofit: Retrofit): SuperAdminApi = retrofit.create(SuperAdminApi::class.java)
}
