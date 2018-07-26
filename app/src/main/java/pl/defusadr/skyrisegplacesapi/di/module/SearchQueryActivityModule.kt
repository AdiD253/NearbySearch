package pl.defusadr.skyrisegplacesapi.di.module

import dagger.Binds
import dagger.Module
import pl.defusadr.skyrisegplacesapi.ui.SearchQueryPresenter
import pl.defusadr.skyrisegplacesapi.ui.SearchQueryPresenterImpl

@Module
abstract class SearchQueryActivityModule {

    @Binds
    abstract fun provideSearchQueryPresenter(presenter: SearchQueryPresenterImpl): SearchQueryPresenter
}