package com.example.rxandroid.observable

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.rxandroid.R
import com.example.rxandroid.User
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.Serializable

private const val TAG = "ObservableActivity"

class ObservableActivity : AppCompatActivity() {
    private var mDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_main)

        val observable: Observable<User> = getObservableUsers()
        val observer = getObserverUser()

        // đăng kí lắng nghe
        // đăng kí thread khi phát dữ liệu và nhận dữ liệu
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer)
    }

    // observer: lắng nghe dữ liệu từ observable
    private fun getObserverUser(): Observer<User> {
        return object : Observer<User> {
            override fun onSubscribe(d: Disposable) {
                Log.e(TAG, "onSubscribe: ")
                mDisposable = d
            }

            override fun onNext(user: User) {
                Log.e(TAG, "onNext: $user + Thread: ${Thread.currentThread().name}")
            }

            override fun onError(e: Throwable) {
                Log.e(TAG, "onError: " + e.printStackTrace())
            }

            override fun onComplete() {
                Log.e(TAG, "onComplete: " + Thread.currentThread().name)
            }

        }
    }

    // Observable: là luồng dữ liệu thực hiện 1 số công việc và phát ra dữ liệu.
    private fun getObservableUsers(): Observable<User> {
        val listUser = getListUsers()
        /**
         *  tạo ra 1 observable bằng create(): tự thiết kế hoạt động của observer
         *  bằng cách gọi các phương thức onError, onNext, onCompleted 1 cách thích hợp.
         *  Lưu ý: onError, onCompleted chỉ được gọi duy nhất 1 lần và sau đó k gọi thêm bất cứ hàm nào của Observer.
         *  */
        return Observable.create(object : ObservableOnSubscribe<User> {
            override fun subscribe(emitter: ObservableEmitter<User>) {
                Log.e(TAG, "subscribe: " + Thread.currentThread().name)
                // emitter: phát ra dữ liệu
                if (listUser.isEmpty()) {
                    // thông báo lỗi
                    if (!emitter.isDisposed) {
                        emitter.onError(Exception())
                    }
                }

                for (user in listUser) {
                    // phải đảm bảo observable và observer phải kết nối với nhau
                    if (!emitter.isDisposed) {
                        // phát đi dữ liệu user
                        emitter.onNext(user)
                    }
                }

                // thông báo cho observer là đã hoàn thành công việc
                if (!emitter.isDisposed) {
                    emitter.onComplete()
                }

            }
        })
    }

    /**
     * Operator: hỗ trợ cho việc sửa đổi dữ liệu được phát ra bởi Observable trước khi Observe nhận chúng
     * */

    /**
     * Schedulers: quyết định thread mà Observable sẽ phát ra dữ liệu và Observer sẽ nhận dữ liệu
     * */

    private fun getListUsers(): ArrayList<User> {
        val list = arrayListOf<User>()
        for (i in 1 until 10) {
            list.add(User(i, "User $i"))
        }
        return list
    }

    // observer: lắng nghe dữ liệu từ observable
    private fun getObserverUserFromArray(): Observer<User> {
        return object : Observer<User> {
            override fun onSubscribe(d: Disposable) {
                Log.e(TAG, "onSubscribe: ")
                mDisposable = d
            }

            override fun onNext(user: User) {
                Log.e(TAG, "onNext: $user + Thread: ${Thread.currentThread().name}")
            }

            override fun onError(e: Throwable) {
                Log.e(TAG, "onError: " + e.printStackTrace())
            }

            override fun onComplete() {
                Log.e(TAG, "onComplete: " + Thread.currentThread().name)
            }

        }
    }

    /**
     * tạo ra 1 observable bằng fromArray(): chuyển đổi 1 list object dữ liệu
     * nào đó thành 1 Observable. Sau đó Observable sẽ phát ra lần lượt các item đó.
     * và chúng được xử lí trong onNext. Sau khi hoàn thành sẽ gọi đến onCompleted()
     * */
    private fun getObservableUsersFromArray(): Observable<User> {
        val user1 = User(1, "User 1")
        val user2 = User(2, "User 2")
        val arrayUser = arrayOf(user1, user2)
        return Observable.fromArray(user1, user2)
    }

    private fun getObserverUserJust(): Observer<Serializable> {
        return object : Observer<Serializable> {
            override fun onSubscribe(d: Disposable) {
                Log.e(TAG, "onSubscribe: ")
                mDisposable = d
            }

            override fun onNext(serializable: Serializable) {
                Log.e(TAG, "onNext: $serializable + Thread: ${Thread.currentThread().name}")
                if (serializable is Array<*>) {
                    val listUser = serializable as List<*>
                    for (user in listUser) {
                        if (user is User) {
                            Log.e(TAG, "onNext: User: $user")
                        }
                    }
                } else if (serializable is String) {
                    val stringData = serializable
                    Log.e(TAG, "onNext: String: $stringData")
                } else if (serializable is User) {
                    val user = serializable
                    Log.e(TAG, "onNext: User: $user")
                }
            }

            override fun onError(e: Throwable) {
                Log.e(TAG, "onError: " + e.printStackTrace())
            }

            override fun onComplete() {
                Log.e(TAG, "onComplete: " + Thread.currentThread().name)
            }

        }
    }

    /**
     * tạo ra 1 observable bằng just(): chuyển đổi 1 object hoặc 1 tập hợp các object
     * thành Observable và phát ra nó. Với just giả sử nếu truyền vào 1 array đó sẽ
     * chuyển đổi thành Observable và phát ra chính array đó.
     * */
    private fun getObservableUsersJust(): Observable<Serializable> {
        val listUser = getListUsers()
        val user1 = User(1, "User 1")
        val user2 = User(2, "User 2")
        val user3 = User(3, "User 3")

        val strData = "User Data"

        val arrayUser = arrayOf(user1, user2)
        return Observable.just(arrayUser, strData, user3)
    }

    override fun onDestroy() {
        // hủy kết nối giữa 2 thằng dùng disposables. tránh rò rỉ bộ nhớ.
        mDisposable?.dispose()
        super.onDestroy()
    }
}