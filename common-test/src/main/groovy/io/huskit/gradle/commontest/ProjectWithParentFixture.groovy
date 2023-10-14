package io.huskit.gradle.commontest

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory

@Canonical
@CompileStatic
@TupleConstructor
class ProjectWithParentFixture {

    final Project project
    final Project parentProject
    final File projectDir
    final File parentProjectDir
    final ObjectFactory objects
    final ProviderFactory providers
}
