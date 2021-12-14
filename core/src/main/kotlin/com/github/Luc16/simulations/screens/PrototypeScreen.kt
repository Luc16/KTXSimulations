package com.github.Luc16.simulations.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.github.Luc16.simulations.Simulations
import com.github.Luc16.simulations.components.Ball
import com.github.Luc16.simulations.components.PlayerBall
import com.github.Luc16.simulations.components.PolygonRect
import com.github.Luc16.simulations.utils.dist2
import com.github.Luc16.simulations.utils.randomColor
import ktx.graphics.moveTo
import ktx.graphics.use
import kotlin.random.Random

const val CLICK_MARGIN = 100f
const val N_BALLS = 0

fun createBallList(size: Int, screenRect: Rectangle, walls: List<PolygonRect>): List<Ball> {
    return List(size){
        var b = Ball(
            (screenRect.width - 10f) * Random.nextFloat(),
            (screenRect.height - 10f) * Random.nextFloat(),
            10f,
            randomColor(0.1f),
            360 * Random.nextFloat()
        )
        var col = true
        while (col) {
            b = Ball(
                (screenRect.width - 10f) * Random.nextFloat(),
                (screenRect.height - 10f) * Random.nextFloat(),
                10f,
                randomColor(0.1f),
                360 * Random.nextFloat()
            )
            col = false
            walls.forEach {
                val (c, d, n) = it.collideBall(b)
                if (c) {
                    col = c
                    return@forEach
                }
            }
        }
        b
    }
}

class PrototypeScreen(game: Simulations): CustomScreen(game) {
    private val screenRect = Rectangle(0f, 0f, 1280f*2, 1600f)
    private val walls = List(40){ i ->
        val numY = 5*screenRect.width.toInt()/1280
        val k = i/numY
        PolygonRect(50f+250*(i%numY), 200f + 400*k, 200f, 50f, randomColor(0.3f), 0f)//180*Random.nextFloat())
    }
    private var balls = createBallList(N_BALLS, screenRect, walls)
    private val camera = viewport.camera
    private val ball = PlayerBall(291f, 325.47528f, 10f, camera)
    private var prevPos = Vector2().setZero()
    private val miniMapRatio = 0.1f
    private var numFrames = 0
    private var v = Vector2(1f, 1f)
    private var end = false
    private var idx = -1

    override fun show() {
        camera.moveTo(ball.pos)
    }

    override fun render(delta: Float) {
        if (end){
            camera.moveTo(v)
            val touchPoint = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
            viewport.unproject(touchPoint)
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) println(touchPoint)
            draw()
            renderer.use(ShapeRenderer.ShapeType.Filled, camera){
                it.color = Color.WHITE
                it.circle(v.x, v.y, 10f)
                it.color = Color.YELLOW
                it.circle(balls[idx].x, balls[idx].y, 10f)
            }
            return
        }
        handleInputs()
        ball.update(delta)
        ball.bounceOfWalls(screenRect)
        balls.forEach { ball ->
            ball.update(delta)
            ball.bounceOfWalls(screenRect)
        }
        walls.forEach { wall ->
            ball.collideWallDirected(wall, delta)
            balls.forEachIndexed {i, ball ->
                val (v1, end1) = ball.collideWallDirected(wall, delta)
                v = v1
                end = end1
                if (numFrames >= 1 && end1){
                    end = end1
                    idx = i
                    return
                }
            }
        }
        draw()

        numFrames++
//        if (numFrames > 10){
//            numFrames = 0
//            balls = createBallList(N_BALLS, screenRect, walls)
//        }
    }

    private fun handleInputs(){
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) ||
        Gdx.input.isTouched(0) && Gdx.input.isTouched(1)) game.setScreen<BallScreen>()

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) ball.speed = 0f

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) balls = createBallList(N_BALLS, screenRect, walls)

        val touchPoint = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
        viewport.unproject(touchPoint)
        walls.forEach { wall ->
            when {
                (Gdx.input.isTouched || Gdx.input.isButtonPressed(Input.Buttons.LEFT)) &&
                        dist2(touchPoint, wall.x, wall.y) <= wall.r*wall.r -> {
//                    ball.speed = 0f
                    wall.rotate(1f)
                    prevPos.setZero()
                }
                Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W) -> wall.rotate(1f)
                Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S) -> wall.rotate(-1f)
            }
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
                if (!dir.isZero(CLICK_MARGIN)) ball.changeDirection(dir)
                prevPos.setZero()
            }
            Gdx.input.isKeyJustPressed(Input.Keys.S) -> ball.speed = 0f
            !Gdx.input.isTouched -> prevPos.setZero()
        }
    }

    private fun showMinimap(renderer: ShapeRenderer){
        val startPoint = Vector2(
            viewport.camera.position.x - viewport.worldWidth/2 + 5f,
            viewport.camera.position.y + viewport.worldHeight/2 - screenRect.height*miniMapRatio - 5f
        )
        renderer.use(ShapeRenderer.ShapeType.Filled, camera){
            renderer.color = Color.LIGHT_GRAY
            renderer.rect(startPoint.x, startPoint.y, screenRect.width*miniMapRatio, screenRect.height*miniMapRatio)
            renderer.color = Color.BLACK
            renderer.rect(startPoint.x + 1, startPoint.y+1, screenRect.width*miniMapRatio-2, screenRect.height*miniMapRatio-2)
            renderer.color = Color.YELLOW
            renderer.circle(startPoint.x + ball.x*miniMapRatio, startPoint.y + ball.y*miniMapRatio, ball.radius*miniMapRatio)
            walls.forEach { wall ->
                renderer.color = wall.color
                wall.forEachPair { v1, v2 ->
                    renderer.line(
                        startPoint.x + v1.x*miniMapRatio,
                        startPoint.y + v1.y*miniMapRatio,
                        startPoint.x + v2.x*miniMapRatio,
                        startPoint.y + v2.y*miniMapRatio,
                    )
                }
            }
        }
    }

    private fun draw(){
        viewport.apply()
        renderer.use(ShapeRenderer.ShapeType.Line, camera){
            renderer.color = Color.LIGHT_GRAY
            renderer.rect(0f, 0f, screenRect.width, screenRect.height)
            walls.forEach { wall ->
                wall.draw(renderer)
            }
            renderer.color = Color.YELLOW
            ball.draw(renderer)
            balls.forEach { it.draw(renderer) }
        }
        showMinimap(renderer)

    }

}