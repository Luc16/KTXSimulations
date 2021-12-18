package com.github.Luc16.simulations.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.github.Luc16.simulations.HEIGHT
import com.github.Luc16.simulations.Simulations
import com.github.Luc16.simulations.WIDTH
import com.github.Luc16.simulations.components.Ball
import com.github.Luc16.simulations.components.PlayerBall
import com.github.Luc16.simulations.utils.translate
import ktx.graphics.moveTo
import ktx.graphics.use
import kotlin.random.Random

const val MAX_RADIUS = 500f
const val MIN_RADIUS = 50f

class UniverseScreen(game: Simulations): CustomScreen(game) {
    private val camera = viewport.camera
    private val offset = Vector2()
    private val player = PlayerBall(WIDTH/2, HEIGHT/2, 10f, camera, Color.BLUE)
    private var prevPos = Vector2().setZero()
    private val stars = mutableMapOf<Pair<Int, Int>, Ball>()
    private val numSectorsX = (WIDTH/(2*MAX_RADIUS)).toInt() + 2
    private val numSectorsY = (HEIGHT/(2*MAX_RADIUS)).toInt() + 2

    override fun show() {
        camera.moveTo(Vector2(WIDTH/2, HEIGHT/2))
    }

    private fun createSeed(i: Int, j: Int): Int = i*i+j*j

    override fun render(delta: Float) {
        handleInputs()
        player.update(delta)
        offset.set(player.x - WIDTH/2, player.y - HEIGHT/2)
        viewport.apply()
        val startSectorX = (offset.x/(2*MAX_RADIUS)).toInt() - 1
        val startSectorY = (offset.y/(2*MAX_RADIUS)).toInt() - 1

        renderer.use(ShapeRenderer.ShapeType.Filled, camera){
            for (i in startSectorX..startSectorX+numSectorsX){
                for (j in startSectorY..startSectorY+numSectorsY){
                    val rand = Random(createSeed(i, j))
                    if (rand.nextInt(0, 256) < 100){
                        if (stars[Pair(i, j)] == null)
                            stars[Pair(i, j)] = Ball(
                                (2*i + 1) * MAX_RADIUS,
                                (j*2 + 1) * MAX_RADIUS,
                                MIN_RADIUS + rand.nextFloat() * (MAX_RADIUS - MIN_RADIUS),
                                color = Color.BROWN
                            )
                        stars[Pair(i, j)]?.let { star ->
                            if (player.collideFixedBall(star)) star.color = Color.YELLOW
                            star.draw(renderer)
                        }
                    }
                }
            }
            player.draw(renderer)
        }

        drawMinimap()
    }

    private fun handleInputs() {
        val speed = 20f
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
        handleSwipe()
    }

    private fun handleSwipe(){
        when {
            Gdx.input.justTouched() || Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) -> {
                prevPos.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
            }
            (!Gdx.input.isTouched || !Gdx.input.isButtonPressed(Input.Buttons.LEFT)) && !prevPos.isZero  -> {
                val dir = Vector2(Gdx.input.x.toFloat() - prevPos.x, -(Gdx.input.y.toFloat() - prevPos.y))
                if (!dir.isZero(CLICK_MARGIN)) player.changeDirection(dir)
                prevPos.setZero()
            }
            !Gdx.input.isTouched -> prevPos.setZero()
        }
    }

    private fun drawMinimap(ratio: Float = 0.01f) {
        val mapNumSectorsX = 16
        val mapNumSectorsY = 10
        val startSectorX = (offset.x/(2*MAX_RADIUS)).toInt() - 8
        val startSectorY = (offset.y/(2*MAX_RADIUS)).toInt() - 5

        val startPoint = Vector2(offset.x + 5f, offset.y + HEIGHT - 135f)
        renderer.use(ShapeRenderer.ShapeType.Filled, camera){
            renderer.color = Color.LIGHT_GRAY
            renderer.rect(startPoint.x, startPoint.y, 190f, 130f)

            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
                println("My sector: (${((offset.x + WIDTH/2)/(2*MAX_RADIUS)).toInt()}, ${((offset.y + HEIGHT/2)/(2*MAX_RADIUS)).toInt()})")
                println("Minimap sector: (${startSectorX + 8 + numSectorsX/2} ${startSectorY + 5 + numSectorsY/2})")
                println("startSectorX = $startSectorX")
                println((startSectorX + 8)*2*MAX_RADIUS - startPoint.x + 5f)
            }

            renderer.color = Color.RED
            for (i in 0..mapNumSectorsX){
                for (j in 0..mapNumSectorsY){
                    val rand = Random(createSeed(startSectorX + i,startSectorY + j))
                    if (rand.nextInt(0, 256) < 100){
                        renderer.circle(
                            startPoint.x + ((2*i + 3.4f)*MAX_RADIUS + ((startSectorX + 8)*2*MAX_RADIUS - startPoint.x + 5f))*ratio,
                            startPoint.y + ((2*j + 4.4f)*MAX_RADIUS + ((startSectorY + 5)*2*MAX_RADIUS - startPoint.y))*ratio,
                            ratio*(MIN_RADIUS + rand.nextFloat() * (MAX_RADIUS - MIN_RADIUS))
                        )
                    }
                }
            }

            renderer.color = Color.BLACK
            renderer.circle(startPoint.x + 190/2, startPoint.y + 130/2, 2f)
        }
    }

}