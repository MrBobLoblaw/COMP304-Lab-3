package com.dylan.dylanmeszaros_comp304lab3_exercise1.di

import com.dylan.dylanmeszaros_comp304lab3_exercise1.data.WeatherRepository
import com.dylan.dylanmeszaros_comp304lab3_exercise1.data.WeatherRepositoryImpl
import com.dylan.dylanmeszaros_comp304lab3_exercise1.viewmodel.WeatherViewModel
import org.koin.dsl.module

val appModules = module {
    single<WeatherRepository> { WeatherRepositoryImpl() }
    single { WeatherViewModel(get()) }
}