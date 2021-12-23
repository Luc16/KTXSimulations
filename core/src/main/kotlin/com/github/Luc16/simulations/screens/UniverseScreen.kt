package com.github.Luc16.simulations.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
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

const val MAX_BC_STAR_RADIUS = 3f

class UniverseScreen(game: Simulations): CustomScreen(game) {
    private val camera = viewport.camera
    private val offset = Vector2()
    private val player = PlayerBall(WIDTH/2, HEIGHT/2, 10f, camera, Color.RED)
    private var prevPos = Vector2().setZero()
    private val stars = mutableMapOf<Pair<Int, Int>, Ball>()

    private val numSectorsX = (WIDTH/(2*MAX_RADIUS)).toInt() + 2
    private val numSectorsY = (HEIGHT/(2*MAX_RADIUS)).toInt() + 2
    private val bGNumSectorsX = (WIDTH/(2*MAX_BC_STAR_RADIUS)).toInt() + 2
    private val bGNumSectorsY = (HEIGHT/(2*MAX_BC_STAR_RADIUS)).toInt() + 2
    private var seedOffset = 0
    private var score = 0
    private val textLayout = GlyphLayout()

    override fun show() {
        val file = Gdx.files.local("assets/seed.txt")
        seedOffset = (file.readString().toInt() + 1)%100_000_000
        camera.moveTo(Vector2(WIDTH/2, HEIGHT/2))
        file.writeString("$seedOffset", false)
    }

    private fun createSeed(i: Int, j: Int): Int = i and 0xFFFF shl 16 or (j and 0xFFFF) + 7//seedOffset

    private fun forEachStarSectorIn(rangeI: IntRange, rangeJ: IntRange, func: (Int, Int) -> Unit) {
        for (i in rangeI){
            for (j in rangeJ) {
                func(i, j)
            }
        }
    }

    override fun render(delta: Float) {
        handleInputs()
        player.update(delta)

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) player.speed = 0f
        offset.set(player.x - WIDTH/2, player.y - HEIGHT/2)

        draw(delta)
        batch.use(camera.combined){
            textLayout.setText(font, "Score: $score")
            font.draw(batch, textLayout, offset.x + WIDTH - textLayout.width - 5, offset.y + HEIGHT - textLayout.height - 5)
        }
    }

    private fun handleInputs() {
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

    private fun drawMinimap(renderer: ShapeRenderer, ratio: Float = 0.01f) {
        val mapNumSectorsX = 16
        val mapNumSectorsY = 10
        val startSectorX = (offset.x/(2*MAX_RADIUS)).toInt() - 8
        val startSectorY = (offset.y/(2*MAX_RADIUS)).toInt() - 5

        val startPoint = Vector2(offset.x + 5f, offset.y + HEIGHT - 135f)
        renderer.color = Color.LIGHT_GRAY
        renderer.rect(startPoint.x, startPoint.y, 190f, 130f)

        forEachStarSectorIn(0..mapNumSectorsX, 0..mapNumSectorsY) { i, j ->
            val rand = Random(createSeed(startSectorX + i,startSectorY + j))
            renderer.color = Color.GRAY
            stars[Pair(startSectorX + i,startSectorY + j)]?.let { star -> renderer.color = star.color }
            if (rand.nextInt(0, 256) < 50){
                renderer.circle(
                    startPoint.x + ((2*i + 3.4f)*MAX_RADIUS + ((startSectorX + 8)*2*MAX_RADIUS - startPoint.x + 5f))*ratio,
                    startPoint.y + ((2*j + 4.4f)*MAX_RADIUS + ((startSectorY + 5)*2*MAX_RADIUS - startPoint.y))*ratio,
                    ratio*(MIN_RADIUS + rand.nextFloat() * (MAX_RADIUS - MIN_RADIUS))
                )
            }
        }

        renderer.color = Color.BLACK
        renderer.circle(startPoint.x + 190/2, startPoint.y + 130/2, 2f)

    }

    private fun drawBackgroundStars(renderer: ShapeRenderer){
        val bGStartSectorX = (offset.x/(2*MAX_BC_STAR_RADIUS)).toInt() - 1
        val bGStartSectorY = (offset.y/(2*MAX_BC_STAR_RADIUS)).toInt() - 1
        renderer.color = Color.LIGHT_GRAY
        forEachStarSectorIn(
            bGStartSectorX..bGStartSectorX+bGNumSectorsX,
            bGStartSectorY..bGStartSectorY+bGNumSectorsY
        ){ i, j ->
            val rand = Random(createSeed(i, j))
            if (rand.nextInt(0, 256) < 3){
                renderer.circle(
                    (2*i + 1) * MAX_BC_STAR_RADIUS,
                    (j*2 + 1) * MAX_BC_STAR_RADIUS,
                    1 + rand.nextFloat() * (MAX_BC_STAR_RADIUS - 1)
                )
            }
        }

    }

    private fun handleEntities(renderer: ShapeRenderer, delta: Float){
        val startSectorX = (offset.x/(2*MAX_RADIUS)).toInt() - 1
        val startSectorY = (offset.y/(2*MAX_RADIUS)).toInt() - 1
        forEachStarSectorIn(
            startSectorX..startSectorX+numSectorsX,
            startSectorY..startSectorY+numSectorsY
        ) { i, j ->
            val rand = Random(createSeed(i, j))
            if (rand.nextInt(0, 256) < 50){
                if (stars[Pair(i, j)] == null)
                    stars[Pair(i, j)] = Ball(
                        (2*i + 1) * MAX_RADIUS,
                        (j*2 + 1) * MAX_RADIUS,
                        MIN_RADIUS + rand.nextFloat() * (MAX_RADIUS - MIN_RADIUS),
                        color = Color.GRAY
                    )
                stars[Pair(i, j)]?.let { star ->
                    if (player.collideFixedBall(star, delta)) {
                        score += 100
                        star.color = Color.YELLOW
                    }
                    star.draw(renderer)
                }
            }
        }
    }

    private fun draw(delta: Float){
        viewport.apply()
        renderer.use(ShapeRenderer.ShapeType.Filled, camera){
            drawBackgroundStars(renderer)
            handleEntities(renderer, delta)
            player.draw(renderer)
            drawMinimap(renderer)
        }
    }
}