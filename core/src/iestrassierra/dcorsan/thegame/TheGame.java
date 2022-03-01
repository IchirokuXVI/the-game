package iestrassierra.dcorsan.thegame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

public class TheGame extends ApplicationAdapter implements InputProcessor {
	SpriteBatch batch;
	//Objeto que recoge el mapa de baldosas
	private TiledMap mapa;

	//Capa del mapa donde se encuentran los tesoros
	private TiledMapTileLayer capaTesoros;

	//Ancho y alto del mapa en tiles
	private int anchoTiles, altoTiles;

	//Arrays bidimensionales de booleanos que contienen los obstáculos y los tesoros del mapa
	private boolean[][] obstaculo, tesoro;

	//Objeto con el que se pinta el mapa de baldosas
	private TiledMapRenderer mapRenderer;

	//Variables de ancho y alto
	public static int anchoMapa, altoMapa, anchoCelda, altoCelda;

	//Variable para contabilizar el número de tesoros
	int totalTesoros;

	// Cámara que nos da la vista del juego
	private OrthographicCamera camara;
	
	private int anchuraPantalla;
	private int alturaPantalla;

	// Este atributo indica el tiempo en segundos transcurridos desde que se inicia la animación,
	// servirá para determinar qué frame se debe representar
	private float stateTime;

	//Booleanos que determinan la dirección de marcha del sprite
	private static boolean izquierda, derecha, arriba, abajo;

	//Dimensiones del sprite
	public static int anchoJugador, altoJugador;

	//Constantes que indican el numero de filas y columnas de la hoja de sprites
	public static final int FRAME_COLS = 3;
	public static final int FRAME_ROWS = 4;

	// Atributo en el que se cargará la imagen del personaje principal.
	private Texture imagenPrincipal;

	//Animacion que se muestra en el metodo render()
	private Animation<TextureRegion> jugador;

	//Animaciones para cada una de las direcciones de mvto. del jugador
	private Animation<TextureRegion> jugadorArriba;
	private Animation<TextureRegion> jugadorDerecha;
	private Animation<TextureRegion> jugadorAbajo;
	private Animation<TextureRegion> jugadorIzquierda;

    //Posición en el eje de coordenadas actual del jugador
	private Vector2 posicionJugador;

	//Velocidad de desplazamiento del jugador para cada iteración del bucle de renderizado
	private float velocidadJugador;

	//Celdas inicial y final del recorrido del personaje principal
	private Vector2 celdaInicial, celdaFinal;

	Enemy[] enemies = new Enemy[Enemy.amount];

	private int cuentaTesoros;
	private TiledMapTile tileTesoro;

	// La música se va reproduciendo poco a poco (sin cargarla en memoria)
	private Music musicaJuego;

	// Los sonidos deben ser de máximo 1 MB y son cargados en memoria
	private Sound tesoroEncontrado;
	private Sound pillado;
	private Sound fracaso;
	private Sound exito;

	private Sound pasos;
	private int cycle, cycle_ant;

	//Elementos para sobreimpresionar informacion
	private BitmapFont fontVidas;
	private BitmapFont fontTesoros;
	//Numero de vidas del jugador
	private int nVidas;

	@Override
	public void create () {
		batch = new SpriteBatch();

		//Cargamos el mapa de baldosas desde la carpeta de assets
		mapa = new TmxMapLoader().load("themap.tmx");
		mapRenderer = new OrthogonalTiledMapRenderer(mapa);

		//Determinamos el alto y ancho del mapa de baldosas. Para ello necesitamos extraer la capa
		//base del mapa y, a partir de ella, determinamos el número de celdas a lo ancho y alto,
		//así como el tamaño de la celda, que multiplicando por el número de celdas a lo alto y
		//ancho, da como resultado el alto y ancho en pixeles del mapa.
		TiledMapTileLayer capa = (TiledMapTileLayer) mapa.getLayers().get(0);

		//Determinamos el ancho y alto de cada celda
		anchoCelda = (int) capa.getTileWidth();
		altoCelda = (int) capa.getTileHeight();

		//Determinamos el ancho y alto del mapa completo
		anchoMapa = capa.getWidth() * anchoCelda;
		altoMapa = capa.getHeight() * altoCelda;

		//Cargamos las capas de los obstáculos y las de los pasos en el TiledMap.
		TiledMapTileLayer capaSuelo = (TiledMapTileLayer) mapa.getLayers().get(0);
		TiledMapTileLayer capaObstaculos = (TiledMapTileLayer) mapa.getLayers().get(2);
		TiledMapTileLayer capaPasos = (TiledMapTileLayer) mapa.getLayers().get(4);
		capaTesoros = (TiledMapTileLayer) mapa.getLayers().get(1);
		TiledMapTileLayer capaProfundidad = (TiledMapTileLayer) mapa.getLayers().get(5);

		//El numero de tiles es igual en todas las capas. Lo tomamos de la capa Suelo
		anchoTiles = capaSuelo.getWidth();
		altoTiles = capaSuelo.getHeight();

		//Creamos un array bidimensional de booleanos para obstáculos y tesoros
		obstaculo = new boolean[anchoTiles][altoTiles];
		tesoro = new boolean[anchoTiles][altoTiles];

		//Rellenamos los valores recorriendo el mapa
		for (int x = 0; x < anchoTiles; x++) {
			for (int y = 0; y < altoTiles; y++) {
				//rellenamos el array bidimensional de los obstaculos
				obstaculo[x][y] = ((capaObstaculos.getCell(x, y) != null) //obstaculos de la capa Obstaculos
						&& (capaPasos.getCell(x, y) == null)); //que no sean pasos permitidos de la capa Pasos
				//rellenamos el array bidimensional de los tesoros
				tesoro[x][y] = (capaTesoros.getCell(x, y) != null);
				//contabilizamos cuántos tesoros se han incluido en el mapa
				if (tesoro[x][y]) {
					totalTesoros++;
					tileTesoro = capaTesoros.getCell(x, y).getTile();
				}
			}
		}

		//Inicializamos la cámara del juego
		anchuraPantalla = Gdx.graphics.getWidth();
		alturaPantalla = Gdx.graphics.getHeight();

		//Creamos una cámara que mostrará una zona del mapa (igual en todas las plataformas)
		int anchoCamara = 400, altoCamara = 240;
		camara = new OrthographicCamera(anchoCamara, altoCamara);

		//Actualizamos la posición de la cámara
		camara.update();

		//Ponemos a cero el atributo stateTime, que marca el tiempo de ejecución de la animación del personaje principal
		stateTime = 0f;
		//Cargamos la imagen del personaje principal en el objeto img de la clase Texture
		imagenPrincipal = new Texture(Gdx.files.internal("characters/player/character.png"));

		//Sacamos los frames de img en un array bidimensional de TextureRegion
		TextureRegion[][] tmp = TextureRegion.split(imagenPrincipal, imagenPrincipal.getWidth() / FRAME_COLS, imagenPrincipal.getHeight() / FRAME_ROWS);

		//Tile Inicial y Final
		celdaInicial = new Vector2(0, 0);
		celdaFinal = new Vector2(10, 6);

		posicionJugador = new Vector2(posicionaMapa(celdaInicial));

		//Creamos las distintas animaciones en bucle, teniendo en cuenta que el timepo entre frames será 150 milisegundos

		float frameJugador = 0.15f;

		jugadorAbajo = new Animation<>(frameJugador, tmp[0]); //Fila 0, dirección abajo
		jugadorAbajo.setPlayMode(Animation.PlayMode.LOOP);
		jugadorIzquierda = new Animation<>(frameJugador, tmp[1]); //Fila 1, dirección izquierda
		jugadorIzquierda.setPlayMode(Animation.PlayMode.LOOP);
		jugadorDerecha = new Animation<>(frameJugador, tmp[2]); //Fila 2, dirección derecha
		jugadorDerecha.setPlayMode(Animation.PlayMode.LOOP);
		jugadorArriba = new Animation<>(frameJugador, tmp[3]); //Fila 3, dirección arriba
		jugadorArriba.setPlayMode(Animation.PlayMode.LOOP);

		//En principio se utiliza la animación en la dirección abajo
		jugador = jugadorAbajo;

		//Dimensiones del jugador
		anchoJugador = tmp[0][0].getRegionWidth();
		altoJugador = tmp[0][0].getRegionHeight();
		//Variable para contar los tesoros recogidos
		cuentaTesoros = 0;

		//Velocidad del jugador (puede hacerse un menú de configuración para cambiar la dificultad del juego)
		velocidadJugador = 2.5f;


		enemies[0] = new Enemy(
				new Texture(Gdx.files.internal("characters/enemy/female_16-1.png")),
				posicionaMapa(new Vector2(1,2)),
				posicionaMapa(new Vector2(8,2))
		);
		enemies[1] = new Enemy(
				new Texture(Gdx.files.internal("characters/enemy/enemy_18.png")),
				posicionaMapa(new Vector2(6,3)),
				posicionaMapa(new Vector2(6,6))
		);
		enemies[1].setSpeed(1.5f);
		enemies[2] = new Enemy(
				new Texture(Gdx.files.internal("characters/enemy/enemy_19.png")),
				posicionaMapa(new Vector2(9,2)),
				posicionaMapa(new Vector2(9,6))
		);
		enemies[3] = new Enemy(
				new Texture(Gdx.files.internal("characters/enemy/dog_01-2r.png")),
				posicionaMapa(new Vector2(12,0)),
				posicionaMapa(new Vector2(12,3))
		);
		enemies[4] = new Enemy(
				new Texture(Gdx.files.internal("characters/enemy/pien.png")),
				posicionaMapa(new Vector2(5,7)),
				posicionaMapa(new Vector2(5,8))
		);
		enemies[4].setSpeed(0.25f);

		//Inicializamos la musica de fondo del juego
		musicaJuego = Gdx.audio.newMusic(Gdx.files.internal("sound/background.mp3"));
		musicaJuego.setLooping(true);
		musicaJuego.setVolume(0.4f);

		//Inicializamos los sonidos
		tesoroEncontrado = Gdx.audio.newSound(Gdx.files.internal("sound/pickup.mp3"));
		pillado = Gdx.audio.newSound(Gdx.files.internal("sound/death.mp3"));
		fracaso = Gdx.audio.newSound(Gdx.files.internal("sound/lose.mp3"));
		exito = Gdx.audio.newSound(Gdx.files.internal("sound/victory.mp3"));

		//Sonido de pasos
		pasos = Gdx.audio.newSound(Gdx.files.internal("sound/steps.mp3"));
		cycle = 0;
		cycle_ant = 0;//Sirven para controlar los ciclos de reproduccion del sonido pasos

		//Textos sobreimpresionados
		fontVidas = new BitmapFont();
		fontTesoros = new BitmapFont();

		nVidas = 3;
	}

	@Override
	public void render () {
		//Reproducimos la musica del juego
		if (!musicaJuego.isPlaying())
			musicaJuego.play();

		//ponemos a la escucha de eventos la propia clase del juego
		Gdx.input.setInputProcessor(this);

		//Para borrar la pantalla
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		//Vinculamos el objeto que dibuja el mapa con la cámara del juego
		mapRenderer.setView(camara);

		//Dibujamos las capas del mapa
		//Posteriormente quitaremos la capa de profundidad para intercalar a los personajes
		int[] capas = {0, 1, 2, 3, 4, 5};
		mapRenderer.render(capas);

		//Centramos la camara en el jugador principal
		camara.position.set(posicionJugador, 0);

		//Comprobamos que la cámara no se salga de los límites del mapa de baldosas con el método MathUtils.clamp
		camara.position.x = MathUtils.clamp(camara.position.x,
				camara.viewportWidth / 2f,
				anchoMapa - camara.viewportWidth / 2f);
		camara.position.y = MathUtils.clamp(camara.position.y,
				camara.viewportHeight / 2f,
				altoMapa - camara.viewportHeight / 2f);

		//Actualizamos la cámara del juego
		camara.update();
		//Vinculamos el objeto que dibuja el mapa con la cámara del juego
		mapRenderer.setView(camara);

		//ANIMACION DEL JUGADOR

		//En este método actualizaremos la posición del jugador principal
		actualizaPosicionJugador();

		// Indicamos al SpriteBatch que se muestre en el sistema de coordenadas específicas de la cámara.
		batch.setProjectionMatrix(camara.combined);

		//Inicializamos el objeto SpriteBatch
		batch.begin();

		batch.draw(jugador.getKeyFrame(stateTime), posicionJugador.x, posicionJugador.y);

		//Deteccion de colisiones con NPC
		detectaColisiones();

		for (Enemy enemy : enemies) {
			enemy.move();
			batch.draw(enemy.getActive().getKeyFrame(enemy.getStateTime()), enemy.getPosition().x, enemy.getPosition().y);
		}

		batch.end();

		String infoTesoros = "Tesoros: " + cuentaTesoros;
		String infoVidas = "Vidas: " + nVidas;

		batch.begin();
		//Pintamos la capa de profundidad del mapa de baldosas.
		mapRenderer.render(new int[] { 5 });

		fontTesoros.draw(batch, infoTesoros, camara.position.x - camara.viewportWidth / 2, camara.position.y - camara.viewportHeight / 2 + 60);
		fontVidas.draw(batch, infoVidas, camara.position.x - camara.viewportWidth / 2, camara.position.y - camara.viewportHeight / 2 + 30);

		//Finalizamos el objeto SpriteBatch
		batch.end();
	}
	
	@Override
	public void dispose () {
		//Texture
		imagenPrincipal.dispose();
		//SpriteBatch
		if (batch.isDrawing())
			batch.dispose();

		for (Enemy enemy : enemies) {
			enemy.getImg().dispose();
		}

		//Music
		musicaJuego.dispose();

		//Sound
		tesoroEncontrado.dispose();
		pillado.dispose();
		exito.dispose();
		fracaso.dispose();

		fontVidas.dispose();
		fontTesoros.dispose();
	}

	private Vector2 posicionaMapa(Vector2 celda) {
		Vector2 res = new Vector2();
		if (celda.x + 1 > anchoTiles ||
				celda.y + 1 > altoTiles) {  //Si la peticion esta mal, situamos en el origen del mapa
			res.set(0, 0);
		}
		res.x = celda.x * anchoCelda;
		res.y = (altoTiles - 1 - celda.y) * altoCelda;
		return res;
	}

	private void actualizaPosicionJugador() {
		//Guardamos la posicion del jugador por si encontramos algun obstaculo
		Vector2 posicionAnterior = new Vector2();
		posicionAnterior.set(posicionJugador);

		float velocidad;
		//Los booleanos izquierda, derecha, arriba y abajo recogen la dirección del personaje,
		//para permitir direcciones oblícuas no deben ser excluyentes.
		//Pero sí debemos excluir la simultaneidad entre arriba/abajo e izquierda/derecha
		//para no tener direcciones contradictorias

		// velocidad = (arriba || abajo) && (derecha || izquierda) ? velocidadJugador / 2 : velocidadJugador;

		velocidad = velocidadJugador;

		if (izquierda) {
			posicionJugador.x -= velocidad;
			jugador = jugadorIzquierda;
		}
		if (derecha) {
			posicionJugador.x += velocidad;
			jugador = jugadorDerecha;
		}
		if (arriba) {
			posicionJugador.y += velocidad;
			jugador = jugadorArriba;
		}
		if (abajo) {
			posicionJugador.y -= velocidad;
			jugador = jugadorAbajo;
		}

		//Avanzamos el stateTime del jugador principal cuando hay algún estado de movimiento activo
		if (izquierda || derecha || arriba || abajo) {
			//Control del sonido de los pasos del jugador
			stateTime += Gdx.graphics.getDeltaTime();
			cycle = (int) (stateTime / 0.2f);
			if (cycle != cycle_ant)
				pasos.play(0.4f);
			cycle_ant = cycle;
		}

		//Limites en el mapa para el jugador
		posicionJugador.x = MathUtils.clamp(posicionJugador.x, 0, anchoMapa - anchoJugador);
		posicionJugador.y = MathUtils.clamp(posicionJugador.y, 0, altoMapa - altoJugador);

		//Detección de obstaculos
		if (obstaculo(posicionJugador))
			posicionJugador.set(posicionAnterior);

		//Deteccion de fin del mapa
		if (celdaActual(posicionJugador).epsilonEquals(celdaFinal)) {
			exito.play();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			restartGame();
		}

		//Deteccion de tesoros: calculamos la celda en la que se encuentran los límites de la zona de contacto.
		int limIzq = (int) ((posicionJugador.x + 0.25 * anchoJugador) / anchoCelda);
		int limDrcha = (int) ((posicionJugador.x + 0.75 * anchoJugador) / anchoCelda);
		int limSup = (int) ((posicionJugador.y + 0.25 * altoJugador) / altoCelda);
		int limInf = (int) ((posicionJugador.y) / altoCelda);

		if (tesoro[limIzq][limInf] || tesoro[limDrcha][limSup]) {
			TiledMapTileLayer.Cell celda;
			if (tesoro[limIzq][limInf]) {
				celda = capaTesoros.getCell(limIzq, limInf);
				tesoro[limIzq][limInf] = false;
			} else {
				celda = capaTesoros.getCell(limDrcha, limSup);
				tesoro[limDrcha][limSup] = false;
			}
			celda.setTile(null);
			tesoroEncontrado.play();
			cuentaTesoros++;
		}
	}

	private void detectaColisiones() {
		//Vamos a comprobar que el rectángulo de contacto del jugador
		//no se solape con el rectángulo de contacto del npc
		Rectangle rJugador = new Rectangle((float) (posicionJugador.x + (0.25 * anchoJugador)), (float) (posicionJugador.y + (0.25 * altoJugador)),
				(float) (anchoJugador * 0.5), (float) (altoJugador * 0.5));
		Rectangle rNPC;
		//Ahora recorremos el array de NPC, para cada uno generamos su rectángulo de contacto
		for (Enemy enemy : enemies) {
			rNPC = new Rectangle((float) (enemy.getPosition().x + 0.1 * anchoJugador), (float) (enemy.getPosition().y + 0.1 * altoJugador),
					(float) (0.65 * anchoJugador), (float) (0.65 * altoJugador));
			//Si hay colision
			if (rJugador.overlaps(rNPC)) {
				//Código de fin de partida
				float posicionMusica = musicaJuego.getPosition();
				musicaJuego.pause();
				//reproducimos el sonido "pillado"
				pillado.play();
				try {
					Thread.sleep(800);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				--nVidas;

				if (nVidas == 0) {
					fracaso.play();
					try {
						Thread.sleep(2300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					restartGame();
				}

				musicaJuego.setPosition(posicionMusica);
				//reanudamos la musica del juego
				musicaJuego.play();
				posicionJugador.set(posicionaMapa(celdaInicial));
				return; //Acabamos el bucle si hay una sola colisión
			}
		}
	}

	//Metodo que detecta si hay un obstaculo en una determinada posicion
	private boolean obstaculo(Vector2 posicion) {
		int limIzq = (int) ((posicion.x + 0.7 * anchoJugador) / anchoCelda);
		int limDrcha = (int) ((posicion.x + 0.15 * anchoJugador) / anchoCelda);
		int limSup = (int) ((posicion.y + 0.05 * altoJugador) / altoCelda);
		int limInf = (int) ((posicion.y) / altoCelda);

		return obstaculo[limIzq][limInf] || obstaculo[limDrcha][limSup];
	}

	private void restartGame() {
		cuentaTesoros = 0;
		nVidas = 3;

		posicionJugador.set(posicionaMapa(celdaInicial));

		capaTesoros = (TiledMapTileLayer) mapa.getLayers().get(1);

		//Creamos un array bidimensional de booleanos para obstáculos y tesoros
		tesoro = new boolean[anchoTiles][altoTiles];

		//Rellenamos los valores recorriendo el mapa
		for (int x = 0; x < anchoTiles; x++) {
			for (int y = 0; y < altoTiles; y++) {
				//rellenamos el array bidimensional de los tesoros
				tesoro[x][y] = (capaTesoros.getCell(x, y) != null);
				if (tesoro[x][y])
					capaTesoros.getCell(x, y).setTile(tileTesoro);
			}
		}
	}

	//Método que convierte la posición del jugador en la celda en la que está
	private Vector2 celdaActual(Vector2 posicion) {
		return new Vector2((int) (posicion.x / anchoCelda), (altoTiles - 1 - (int) (posicion.y / altoCelda)));
	}

	//Con estos setters se impide la situacion de direcciones contradictorias pero no las
	//direcciones compuestas que permiten movimientos oblícuos
	private void setIzquierda(boolean izq) {
		if (derecha && izq) derecha = false;
		izquierda = izq;
	}

	private void setDerecha(boolean der) {
		if (izquierda && der) izquierda = false;
		derecha = der;
	}

	private void setArriba(boolean arr) {
		if (abajo && arr) abajo = false;
		arriba = arr;
	}

	private void setAbajo(boolean abj) {
		if (arriba && abj) arriba = false;
		abajo = abj;
	}

	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {
			case Input.Keys.LEFT:
				setIzquierda(true);
				break;
			case Input.Keys.RIGHT:
				setDerecha(true);
				break;
			case Input.Keys.UP:
				setArriba(true);
				break;
			case Input.Keys.DOWN:
				setAbajo(true);
				break;
		}
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch (keycode) {
			case Input.Keys.LEFT:
				setIzquierda(false);
				break;
			case Input.Keys.RIGHT:
				setDerecha(false);
				break;
			case Input.Keys.UP:
				setArriba(false);
				break;
			case Input.Keys.DOWN:
				setAbajo(false);
				break;
		}

		//Para ocultar/mostrar las distintas capas pulsamos desde el 1 en adelante...
		int codigoCapa = keycode - Input.Keys.NUM_1;
		if (codigoCapa <= mapa.getLayers().getCount())
			mapa.getLayers().get(codigoCapa).setVisible(!mapa.getLayers().get(codigoCapa).isVisible());

		return true;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		Vector3 clickCoordinates = new Vector3(screenX, screenY, 0f);
		//Transformamos las coordenadas del vector a coordenadas de nuestra camara
		Vector3 pulsacion3d = camara.unproject(clickCoordinates);
		Vector2 pulsacion = new Vector2(pulsacion3d.x, pulsacion3d.y);

		//Calculamos la diferencia entre la pulsacion y el centro del jugador
		Vector2 centroJugador = new Vector2(posicionJugador).add((float) anchoJugador / 2, (float) altoJugador / 2);
		Vector2 diferencia = new Vector2(pulsacion.sub(centroJugador));

		//Vamos a determinar la intencion del usuario para mover al personaje en funcion del
		//angulo entre la pulsacion y la posicion del jugador
		float angulo = diferencia.angleDeg();

		if (angulo > 30 && angulo <= 150) setArriba(true);
		if (angulo > 120 && angulo <= 240) setIzquierda(true);
		if (angulo > 210 && angulo <= 330) setAbajo(true);
		if ((angulo > 0 && angulo <= 60) || (angulo > 300 && angulo < 360)) setDerecha(true);

		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		setArriba(false);
		setAbajo(false);
		setIzquierda(false);
		setDerecha(false);

		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		//mismo caso que touchDown
		touchDown(screenX,screenY,pointer,0);
		return true;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		return false;
	}
}
