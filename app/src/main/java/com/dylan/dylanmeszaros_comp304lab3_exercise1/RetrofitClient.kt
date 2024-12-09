package com.dylan.dylanmeszaros_comp304lab3_exercise1

import com.dylan.dylanmeszaros_comp304lab3_exercise1.data.OpenWeatherAPI
import com.dylan.dylanmeszaros_comp304lab3_exercise1.data.Weather
import com.dylan.dylanmeszaros_comp304lab3_exercise1.data.WeatherRepository
import com.dylan.dylanmeszaros_comp304lab3_exercise1.data.WeatherRepositoryImpl
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build();

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build();

    val weatherAPI: OpenWeatherAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()
            .create(OpenWeatherAPI::class.java)
    }

}