package com.example.yungasdistribuidora.data.repository

import com.example.yungasdistribuidora.data.local.database.AppDatabase
import com.example.yungasdistribuidora.data.local.metadata.CatalogSyncMetadataEntity
import com.example.yungasdistribuidora.data.mapper.toDomain
import com.example.yungasdistribuidora.data.mapper.toEntity
import com.example.yungasdistribuidora.data.remote.api.CategoryApi
import com.example.yungasdistribuidora.data.remote.api.ProductApi
import com.example.yungasdistribuidora.data.remote.api.SubCategoryApi
import com.example.yungasdistribuidora.domain.model.Category
import com.example.yungasdistribuidora.domain.model.Product
import com.example.yungasdistribuidora.domain.model.SubCategory
import com.example.yungasdistribuidora.domain.repository.ProductRepository
import com.example.yungasdistribuidora.util.ErrorMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import androidx.room.withTransaction

class ProductRepositoryImpl(
    private val productApi: ProductApi,
    private val categoryApi: CategoryApi,
    private val subCategoryApi: SubCategoryApi,
    private val database: AppDatabase
) : ProductRepository {

    private val productDao = database.productDao()
    private val categoryDao = database.categoryDao()
    private val subCategoryDao = database.subCategoryDao()
    private val metadataDao = database.catalogSyncMetadataDao()

    override fun getProducts(categoryId: String?, subCategoryId: String?): Flow<List<Product>> {
        return combine(
            productDao.getProducts(categoryId, subCategoryId),
            categoryDao.getCategories(),
            subCategoryDao.getSubCategories(null)
        ) { productEntities, categories, subCategories ->
            val catMap = categories.associateBy { it.id }
            val subCatMap = subCategories.associateBy { it.id }
            productEntities.map { entity ->
                val catName = catMap[entity.categoryId]?.name
                val subCatName = entity.subCategoryId?.let { subCatMap[it]?.name }
                entity.toDomain(catName, subCatName)
            }
        }
    }

    override suspend fun refreshCatalog(): Result<Unit> {
        return try {
            val prodResponse = productApi.getProducts(null, null, null)
            val catResponse = categoryApi.getCategories()
            val subCatResponse = subCategoryApi.getSubCategories(null)

            if (prodResponse.isSuccessful && prodResponse.body() != null &&
                catResponse.isSuccessful && catResponse.body() != null &&
                subCatResponse.isSuccessful && subCatResponse.body() != null
            ) {
                val prodDtos = prodResponse.body()!!
                val catDtos = catResponse.body()!!
                val subCatDtos = subCatResponse.body()!!

                val productEntities = prodDtos.map { it.toEntity() }
                val categoryEntities = catDtos.map { it.toEntity() }
                val subCategoryEntities = subCatDtos.map { it.toEntity() }
                val syncTime = System.currentTimeMillis()

                database.withTransaction {
                    productDao.deleteAllProducts()
                    productDao.insertProducts(productEntities)
                    categoryDao.deleteAllCategories()
                    categoryDao.insertCategories(categoryEntities)
                    subCategoryDao.deleteAllSubCategories()
                    subCategoryDao.insertSubCategories(subCategoryEntities)
                    metadataDao.insertMetadata(
                        CatalogSyncMetadataEntity(key = "PRODUCT_CATALOG", lastSuccessfulSync = syncTime)
                    )
                }

                Result.success(Unit)
            } else {
                val code = prodResponse.code().takeIf { it != 200 } ?: catResponse.code().takeIf { it != 200 } ?: subCatResponse.code()
                Result.failure(Exception(ErrorMapper.mapHttpCode(code)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(ErrorMapper.mapException(e)))
        }
    }

    override suspend fun getProductById(id: String): Result<Product?> {
        return try {
            val entity = try { productDao.getProductById(id) } catch (_: Exception) { null }
            if (entity != null) {
                val category = try { categoryDao.getCategoryById(entity.categoryId) } catch (_: Exception) { null }
                val subCategory = entity.subCategoryId?.let { try { subCategoryDao.getSubCategoryById(it) } catch (_: Exception) { null } }
                Result.success(entity.toDomain(category?.name, subCategory?.name))
            } else {
                val response = productApi.getProductById(id)
                if (response.isSuccessful && response.body() != null) {
                    val dto = response.body()!!
                    val category = try { categoryDao.getCategoryById(dto.categoryId) } catch (_: Exception) { null }
                    val subCategory = dto.subCategoryId?.let { try { subCategoryDao.getSubCategoryById(it) } catch (_: Exception) { null } }
                    Result.success(dto.toDomain(category?.name, subCategory?.name))
                } else {
                    Result.failure(Exception(ErrorMapper.mapHttpCode(response.code())))
                }
            }
        } catch (e: Exception) {
            val local = try { productDao.getProductById(id) } catch (_: Exception) { null }
            if (local != null) {
                val category = try { categoryDao.getCategoryById(local.categoryId) } catch (_: Exception) { null }
                val subCategory = local.subCategoryId?.let { try { subCategoryDao.getSubCategoryById(it) } catch (_: Exception) { null } }
                Result.success(local.toDomain(category?.name, subCategory?.name))
            } else {
                Result.failure(Exception(ErrorMapper.mapException(e)))
            }
        }
    }

    override fun getCategories(): Flow<List<Category>> {
        return categoryDao.getCategories().map { list -> list.map { it.toDomain() } }
    }

    override fun getSubCategories(categoryId: String?): Flow<List<SubCategory>> {
        return subCategoryDao.getSubCategories(categoryId).map { list -> list.map { it.toDomain() } }
    }

    override fun getLastSyncTime(): Flow<Long?> {
        return metadataDao.getLastSyncTimeFlow("PRODUCT_CATALOG")
    }
}
