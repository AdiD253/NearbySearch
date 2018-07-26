package pl.defusadr.skyrisegplacesapi.di.module

import dagger.Module
import dagger.android.ContributesAndroidInjector
import pl.defusadr.skyrisegplacesapi.ui.SearchQueryActivity

@Module
abstract class ActivityBindingModule {

    @ContributesAndroidInjector(modules = [
        SearchQueryActivityModule::class
    ])
    abstract fun bindSearchQueryActivity(): SearchQueryActivity
}