This gradle module contains the `internal-convention-plugin`,
which is a plugin that applies the conventions that are used in other projects in this repository.
The plugin is applied to the project by adding the following line to the build.gradle file:

```groovy
plugins {
    id 'internal-convention-plugin'
}
```