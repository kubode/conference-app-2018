package io.github.droidkaigi.confsched2018.presentation.detail

import android.arch.lifecycle.Observer
import com.nhaarman.mockito_kotlin.*
import io.github.droidkaigi.confsched2018.DUMMY_SESSION_ID_1
import io.github.droidkaigi.confsched2018.createDummySession
import io.github.droidkaigi.confsched2018.createDummySessions
import io.github.droidkaigi.confsched2018.data.repository.SessionRepository
import io.github.droidkaigi.confsched2018.model.Session
import io.github.droidkaigi.confsched2018.presentation.Result
import io.github.droidkaigi.confsched2018.util.rx.TestSchedulerProvider
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SessionDetailViewModelTest {
    @Mock private val repository: SessionRepository = mock()

    private lateinit var viewModel: SessionDetailViewModel

    @Before fun init() {
        whenever(repository.refreshSessions()).doReturn(Completable.complete())
    }

    @Test fun sessions_Empty() {
        whenever(repository.sessions).doReturn(Flowable.empty())
        viewModel = SessionDetailViewModel(repository, TestSchedulerProvider())
        val result: Observer<Result<Session>> = mock()

        viewModel.session.observeForever(result)

        verify(repository).sessions
        verify(result).onChanged(Result.inProgress())
    }

    @Test fun sessions_Basic() {
        val sessions = createDummySessions()
        whenever(repository.sessions).doReturn(Flowable.just(sessions))
        viewModel = SessionDetailViewModel(repository, TestSchedulerProvider())
        viewModel.sessionId = DUMMY_SESSION_ID_1
        val result: Observer<Result<Session>> = mock()

        viewModel.session.observeForever(result)

        verify(repository).sessions
        verify(result).onChanged(Result.success(createDummySession(DUMMY_SESSION_ID_1)))
    }

    @Test fun sessions_Error() {
        val runtimeException = RuntimeException("test")
        whenever(repository.sessions).doReturn(Flowable.error(runtimeException))
        viewModel = SessionDetailViewModel(repository, TestSchedulerProvider())
        val result: Observer<Result<Session>> = mock()

        viewModel.session.observeForever(result)

        verify(repository).sessions
        verify(result).onChanged(Result.failure(runtimeException.message!!, runtimeException))
    }

    @Test fun favorite() {
        whenever(repository.favorite(any())).doReturn(Single.just(true))
        viewModel = SessionDetailViewModel(repository, TestSchedulerProvider())
        val session = mock<Session>()

        viewModel.onFavoriteClick(session)

        verify(repository).favorite(session)
    }
}
