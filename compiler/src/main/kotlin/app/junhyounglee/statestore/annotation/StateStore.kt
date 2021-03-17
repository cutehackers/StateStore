package app.junhyounglee.statestore.annotation

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
annotation class StateStore(val store: KClass<*>)