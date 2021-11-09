package org.rfcx.incidents

private val randomCharPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

fun randomAlphanumeric(length: Int = 8): String {
    return (1..length)
            .map { i -> kotlin.random.Random.nextInt(0, randomCharPool.size) }
            .map(randomCharPool::get)
            .joinToString("")
}
