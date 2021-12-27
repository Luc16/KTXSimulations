package com.github.Luc16.simulations.components

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2

open class Ball(
    iniX: Float,
    iniY: Float,
    var radius: Float,
    var color: Color
) {
    val pos = Vector2(iniX, iniY)
    var x: Float
        get() = pos.x
        set(value) {pos.x = value}
    var y: Float
        get() = pos.y
        set(value) {pos.y = value}

    fun draw(renderer: ShapeRenderer){
        renderer.color = color
        renderer.circle(x, y, radius)
    }
}