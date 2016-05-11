package br.uefs.redes.pbl4;

import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * Classe que reúne o estado dos trens do sistema
 * 
 * @author Gordinh e Sillas
 * 
 */
public class Train extends JLabel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Rectangle walkPath;
	private double x, y;
	private int angle;
	private double velocity;
	private int walkState = 0;
	private Point criticalEnter, criticalLeave;

	/**
	 * Construtor, recebe
	 * 
	 * 
	 * @param walkPath
	 *            O retângulo por onde ele vai andar
	 * @param startLocation
	 *            O ponto de onde ele começa
	 * @param criticalEnter
	 *            O ponto que ele entra na zona crítica
	 * @param criticalLeave
	 *            O ponto que ele sai da zona crítica
	 * @param image
	 *            A imagem que será mostrada.
	 */
	public Train(Rectangle walkPath, Point startLocation, Point criticalEnter,
			Point criticalLeave, ImageIcon image) {
		super(image);
		this.velocity = 0;
		this.walkPath = walkPath;
		this.x = startLocation.x;
		this.y = startLocation.y;
		this.criticalEnter = criticalEnter;
		this.criticalLeave = criticalLeave;
		setLocation(startLocation);
		setSize(getPreferredSize());
	}

	public void setVelocity(double newVelocity) {
		this.velocity = newVelocity;
	}

	public double getVelocity() {
		return this.velocity;
	}

	public void accel() {
		this.velocity += 1.0;
	}

	/**
	 * Faz o trem andar sobre o retângulo
	 * 
	 * 
	 */
	public void walk() {
		if (walkState == 0) { // Indo para a direita
			rotate(0);
			x += velocity;
			// top-right
			if (walkPath.width - x < 32)
				angle += (velocity * Math.PI);
			if (angle > 90)
				angle = 90;

			this.rotate(angle);

			if (x >= walkPath.width) {
				x = walkPath.width;
				walkState = 1;
				angle = 90;
			}
		} else if (walkState == 1) { // Descendo
			this.rotate(90);

			// bottom-right
			if (walkPath.height - y < 32)
				angle += (velocity * Math.PI);
			if (angle > 180)
				angle = 180;
			this.rotate(angle);

			y += velocity;
			if (y >= walkPath.height) {
				y = walkPath.height;
				walkState = 2;
				angle = 180;
			}
		} else if (walkState == 2) { // Indo para a esquerda
			this.rotate(180);

			x -= velocity;

			// bottom-left
			if (x - walkPath.x < 32)
				angle += (velocity * Math.PI);
			if (angle > 270)
				angle = 270;
			this.rotate(angle);

			if (x <= walkPath.x) {
				x = walkPath.x;
				walkState = 3;
				angle = 270;
			}

		} else if (walkState == 3) { // Subindo
			this.rotate(270);
			y -= velocity;

			if (y - walkPath.y < 32)
				angle += (velocity * Math.PI);
			if (angle > 360)
				angle = 360;
			this.rotate(angle);

			if (y <= walkPath.y) {
				y = walkPath.y;
				walkState = 0;
				angle = 0;
			}
		}

		setLocation((int) x, (int) y);

	}

	private void rotate(int i) {
		//

	}

	/**
	 * 
	 * Verifica se o trem está entrando na zona crítica
	 * 
	 * @return
	 */
	public boolean isEntering() {
		if (criticalEnter.distance(getLocation()) < 50)
			return true;
		else if (criticalEnter.distance(getLocation()) < 40)
			return true;
		else if (criticalEnter.distance(getLocation()) < 20)
			return true;
		return false;
	}


	
	/**
	 * Verifica se o trem está saindo da zona crítica
	 * 
	 * @return
	 */
	public boolean isLeaving() {
		if (criticalLeave.distance(getLocation()) < 10)
			return true;

		return false;
	}

}
