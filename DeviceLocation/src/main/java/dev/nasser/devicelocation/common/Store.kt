package dev.nasser.devicelocation.common

/**
 * Created by Nasser Munshi on 03-Jul-2023.
 * @author Nasser Munshi
 */
interface Store<T> {
    operator fun get(key: String?): T
    fun put(key: String?, value: T)
    fun remove(key: String?)
}