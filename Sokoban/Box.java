package Sokoban;
import java.awt.Point;

public class Box {

	Point position;
	
	public Box(int xpos, int ypos) {
		position = new Point(xpos, ypos);
		
	}

	public Point getPosition() {
		return position;
	}
}