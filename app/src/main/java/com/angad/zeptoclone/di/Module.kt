package com.angad.zeptoclone.di

import com.angad.zeptoclone.data.api.FakeStoreApiService
import com.angad.zeptoclone.data.repository.CategoryRepository
import com.angad.zeptoclone.data.repository.CategoryRepositoryImpl
import com.angad.zeptoclone.data.repository.ProductRepository
import com.angad.zeptoclone.data.repository.ProductRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

//  First module for @Binds methods
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindingModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        productRepositoryImpl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository

}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TAG = "Network Module"

    @Provides
    @Singleton
    fun provideFakeStoreApiService(): FakeStoreApiService{
        return FakeStoreApiService()
    }
}