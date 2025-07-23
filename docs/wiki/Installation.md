# Installation

Follow these steps to include **AppToolkit** in your Android project.

## Kotlin DSL
```kotlin
dependencies {
    api("com.github.MihaiCristianCondrea:AppToolkit:+") {
        isTransitive = true
    }
}
```

## Groovy DSL
```groovy
dependencies {
    api('com.github.MihaiCristianCondrea:AppToolkit:+') {
        transitive = true
    }
}
```

Enabling transitive dependencies ensures all required libraries are available.
