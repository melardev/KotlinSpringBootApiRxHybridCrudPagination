package com.melardev.spring.restcrud.services

import com.melardev.spring.restcrud.config.DbConfig.Companion.DB_SCHEDULER
import com.melardev.spring.restcrud.entities.Todo
import com.melardev.spring.restcrud.repositories.TodosRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Service
class TodoService {
    @Autowired
    private lateinit var todosRepository: TodosRepository

    @Autowired
    private lateinit var transactionTemplate: TransactionTemplate


    fun findAllHqlSummary(page: Int, pageSize: Int): Flux<Todo> {
        val pageRequest = PageRequest.of(page, pageSize)
        val defer = Flux.defer { Flux.fromIterable(this.todosRepository.findAllHqlSummary(pageRequest)) }
        return defer.subscribeOn(DB_SCHEDULER)
    }


    fun findAllHqlSummaryAsMonoPage(page: Int, pageSize: Int): Mono<Page<Todo>> {
        val pageRequest = PageRequest.of(page, pageSize)
        val resu = Mono.fromCallable { this.todosRepository.findAllHqlSummary(pageRequest) }.subscribeOn(DB_SCHEDULER)
        return resu
    }

    fun findAllPending(page: Int, pageSize: Int): Flux<Todo> {
        val pageRequest = PageRequest.of(page, pageSize)
        val defer = Flux.defer { Flux.fromIterable(this.todosRepository.findByHqlPending(pageRequest)) }
        return defer.subscribeOn(DB_SCHEDULER)
    }

    fun findAllCompleted(page: Int, pageSize: Int): Flux<Todo> {
        val defer = Flux.defer { Flux.fromIterable(this.todosRepository.findByHqlCompleted()) }
        return defer.subscribeOn(DB_SCHEDULER)
    }

    fun findById(id: Long): Mono<Optional<Todo>> {

        return Mono
                .defer { Mono.just(todosRepository.findById(id)) }
                .subscribeOn(DB_SCHEDULER)
    }

    fun save(todo: Todo): Mono<Todo> {
        return Mono.fromCallable<Todo> {
            transactionTemplate.execute { status ->
                val persistedTodo = todosRepository.save(todo)
                persistedTodo
            }
        }.subscribeOn(DB_SCHEDULER)
    }

    fun deleteAll(): Mono<Boolean> {
        return Mono.fromCallable {
            todosRepository.deleteAll()
            true
        }.subscribeOn(DB_SCHEDULER)
    }

    fun delete(todo: Optional<Todo>): Mono<Boolean> {
        return if (!todo.isPresent) Mono.empty() else Mono.defer {
            todosRepository.delete(todo.get())
            Mono.just(true)
        }.subscribeOn(DB_SCHEDULER)
    }

    fun count(): Mono<Long> {
        return Mono.defer { Mono.just(todosRepository.count()) }.subscribeOn(DB_SCHEDULER)
    }

    fun saveAll(todos: Set<Todo>): Flux<Todo> {
        return Flux.defer { Flux.fromIterable(this.todosRepository.saveAll(todos)) }.subscribeOn(DB_SCHEDULER)
    }

    fun getCompletedCount(): Mono<Long> {
        return Mono
                .defer { Mono.just(this.todosRepository.getCompletedCount()) }
                .subscribeOn(DB_SCHEDULER)
    }

    fun getPendingCount(): Mono<Long> {
        return Mono
                .defer { Mono.just(this.todosRepository.getPendingCount()) }
                .subscribeOn(DB_SCHEDULER)
    }
}
