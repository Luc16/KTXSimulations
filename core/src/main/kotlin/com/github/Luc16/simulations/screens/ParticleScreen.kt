package com.github.Luc16.simulations.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.github.Luc16.simulations.Simulations
import com.github.Luc16.simulations.HEIGHT
import com.github.Luc16.simulations.WIDTH
import com.github.Luc16.simulations.components.DECELERATION
import com.github.Luc16.simulations.components.MAX_SPEED
import com.github.Luc16.simulations.components.Particle
import com.github.Luc16.simulations.utils.toRad
import ktx.graphics.moveTo
import ktx.graphics.use
import kotlin.math.abs
import kotlin.math.cos
import kotlin.random.Random.Default.nextFloat

val fireColors = listOf(
    Color(1f, 0f, 0f, 1f),
    Color(1f, 90/255f, 0f, 1f),
    Color(1f, 154/255f, 0f, 1f),
    Color(1f, 206/255f, 0f, 1f),
    Color(1f, 232/255f, 8/255f, 1f)
)

class Fire(private val pos: Vector2){
    val particles = mutableListOf<Particle>()

    fun update(delta: Float){
        val remove = mutableListOf<Particle>()
        val angle = 80f+20*nextFloat()
        particles.add(
            Particle(
                pos.x,
                pos.y,
                5f + 5f*nextFloat() - abs(30*cos(angle.toRad())),
                fireColors.random(),
                angle,
                MAX_SPEED/3,
                DECELERATION/3,
                gravity = 50f
            )
        )
        particles.forEach { particle ->
            particle.update(delta)
            if (particle.radius < 0) remove.add(particle)
        }
        remove.forEach { particle ->
            particles.remove(particle)
        }
    }

    fun draw(renderer: ShapeRenderer){
        particles.forEach { it.draw(renderer) }
    }
}

class ParticleScreen(game: Simulations): CustomScreen(game) {

    private val fires = mutableListOf<Fire>()


    override fun show() {
        viewport.worldWidth = WIDTH
        viewport.worldHeight = HEIGHT
        viewport.camera.moveTo(Vector2(WIDTH/2, HEIGHT/2))
    }

    override fun render(delta: Float) {
        handleInputs()
        fires.forEach{ fire ->
            fire.update(delta)
        }
        viewport.apply()
        renderer.use(ShapeRenderer.ShapeType.Filled, viewport.camera){
            fires.forEach { fire -> fire.draw(renderer) }
        }
    }

    private fun handleInputs(){
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) ||
            Gdx.input.isTouched(0) && Gdx.input.isTouched(1)) game.setScreen<PrototypeScreen>()

        val touchPoint = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
        viewport.unproject(touchPoint)
        if ((Gdx.input.justTouched() || Gdx.input.isButtonJustPressed(Input.Buttons.LEFT))){
            fires.add(Fire(touchPoint))
        }

    }

}