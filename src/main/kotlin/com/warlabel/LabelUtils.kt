package com.warlabel

class LabelUtils {

    private val labels = Label::class.nestedClasses.flatMap { it.nestedClasses }
        .map { it.objectInstance as Label }

    val labelValues = labels.map { it.label }
    var availableLabelString: String = "Valid labels are:\n"

    init {
        labelValues.forEach { availableLabelString += "$it\n" }
    }

    fun getLabel(input: String): Label? {
        return labels.firstOrNull { it.label == input }
    }

}