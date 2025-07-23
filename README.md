# AppToolkit

AppToolkit is a versatile Android library designed to streamline development by providing pre-built components and utilities that adhere to modern design principles.

The library initializes Firebase App Check with Play Integrity by default to help protect your Firebase resources from unauthorized access.

## Installation

To integrate AppToolkit into your project, add the following dependency to your `build.gradle` file:

### **Kotlin DSL**:

```kotlin
dependencies {
    api("com.github.MihaiCristianCondrea:AppToolkit:+") {
        isTransitive = true
    }
}
```

### **Groovy DSL**:

```groovy
dependencies {
    api('com.github.MihaiCristianCondrea:AppToolkit:+') {
        transitive = true
    }
}
```

**Note**: Enabling transitive dependencies ensures that all necessary dependencies included in AppToolkit are available in your project, simplifying setup and reducing potential conflicts.

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request. For major changes, open an issue first to discuss your ideas.

## License

This project is licensed under the GNU General Public License v3.0. See the [LICENSE](LICENSE.md) file for details.

## Contact

For any questions or suggestions, feel free to reach out to me at [contact.mihaicristiancondrea@gmail.com](mailto:contact.mihaicristiancondrea@gmail.com).