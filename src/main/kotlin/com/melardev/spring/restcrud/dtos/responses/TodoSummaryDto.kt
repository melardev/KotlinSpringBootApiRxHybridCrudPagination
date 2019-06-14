package com.melardev.spring.restcrud.dtos.responses

import com.melardev.spring.restcrud.entities.Todo
import java.time.LocalDateTime


class TodoSummaryDto(val id: Long?, val title: String, val isCompleted: Boolean, val createdAt: LocalDateTime, val updatedAt: LocalDateTime) {
    companion object {

        fun build(todo: Todo): TodoSummaryDto {
            return TodoSummaryDto(todo.id, todo.title, todo.isCompleted, todo.createdAt!!, todo.updatedAt!!)
        }
    }
}
