package com.github.Luc16.simulations.components

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.github.Luc16.simulations.utils.dist2
import com.github.Luc16.simulations.utils.toRad
import kotlin.math.*

const val DIAGONAL_OFFSET = 1f

class PolygonRect( x: Float, y: Float, width: Float, height: Float, val color: Color, angle: Float = 0f) {
//    private val vertices = listOf(
//        Vector2(x, y + height - DIAGONAL_OFFSET),
//        Vector2(x + DIAGONAL_OFFSET, y + height),
//
//        Vector2(x + width, y + height + DIAGONAL_OFFSET),
//        Vector2(x + width + DIAGONAL_OFFSET, y + height),
//
//        Vector2(x + width + DIAGONAL_OFFSET, y),
//        Vector2(x + width, y - DIAGONAL_OFFSET),
//
//        Vector2(x + DIAGONAL_OFFSET, y),
//        Vector2(x, y + DIAGONAL_OFFSET)
//    )
    private val vertices = listOf(
        Vector2(x, y + height),
        Vector2(x + width, y + height),
        Vector2(x + width, y),
        Vector2(x, y)
    )
    var x = x + width/2
    var y = y + height/2
    val r = 40f
    val normalW: Vector2 get() = Vector2(vertices[1].x - vertices[0].x, vertices[1].y - vertices[0].y).nor()
    val normalH: Vector2 get() = Vector2(vertices[0].x - vertices[3].x, vertices[0].y - vertices[3].y).nor()

    init {
        rotate(angle)
    }

    fun move(vec: Vector2){
        x += vec.x
        y += vec.y
        vertices.forEach { vertex ->
            vertex.x += vec.x
            vertex.y += vec.y
        }
    }

    fun rotate(deg: Float){
        val rad = deg.toRad()
        val cos = cos(rad)
        val sin = sin(rad)
        vertices.forEach { v ->
            v.x -= x
            v.y -= y
            val oldX: Float = v.x
            v.x = (cos * v.x - sin * v.y) + x
            v.y = (sin * oldX + cos * v.y) + y
        }
    }

    fun neighborInLine(vertex: Vector2, vec: Vector2): Vector2{
        val epsilon = 0.1f
        var i = vertices.indexOf(vertex)
        val v1 = vertices[(i+1)%vertices.size]
        if (abs(abs(vec.dot(v1)) - abs(vec.dot(vertex))) < epsilon) return v1

        i = if (i - 1 < 0) vertices.lastIndex else i - 1
        val v2 = vertices[i]
        if (abs(abs(vec.dot(v2)) - abs(vec.dot(vertex))) < epsilon) return v2

        return Vector2()
    }

    fun forEachPair(action: (Vector2, Vector2) -> Unit){
        for (i in vertices.indices){
            action(vertices[i], vertices[(i+1)%vertices.size])
        }
    }

    fun findClosestPoint(pos: Vector2): Vector2{
        var dist = Float.MAX_VALUE
        var point = Vector2()
        vertices.forEach { vertex ->
            val d = dist2(vertex, pos)
            if (d < dist) {
                dist = d
                point = vertex
            }
        }
        return point
    }

    fun collideBallSAT(ball: Ball): Triple<Boolean, Float, Vector2> {
        var depth = Float.MAX_VALUE
        val normal = Vector2()

        var colliding = false
        forEachPair { v1, v2 ->
            if (colliding) return@forEachPair

            val axis = Vector2(v1.y - v2.y, v2.x - v1.x)
            axis.nor()

            val (minPR, maxPR) = projectVertices(axis)
            val (minB, maxB) = ball.projectCircle(axis)

            if (minB >= maxPR || minPR >= maxB) {
                colliding = true
                return@forEachPair
            }

            val axisDepth = min(maxB - minPR, maxPR - minB)


            if (axisDepth < depth){
                normal.set(axis)
                depth = axisDepth
            }

        }
        if (colliding) return Triple(false, 0f, Vector2())

        val closestPoint = findClosestPoint(ball.pos)

        val axis = Vector2(closestPoint.x - ball.nextPos.x, closestPoint.y - ball.nextPos.y)
        axis.nor()

        val (minPR, maxPR) = projectVertices(axis)
        val (minB, maxB) = ball.projectCircle(axis)

        if (minB >= maxPR || minPR >= maxB) {
            return Triple(false, 0f, Vector2())
        }

        val axisDepth = min(maxB - minPR, maxPR - minB)
        if (axisDepth < depth){
            normal.set(axis)
            depth = axisDepth
        }

        val direction = Vector2(x - ball.nextPos.x, y - ball.nextPos.y)

        if (direction.dot(normal) < 0f) normal.scl(-1f)

        return Triple(true, depth, normal)
    }

    fun collideBall(ball: Ball): Triple<Boolean, Float, Vector2> {
        var depth = Float.MAX_VALUE
        val normal = Vector2()
        val colDir = collisionDirection(ball.direction)
        val vecList = when(colDir){
            0 -> listOf(normalW, normalH)
            1 -> listOf(normalW, normalH.scl(-1f))
            2 -> listOf(normalW.scl(-1f), normalH.scl(-1f))
            3 -> listOf(normalW.scl(-1f), normalH)
            else -> listOf()
        }
        val vertex = vertices[colDir]
        val neighbors = listOf(
            vertices[(colDir+1)%vertices.size],
            vertices[if (colDir - 1 < 0) vertices.lastIndex else colDir - 1]
        )

        vecList.forEach { vec ->

            val (minPR, maxPR) = projectVertices(vec)
            val (minB, maxB) = ball.projectCircle(vec)
            if (minB >= maxPR || minPR >= maxB) {
                return Triple(false, 0f, Vector2())
            }

            val axisDepth = min(maxB - minPR, maxPR - minB)

            val neighbor = neighborInLine(vertex, vec)
            val other = if (neighbor == neighbors[0]) neighbors[1] else neighbors[0]
            if (axisDepth < depth && (dist2(ball.nextPos, other) > dist2(ball.nextPos, vertex))){
                normal.set(vec)
                depth = axisDepth
            }

        }

        val closestPoint = findClosestPoint(ball.nextPos)
        val axis = Vector2(closestPoint.x - ball.nextPos.x, closestPoint.y - ball.nextPos.y)
        axis.nor()
        val (minPR, maxPR) = projectVertices(axis)
        val (minB, maxB) = ball.projectCircle(axis)
        if (minB >= maxPR || minPR >= maxB) {
            return Triple(false, 0f, Vector2())
        }

        val direction = Vector2(x - ball.nextPos.x, y - ball.nextPos.y)

        if (direction.dot(normal) < 0f) normal.scl(-1f)

        return Triple(true, depth, normal)
    }

    private fun collisionDirection(dir: Vector2): Int{
        val dotW = dir.dot(normalW)
        val dotH = dir.dot(normalH)
        return when {
            dotH <= 0 && dotW >= 0 -> 0
            dotH <= 0 && dotW <= 0 -> 1
            dotH >= 0 && dotW <= 0 -> 2
            dotH >= 0 && dotW >= 0 -> 3
            else -> 1
        }
    }

    fun collisionVertex(dir: Vector2): Vector2 = vertices[collisionDirection(dir)]

    private fun projectVertices(axis: Vector2): Pair<Float, Float> {
        var min = Float.MAX_VALUE
        var max = -Float.MAX_VALUE
        vertices.forEach { vertex ->
            val projection = vertex.dot(axis)
            min = min(projection, min)
            max = max(projection, max)
        }
        return Pair(min, max)
    }

    fun containsPoint(point: Vector2): Boolean{
        val vecList = listOf(normalW, normalH)
        vecList.forEachIndexed {idx, vec ->
            val d1 =  vertices[1].dot(vec)
            val d2 = if (idx == 1) vertices[2].dot(vec) else vertices[0].dot(vec)
            if (point.dot(vec) !in min(d1, d2)..max(d1,d2)) return false
        }
        return true
    }

    fun draw(renderer: ShapeRenderer){
        renderer.color = color
        forEachPair { v1, v2 ->
            renderer.line(v1, v2)
        }
        renderer.color = Color.WHITE
        renderer.circle(x, y, 40f)
    }
}