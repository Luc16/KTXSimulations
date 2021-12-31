package com.github.Luc16.simulations.components

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.github.Luc16.simulations.utils.bhaskara
import com.github.Luc16.simulations.utils.dist2
import com.github.Luc16.simulations.utils.ortho
import com.github.Luc16.simulations.utils.toRad
import kotlin.math.*

const val MAX_SPEED = 600f
const val DECELERATION = 0f

open class DynamicBall(iniX: Float,
                       iniY: Float,
                       radius: Float,
                       color: Color = Color.YELLOW,
                       angle: Float = 0f,
                       private val deceleration: Float = DECELERATION,
                       val maxSpeed: Float = MAX_SPEED
): Ball(iniX, iniY, radius, color) {
    val direction = Vector2(cos(angle.toRad()), sin(angle.toRad()))
    var speed = maxSpeed
    val nextPos = Vector2(pos)
    private val radius2 get() = radius*radius

    fun move(valX: Float, valY: Float){
        pos.set(nextPos)
        nextPos.x += valX
        nextPos.y += valY
    }

    private fun move(vec: Vector2){
        move(vec.x, vec.y)
    }

    private fun moveTo(vec: Vector2){
        move(vec.x - x, vec.y - y)
    }

    fun moveTo(valX: Float, valY: Float){
        move(valX - x, valY - y)
    }

    open fun update(delta: Float){
//        move(direction.x*speed/60, direction.y*speed/60) //???????????
        move(direction.x*speed*delta, direction.y*speed*delta)
        speed -= deceleration*delta
        if (speed < 0) speed = 0f
    }

    fun collideFixedBall(other: DynamicBall, delta: Float): Boolean {
        val vec = Vector2(other.x - nextPos.x, other.y - nextPos.y)
        val dot = vec.dot(direction)
        val cpOnLine = Vector2(nextPos.x + direction.x*dot, nextPos.y + direction.y*dot)
        val dotCPDir = direction.dot(cpOnLine)

        val distToLine2 = dist2(other.pos, cpOnLine)
        val distToCompare2 = when {
            dotCPDir > direction.dot(nextPos) -> dist2(other.pos, nextPos)
            dotCPDir < direction.dot(pos) -> dist2(other.pos, pos)
            else -> distToLine2
        }

        val radiusSum2 = (radius + other.radius)*(radius + other.radius)
        if (distToCompare2 <= radiusSum2){
            val offset = sqrt(radiusSum2 - distToLine2) //+ 0.1f
            val prevDir = Vector2(direction)
            val normal = Vector2(nextPos.x - other.x, nextPos.y - other.y).nor()
            bounce(normal)
            val movementCorrection = speed*delta - sqrt(dist2(pos, pos)) + 0.01f
            nextPos.set(
                cpOnLine.x - prevDir.x*offset + direction.x*movementCorrection,
                cpOnLine.y - prevDir.y*offset + direction.y*movementCorrection
            )
            return true
        }
        return false
    }

    fun collideMovingBall(other: DynamicBall, delta: Float): Boolean{
        val rSum = radius + other.radius
        val dx = direction.x*speed*delta - other.direction.x*other.speed*delta
        val dy = direction.y*speed*delta - other.direction.y*other.speed*delta
        val ddx = x - other.x
        val ddy = y - other.y

        val (t1, t2) = bhaskara(dx*dx + dy*dy, 2*(ddx*dx + ddy*dy), ddx*ddx + ddy*ddy - rSum*rSum)
        if (t1 == null || t2 == null) return false

        val tf = if (t1 in 0f..1f) t1 else t2

        if (tf in 0f..1f){
            val normal = Vector2(nextPos.x - other.nextPos.x, nextPos.y - other.nextPos.y).nor()
            val extraMov = tf - 1
            val backMov = extraMov*speed*delta
            nextPos.add(direction.x*backMov, direction.y*backMov)
            bounce(normal)
            nextPos.add(backMov*direction.x, backMov*direction.y)

            val otherBackMov = extraMov*other.speed*delta
            other.nextPos.add(other.direction.x*otherBackMov, other.direction.y*otherBackMov)
            other.bounce(normal)
            other.nextPos.add(otherBackMov*direction.x, otherBackMov*direction.y)

            return true
        }
        return false
    }

    fun collideBall(other: DynamicBall){
        if (dist2(nextPos, other.nextPos) < (radius + other.radius)*(radius + other.radius)){
            val offset = radius + other.radius - sqrt(dist2(nextPos, other.nextPos))
            val normal = Vector2(nextPos.x - other.nextPos.x, nextPos.y - other.nextPos.y).nor()

            nextPos.add(normal.x * offset/2, normal.y * offset/2)
            bounce(normal)

            other.nextPos.add(-normal.x * offset/2, -normal.y * offset/2)
            other.bounce(normal)
        }

    }

    fun collideWall(wall: PolygonRect){
        val (collided, depth, normal) = wall.collideBall(this)
        if (collided){
            move(-normal.x*depth, -normal.y*depth)
        }
    }

    fun collideWallDirected(wall: PolygonRect, delta: Float): Pair<Vector2, Boolean> {
        var (collided, depth, normal) = wall.collideBall(this)
        if (collided) {

            val vertex = wall.findClosestPoint(pos)
            // gets the intended offset
            var offset = depth/direction.dot(normal)
            offset = if (abs(offset) < speed/60) offset else speed/60 // temporario
            val tg = normal.ortho()

            val newPos = Vector2(nextPos.x-direction.x*offset, nextPos.y-direction.y*offset)

            // sees if moving this way will cause the ball to stay out of the Polly
//            val vertex = wall.findClosestPoint(pos)
            val neighbor = wall.neighborInLine(vertex, normal)
            val dv = tg.dot(vertex)
            val dn = tg.dot(neighbor)
            val db = tg.dot(newPos)
            if (db > max(dv, dn) || db < min(dv, dn)) {

                val vec = Vector2(vertex.x - nextPos.x, vertex.y - nextPos.y)
                val dot = vec.dot(direction)
                val vecProjDir = Vector2(nextPos.x + direction.x*dot, nextPos.y + direction.y*dot)

                val distToLine2 = dist2(vertex, vecProjDir)
                if (distToLine2 > radius2) throw Exception("No collision should have happened," +
                        " dist to line: ${sqrt(distToLine2)}, radius: $radius")
                offset = sqrt(radius2 - distToLine2)
                newPos.set(vecProjDir.x - direction.x*offset, vecProjDir.y - direction.y*offset)

                val dir = Vector2(newPos.x - vertex.x, newPos.y - vertex.y)
                val n1 = Vector2(normal.x + tg.x, normal.y + tg.y)
                val n2 = n1.ortho()
                normal = if (abs(dir.dot(n1)) > abs(dir.dot(n2))) n1 else n2
            }
            val movementCorrection = speed/60 - sqrt(dist2(pos, newPos))
            nextPos.set(newPos)
            bounce(normal)
            move(direction.x*movementCorrection, direction.y*movementCorrection)
            speed = 0f
            return Pair(vertex, false)
        }
        return Pair(Vector2(), false)
    }

    private fun bounce(normal: Vector2){
        val dot = direction.dot(normal)
        direction.x -= 2*normal.x*dot
        direction.y -= 2*normal.y*dot
        direction.nor()
    }

    fun projectCircle(axis: Vector2): Pair<Float, Float>{
        val direction = Vector2(axis.x*radius, axis.y*radius)
        val p1 = Vector2(nextPos.x + direction.x, nextPos.y + direction.y)
        val p2 = Vector2(nextPos.x - direction.x, nextPos.y - direction.y)

        var min = p1.dot(axis)
        var max = p2.dot(axis)

        if (max < min) min = max.also { max = min }

        return Pair(min, max)
    }

    fun changeDirection(dir: Vector2){
        direction.set(dir).nor()
        speed = maxSpeed
    }

    fun bounceOfWalls(screenRect: Rectangle){
        when {
            nextPos.x + radius > screenRect.width -> {
                nextPos.add(screenRect.width - (radius + nextPos.x) - 0.1f, 0f)
                bounce(Vector2(-1f, 0f))
            }
            nextPos.x - radius < 0 -> {
                nextPos.add(-nextPos.x + radius + 0.1f, 0f)
                bounce(Vector2(1f, 0f))
            }
            nextPos.y + radius > screenRect.height -> {
                nextPos.add(0f, screenRect.height - radius - nextPos.y - 0.1f)
                bounce(Vector2(0f, -1f))
            }
            nextPos.y - radius < 0 -> {
                nextPos.add(0f, -nextPos.y + radius + 0.1f)
                bounce(Vector2(0f, 1f))
            }
        }
    }

}