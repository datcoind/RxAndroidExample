package com.example.rxandroid

import io.reactivex.rxjava3.core.Observable
import java.io.Serializable

data class User(var id: Int, var name: String) : Serializable {
    public fun getNameObservable(): Observable<String> {
        return Observable.just(name)
    }

    public fun getNameDeferObservable(): Observable<String> {
        // defer trì hoãn cái gì đấy.
        return Observable.defer { Observable.just(name) }
    }
}