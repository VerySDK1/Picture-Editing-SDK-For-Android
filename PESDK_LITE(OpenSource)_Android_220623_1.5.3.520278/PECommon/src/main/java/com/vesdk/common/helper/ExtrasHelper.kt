package com.vesdk.common.helper

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <U, T> bindExtra(key: String, default: T) = BindLoader<U, T>(key, default)

fun <U, T> bindExtra(key: String) = BindLoader<U, T?>(key, null)

fun <U, T> bindArgument(key: String, default: T) = BindLoader<U, T>(key, default)

fun <U, T> bindArgument(key: String) = BindLoader<U, T?>(key, null)


private class IntentDelegate<in U, out T>(val key: String, val default: T) :
    ReadOnlyProperty<U, T> {
    override fun getValue(thisRef: U, property: KProperty<*>): T {
        return when (thisRef) {
            is Fragment -> thisRef.arguments?.take<T>(key)
            is android.app.Fragment -> thisRef.arguments?.take<T>(key)
            is AppCompatActivity -> thisRef.intent?.extras?.take<T>(key)
            is Activity -> thisRef.intent?.extras?.take<T>(key)
            else -> default
        } ?: default
    }
}

class BindLoader<in U, out T>(val key: String, val default: T) {

    operator fun provideDelegate(thisRef: U, prop: KProperty<*>): ReadOnlyProperty<U, T> {
        return IntentDelegate(key, default)
    }

}

@Suppress("UNCHECKED_CAST")
fun <T> Bundle.take(key: String): T? {
    try {
        return get(key) as? T?
    } catch (e: Exception) {
        e.log()
    }
    return null
}