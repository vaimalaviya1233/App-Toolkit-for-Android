# Coroutine dispatcher extensions for tests

The test sources expose a pair of JUnit 5 extensions that prepare the `Dispatchers.Main` dispatcher before each test and reset it afterwards:

- `StandardDispatcherExtension` creates a fresh `StandardTestDispatcher` for every test case. Use it when your subject under test relies on cooperative scheduling or when you need to manually advance virtual time.
- `UnconfinedDispatcherExtension` exposes a shared `UnconfinedTestDispatcher`. Use it for tests that benefit from eager execution without explicit scheduler control.

Both modules (`app` and `apptoolkit`) host their own copy of these helpers so they can be imported from their respective packages.

## Usage pattern

```kotlin
class ExampleViewModelTest {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = StandardDispatcherExtension()
    }

    @Test
    fun `example assertion`() = runTest(dispatcherExtension.testDispatcher) {
        val dispatchers = TestDispatchers(dispatcherExtension.testDispatcher)
        // exercise and verify subject under test
    }
}
```

Key points:

- Always opt-in via `@RegisterExtension` to ensure `Dispatchers.Main` is configured before the test body executes.
- Pass `dispatcherExtension.testDispatcher` to `runTest` so coroutine work shares the same scheduler managed by the extension.
- Inject the dispatcher into collaborators via `TestDispatchers` (in the `app` module) or by wiring the dispatcher directly in your fake dependencies.
- Prefer the standard dispatcher for deterministic scheduling; switch to the unconfined variant when you explicitly need eager execution.

The helper tests in `app` and `apptoolkit` assert that these extensions install the expected dispatcher instances, protecting against regressions should the implementations change.
