package util

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder as BrigadierLiteral
import com.mojang.brigadier.builder.RequiredArgumentBuilder as BrigadierArgument
import com.mojang.brigadier.tree.ArgumentCommandNode
import com.mojang.brigadier.tree.CommandNode
import me.ancientri.rimelib.RimeLib
import me.ancientri.rimelib.util.command.command
import net.minecraft.command.CommandSource
import net.minecraft.server.command.ServerCommandSource
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CommandUtilTest {
	@Test
	fun test() {
		val a = BrigadierLiteral.literal<ServerCommandSource>(RimeLib.NAMESPACE)
			.then(
				BrigadierLiteral.literal<ServerCommandSource>("example")
					.then(BrigadierLiteral.literal<ServerCommandSource>("subcommand"))
					.then(BrigadierArgument.argument<ServerCommandSource, String>("subcommand2", StringArgumentType.word()))
				)
			.build()
		val b = command<ServerCommandSource>(RimeLib.NAMESPACE) {
			literal("example") {
				literal("subcommand")
				argument("subcommand2", StringArgumentType.word())
			}
		}
		// compare map elements
		assertTrue(compareCommands(a, b))
	}

	fun compareCommands(a: CommandNode<out CommandSource>, b: CommandNode<out CommandSource>): Boolean {
		if (a.children.size != b.children.size) {
			eprintln(
				"""Size mismatch:
				|  a: ${a.children.size}
				|  b: ${b.children.size}
			""".trimIndent()
			)
			return false
		}
		if (a.name != b.name) {
			eprintln(
				"""Name mismatch:
				|  a: ${a.name}
				|  b: ${b.name}
			""".trimIndent()
			)
			return false
		}
		if (a is ArgumentCommandNode<out CommandSource, *> && b is ArgumentCommandNode<out CommandSource, *>
			&& a.type != b.type && a.type.javaClass != b.type.javaClass
		) {
			eprintln(
				"""Argument type mismatch:
				|  a: ${a.type.javaClass}
				|  b: ${b.type.javaClass}
				""".trimIndent()
			)
			return false
		}
		if (a.children.isEmpty()) return true

		val aIterator = a.children.iterator()
		val bIterator = b.children.iterator()
		repeat(a.children.size) {
			val a = aIterator.next()
			val b = bIterator.next()
			if (!compareCommands(a, b)) return false // The failure will be printed in the recursive call
		}

		return true
	}

	fun eprintln(message: String) {
		System.err.println(message)
	}
}