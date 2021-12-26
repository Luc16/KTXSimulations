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

class DynamicBallBallCollision(game: Simulations): CustomScreen(game) {

    private val angle = 270f
    private val ball = Ball(-84f, 300f, 10f, angle = angle)
    private val centerBall = Ball(0f, -120f, 90f, color = Color.BLUE, speed = 300f)
    private val prevPos = Vector2(ball.x, ball.y)

    override fun render(delta: Float) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            centerBall.move(-centerBall.x-225, 0f)
            centerBall.speed = 800f
        }
        ball.update(delta)
        centerBall.update(delta)
        ball.collideMovingBall(centerBall, delta)
        handleSwipe()

        viewport.apply()
        renderer.use(ShapeRenderer.ShapeType.Line, viewport.camera.combined){
            ball.draw(renderer)
            centerBall.draw(renderer)

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