package com.warlabel

class LabelUtils {

    private val labels = Label::class.nestedClasses.flatMap { it.nestedClasses }
        .map { it.objectInstance as Label }
    private val groups = Label::class.nestedClasses
    val labelValues = labels.map { it.label }
    var availableLabelString: String = "Valid labels are:\n"
    var labelMap =mutableMapOf<String?,List<Label>>()
    var availableLabels:MutableList<String> = mutableListOf()
    init {
        labelValues.forEach { availableLabelString += "$it\n" }
        groups.forEach { it.simpleName?.let { name ->
            labelMap[name] = it.nestedClasses.map { it.objectInstance as Label }
        }}
        labelMap.keys.forEach {
            var labelString = "$it:\n"
            val values = labelMap[it]
            values!!.forEach {label-> labelString+="${label.label}\n" }
            availableLabels.add(labelString)
        }

    }

    fun getLabel(input: String): Label? {
        return labels.firstOrNull { it.label == input }
    }

}