package pl.defusadr.skyrisegplacesapi.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import pl.defusadr.skyrisegplacesapi.NearbySearchApp
import pl.defusadr.skyrisegplacesapi.di.module.ActivityBindingModule
import pl.defusadr.skyrisegplacesapi.di.module.ServiceModule

@Component(modules = [
    AndroidSupportInjectionModule::class,
    ActivityBindingModule::class,
    ServiceModule::class
])
interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(app: NearbySearchApp)
}