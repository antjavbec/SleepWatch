package es.lacabradev.sleepwatch;

import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.StringUtils;
import org.jcodec.api.awt.AWTSequenceEncoder;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamMotionDetector;
import com.github.sarxos.webcam.WebcamMotionDetectorDefaultAlgorithm;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.ds.ipcam.IpCamDeviceRegistry;
import com.github.sarxos.webcam.ds.ipcam.IpCamDriver;
import com.github.sarxos.webcam.ds.ipcam.IpCamMode;

import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.ScrollPaneConstants;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.awt.event.ActionEvent;

public class Main extends JFrame {
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd.HH_mm_ss");

	private JPanel contentPane;
	private JTextField textFieldNombreDispositivo;
	private JTextField textFieldIP;
	private JTextField textFieldUsuario;
	private JTextField textFieldContrasenia;
	private JTextField textFieldDirectorio;
	private JTextArea textAreaMensajes;

	private JPanel panelCam;
	private JTextField textFieldSegundosInactivo;
	private JTextField textFieldPixelThreshold;
	private JTextField textFieldAreaThreshold;

	private JButton btnActivar;
	private Detector detector;
	boolean camRegistered;
	private JButton btnConectar;

	private Webcam webcam;
	private boolean detecting;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		Webcam.setDriver(new IpCamDriver());
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main frame = new Main();
					frame.setVisible(true);
					frame.pack();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Main() {
		setTitle("Grabaci\u00F3n de v\u00EDdeos por detecci\u00F3n de movimiento");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[][grow][]", "[][][][][][][][][][][][grow]"));
		
		JLabel lblNewLabel = new JLabel("Nombre dispositivo:");
		contentPane.add(lblNewLabel, "cell 0 0,alignx trailing");
		
		textFieldNombreDispositivo = new JTextField();
		textFieldNombreDispositivo.setText("VegaRondaCam");
		contentPane.add(textFieldNombreDispositivo, "cell 1 0 2 1,growx");
		textFieldNombreDispositivo.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("IP de la c\u00E1mara:");
		contentPane.add(lblNewLabel_1, "cell 0 1,alignx trailing");
		
		textFieldIP = new JTextField();
		textFieldIP.setText("192.168.1.144");
		contentPane.add(textFieldIP, "cell 1 1 2 1,growx");
		textFieldIP.setColumns(10);
		
		JLabel lblNewLabel_2 = new JLabel("Usuario:");
		contentPane.add(lblNewLabel_2, "cell 0 2,alignx trailing");
		
		textFieldUsuario = new JTextField();
		textFieldUsuario.setText("admin");
		contentPane.add(textFieldUsuario, "cell 1 2 2 1,growx");
		textFieldUsuario.setColumns(10);
		
		JLabel lblNewLabel_3 = new JLabel("Contrase\u00F1a:");
		contentPane.add(lblNewLabel_3, "cell 0 3,alignx trailing");
		
		textFieldContrasenia = new JTextField();
		textFieldContrasenia.setText("xeniaantonio");
		contentPane.add(textFieldContrasenia, "cell 1 3 2 1,growx");
		textFieldContrasenia.setColumns(10);
		
		JLabel lblNewLabel_5 = new JLabel("Segundos de inactividad para parar v\u00EDdeo:");
		contentPane.add(lblNewLabel_5, "cell 0 4,alignx trailing");
		
		textFieldSegundosInactivo = new JTextField();
		textFieldSegundosInactivo.setText("60");
		contentPane.add(textFieldSegundosInactivo, "cell 1 4 2 1,growx");
		textFieldSegundosInactivo.setColumns(10);
		
		JLabel lblNewLabel_6 = new JLabel("Pixel threshold:");
		contentPane.add(lblNewLabel_6, "cell 0 5,alignx trailing");
		
		textFieldPixelThreshold = new JTextField();
		contentPane.add(textFieldPixelThreshold, "cell 1 5,growx");
		textFieldPixelThreshold.setColumns(10);
		textFieldPixelThreshold.setText("10");
		
		JLabel lblNewLabel_7 = new JLabel("Area threshold:");
		contentPane.add(lblNewLabel_7, "cell 0 6,alignx trailing");
		
		textFieldAreaThreshold = new JTextField();
		contentPane.add(textFieldAreaThreshold, "cell 1 6,growx");
		textFieldAreaThreshold.setColumns(10);
		textFieldAreaThreshold.setText("0.1");
		
		JLabel lblNewLabel_4 = new JLabel("Directorio de grabaci\u00F3n:");
		contentPane.add(lblNewLabel_4, "cell 0 7,alignx trailing");
		
		textFieldDirectorio = new JTextField();
		textFieldDirectorio.setEditable(false);
		contentPane.add(textFieldDirectorio, "cell 1 7,growx");
		textFieldDirectorio.setColumns(10);
		
		JButton btnSeleccionar = new JButton("Seleccionar");
		btnSeleccionar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (fc.showOpenDialog(Main.this) == JFileChooser.APPROVE_OPTION) {
					Path dir = fc.getSelectedFile().toPath();
					textFieldDirectorio.setText(dir.toString());
				}				
			}
		});
		contentPane.add(btnSeleccionar, "cell 2 7");
		
		btnActivar = new JButton("Activar detecci\u00F3n");
		btnActivar.setEnabled(false);
		btnActivar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!detecting) {
					try {
						lanzarDeteccion();
					} catch (Exception e1) {
						mensaje("Ha ocurrido un error al lanzar la detección: \n" + e1);
					}
				} else {
					pararDeteccion();
				}
			}
		});
		
		btnConectar = new JButton("Conectar c\u00E1mara");
		btnConectar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							conectarCamara();
						} catch (Exception e) {
						}
					}
				});
			}
		});
		contentPane.add(btnConectar, "cell 0 8 3 1,growx");
		contentPane.add(btnActivar, "cell 0 9 3 1,growx");
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setMinimumSize(new Dimension(23, 100));
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		contentPane.add(scrollPane, "cell 0 10 3 1,grow");
		
		textAreaMensajes = new JTextArea();
		textAreaMensajes.setMinimumSize(new Dimension(5, 480));
		textAreaMensajes.setLineWrap(true);
		textAreaMensajes.setFont(UIManager.getFont("Button.font"));
		textAreaMensajes.setEditable(false);
		scrollPane.setViewportView(textAreaMensajes);
		
		panelCam = new JPanel();
		panelCam.setMinimumSize(new Dimension(640, 480));
		contentPane.add(panelCam, "cell 0 11 3 1,grow");
	}
	
	protected void conectarCamara() {
		if (validarDatos()) {
			try {
				IpCamDeviceRegistry.register(textFieldNombreDispositivo.getText(), "http://" + textFieldIP.getText() + "/videostream.cgi?user=" + textFieldUsuario.getText() + "&pwd=" + textFieldContrasenia.getText() + "&count=0", IpCamMode.PUSH);
				webcam = Webcam.getDefault();
				Dimension[] viewSizes = webcam.getViewSizes();
				Dimension max = new Dimension(0, 0);
				for (Dimension dimension : viewSizes) {
					if (dimension.getHeight() > max.getHeight()) {
						max = dimension;
					}
				}
				webcam.setViewSize(max);
				detector = new Detector(webcam, Integer.parseInt(textFieldPixelThreshold.getText()), Double.parseDouble(textFieldAreaThreshold.getText()));
				panelCam.add(new WebcamPanel(webcam));
				webcam.getImage();
				btnConectar.setEnabled(false);
				btnActivar.setEnabled(true);
				pack();
			} catch (Exception e) {
				mensaje("ERROR: no se puedo conectar la cámara: " + e);
			}
		} else {
			mensaje("ERROR: ha de completar los campos para iniciar la detección");
		}
	}

	protected void mensaje(String mensaje) {
		textAreaMensajes.append(sdf.format(new Date()) + ": " + mensaje + "\n");
	}

	protected void lanzarDeteccion() throws Exception {
		mensaje("Lanzando detección...");
		Thread t = new Thread(detector);
		t.start();
		mensaje("Detección funcionando");
		btnActivar.setText("Desactivar detección");
		detecting = true;
	}
	
	protected void pararDeteccion() {
		mensaje("Parando detección");
		detector.halt();
		btnActivar.setText("Activar Detección");
		mensaje("Detección parada");
		detecting = false;
	}

	protected boolean validarDatos() {
		boolean result = true;
		result = !(StringUtils.isBlank(textFieldNombreDispositivo.getText()) ||
				StringUtils.isBlank(textFieldIP.getText()) ||
				StringUtils.isBlank(textFieldUsuario.getText()) ||
				StringUtils.isBlank(textFieldContrasenia.getText()) ||
				StringUtils.isBlank(textFieldDirectorio.getText()) ||
				StringUtils.isBlank(textFieldSegundosInactivo.getText()) ||
				StringUtils.isBlank(textFieldAreaThreshold.getText()) ||
				StringUtils.isBlank(textFieldPixelThreshold.getText()));
		return result;
	}
	
	class Detector implements Runnable {
		private Webcam webcam;
		private WebcamMotionDetector detector;
		private AtomicBoolean running;
		private Recorder recorder;
		private long lastMotionTime;
		private boolean recording;
		
		Detector(Webcam webcam, int pixelThreshold, double areaThreshold) throws Exception {
			this.webcam = webcam;
			detector = new WebcamMotionDetector(webcam, pixelThreshold, areaThreshold);
			detector.setInterval(100);
			detector.start();
			recorder = new Recorder(webcam, Paths.get(textFieldDirectorio.getText()));
		}

		@Override
		public void run() {
			running = new AtomicBoolean(true);
			recording = false;
			while (running.get()) {
				if (detector.isMotion()) {
					if (!recording) {
						mensaje("Detectado movimiento");
						try {
							Thread t = new Thread(recorder);
							t.start();
							recording = true;
						} catch (Exception e) {
							mensaje("ERROR: no se pudo lanzar la grabación de vídeo: " + e);
						}
					} else {
						lastMotionTime = System.currentTimeMillis();
					}
				} else if (recording && (System.currentTimeMillis() - lastMotionTime) > Integer.parseInt(textFieldSegundosInactivo.getText()) * 1000L) {
					mensaje("Se para la grabación por inactividad");
					recorder.halt();
					recording = false;
				}
			}
		}
		
		public void halt() {
			running.set(false);
			if (recording) {
				recorder.halt();
			}
		}
	}
	
	class Recorder implements Runnable {
		private Webcam webcam;
		private AWTSequenceEncoder encoder;
		private Path outputDir;
		private AtomicBoolean running;
		
		public Recorder(Webcam webcam, Path outputDir) throws Exception {
			this.webcam = webcam;
			this.outputDir = outputDir;
		}

		@Override
		public void run() {
			try {
				encoder = AWTSequenceEncoder.create25Fps(Paths.get(outputDir.toString(), sdf.format(new Date()) + ".mp4").toFile());
			} catch (IOException e1) {
				mensaje("ERROR: no se puedo crear el grabador: " + e1);
			}
			running = new AtomicBoolean(true);
			while (running.get()) {
				try {
					encoder.encodeImage(webcam.getImage());
				} catch (IOException e) {
					mensaje("ERROR: se produjo un error durante la grabación: " + e);
					running.set(false);
				}				
			}
			try {
				encoder.finish();
			} catch (IOException e) {
				mensaje("ERROR: no se pudo finalizar el vídeo correctamente: " + e);
			}
		}
		
		public void halt() {
			running.set(false);
		}
	}
}
