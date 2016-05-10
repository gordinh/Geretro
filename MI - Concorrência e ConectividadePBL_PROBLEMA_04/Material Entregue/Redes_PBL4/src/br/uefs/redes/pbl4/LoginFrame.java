package br.uefs.redes.pbl4;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.UUID;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * 
 * Tela de login inicial. Nesta tela a senha é passada para validação, ou um
 * endereço de IP conhecido Caso a senha esteja correta, 2 sockets de escuta são
 * abertos. Se um endereço de IP for digitado, ocorre a troca de chaves com o
 * par, criptografando-se o canal.
 * 
 * 
 * @author gordinh
 * 
 */
public class LoginFrame extends JFrame {

	/**
	 * 
	 * 
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textField;
	private JLabel lblStatus;

	/**
	 * 
	 * Método principal de execução
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoginFrame frame = new LoginFrame();
					frame.setVisible(true);
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Altera o texto da label de status
	 * 
	 * @param status
	 */
	private void setStatus(String status) {
		lblStatus.setText(status);
	}

	/**
	 * Construtor, inicializa a tela principal
	 * 
	 * @throws IOException
	 */
	public LoginFrame() throws IOException {
		setTitle("Login - Monitoramento de trens");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 176);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);

		final JRadioButton rdbtnSenha = new JRadioButton("Senha");
		panel.add(rdbtnSenha);

		rdbtnSenha.setSelected(true);

		final JRadioButton rdbtnEndereoIp = new JRadioButton("Endereço IP");
		panel.add(rdbtnEndereoIp);

		ButtonGroup btnGroup = new ButtonGroup();
		btnGroup.add(rdbtnSenha);
		btnGroup.add(rdbtnEndereoIp);

		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));

		textField = new JTextField();
		panel_1.add(textField, BorderLayout.NORTH);
		textField.setColumns(20);
		textField.setText("172.16.111.226");

		lblStatus = new JLabel("Status: Idle...");
		panel_1.add(lblStatus, BorderLayout.SOUTH);

		JPanel panel_2 = new JPanel();

		contentPane.add(panel_2, BorderLayout.SOUTH);

		JButton btnIniciar = new JButton("Iniciar");

		btnIniciar.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {

				if (!((JButton) e.getSource()).isEnabled()) // Evita o bug de
															// clicar no botão
															// desativado
					return;

				((JButton) e.getSource()).setEnabled(false); // Desativa o botão

				new Thread(new Runnable() {

					@Override
					public void run() {

						if (rdbtnSenha.isSelected()) { // O usuáro digitou a
														// senha

							String pass = textField.getText();
							if (Security.validatePassword(pass)) { // A senha é
																	// válida
								setStatus("Aguardando conexões...");
								try {
									ConnectionHandler.acceptFirst(); // Accept
																		// first
																		// connection
									String rc4key = UUID.randomUUID()
											.toString(); // Generate random
															// password
									Security.symKey = rc4key;

									ConnectIO io = new ConnectIO(
											ConnectionHandler.getFirst(),
											rc4key); // Open secure channel
									Security.firstKey = Security
											.requestPublicKey(ConnectionHandler
													.getFirst()); // Accept key
																	// from 1st
																	// peer

									io.post(Base64.encodeBytes(RSA.encrypt(
											rc4key, Security.firstKey))); // Crypt
																			// with
																			// 1st
																			// peer
																			// key
									setStatus("Chave simétrica enviada...");

									if (io.getSecure().startsWith(
											"QUERY_STATUS:"))
										io.postSecure("YOU_FIRST:"); // Informa
																		// ao
																		// par
																		// que
																		// ele é
																		// o
																		// primeiro
																		// conectado

									ConnectionHandler.acceptLast(); // Accept
																	// second
																	// connection
									io = new ConnectIO(ConnectionHandler
											.getLast(), rc4key);
									Security.lastKey = Security
											.requestPublicKey(ConnectionHandler
													.getLast()); // Accept key
																	// from 2nd
																	// peer

									io.post(Base64.encodeBytes(RSA.encrypt(
											rc4key, Security.lastKey))); // Crypt
																			// with
																			// 2nd
																			// peer
																			// key

									if (io.getSecure().startsWith(
											"QUERY_STATUS:"))
										io.postSecure("YOU_LAST:" // Informa ao
																	// par que
																	// ele é o
																	// segundo
																	// conectado
																	// Informa
																	// também o
																	// IP do
																	// primeiro
																	// par.
												+ ConnectionHandler.getFirst()
														.getInetAddress()
														.getHostAddress());

									dispose();
									new MainWindow(0).initUI();
								} catch (IOException ioe){
									
								}catch(ClassNotFoundException e1) {
									e1.printStackTrace();
								}

							} else {
								JOptionPane.showMessageDialog(LoginFrame.this,
										"Senha incorreta.");
							}

						} else { // Se o usuário digitou um IP

							try {
								ConnectionHandler.connectFirst(InetAddress // Connecta
																			// ao
																			// par
										.getByName(textField.getText()));
								ConnectIO io = new ConnectIO(ConnectionHandler
										.getFirst());
								Security.sendPublicKey(ConnectionHandler // Envia
																			// a
																			// chave
																			// pública
										.getFirst());
								setStatus("Conectado, aguardando terceiro par...");
								String message = io.get();
								String decrypt = RSA.decrypt(
										Base64.decode(message),
										RSA.getPrivateKey()); // Decripta a
																// mensagem com
																// a chave
																// privada
								Security.symKey = decrypt; // A mensagem que
															// chegou é a chave
															// simétrica
															// aleatória

								io = new ConnectIO(
										// Abre o canal de comunicação seguro
										ConnectionHandler.getFirst(),
										Security.symKey);

								io.postSecure("QUERY_STATUS:");

								message = io.getSecure();

								if (message.startsWith("YOU_FIRST:")) { // Este
																		// par é
																		// o
																		// primeiro
									ConnectionHandler.acceptLast();
									new MainWindow(1).initUI(); // Inicializa o
																// sistema

								} else if (message.startsWith("YOU_LAST:")) { // Este
																				// par
																				// é
																				// o
																				// segundo
									String tokens[] = message.split(":");
									ConnectionHandler.connectLast(InetAddress
											.getByName(tokens[1]));

									new MainWindow(2).initUI(); // Inicializa o
																// sistema

								}

								dispose(); // Fecha a tela de login

							} catch (IOException ioe){
								
							} catch(ClassNotFoundException e1) {

								e1.printStackTrace();
							}
						}

						((JButton) e.getSource()).setEnabled(true); // Reativa o
																	// botão
					}

				}).start();

			}
		});
		panel_2.add(btnIniciar);
	}

}
