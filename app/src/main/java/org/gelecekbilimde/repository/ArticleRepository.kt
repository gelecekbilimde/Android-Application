package org.gelecekbilimde.repository

import org.gelecekbilimde.data.entity.Article
import org.gelecekbilimde.data.entity.Categories
import org.gelecekbilimde.data.entity.MarkedArticle
import org.gelecekbilimde.data.local.ArticleDao
import org.gelecekbilimde.data.local.CategoryDao
import org.gelecekbilimde.data.local.MarkedArticleDao
import org.gelecekbilimde.data.model.ArticleResult
import org.gelecekbilimde.data.model.CategoryResult
import org.gelecekbilimde.data.remote.AppApi
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 *
 * @author ferhatozcelik
 * @since 2023-03-31
 */

@Singleton
class ArticleRepository @Inject constructor(private val appApi: AppApi, private val articleDao: ArticleDao,
                                            private val markedArticleDao: MarkedArticleDao, private val categoryDao: CategoryDao) {

    /**
     *  Article
     */

    fun getArticle() = articleDao.getArticle()

    fun getMarkedArticle() = markedArticleDao.getMarkedArticle()

    fun getArticleById(id: Int) = articleDao.getItemById(id)

    fun getMarkedArticleById(id: Int) = markedArticleDao.getItemById(id)

    fun getCategories() = categoryDao.getCategories()

    suspend fun getCategory(id: Int) = categoryDao.getItemById(id)

    suspend fun setCategoriesActive(isActive: Boolean) = categoryDao.setCategoriesActive(isActive)

    suspend fun insertOrUpdate(categories: List<Categories>) {
        categoryDao.insertOrUpdate(categories)
    }

    suspend fun flushArticleInsert(article: List<Article>, flush: Boolean) {
        if (flush){
            articleDao.flushInsert(article)
        }else{
            articleDao.insertAll(article)
        }
    }

    suspend fun getArticle(page: Int, categories: String?, search:String?): Response<List<ArticleResult>> {
        return appApi.getArticle(page, categories, search)
    }

    suspend fun getAPICategories(): Response<List<CategoryResult>> {
        return appApi.getCategories()
    }

    suspend fun getCategoryActive(categoryId: Int?): List<Categories> {
        if (categoryId == 0) {
            categoryDao.updateResetActive(false)
            categoryDao.update(getCategory(categoryId)?.copy(isActive = true))
        } else {
            categoryDao.update(getCategory(0)?.copy(isActive = false))
            categoryDao.update(getCategory(categoryId!!)?.copy(isActive = !getCategory(categoryId)?.isActive!!))
        }
        return setCategoriesActive(true)
    }

    /**
     *  Article Bookmarks
     */

    suspend fun getBookmarkArticle() = markedArticleDao.getBookmarkArticle()

    suspend fun removeArticleBookmarks(markedArticle: MarkedArticle) {
        if (markedArticle.isBookmark == true) {
            markedArticleDao.deleteById(markedArticle.articleId)
        }
    }

    suspend fun insertBookmark(article: Article) {
        if (article.isBookmark == true) {
            markedArticleDao.deleteById(article.articleId)
            articleDao.update(article.copy(isBookmark = false))
        } else {
            markedArticleDao.insert(
                MarkedArticle(
                    article.articleId,
                    article.articleTitle,
                    article.articleContent,
                    article.articleCategory,
                    article.articleImageUrl,
                    article.authorId,
                    article.authorName,
                    article.authorUrl,
                    true,
                    article.articleCreateAtDate
                )
            )
            articleDao.update(article.copy(isBookmark = true))
        }

    }

}