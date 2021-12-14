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

class PollyRectBallCollision(game: Simulations): CustomScreen(game) {

    private val angle = 270f
    private val ball = Ball(-84f, 300f, 20f, angle = angle, speed = 600f)
    private val wall = PolygonRect(-80f, -120f, 90f, 80f, Color.BLUE)
    private val prevPos = Vector2(ball.x, ball.y)
    private var normal = Vector2()
    private var v = Vector2()

    override fun render(delta: Float) {
        ball.update(delta)
        val (v0, _) = ball.collideWallDirected(wall, delta)
        v = if(!v0.isZero) v0 else v
        handleSwipe()
        when {
            (Gdx.input.isKeyPressed(Input.Keys.NUM_1)) -> game.setScreen<PrototypeScreen>()
            (Gdx.input.isKeyJustPressed(Input.Keys.R)) -> {
                ball.changeDirection(Vector2(cos(angle.toRad()), sin(angle.toRad())))
                ball.x = prevPos.x
                ball.y = prevPos.y
            }
            (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) -> {
                prevPos.set(ball.x, ball.y)
                val (collided, depth, nor) = wall.collideBall(ball)
                normal = nor
                if (collided){
                    val offset = -depth/ball.direction.dot(normal)
                    ball.move(ball.direction.x*offset, ball.direction.y*offset)
                }
            }
            (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) -> {
                prevPos.set(ball.x, ball.y)
                ball.collideWallDirected(wall, delta)
            }
            (Gdx.input.isKeyPressed(Input.Keys.W)) -> wall.rotate(1f)
            (Gdx.input.isKeyPressed(Input.Keys.S)) -> wall.rotate(-1f)

        }
        viewport.apply()
        renderer.use(ShapeRenderer.ShapeType.Line, viewport.camera.combined){
            renderer.color = Color.RED
            renderer.circle(ball.x, ball.y, 5f)
            renderer.color = Color.WHITE
            renderer.circle(ball.x, ball.y, ball.radius)

            renderer.color = Color.FOREST
            renderer.circle(v.x, v.y, ball.radius)
            renderer.color = Color.YELLOW
            renderer.line(wall.x, wall.y, wall.x + wall.normalH.x*100f, wall.y + wall.normalH.y*100 )
            renderer.color = Color.BROWN
            renderer.line(wall.x, wall.y, wall.x + wall.normalW.x*100f, wall.y + wall.normalW.y*100 )

            renderer.color = Color.RED
            renderer.line(wall.x, wall.y, wall.x + normal.x*100f, wall.y + normal.y*100 )
            wall.draw(renderer)
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