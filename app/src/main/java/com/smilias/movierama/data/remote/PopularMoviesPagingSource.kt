package com.smilias.movierama.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.smilias.movierama.data.mapper.toMovie
import com.smilias.movierama.domain.model.Movie
import com.smilias.movierama.util.Constants.NETWORK_PAGE_SIZE
import com.smilias.movierama.util.Constants.STARTING_PAGE
import okio.IOException
import retrofit2.HttpException

class PopularMoviesPagingSource(
    private val movieApi: MovieApi
) : PagingSource<Int, Movie>() {

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val position = params.key ?: STARTING_PAGE
        return try {
            val response = movieApi.getPopularMovies(position)
            val moviesDto = response.movies
            val nextKey = if (moviesDto.isEmpty()) {
                null
            } else {
                position + (params.loadSize / NETWORK_PAGE_SIZE)
            }
            LoadResult.Page(
                data = moviesDto.map { it.toMovie() },
                prevKey = if (position == STARTING_PAGE) null else position - 1,
                nextKey = nextKey
            )

        } catch (e: IOException) {
            LoadResult.Error(Exception("No internet connection"))
        } catch (e: HttpException) {
            LoadResult.Error(Exception("Network error, please try again later"))
        }
    }

}