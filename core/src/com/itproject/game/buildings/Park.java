package com.itproject.game.buildings;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Polygon;
import com.itproject.game.Assets;
import com.itproject.game.Citizen;
import com.itproject.game.Hud;

public class Park extends Building {

	public static final int TILE_HEIGHT = 32;
	public static final int TILE_WIDTH = 64;	
	public static final int PARK_OK = 0;
	public static final int PARK_ON_FIRE = 1;
	public static final int PARK_SELECTED = 2;
	public static final int PARK_UNSELECTED = 3;
	public static final int PARK_DESTROYED = 4;
	public static final int PARK_HEIGHT = 3;
	public static final int PARK_WIDTH = 3;
	
	TiledMapTileLayer.Cell[] cell;
	int state;
	private int col, row;
	private Polygon shape;
	List<Citizen> camper;
	TiledMapTileLayer layer;
	  
	public Park(int row, int col) {
		super(10000, 500);
		
		state = 0;
		
		this.col = col;
		this.row = row;
		cell = new TiledMapTileLayer.Cell[6];
		camper = new ArrayList<Citizen>(10); // default 10 firefighters at start
		layer = (TiledMapTileLayer)Assets.tiledMap.getLayers().get(0);
	}
	
	public void update() {
		updateSelected();
	}

	@Override
	public void setElectricityBill(short electricityBill) {
		//not used for park
	}

	@Override
	public void setWaterBill(short waterBill) {
		//not used for park
	}

	public void updateSelected() {
		/*if(state == PARK_SELECTED) {
			cell[0] = layer.getCell(row, col);
			cell[1] = layer.getCell(row + 1, col);
			cell[2] = layer.getCell(row, col + 1);
			cell[3] = layer.getCell(row + 1, col + 1);
			cell[4] = layer.getCell(row, col + 2);
			cell[5] = layer.getCell(row + 1, col + 2);
			
			cell[0].setTile(new StaticTiledMapTile(Assets.selectedFireStationCell5));
			cell[1].setTile(new StaticTiledMapTile(Assets.selectedFireStationCell6));
			cell[2].setTile(new StaticTiledMapTile(Assets.selectedFireStationCell3));
			cell[3].setTile(new StaticTiledMapTile(Assets.selectedFireStationCell4));
			cell[4].setTile(new StaticTiledMapTile(Assets.selectedFireStationCell1));
			cell[5].setTile(new StaticTiledMapTile(Assets.selectedFireStationCell2));
		} else if(state == PARK_UNSELECTED) {
	
			cell[0].setTile(new StaticTiledMapTile(Assets.fireStationCell5));
			cell[1].setTile(new StaticTiledMapTile(Assets.fireStationCell6));
			cell[2].setTile(new StaticTiledMapTile(Assets.fireStationCell3));
			cell[3].setTile(new StaticTiledMapTile(Assets.fireStationCell4));
			cell[4].setTile(new StaticTiledMapTile(Assets.fireStationCell1));
			cell[5].setTile(new StaticTiledMapTile(Assets.fireStationCell2));
			
			state = PARK_OK;
		}*/
	}
	
	public void createShape() {
		this.col = col; 
		this.row = row;
		int screenx = (col + row + 1) * TILE_WIDTH / 2 - 32;
	    int screeny = (col - row + 1) * TILE_HEIGHT / 2;
	    float[] vertices = new float[12];
	    vertices[0] = screenx;   vertices[1] = screeny;
	    vertices[2] = screenx + 64; vertices[3] = screeny - 32;
	    vertices[4] = screenx + 128; vertices[5] = screeny;
	    vertices[6] = screenx + 128; vertices[7] = screeny + 32;
	    vertices[8] = screenx + 64; vertices[9] = screeny - 32 + 128;
	    vertices[10] = screenx; vertices[11] = screeny + 64;
		shape = new Polygon(vertices);
	}
	
	public int getState() {
		return state;
	}
	
	public void setState(int state) {
		this.state = state;
	}
	
	public Polygon getShape() {
		return shape;
	}
	
	public int getCol() {
		return col;
	}
	
	public int getRow() {
		return row;
	}
	
	public void showInfo(float screenX, float screenY) {
		// to implement
		Hud.setInformationScreen(this, screenX, screenY);

		System.out.println("It is a Park!!");
	}

	@Override
	public void createCollisionShape() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Polygon getCollisionShape() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getZIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setZIndex(int zIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getPeopleSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isPowered() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setPowered(boolean isPowered) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return PARK_HEIGHT;
	}

	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return PARK_WIDTH;
	}
	
	
}
