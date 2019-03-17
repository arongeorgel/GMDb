package com.garon.gmdb.utils

import android.app.Activity
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.Subject

fun <T> Any.unsafeListCast(): List<T> {
    @Suppress("UNCHECKED_CAST")
    return this as List<T>
}

fun <T> Subject<T>.toDisposableObserver(): DisposableObserver<T> =
        DisposableSubject(this)

operator fun CompositeDisposable.plusAssign(subscribe: Disposable) {
    add(subscribe)
}

fun View.toggleVisible(show: Boolean) {
    if (show) show() else hide()
}

fun View.hide() {
    this.visibility = GONE
}

fun View.show() {
    this.visibility = VISIBLE
}

fun ViewGroup.selfInflate(@LayoutRes resId: Int) {
    View.inflate(context, resId, this)
}

fun Activity.setScreenOrientation() {
    requestedOrientation = if(resources.getBoolean(com.garon.gmdb.R.bool.portrait_only)){
        android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    } else {
        android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
}

inline fun View.doInRuntime(code: () -> Unit) {
    if (!isInEditMode) code()
}
