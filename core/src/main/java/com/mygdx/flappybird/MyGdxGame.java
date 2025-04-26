package com.mygdx.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

import org.w3c.dom.Text;

import java.util.Random;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class MyGdxGame extends ApplicationAdapter {
    SpriteBatch batch;
    Texture background;
    Texture[] birds;
    Texture topTube, bottomTube;
    Texture gameOver;
    int flapState = 0;
    float birdY = 0;
    float velocity = 0;
    float gameState = 0;
    float gap = 750;

    //variables para aleatorizar la aparición de tuberías
    Random randomGenerator;
    int totalTubes = 10;
    float [] tubeX = new float[totalTubes];
    float [] tubeOffset = new float[totalTubes];
    float distanceBetweenTubes;

    //variables para el control de velocidad
    float stateTime = 0;
    float flapRate = 0.1f; //el valor son los segundos que deben pasar entre el cambio de sprite, /1f es un segundo

    //variables para las colisiones
    Circle birdCircle;
    Rectangle[] topTubeRectangles;
    Rectangle[] bottomTubeRectangles;

    //variables para la puntuacion
    Double score  =   0.0;
    int scoreInt = 0;
    Rectangle[] scoreRectangle;

    //Para imprimir la puntuacion
    BitmapFont font;

    @Override
    public void create() {
        batch = new SpriteBatch();
        background = new Texture("background.png");
        birds = new Texture[2];
        birds[0] = new Texture("flappybirdup.png");
        birds[1] = new Texture("flappybirddown.png");
        topTube = new Texture("toptube.png");
        bottomTube = new Texture("bottomtube.png");
        gameOver = new Texture("gameover.png");

        randomGenerator = new Random();
        distanceBetweenTubes = Gdx.graphics.getWidth()/2;
        birdY = Gdx.graphics.getHeight()/2 - birds[0].getHeight()/2;

        topTubeRectangles = new Rectangle[totalTubes];
        bottomTubeRectangles = new Rectangle[totalTubes];
        scoreRectangle = new Rectangle[totalTubes];

        birdCircle = new Circle();

        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(5);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        stateTime += delta;

        batch.begin();
        batch.draw(background,
            0,
            0,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );
        if(gameState == 1){
            if(Gdx.input.justTouched()){
                velocity -= 30;
            }
            for(int i = 0; i < totalTubes; i++){
                if(tubeX[i] < -topTube.getWidth()){
                    tubeX[i] += totalTubes*distanceBetweenTubes;
                    tubeOffset[i] = (randomGenerator.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - gap -940);
                }
                else {
                    tubeX[i] -= 2;//controla la velocidad a la que aparecen los tubos
                }

                batch.draw(topTube,
                    tubeX[i],
                    Gdx.graphics.getHeight()/2 + gap/2 + tubeOffset[i]);
                batch.draw(bottomTube,
                    tubeX[i],
                    Gdx.graphics.getHeight()/2 - gap/2 - bottomTube.getHeight() + tubeOffset[i]);

                topTubeRectangles[i] = new Rectangle(
                    tubeX[i],
                    Gdx.graphics.getHeight()/2 + gap / 2 + tubeOffset[i],
                    topTube.getWidth(),
                    topTube.getHeight()
                );

                bottomTubeRectangles[i] = new Rectangle(
                    tubeX[i],
                    Gdx.graphics.getHeight()/2 - gap / 2 - bottomTube.getHeight() + tubeOffset[i],
                    topTube.getWidth(),
                    topTube.getHeight()
                );

                scoreRectangle[i] = new Rectangle(
                    tubeX[i],
                    Gdx.graphics.getHeight() / 2 - gap / 2 +tubeOffset[i],
                    5,
                    gap
                );
            }

            if (birdY > Gdx.graphics.getHeight() - birds[flapState].getHeight()) {
                gameState = 2;
            } else if (birdY > 0 || velocity < 0) {
                velocity += 2;
                birdY -= velocity;
            } else {
                gameState = 2;
            }
        }
        else if(gameState == 0){
            if(Gdx.input.justTouched()){
                gameState = 1;
                StartGame();
            }
        }

        else if(gameState == 2){
            batch.draw(gameOver,
                Gdx.graphics.getWidth()/2 - gameOver.getWidth()/2,
                Gdx.graphics.getHeight()/2 - gameOver.getHeight()/2);
            if(Gdx.input.justTouched()){
                gameState = 1;
                velocity = 1;
                score = 0.0;
                StartGame();
            }
        }

        //agregado para poder controlar la velocidad del pájaro
        if(stateTime > flapRate){
            flapState = 1 - flapState;
            stateTime = 0;
        }

        /* código anterior al control de velocidad
        if(flapState == 0){
            flapState = 1;
        }
        else{
            flapState = 0;
        }*/


        batch.draw(birds[flapState],
            Gdx.graphics.getWidth()/2 - birds[flapState].getWidth()/2,
            birdY
        );

        birdCircle.set(
            Gdx.graphics.getWidth()/2,
            birdY + birds[flapState].getHeight()/2,
            birds[flapState].getWidth()/2
        );

        for(int i = 0; i < totalTubes; i++){
            if(birdCircle!= null && topTubeRectangles[i] != null && bottomTubeRectangles[i] != null){
                if(Intersector.overlaps(birdCircle, topTubeRectangles[i])
                    || Intersector.overlaps(birdCircle, bottomTubeRectangles[i])){
                    //Ha colisionado
                    gameState = 2;
                }

                if(gameState == 1){
                    if(Intersector.overlaps(birdCircle, scoreRectangle[i])){
                        score += 0.05;
                    }
                }
            }
        }

        scoreInt = (int) Math.round(score);
        font.draw(batch, "Score " + scoreInt, 50, Gdx.graphics.getHeight() - 50);

        batch.end();
    }

    //Liberamos las texturas para evitar errores de ejecución
    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
        birds[0].dispose();
        birds[1].dispose();
    }

    public void StartGame(){
        birdY = Gdx.graphics.getHeight()/2 - birds[0].getHeight()/2;

        for(int i = 0; i< totalTubes; i++){
            tubeOffset[i] = (randomGenerator.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - gap - 940);
            tubeX[i] = Gdx.graphics.getWidth()/2 - topTube.getWidth()/2 + Gdx.graphics.getWidth() + i * distanceBetweenTubes;

            topTubeRectangles[i] = new Rectangle();
            bottomTubeRectangles[i] = new Rectangle();
            scoreRectangle[i] = new Rectangle();
        }

    }
}
