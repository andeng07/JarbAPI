package me.centauri07.jarbapi.form.field

import jdk.jshell.spi.ExecutionControl.NotImplementedException
import me.centauri07.jarbapi.form.NoFieldFoundException

class GroupFormField(
    name: String, description: String, required: Boolean = true, override var value: List<FormField<*>>? = listOf()
) : FormField<List<FormField<*>>>(name, description, required) {

    //companion object {
    //   var inquiryMessage = "Enter %name%"
    //}

    //override var inquiryMessage: String = GroupFormField.inquiryMessage.replace("%name%", name)

    override fun validate(predicate: List<FormField<*>>.() -> Boolean, failureMessage: String): Boolean =
        throw NotImplementedException("${this::class.java.name} does not support validation.")

    override fun set(value: Any): Result<FormField<*>> {
        return next()?.set(value) ?: Result.failure(NoFieldFoundException())
    }

    override fun next(): FormField<*>? {
        var curr: FormField<*>? = value?.find { !it.acknowledged }

        while (curr != null && curr is GroupFormField) {
            if (!curr.required) break

            val child = curr.next()

            if (child == null) {
                curr.acknowledged = true
                curr = value?.find { !it.acknowledged }
            } else curr = child

        }

        return curr
    }

}