package com.garon.gmdb.utils

import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.Subject

class DisposableSubject<T>(
    private val subject: Subject<T>
) : DisposableObserver<T>() {

    override fun onNext(next: T) {
        subject.onNext(next)
    }

    override fun onComplete() {
        subject.onComplete()
    }

    override fun onError(e: Throwable) {
        subject.onError(e)
    }
}
