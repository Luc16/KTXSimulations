package com.github.Luc16.simulations.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.github.Luc16.simulations.Simulations
import com.github.Luc16.simulations.components.Ball
import com.github.Luc16.simulations.components.PolygonRect
import com.github.Luc16.simulations.utils.toRad
import ktx.graphics.use
import kotlin.math.*

class BallBallCollision(game: Simulations): CustomScreen(game) {

    private val angle = 270f
    private val ball = Ball(-84f, 300f, 20f, angle = angle)
    private val centerBall = Ball(0f, -120f, 90f, color = Color.BLUE)
    private val prevPos = Vector2(ball.x, ball.y)
    private var c = 0

    override fun render(delta: Float) {
        ball.update(delta)
        ball.collideFixedBall(centerBall, delta)
        handleSwipe()

        viewport.apply()
        renderer.use(ShapeRenderer.ShapeType.Line, viewport.camera.combined){
            renderer.color = Color.RED
            renderer.circle(ball.x, ball.y, 5f)
            renderer.color = Color.WHITE
            renderer.circle(ball.x, ball.y, ball.radius)
            centerBall.draw(renderer)
            renderer.color = Color.FOREST
            renderer.circle(centerBall.x, centerBall.y, centerBall.radius + ball.radius)

        }

    }

    private fun handleSwipe(){
        when {
            Gdx.input.justTouched() || Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) -> {
                prevPos.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
            }
            (!Gdx.input.isTouched || !Gdx.input.isButtonPressed(Input.Buttons.LEFT)) && !prevPos.isZero  -> {
                val dir = Vector2(Gdx.input.x.toFloat() - prevPos.x, -(Gdx.input.y.toFloat() - prevPos.y))
                if (!dir.isZero(CLICK_MARGIN)) ball.changeDirection(dir)
                viewport.unproject(prevPos)
                ball.move(prevPos.x - ball.x, prevPos.y - ball.y)
                prevPos.setZero()
            }
            Gdx.input.isKeyJustPressed(Input.Keys.S) -> ball.speed = 0f
            !Gdx.input.isTouched -> prevPos.setZero()
        }
    }


}