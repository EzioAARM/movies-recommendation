package com.ia.movies;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.control.Label;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import com.google.gson.Gson;
import com.ia.movies.clases.CalculosMovie;
import com.ia.movies.clases.Movie;
import com.ia.movies.clases.MovieDisplay;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;

public class App extends Application
{
    VBox contenedor_peliculas = new VBox();
    MongoClient mongo_client = null;
    List<MovieDisplay> a_desplegar = new ArrayList();
    List<Movie> PeliculasGeneral = new ArrayList<>();

    /*Listas con los distintos campos que existen*/
    List<Object> generos = new ArrayList<>();
    List<Object> director = new ArrayList<>();
    List<Object> actores = new ArrayList<>();
    List<Object> keywords = new ArrayList<>();
    List<Object> rating = new ArrayList<>();
    List<Object> language = new ArrayList<>();

    /*Listas que guardan la informacion*/
    List<String> spamGeneros = new ArrayList<>();
    List<String> spamDirector = new ArrayList<>();
    List<String> spamActores = new ArrayList<>();
    List<String> spamKeywords = new ArrayList<>();
    List<String> spamRating = new ArrayList<>();
    List<String> spamLanguage = new ArrayList<>();

    List<String> hamGeneros = new ArrayList<>();
    List<String> hamDirector = new ArrayList<>();
    List<String> hamActores = new ArrayList<>();
    List<String> hamKeywords = new ArrayList<>();
    List<String> hamRating = new ArrayList<>();
    List<String> hamLanguage = new ArrayList<>();

    List<String> spamGenerosOraciones = new ArrayList<>();
    List<String> spamActoresOraciones = new ArrayList<>();
    List<String> spamKeywordsOraciones = new ArrayList<>();

    List<String> hamGenerosOraciones = new ArrayList<>();
    List<String> hamActoresOraciones = new ArrayList<>();
    List<String> hamKeywordsOraciones = new ArrayList<>();

    /*Diccionario de peliculas con probabilidad*/
    List<CalculosMovie> peliculasCalculadas = new ArrayList<>();


    @Override
    public void start(Stage stage) {
        stage.setTitle("Recomendacion de peliculas");
        PeliculasGeneral = getMovies();
        PeliculasGeneral.forEach(new Consumer<Movie>() {
            @Override
            public void accept(final Movie pelicula) {
                agregarModelo(pelicula, pelicula.score >= 7);
            }
        });
        reloadModel();
        final List<Movie> recomendadas = new ArrayList<>();
        System.out.print(peliculasCalculadas.size());
        peliculasCalculadas.forEach(new Consumer<CalculosMovie>() {
            @Override
            public void accept(final CalculosMovie item) {
                if (item.probabilidad > 0.6) {
                    recomendadas.add(item.pelicula);
                }
            }
        });
        Collections.sort(recomendadas);
        Collections.sort(PeliculasGeneral);
        List<MovieDisplay> recomendadasDisplay = fromMovieToDisplay(recomendadas);
        System.out.println(recomendadasDisplay.size());
        VBox recommendSection = loadSection("Recomendadas", FXCollections.observableList(recomendadasDisplay));
        a_desplegar = fromMovieToDisplay(PeliculasGeneral);
        ObservableList data = FXCollections.observableList(a_desplegar);
        VBox contenido = loadSection("Todas las peliculas", data);
        contenedor_peliculas = new VBox(recommendSection, contenido);
        Scene scene = new Scene(new StackPane(contenedor_peliculas), 720, 480);
        stage.setScene(scene);
        stage.show();
    }

    public void reloadModel() {
        final List<Double> probabilidadesPelicula = new ArrayList();
        PeliculasGeneral.forEach(new Consumer<Movie>() {
            @Override
            public void accept(final Movie peliculaActual) {
                /*generos*/
                int cantSpam = 0;
                int cantHam = 0;
                double probtemp = 1.0;
                for (String genero : peliculaActual.genres) {
                    cantSpam = getTimesArray(spamGeneros, genero);
                    cantHam = getTimesArray(hamGeneros, genero);
                    probtemp *= calcularProbabilidad(
                        Double.valueOf(cantSpam) / Double.valueOf(spamGeneros.size()), 
                        Double.valueOf(spamGenerosOraciones.size()) / (Double.valueOf(spamGenerosOraciones.size()) + Double.valueOf(hamGenerosOraciones.size())), 
                        Double.valueOf(cantHam) / hamGeneros.size(), 
                        Double.valueOf(hamGenerosOraciones.size()) / (Double.valueOf(spamGenerosOraciones.size()) + Double.valueOf(hamGenerosOraciones.size())));
                    
                }
                peliculasCalculadas.add(new CalculosMovie(peliculaActual, probtemp));
            }
        });
    }

    public int getTimesArray(String[] container, String value) {
        int apariciones = 0;
        for (String item : container) {
            if (item.equals(value)) apariciones++;
        }
        return apariciones;
    }

    public int getTimesArray(List<String> container, String value) {
        int apariciones = 0;
        for (String item : container) {
            if (item.equals(value)) apariciones++;
        }
        return apariciones;
    }

    public double calcularProbabilidad(double mspam, double spam, double mham, double ham) {
        return (mspam * spam) / ((mspam * spam) + (mham * ham));
    }

    /**
     * Guarda en ham o spam los datos de una pelicula, segun lo indicado en esSpam
     * @param pelicula objeto pelicula para obtener su informacion
     * @param esSpam para saber si guardarlo en spam o en ham
     */
    public void agregarModelo(Movie pelicula, boolean esSpam) {
        if (esSpam) {
            for (String genero : pelicula.genres) spamGeneros.add(genero);
            spamDirector.add(pelicula.director);
            for (String actor : pelicula.actores) spamActores.add(actor);
            for (String key : pelicula.keywords) spamKeywords.add(key);
            spamRating.add(pelicula.rating);
            spamLanguage.add(pelicula.language);
            spamGenerosOraciones.add(fromArrayString(pelicula.genres));
            spamActoresOraciones.add(fromArrayString(pelicula.actores));
            spamKeywordsOraciones.add(fromArrayString(pelicula.keywords));
        } else {
            for (String genero : pelicula.genres) hamGeneros.add(genero);
            hamDirector.add(pelicula.director);
            for (String actor : pelicula.actores) hamActores.add(actor);
            for (String key : pelicula.keywords) hamKeywords.add(key);
            hamRating.add(pelicula.rating);
            hamLanguage.add(pelicula.language);
            hamGenerosOraciones.add(fromArrayString(pelicula.genres));
            hamActoresOraciones.add(fromArrayString(pelicula.actores));
            hamKeywordsOraciones.add(fromArrayString(pelicula.keywords));
        }
    }

    public String fromArrayString(String[] miArray) {
        if (miArray != null) {
            if (miArray.length > 0) {
                String arrayData = "";
                for (String itemString : miArray) {
                    arrayData += itemString + "|";
                }
                return arrayData.substring(0, arrayData.length() - 2);
            }
        }
        return "";
    }

    /**
     * Funcion para convertir de Lista de Movie a Lista de MovieDisplay
     * @param peliculas listado de peliculas
     * @return listado de peliculas para mostrar
     */
    public List<MovieDisplay> fromMovieToDisplay(List<Movie> peliculas) {
        List<MovieDisplay> transformadas = new ArrayList();
        for (int i = 0; i < peliculas.size(); i++) {
            if (peliculas.get(i) != null) {
                transformadas.add(new MovieDisplay(peliculas.get(i)));
            }
        }
        return transformadas;
    }

    /**
     * Construye una seccion con titulo y tabla de datos
     * @param title Titulo de la seccion
     * @param data El listado de peliculas que se van a mostrar
     * @return Devuelve el objeto VBox para agregarlo al contenedor
     */
    public VBox loadSection(String title, ObservableList data) {
        TableView tabla = loadTable(data);
        Label tituloSeccion = new Label();
        tituloSeccion.setText(title);
        tituloSeccion.setFont(new Font("Arial", 24));
        VBox seccion = new VBox(tituloSeccion, tabla);
        return seccion;
    }

    /**
     * Carga la tabla con los datos que se envio en data con los campos del array titulos
     * @param data las peliculas que se desean tener en la tabla
     * @return devuelve el objeto TableView para mostrar en el form
     */
    public TableView loadTable(ObservableList data) {
        String[] titulos = {
            "Titulo", "Director", "Año", "Generos", "Actores", 
            "Rating", "Idioma", "Puntuación"
        };
        String[] nombres = {
            "title", "director", "year", "genres", "actores", 
            "rating", "language", "score"
        };
        TableView tabla = new TableView();
        tabla.setItems(data);
        int i = 0;
        while (i < titulos.length) {
            TableColumn columna = new TableColumn(nombres[i]);
            columna.setCellValueFactory(new PropertyValueFactory(nombres[i]));
            tabla.getColumns().add(columna);
            i++;
        }
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return tabla;
    }

    /**
     * Obtiene las peliculas de una base de datos de mongodb en localhost
     * @return Devuelve un list de Movie con todas las peliculas que encontro
     */
    public List<Movie> getMovies() {
        List<Movie> peliculas = new ArrayList<Movie>();
        mongo_client = new MongoClient("localhost", 27017);
        MongoDatabase database = mongo_client.getDatabase("movie_metadata");
        MongoCollection<Document> collection = database.getCollection("movies");
        MongoCursor<Document> cursor = collection.find().iterator();
        try {
            while (cursor.hasNext()) {
                String result_string = cursor.next().toJson().toString();
                Gson json_result = new Gson();
                Movie pelicula = json_result.fromJson(result_string, Movie.class);
                peliculas.add(pelicula);
                for (int j = 0; j < pelicula.genres.length; j++) {
                    generos.add(pelicula.genres[j].trim());
                }
                for (int j = 0; j < pelicula.actores.length; j++) {
                    actores.add(pelicula.actores[j].trim());
                }
                for (int j = 0; j < pelicula.keywords.length; j++) {
                    keywords.add(pelicula.keywords[j].trim());
                }
                director.add(pelicula.director.trim());
                rating.add(pelicula.rating);
                language.add(pelicula.language);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            cursor.close();
            generos = generos.stream().distinct().collect(Collectors.toList());
            actores = actores.stream().distinct().collect(Collectors.toList());
            keywords = keywords.stream().distinct().collect(Collectors.toList());
            director = director.stream().distinct().collect(Collectors.toList());
            rating = rating.stream().distinct().collect(Collectors.toList());
            language = language.stream().distinct().collect(Collectors.toList());
        }
        return peliculas;
    }

    public static void main( String[] args )
    {
        launch();
    }
}
