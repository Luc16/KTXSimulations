package com.github.Luc16.simulations.components

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.github.Luc16.simulations.utils.translate

class PlayerBall(x: Float, y: Float, radius: Float,
                 private val camera: Camera,
                 color: Color = Color.YELLOW):
    DynamicBall(x, y, radius, color) {

    init {
        direction.setZero()
    }

    override fun update(delta: Float) {
        camera.translate(nextPos.x - x, nextPos.y - y)
        super.update(delta)
    }
}