package com.goreecloud.keyboard

internal object KeyboardImeComponentPolicy {
    fun enabledListContainsPackage(
        enabledInputMethods: String?,
        packageName: String,
    ): Boolean =
        enabledInputMethods
            .orEmpty()
            .split(':')
            .any { componentPackage(it) == packageName }

    fun defaultMethodMatchesPackage(
        defaultInputMethod: String?,
        packageName: String,
    ): Boolean =
        componentPackage(defaultInputMethod.orEmpty()) == packageName

    private fun componentPackage(flattenedComponent: String): String? {
        val separator = flattenedComponent.indexOf('/')
        if (separator <= 0) return null

        val candidate = flattenedComponent.substring(0, separator)
        return candidate.takeIf { it.isNotBlank() }
    }
}
