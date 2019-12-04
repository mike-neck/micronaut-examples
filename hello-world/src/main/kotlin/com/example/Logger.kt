package com.example

import org.slf4j.LoggerFactory

@Suppress("unused")
class Logger<T: Any>(delegate: org.slf4j.Logger): org.slf4j.Logger by delegate {
  companion object {
    inline fun <reified T: Any> get(): Logger<T> = Logger(LoggerFactory.getLogger(T::class.java)) 
  }
}
