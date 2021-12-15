package com.github.Luc16.simulations.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.github.Luc16.simulations.HEIGHT
import com.github.Luc16.simulations.Simulations
import com.github.Luc16.simulations.WIDTH
import com.github.Luc16.simulations.utils.translate
import ktx.graphics.moveTo
import ktx.graphics.use
import kotlin.random.Random

const val MAX_RADIUS = 16f

class UniverseScreen(game: Simulations): CustomScreen(game) {
    private val camera = viewport.camera
    private val offset = Vector2()
    private val numSectorsX = (WIDTH/(2*MAX_RADIUS)).toInt() + 1
    private val numSectorsY = (HEIGHT/(2*MAX_RADIUS)).toInt() + 1

    override fun show() {
        camera.moveTo(Vector2(WIDTH/2, HEIGHT/2))
    }

    override fun render(delta: Float) {
        handleInputs()
        viewport.apply()
        val startSectorX = (offset.x/(2*MAX_RADIUS)).toInt() - 1
        val startSectorY = (offset.y/(2*MAX_RADIUS)).toInt() - 1
        renderer.use(ShapeRenderer.ShapeType.Filled, camera){
            renderer.color = Color.YELLOW
            for (i in startSectorX..startSectorX+numSectorsX){
                for (j in startSectorY..startSectorY+numSectorsY){
                    val seed = i*i+j*j
                    renderer.color = Color.YELLOW
                    if (Random(seed).nextInt(0, 256) < 40){
                        renderer.circle((2*i + 1) * MAX_RADIUS, (j*2 + 1) * MAX_RADIUS, MAX_RADIUS)
                    }
                }
            }

        }

    }

    private fun handleInputs() {
        val speed = 8f
        if(Gdx.input.isKeyPressed(Input.Keys.W)) {
            offset.y += speed
            camera.translate(0f, speed)
        }
        if(Gdx.input.isKeyPressed(Input.Keys.A)) {
            offset.x -= speed
            camera.translate(-speed, 0f)
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S)) {
            offset.y -= speed
            camera.translate(0f, -speed)
        }
        if(Gdx.input.isKeyPressed(Input.Keys.D)) {
            offset.x += speed
            camera.translate(speed, 0f)
        }
    }

}