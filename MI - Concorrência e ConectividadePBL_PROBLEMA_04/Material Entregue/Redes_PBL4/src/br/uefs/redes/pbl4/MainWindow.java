package br.uefs.redes.pbl4;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.NotActiveException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;

/**
 * Classe responsável pelo visual da aplicação Nela os trens são exibidos ao
 * usuário.
 * 
 * 
 * @author Gordinh e Sillas 
 * 
 */
public class MainWindow {
	private JPanel frame;
	private Train[] train = new Train[3];
	private int thisTrain;
	private ConnectIO firstConn;
	private ConnectIO secondConn;
	private Timer timer1;
	private boolean zoneLocked = false;
	private int control = 1;

	/**
	 * Inicializa a tela principal
	 * 
	 * @wbp.parser.entryPoint
	 * 
	 * 
	 */
	public void initUI() {
		final JFrame window = new JFrame("");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.getContentPane().setLayout(new BorderLayout()); // Auto explicativo... coisas do Javax.Swing.
		frame = new JPanel(null);
		JPanel south = new JPanel(new BorderLayout());

		JSlider firstVel = new JSlider(10, 100);
		JSlider secondVel = new JSlider(10, 100);
		JLabel info = new JLabel("Controle de velocidade dos trens"); // Auto explicativo... coisas do Javax.Swing.

		south.add(firstVel, BorderLayout.NORTH);
		south.add(info, BorderLayout.CENTER);
		south.add(secondVel, BorderLayout.SOUTH);

		window.getContentPane().add(south, BorderLayout.SOUTH);
		window.getContentPane().add(frame, BorderLayout.CENTER); // Auto explicativo... coisas do Javax.Swing.

		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setSize(741, 680);

		JLabel lbl = new JLabel(new ImageIcon(getClass().getResource("/images/bg.png")));
		lbl.setSize(lbl.getPreferredSize());

		ImageIcon image = new ImageIcon(getClass().getResource("/images/train.png"));
		train[0] = new Train(new Rectangle(200, 100, 500, 250), new Point(200,
				100), new Point(500, 150), new Point(200, 150), image);
		train[0].setVelocity(1.6);

		image = new ImageIcon(getClass().getResource("/images/train2.png"));
		train[1] = new Train(new Rectangle(50, 250, 350, 550), new Point(50,
				250), new Point(150, 250), new Point(300, 550), image);
		train[1].setVelocity(2);

		image = new ImageIcon(getClass().getResource("/images/train3.png"));
		train[2] = new Train(new Rectangle(350, 250, 650, 550), new Point(600,
				250), new Point(500, 550), new Point(550, 250), image);
		train[2].setVelocity(3);

		frame.add(train[0]);
		frame.add(train[1]); // Auto explicativo... coisas do Javax.Swing.
		frame.add(train[2]);

		frame.add(lbl);

		new Thread(new Runnable() { // Esta thread recebe o comando do primeiro trem.
			@Override
			public void run() {
				while (true) {
					try {
						parse(firstConn.getSecure());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) { // Esta thread recebe o comando do segundo trem.
					try {
						parse(secondConn.getSecure());
					} catch (IOException e) {
						System.exit(0);
						e.printStackTrace();
					}
				}
			}
		}).start();

		timer1 = new Timer(1000 / 24, new ActionListener() { // Esta thread

					// envia o
					// comando para
					// os outros 2;
					@Override
					public void actionPerformed(ActionEvent e) {

						train[thisTrain].walk(); // Atualiza a posição deste
													// trem;
						window.repaint();

						window.setTitle("PosX: " + train[thisTrain].getX()
								+ " PosY: " + train[thisTrain].getY());

						try {

							multicast("WALK:" + thisTrain + ':' + train[thisTrain].getX() + ':' + train[thisTrain].getY()); 
							// Envia o comando de andar aos outros trens

							if (train[thisTrain].isEntering()) { 
								if (zoneLocked) {// Se quer entrar mas está bloqueado, diminue drasticamente.
									multicast("SET_VEL:" + thisTrain + ":" + 0.2);
								} else {//SE QUER ENTRAR E A ZONA NAO ESTA BLOQUEADA
									multicast("QUERY_STATUS:" + thisTrain);  // Se quer entrar, pergunta o status.
								}
							}

							if (train[thisTrain].isEntering()) {
								
							} else if (train[thisTrain].isLeaving()) {
								multicast("UNLOCK_RESOURCE:"); // Se está saindo, libera o trilho.
								
							} 
							

						} catch (NotActiveException e1) {
							e1.printStackTrace();
						}

					}
				});

		if (thisTrain != 0) { // Se este não for o prioritário, esconde os
								// sliders.
			firstVel.setVisible(false);
			secondVel.setVisible(false);
			info.setVisible(false);
		}

		window.setVisible(true); // mostra a janela.
		timer1.start();

	}

	/**
	 * Código obscuro da lógica de controle de fluxo.
	 * 
	 * 
	 * @param inputMessage
	 */
	private void parse(String inputMessage) {

		try {
			if (inputMessage.startsWith("WALK:")) { // O comando de andar, recebido pelo peer.
				String[] tokens = inputMessage.split(":");
				//train[Integer.parseInt(tokens[1])].walk();
				train[Integer.parseInt(tokens[1])].atualizarPosicao(Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3]));
			}
			else if (inputMessage.startsWith("SET_VEL:")) {
				String[] tokens = inputMessage.split(":");
				train[Integer.parseInt(tokens[1])].setVelocity(Double
						.parseDouble(tokens[2]));
				multicast("WALK:" + thisTrain + ':' + train[thisTrain].getX() + ':' + train[thisTrain].getY());

			} else if (inputMessage.startsWith("QUERY_STATUS:")) { // Alguém quer entrar na zona crítica
				String[] tokens = inputMessage.split(":");
				System.out.println("THIS_TRAIN:" + thisTrain);
				if((Integer.parseInt(tokens[1]) + 1   ) % 3 == thisTrain) // Responde ao trem que chegou depois de você
					multicast("STATUS:" + tokens[1] + ":" + (zoneLocked ? 1 : 0)); // Com o status
				
			} else if (inputMessage.startsWith("STATUS:")) {
				String[] tokens = inputMessage.split(":");
				if (Integer.parseInt(tokens[1]) == thisTrain) {

					if (Integer.parseInt(tokens[2]) == 0) { //zona esta liberada para mim
						multicast("SET_VEL:" + thisTrain + ":" + 10);
						multicast("LOCK_RESOURCE:");											
					}
					else{ //zona esta bloqueada para mim
						multicast("SET_VEL:" + thisTrain + ":" + 0.2);
					}
				}
			} else if (inputMessage.startsWith("LOCK_RESOURCE:")) { // Bloqueia a zona para você
				zoneLocked = true;
				multicast("ALGUEM ENTROU NA ZONA ENTÃO ELA ESTA SENDO BLOQUEADA PARA MIM, TREM = " + thisTrain);
				
				
				
			} else if (inputMessage.startsWith("UNLOCK_RESOURCE:")) { // Desbloqueia a zona para você
				zoneLocked = false;
				multicast("LIBERANDO A ZONA PARA OS TRENS");				
			}

		} catch (NotActiveException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Envia a mensagem para as duas conexões
	 * 
	 * 
	 * @param message
	 * @throws NotActiveException
	 */
	private void multicast(String message) throws NotActiveException {
		firstConn.postSecure(message);
		secondConn.postSecure(message);
	}

	/**
	 * Cria a janela
	 * 
	 * @throws IOException
	 * @wbp.parser.constructor
	 */
	public MainWindow(int accessLevel) throws IOException {
		this.thisTrain = accessLevel;

		// Abre os canais seguros de comunicação
		firstConn = new ConnectIO(ConnectionHandler.getFirst(), Security.symKey);
		secondConn = new ConnectIO(ConnectionHandler.getLast(), Security.symKey);

	}

}
