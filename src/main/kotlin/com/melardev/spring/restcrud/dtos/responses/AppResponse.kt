package com.melardev.spring.restcrud.dtos.responses

import java.util.ArrayList

open class AppResponse {
    var success: Boolean? = null
        private set
    private var fullMessages: MutableList<String>? = null

    val isSuccess: Boolean
        get() = success!!

    constructor(success: Boolean, message: String) {
        this.success = success
        addFullMessage(message)
    }

    fun getFullMessages(): List<String>? {
        return fullMessages
    }

    fun setFullMessages(fullMessages: MutableList<String>) {
        this.fullMessages = fullMessages
    }

    constructor() {
        println("Created AppResponse")
    }

    constructor(success: Boolean) {
        this.success = success
        fullMessages = ArrayList()
    }


    protected fun addFullMessage(message: String?) {
        if (message == null)
            return
        if (fullMessages == null)
            fullMessages = ArrayList()

        fullMessages!!.add(message)
    }

}