package com.garon.gmdb.movies.bl

import org.junit.Rule
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit

class GetMoviesUseCaseTest {

    @Rule
    @JvmField
    val mockitoRule = MockitoJUnit.rule()

    @Mock
    lateinit var api: GetMoviesApi
}