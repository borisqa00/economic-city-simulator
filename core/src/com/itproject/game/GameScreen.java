package com.itproject.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.itproject.game.City.CityListener;

public class GameScreen extends ScreenAdapter {
	
	static final int GAME_READY = 0;
	static final int GAME_RUNNING = 1;
	static final int GAME_PAUSED = 2;
	static final int GAME_REDACTOR_MODE = 3; // new
	static final int GAME_OVER = 4;
	
	EconomicCitySimulator game;
	
	static int state;
	static int redactorState;
	static final int BUILD_HOUSE = 0;
	static final int BUILD_HOSPITAL = 1;
	static final int BUILD_FIRESTATION = 2;
	static final int BUILD_POLICESTATION = 3;
	static final int BUILD_POWERSTATION = 4;
	static final int BUILD_BANK = 5;
	static final int BUILD_CITYHALL = 6;
	static final int BUILD_WATERSTATION= 7;
	static final int BUILD_GROCERY_SHOP = 8;
	static final int BUILD_BAR = 9;
	static final int BUILD_WTC = 10;
	static final int BUILD_PARK = 11;
	static final int BUILD_ROAD = 12;
	static final int BUILD_OIL_PLANT = 13;
	static final int BUILD_IRON_PLANT = 14;
	static final int BUILD_OFF = 15;
	
	public static City city;
	CityListener cityListener;
	CityRenderer renderer;
	
	OrthographicCamera guiCam;
	private Viewport guiPort;
	
	Rectangle redactorWindow;
	Rectangle buildHouse;
	Rectangle buildBlock;
	Rectangle buildFireStation;
	public static InputMultiplexer multi = new InputMultiplexer();
	ShapeRenderer shape;
	float time;
	float xInit, yInit;
	
	Vector3 touchPoint;
	
	private Hud hud;
	
	public GameScreen(EconomicCitySimulator game) {
		
		this.game = game;
		state = GAME_READY;
		redactorState = BUILD_OFF;
		cityListener = new CityListener() {
			// will be added later
		};
		
		city = new City(cityListener);
		renderer = new CityRenderer(city);
		shape = new ShapeRenderer();
		this.guiCam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		//this.guiCam.translate(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
		guiPort = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),  guiCam);
		touchPoint = new Vector3();
		hud = new Hud(game.batcher, guiCam);
		
		
		//
	}
	
	
	public void update(float deltaTime) {
		updateRunning(deltaTime);

	}
	
	public void render(float delta) {
		//delta = Math.min(0.06f, Gdx.graphics.getDeltaTime());
		time += delta;
		update(delta);
		multi.addProcessor(Hud.stage);
		Gdx.gl.glClearColor(0.5f, 0.75f, 1, 1);			
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		renderer.render(delta);
	
		guiCam.update();
		game.batcher.setProjectionMatrix(Hud.stage.getCamera().combined);
		Hud.stage.act(Gdx.graphics.getDeltaTime());
		Hud.stage.draw();
		hud.renderShapes();
		//  Gdx.gl.glEnable(GL20.GL_BLEND);
		//  Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
			
	}
	
	public void updateRunning(float deltaTime) {
		
		city.update(deltaTime);
	}


	
	@Override
	public void resize(int width, int height) {
		guiPort.update(width, height);
		guiCam.update();
	}

	@Override
	public void pause() {
		if (state == GAME_RUNNING) {
			state = GAME_PAUSED;
		}
	}
}
