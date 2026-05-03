package nl.abnamro.amrotv.core.testing.robot

/**
 * Marker interface for the action scope of a [Robot].
 *
 * Implement this interface to define the DSL for performing actions (e.g. clicks, input) inside
 * [Robot.execute] blocks.
 */
interface RobotActionScope

/**
 * Marker interface for the verification scope of a [Robot].
 *
 * Implement this interface to define the DSL for asserting UI state inside [Robot.verify] blocks.
 */
interface RobotVerificationScope

/**
 * Base abstraction for the Robot pattern in UI tests.
 *
 * A [Robot] encapsulates all interactions and assertions for a single screen or component.
 * Call [execute] to perform actions and [verify] to assert state. Both blocks receive a
 * strongly-typed scope so only relevant operations are accessible at each call site.
 *
 * @param A the action scope type, must implement [RobotActionScope]
 * @param V the verification scope type, must implement [RobotVerificationScope]
 */
interface Robot<A : RobotActionScope, V : RobotVerificationScope> {

    /**
     * Returns the [RobotActionScope] for this robot.
     *
     * Called internally by [execute] to obtain the scope passed to the block.
     */
    fun actionScope(): A

    /**
     * Returns the [RobotVerificationScope] for this robot.
     *
     * Called internally by [verify] to obtain the scope passed to the block.
     */
    fun verificationScope(): V

    /**
     * Executes UI actions in the action scope of this robot.
     *
     * @param block a lambda with [A] as receiver in which to perform actions
     * @return this robot for chaining with [verify]
     */
    fun execute(block: A.() -> Unit): Robot<A, V> {
        actionScope().block()
        return this
    }

    /**
     * Asserts UI state in the verification scope of this robot.
     *
     * @param block a lambda with [V] as receiver in which to assert state
     * @return this robot for chaining with [execute]
     */
    fun verify(block: V.() -> Unit): Robot<A, V> {
        verificationScope().block()
        return this
    }
}

/**
 * Convenience function that creates a [Robot] and applies [block] to it.
 *
 * @param robot the robot to use
 * @param block an optional block that calls [Robot.execute] and/or [Robot.verify]
 * @return the robot after the block has been applied
 */
fun <T : Robot<*, *>> withRobot(
    robot: T,
    block: T.() -> Unit = {},
): T = robot.apply(block)
