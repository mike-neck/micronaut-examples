package db.example

import io.micronaut.context.annotation.Factory
import io.micronaut.runtime.Micronaut
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Singleton

object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
                .packages("db.example")
                .mainClass(Application.javaClass)
                .start()
    }
}

@Factory
class AppFactory {

    private val atomicLong: AtomicLong = AtomicLong(0)

    @Singleton
    fun idGen(): IdGen = object : IdGen {
        override fun newId(): Long = atomicLong.incrementAndGet()
    }
}

interface IdGen {
    fun newId(): Long
}
