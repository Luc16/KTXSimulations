package com.github.Luc16.simulations.components

import com.badlogic.gdx.graphics.Color

class Particle(x: Float,
               y: Float,
               radius: Float,
               color: Color,
               angle: Float,
               speed: Float = MAX_SPEED/3,
               deceleration: Float = DECELERATION/2,
               private val gravity: Float
): Ball(x, y, radius, color, angle, deceleration,  speed) {

    override fun update(delta: Float) {
        super.update(delta)
        if (speed <= 0) speed = 30f
        radius -= 2*delta
        move(0f, -gravity*delta)
    }
}