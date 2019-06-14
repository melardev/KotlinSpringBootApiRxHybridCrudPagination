package com.melardev.spring.restcrud.controllers

import com.melardev.spring.restcrud.dtos.responses.*
import com.melardev.spring.restcrud.entities.Todo
import com.melardev.spring.restcrud.services.TodoService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*
import java.util.function.Function
import javax.validation.Valid

@CrossOrigin
@RestController
@RequestMapping("/api/todos")
class TodosController(@Autowired
                      private val todoService: TodoService) {

    @GetMapping
    fun getAll(request: ServerHttpRequest,
               @RequestParam(value = "page", defaultValue = "1") page: Int,
               @RequestParam(value = "page_size", defaultValue = "5") pageSize: Int): Mono<out AppResponse> {
        val todos = todoService.findAllHqlSummary(page, pageSize)
        return getResponseFromTodosFlux(todos, todoService.count(), request, page, pageSize)
    }


    @GetMapping("/pending")
    fun getPending(request: ServerHttpRequest,
                   @RequestParam(value = "page", defaultValue = "1") page: Int,
                   @RequestParam(value = "page_size", defaultValue = "5") pageSize: Int): Mono<out AppResponse> {
        val todos = todoService.findAllPending(page, pageSize)
        return getResponseFromTodosFlux(todos, todoService.getPendingCount(), request, page, pageSize)
    }


    @GetMapping("/completed")
    fun getCompleted(request: ServerHttpRequest,
                     @RequestParam(value = "page", defaultValue = "1") page: Int,
                     @RequestParam(value = "page_size", defaultValue = "5") pageSize: Int): Mono<out AppResponse> {
        val todos = todoService.findAllCompleted(page, pageSize)
        return getResponseFromTodosFlux(todos, todoService.getCompletedCount(), request, page, pageSize)
    }

    @GetMapping("/{id}")
    operator fun get(@PathVariable("id") id: Long): Mono<ResponseEntity<out AppResponse>> {
        return this.todoService.findById(id)
                .map { optionalTodo ->
                    if (optionalTodo.isPresent) {
                        val todo = optionalTodo.get()
                        ResponseEntity.ok(TodoDetailsResponse(todo))
                    } else
                        ResponseEntity(ErrorResponse("Todo not found"), HttpStatus.NOT_FOUND)
                }
    }

    @PostMapping
    fun create(@Valid @RequestBody todo: Todo): Mono<ResponseEntity<out AppResponse>> {
        return todoService.save(todo)
                .map { savedTodo -> ResponseEntity(TodoDetailsResponse(savedTodo, "Todo created successfully"), HttpStatus.CREATED) }
    }


    @PutMapping("/{id}")
    fun update(@PathVariable("id") id: Long, @RequestBody todoInput: Todo): Mono<ResponseEntity<*>> {
        // Do you know how to make it better? Let me know on Twitter or Pull request please.
        return todoService.findById(id)
                .map(Function<Optional<Todo>, Mono<ResponseEntity<*>>> { t ->
                    if (!t.isPresent)
                        return@Function Mono.just(ResponseEntity(ErrorResponse("Not found"), HttpStatus.NOT_FOUND) as ResponseEntity<*>)

                    val todo = t.get()
                    val title = todoInput.title
                    todo.title = title

                    val description = todoInput.description
                    todo.description = description


                    todo.isCompleted = todoInput.isCompleted

                    todoService.save(todo)
                            .flatMap { todo1 -> Mono.just(ResponseEntity.ok<Any>(TodoDetailsResponse(todo1, "Todo Updated successfully"))) }
                }).flatMap { responseEntityMono -> responseEntityMono.map<ResponseEntity<*>> { responseEntity -> responseEntity } }
    }


    @DeleteMapping("/{id}")
    fun delete(@PathVariable("id") id: Long): Mono<ResponseEntity<out AppResponse>> {
        return todoService.findById(id)
                .flatMap { ot -> todoService.delete(ot) }
                .map { ResponseEntity.ok<AppResponse>(SuccessResponse("You have successfully deleted the article")) as ResponseEntity<out AppResponse> }
                .defaultIfEmpty(ResponseEntity<AppResponse>(ErrorResponse("Todo not found"), HttpStatus.NOT_FOUND))
    }


    @DeleteMapping
    fun deleteAll(): Mono<ResponseEntity<out AppResponse>> {
        return todoService.deleteAll().then(Mono.just(
                (ResponseEntity(SuccessResponse("All todos deleted successfully"), HttpStatus.OK)) as ResponseEntity<out AppResponse>))
    }

    private fun getResponseFromTodosFlux(todoFlux: Flux<Todo>, countMono: Mono<Long>, request: ServerHttpRequest, page: Int, pageSize: Int): Mono<AppResponse> {
        return todoFlux.collectList().flatMap<AppResponse> { todoList ->
            countMono
                    .map<PageMeta> { totalItemsCount -> PageMeta.build(todoList, request.uri.path, page, pageSize, totalItemsCount) }
                    .map<AppResponse> { pageMeta -> TodoListResponse.build(pageMeta, todoList) }
        }
    }

}
