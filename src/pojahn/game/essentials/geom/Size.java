package pojahn.game.essentials.geom;

public class Size {

	public float width, height;
	
	public Size(){}
	
	public Size(float width, float height){
		this.width = width;
		this.height = height;
	}
	
	public Size copy(){
		return new Size(width, height);
	}
}
