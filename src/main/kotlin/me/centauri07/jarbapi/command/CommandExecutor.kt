package me.centauri07.jarbapi.command

import com.github.stefan9110.dcm.builder.CommandBuilder
import com.github.stefan9110.dcm.command.CommandArgument
import com.github.stefan9110.dcm.manager.executor.reply.InteractionResponse
import me.centauri07.jarbapi.command.annotation.Command
import me.centauri07.jarbapi.command.annotation.Executor
import me.centauri07.jarbapi.command.annotation.Option
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters

/**
 * @author Centauri07
 */
open class CommandExecutor : BaseExecutor() {
    override val commandAnnotation: Command by lazy { this::class.findAnnotation()!! }
    override var executor: KFunction<InteractionResponse>? = null
    override val data: BaseExecutorData by lazy { BaseExecutorData(commandAnnotation.name.split("\\.").first(), commandAnnotation.description) }

    private val subCommandFunctions: MutableMap<String, KFunction<InteractionResponse>> = mutableMapOf()

    init {

        val functions = this::class.memberFunctions

        executor = functions.firstOrNull { it.hasAnnotation<Executor>() } as KFunction<InteractionResponse>?

        functions.filter { it.hasAnnotation<Command>() }.forEach {
            subCommandFunctions[it.findAnnotation<Command>()!!.name] = it as KFunction<InteractionResponse>
        }

        if (subCommandFunctions.isEmpty() && executor == null) throw NullPointerException("Cannot find function annotated with Executor")

    }

    override fun build(): com.github.stefan9110.dcm.command.Command {

        val commandBuilder = CommandBuilder.create(data.name)

        commandBuilder.setDescription(data.description)

        for (entry in subCommandFunctions.entries) {
            val keys = entry.key.split(".")

            var current: BaseExecutor = this

            for (i in keys.indices) {
                val key = keys[i]

                if (current.subCommands.containsKey(key)) {
                    current = current.subCommands[key]!!

                    continue
                }

                val childSubCommandExecutor = SubCommandExecutor(
                    this,
                    null,
                    entry.value.findAnnotation()!!,
                    BaseExecutorData(key, entry.value.findAnnotation<Command>()!!.description)
                )

                current.subCommands[key] = childSubCommandExecutor

                if (i == keys.size - 1) {
                    childSubCommandExecutor.executor = entry.value
                } else {
                    current = childSubCommandExecutor
                }

            }

        }

        subCommands.values.forEach {
            commandBuilder.addSubCommand(
                it.build()
            )
        }

        executor?.let {
            commandBuilder.addArguments(
                *executor!!.valueParameters.filter { it.hasAnnotation<Option>() }.map {
                    it.findAnnotation<Option>()
                }.map { CommandArgument(it!!.type, it.name, it.description, it.required) }.toTypedArray()
            )
        }

        commandBuilder.setCommandExecutor(this)

        return commandBuilder.build(true)


    }


}