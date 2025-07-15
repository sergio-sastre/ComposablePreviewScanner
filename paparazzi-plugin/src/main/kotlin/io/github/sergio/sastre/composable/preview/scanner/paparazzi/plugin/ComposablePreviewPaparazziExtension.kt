package io.github.sergio.sastre.composable.preview.scanner.paparazzi.plugin

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

open class ComposablePreviewPaparazziExtension @Inject constructor(objects: ObjectFactory) {

    /**
     * Whether to enable the generation of Paparazzi tests for Composable Previews.
     */
    val enable: Property<Boolean> = objects.property(Boolean::class.java)

    /**
     * The package names to scan for the Composable Previews.
     */
    val packages: ListProperty<String> = objects.listProperty(String::class.java)

    /**
     * If true, the private previews will be included in the test.
     */
    val includePrivatePreviews: Property<Boolean> = objects.property(Boolean::class.java)

    /**
     * The name of the generated test class.
     */
    val testClassName: Property<String> = objects.property(String::class.java)

    /**
     * The package name for the generated test class.
     */
    val testPackageName: Property<String> = objects.property(String::class.java)
}