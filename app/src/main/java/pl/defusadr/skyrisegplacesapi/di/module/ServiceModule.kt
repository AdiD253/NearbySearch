package pl.defusadr.skyrisegplacesapi.di.module

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import pl.defusadr.skyrisegplacesapi.service.GPlacesService
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Module
class ServiceModule {

    @Provides
    fun provideOkHttp(): OkHttpClient =
            OkHttpClient().newBuilder()
                    .connectTimeout(5000, TimeUnit.MILLISECONDS)
                    .readTimeout(5000, TimeUnit.MILLISECONDS)
                    .build()

    @Provides
    fun provideRetrofit(client: OkHttpClient): Retrofit =
            Retrofit.Builder()
                    .baseUrl("https://maps.googleapis.com")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(client)
                    .build()

    @Provides
    fun provideGPlacesService(retrofit: Retrofit): GPlacesService =
            retrofit.create(GPlacesService::class.java)
}