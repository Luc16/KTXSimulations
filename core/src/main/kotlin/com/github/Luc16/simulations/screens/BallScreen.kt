package com.github.Luc16.simulations.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.github.Luc16.simulations.Simulations
import com.github.Luc16.simulations.HEIGHT
import com.github.Luc16.simulations.WIDTH
import com.github.Luc16.simulations.components.Ball
import com.github.Luc16.simulations.utils.randomColor
import com.github.Luc16.simulations.utils.translate
import ktx.graphics.moveTo
import ktx.graphics.use
import kotlin.random.Random.Default.nextFloat

const val NUM_BALLS = 100


class BallScreen(game: Simulations): CustomScreen(game) {

    private val screenRect = Rectangle(0f, 0f, WIDTH, HEIGHT)
    private val camera = viewport.camera
    private val balls = List(NUM_BALLS){
        Ball(
            WIDTH * nextFloat(),
            HEIGHT * nextFloat(),
            10f,
            randomColor(0.1f),
            360 * nextFloat()
        )
    }
    private val accelerometer = Vector3()
    private var speedIncrease = 0f
    private var frame = 0

    override fun show() {
        viewport.worldWidth = WIDTH
        viewport.worldHeight = HEIGHT
        viewport.camera.moveTo(Vector2(WIDTH/2, HEIGHT/2))
    }

    override fun render(delta: Float) {
        frame++
        handleInputs()

        accelerometer.set(Gdx.input.accelerometerX, Gdx.input.accelerometerY, Gdx.input.accelerometerZ)

        speedIncrease = if (accelerometer.x > 50 || accelerometer.y > 50 || accelerometer.z > 50) 100f else 0f

        balls.forEachIndexed { i, ball ->
            ball.speed += speedIncrease
            if (ball.speed > 800) ball.speed = 800f
            ball.update(delta)
            ball.bounceOfWalls(screenRect)
            for(j in i + 1 until balls.size){
                ball.collideBall(balls[j])
            }
        }

        viewport.apply()
        renderer.use(ShapeRenderer.ShapeType.Filled, camera){
            renderer.color = Color.LIGHT_GRAY
            renderer.rect(0f, 0f, screenRect.width, screenRect.height)
            renderer.color = Color.YELLOW
            balls.forEach { it.draw(renderer) }

        }
    }

    private fun handleInputs(){
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) ||
            Gdx.input.isTouched(0) && Gdx.input.isTouched(1)) game.setScreen<PrototypeScreen>()

        val touchPoint = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
        viewport.unproject(touchPoint)

        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) camera.translate(y = 10f)
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) camera.translate(x = -10f)
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) camera.translate(y = -10f)
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) camera.translate(x = 10f)

    }

}