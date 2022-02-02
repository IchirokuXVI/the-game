package iestrassierra.dcorsan.thegame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

public class TheGame extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;
	//Objeto que recoge el mapa de baldosas
	private TiledMap mapa;

	//Capa del mapa donde se encuentran los tesoros
	private TiledMapTileLayer capaTesoros;

	//Ancho y alto del mapa en tiles
	private int anchoTiles, altoTiles;

	//Celda en la que el mapa finaliza
	private Vector2 celdaFinal;

	//Arrays bidimensionales de booleanos que contienen los obstáculos y los tesoros del mapa
	private boolean[][] obstaculo, tesoro;

	//Objeto con el que se pinta el mapa de baldosas
	private TiledMapRenderer mapaRenderer;

	//Variables de ancho y alto
	int anchoMapa, altoMapa, anchoCelda, altoCelda;

	//Variable para contabilizar el número de tesoros
	int totalTesoros;


	@Override
	public void create () {
		batch = new SpriteBatch();

		//Cargamos el mapa de baldosas desde la carpeta de assets
		mapa = new TmxMapLoader().load("map/TreasureMap.tmx");
		mapaRenderer = new OrthogonalTiledMapRenderer(mapa);

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
		TiledMapTileLayer capaObstaculos = (TiledMapTileLayer) mapa.getLayers().get(1);
		TiledMapTileLayer capaPasos = (TiledMapTileLayer) mapa.getLayers().get(2);
		capaTesoros = (TiledMapTileLayer) mapa.getLayers().get(3);
		TiledMapTileLayer capaProfundidad = (TiledMapTileLayer) mapa.getLayers().get(4);

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
				if (tesoro[x][y]) totalTesoros++;
			}
		}

		//Posiciones inicial y final del recorrido
		Vector2 celdaInicial = new Vector2(0, 0);
		celdaFinal = new Vector2(24, 1);
	}

	@Override
	public void render () {
		ScreenUtils.clear(1, 0, 0, 1);
		batch.begin();
		batch.draw(img, 0, 0);
		batch.end();

		//Para borrar la pantalla
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		//Vinculamos el objeto que dibuja el mapa con la cámara del juego
		// mapaRenderer.setView(camara);

		//Dibujamos las capas del mapa
		//Posteriormente quitaremos la capa de profundidad para intercalar a los personajes
		int[] capas = {0, 1, 2, 3, 4};
		mapaRenderer.render(capas);
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		//TiledMap
		mapa.dispose();
	}
}
