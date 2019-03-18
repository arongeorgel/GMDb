package com.garon.gmdb.movies.bl

import com.garon.gmdb.movies.MoviesView
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.TestSubscriber
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import retrofit2.Response
import java.util.Calendar

class GetMoviesUseCaseTest {

    private companion object {
        val successResponse = MovieChangesResponse(results = listOf(
                MovieChangesResponse.MovieChanges(1, false),
                MovieChangesResponse.MovieChanges(2, true),
                MovieChangesResponse.MovieChanges(3, false),
                MovieChangesResponse.MovieChanges(4, false)
        ))
        val successResult = listOf(1, 3, 4)
    }

    @Rule
    @JvmField
    val mockitoRule = MockitoJUnit.rule()

    @Mock
    lateinit var api: GetMoviesApi

    private lateinit var useCase: GetMoviesUseCase

    @Before
    fun setup() {
        val calendar = Calendar.getInstance()
        calendar.set(2019, 2, 17)

        useCase = GetMoviesUseCase(api, calendar, Schedulers.trampoline())
    }

    @Test
    fun `when useCase executed then return success response`() {
        whenever(api.getChanges("2019-03-14", "2019-03-17"))
                .thenReturn(Observable.just(Response.success(successResponse)))

        val testObserver = useCase.execute(MoviesView.DaysDateRange.LAST_3).test()

        testObserver.run {
            assertNoErrors()
            assertComplete()
            assertValueCount(2)
            assertValueAt(1) { (it as MoviesResult.Success).list == successResult }
        }
    }

    fun `when useCase executed then return failed response`() {

    }

    @Test
    fun `get date range for last 3 days`() {
        val actual = useCase.getDateRange(MoviesView.DaysDateRange.LAST_3)
        assertEquals(Pair("2019-03-14", "2019-03-17"), actual)
    }

    @Test
    fun `get date range for last 5 days`() {
        val actual = useCase.getDateRange(MoviesView.DaysDateRange.LAST_5)
        Assert.assertEquals(Pair("2019-03-12", "2019-03-17"), actual)
    }

    @Test
    fun `get date range for last 10 days`() {
        val actual = useCase.getDateRange(MoviesView.DaysDateRange.LAST_10)
        Assert.assertEquals(Pair("2019-03-07", "2019-03-17"), actual)
    }

    @Test
    fun `get date range for last 14 days`() {
        val actual = useCase.getDateRange(MoviesView.DaysDateRange.LAST_14)
        Assert.assertEquals(Pair("2019-03-03", "2019-03-17"), actual)
    }
}
