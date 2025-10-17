/**

* Returns a flow that invokes the given [action] **after** the flow is completed or cancelled, passing
* the cancellation exception or failure as cause parameter of [action].
*
* Conceptually, `onCompletion` is similar to wrapping the flow collection into a `finally` block,
* for example the following imperative snippet:
*
* ```
* runCatching {
*      myFlow.collect { value ->
*      println(value)
* }
* }.onFailure { e ->
*      e.printStackTrace()
* }.also {
*      println("Done")
* }
* ```
*
* can be replaced with a declarative one using `onCompletion`:
*
* ```
* myFlow
*     .onEach { println(it) }
*     .onCompletion { println("Done") }
*     .collect()
* ```
*
* Unlike [catch], this operator reports exception that occur both upstream and downstream
* and observe exceptions that are thrown to cancel the flow. Exception is empty if and only if
* the flow had fully completed successfully. Conceptually, the following code:
*
* ```
* myFlow.collect { value ->
*     println(value)
* }
* println("Completed successfully")
* ```
*
* can be replaced with:
*
* ```
* myFlow
*     .onEach { println(it) }
*     .onCompletion { if (it == null) println("Completed successfully") }
*     .collect()
* ```
*
* The receiver of the [action] is [FlowCollector] and this operator can be used to emit additional
* elements at the end **if it completed successfully**. For example:
*
* ```
* flowOf("a", "b", "c")
*     .onCompletion { emit("Done") }
*     .collect { println(it) } // prints a, b, c, Done
* ```
*
* In case of failure or cancellation, any attempt to emit additional elements throws the corresponding exception.
* Use [catch] if you need to suppress failure and replace it with emission of elements.
  */