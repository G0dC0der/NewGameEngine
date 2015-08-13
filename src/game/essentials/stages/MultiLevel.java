package game.essentials.stages;

import java.io.IOException;

import game.core.Level;

public abstract class MultiLevel extends Level{

	private Level level;
	
	public MultiLevel(Level level){
		this.level = level;
	}
	
	@Override
	public void init() throws IOException {
		level.init();
	}
	
	@Override
	public void build() {
		level.build();
	}
	
	@Override
	public void dispose() {
		level.dispose();
	}
	
	public void changeLevel(Level level) throws IOException{
		this.level.dispose();
		this.level = level;
		this.level.init();
		this.level.build();
	}
}
