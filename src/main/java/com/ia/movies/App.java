package com.ia.movies;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;

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
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import java.util.ArrayList;
import java.util.Collections;

public class App extends Application
{
    VBox contenedor_peliculas = new VBox();
    MongoClient mongo_client = null;
    List<MovieDisplay> a_desplegar = new ArrayList<>();
    List<Movie> PeliculasGeneral = new ArrayList<>();
    VBox allDisplayMovie = new VBox();

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
    Button botonReload = new Button("Recargar recomendadas");


    @Override
    public void start(Stage stage) {
        stage.setTitle("Recomendacion de peliculas");

        botonReload.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                reloadModel();
                final List<Movie> recomendadas = new ArrayList<>();
                peliculasCalculadas.forEach(new Consumer<CalculosMovie>() {
                    @Override
                    public void accept(final CalculosMovie item) {
                        if (item.probabilidad > 0.5) {
                            recomendadas.add(item.pelicula);
                        }
                        System.out.println(item.probabilidad);
                    }
                });
                System.out.println("Hay " + recomendadas.size() + " recomendadas");
                Collections.sort(recomendadas);
                List<MovieDisplay> recomendadasDisplay = fromMovieToDisplay(recomendadas);
                VBox recommendSection = loadSection("Recomendadas", FXCollections.observableList(recomendadasDisplay));
                a_desplegar = fromMovieToDisplay(PeliculasGeneral);
                ObservableList data = FXCollections.observableList(a_desplegar);
                allDisplayMovie= loadSection("Todas las peliculas", data);
                contenedor_peliculas = new VBox(recommendSection, allDisplayMovie);
                Scene scene = new Scene(new StackPane(new VBox(new VBox(botonReload), contenedor_peliculas)), 1000, 720);
                stage.setScene(scene);
                //stage.show();
                System.out.println("Se recargaron las recomendadas");
            }
        });

        PeliculasGeneral = getMovies();
        /*PeliculasGeneral.forEach(new Consumer<Movie>() {
            @Override
            public void accept(final Movie pelicula) {
                agregarModelo(pelicula, pelicula.score >= 7);
            }
        });*/
        reloadModel();
        final List<Movie> recomendadas = new ArrayList<>();
        peliculasCalculadas.forEach(new Consumer<CalculosMovie>() {
            @Override
            public void accept(final CalculosMovie item) {
                if (item.probabilidad > 0.5) {
                    recomendadas.add(item.pelicula);
                }
            }
        });
        Collections.sort(recomendadas);
        Collections.sort(PeliculasGeneral);
        List<MovieDisplay> recomendadasDisplay = fromMovieToDisplay(recomendadas);
        VBox recommendSection = loadSection("Recomendadas", FXCollections.observableList(recomendadasDisplay));
        a_desplegar = fromMovieToDisplay(PeliculasGeneral);
        ObservableList data = FXCollections.observableList(a_desplegar);
        allDisplayMovie= loadSection("Todas las peliculas", data);
        contenedor_peliculas = new VBox(recommendSection, allDisplayMovie);
        Scene scene = new Scene(new StackPane(new VBox(new VBox(botonReload), contenedor_peliculas)), 720, 480);
        stage.setScene(scene);
        stage.show();
    }

    public void reloadModel() {
        peliculasCalculadas = new ArrayList<>();
        PeliculasGeneral.forEach(new Consumer<Movie>() {
            @Override
            public void accept(final Movie peliculaActual) {
                /*generos*/
                List<Double> tempPorcentajes = new ArrayList();
                int cantSpam = 0;
                int cantHam = 0;
                double probtemp = 1.0;
                for (String genero : peliculaActual.genres) {
                    cantSpam = getTimesArray(spamGeneros, genero);
                    cantHam = getTimesArray(hamGeneros, genero);
                    probtemp *= calcularProbabilidad(
                        Double.valueOf(cantSpam) / Double.valueOf(spamGeneros.size()), 
                        Double.valueOf(spamGenerosOraciones.size()) / Double.valueOf(spamGenerosOraciones.size() + hamGenerosOraciones.size()), 
                        Double.valueOf(cantHam) / Double.valueOf(hamGeneros.size()), 
                        Double.valueOf(hamGenerosOraciones.size()) / Double.valueOf(spamGenerosOraciones.size() + hamGenerosOraciones.size()));
                    
                }
                tempPorcentajes.add(probtemp);
                /*Actores
                probtemp = 1;
                for (String actores : peliculaActual.actores) {
                    cantSpam = getTimesArray(spamActores, actores);
                    cantHam = getTimesArray(hamActores, actores);
                    probtemp *= calcularProbabilidad(
                        Double.valueOf(cantSpam) / Double.valueOf(spamActores.size()), 
                        Double.valueOf(spamActoresOraciones.size()) / Double.valueOf(spamActoresOraciones.size() + hamActoresOraciones.size()), 
                        Double.valueOf(cantHam) / Double.valueOf(hamActores.size()), 
                        Double.valueOf(hamActoresOraciones.size()) / Double.valueOf(spamActoresOraciones.size() + hamActoresOraciones.size()));
                    
                }
                tempPorcentajes.add(probtemp);*/
                /*Keywords*/
                probtemp = 1;
                for (String keyword : peliculaActual.keywords) {
                    cantSpam = getTimesArray(spamKeywords, keyword);
                    cantHam = getTimesArray(hamKeywords, keyword);
                    probtemp *= calcularProbabilidad(
                        Double.valueOf(cantSpam) / Double.valueOf(spamKeywords.size()), 
                        Double.valueOf(spamKeywordsOraciones.size()) / Double.valueOf(spamKeywordsOraciones.size() + hamKeywordsOraciones.size()), 
                        Double.valueOf(cantHam) / Double.valueOf(hamKeywords.size()), 
                        Double.valueOf(hamKeywordsOraciones.size()) / Double.valueOf(spamKeywordsOraciones.size() + hamKeywordsOraciones.size()));
                    
                }
                tempPorcentajes.add(probtemp);
                probtemp = 1.0;
                for (double itemProbabilidad : tempPorcentajes) {
                    probtemp *= itemProbabilidad;
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
        return (mham * ham) / ((mham * ham) + (mspam * spam));
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
        final ContextMenu tableContextMenu = new ContextMenu();
        final MenuItem likeItem = new MenuItem("Me gusta");
        likeItem.setOnAction(
            new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    final List<MovieDisplay> selectedMovie = new ArrayList<>(tabla.getSelectionModel().getSelectedItems());
                    for (Movie peliculaActual : PeliculasGeneral) {
                        if (peliculaActual._id.compareTo(selectedMovie.get(0)._id) == 0) {
                            agregarModelo(peliculaActual, false);
                        }
                    }
                }
            }
        );
        final MenuItem dislikeItem = new MenuItem("No me gusta");
        dislikeItem.setOnAction(
            new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    final List<MovieDisplay> selectedMovie = new ArrayList<>(tabla.getSelectionModel().getSelectedItems());
                    for (Movie peliculaActual : PeliculasGeneral) {
                        if (peliculaActual._id.compareTo(selectedMovie.get(0)._id) == 0) {
                            agregarModelo(peliculaActual, true);
                        }
                    }
                }
            }
        );
        tabla.setContextMenu(tableContextMenu);
        tabla.setRowFactory(
            new Callback<TableView<MovieDisplay>, TableRow<MovieDisplay>>() {
                @Override
                public TableRow<MovieDisplay> call(TableView<MovieDisplay> tableView) {
                    final TableRow<MovieDisplay> row = new TableRow<>();
                    final ContextMenu rowMenu = new ContextMenu();
                    ContextMenu tableMenu = tableView.getContextMenu();
                    if (tableMenu != null) {
                        rowMenu.getItems().addAll(tableMenu.getItems());
                    }
                    MenuItem likeItem = new MenuItem("Me gusta");
                    likeItem.setOnAction(
                        new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                final List<MovieDisplay> selectedMovie = new ArrayList<>(tabla.getSelectionModel().getSelectedItems());
                                int i = 0;
                                for (Movie peliculaActual : PeliculasGeneral) {
                                    for (MovieDisplay selectedActual : selectedMovie) {
                                        if (peliculaActual._id.compareTo(selectedActual._id) == 0) {
                                            agregarModelo(peliculaActual, false);
                                            i++;
                                        }
                                    }
                                }
                                System.out.println("Se recargaron " + i + " a ham");
                            }
                        }
                    );
                    MenuItem dislikeItem = new MenuItem("No me gusta");
                    dislikeItem.setOnAction(
                        new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                final List<MovieDisplay> selectedMovie = new ArrayList<>(tabla.getSelectionModel().getSelectedItems());
                                for (Movie peliculaActual : PeliculasGeneral) {
                                    for (MovieDisplay selectedActual : selectedMovie) {
                                        if (peliculaActual._id.compareTo(selectedActual._id) == 0) {
                                            agregarModelo(peliculaActual, false);
                                        }
                                    }
                                }
                            }
                        }
                    );
                    rowMenu.getItems().addAll(likeItem, dislikeItem);
                    row.contextMenuProperty().bind(
                        Bindings.when(Bindings.isNotNull(row.itemProperty()))
                        .then(rowMenu)
                        .otherwise((ContextMenu) null)
                    );
                    return row;
                }
            }
        );
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
        tabla.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        return tabla;
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

            spamGeneros = pasar(generos);
            spamActores = pasar(actores);
            spamKeywords = pasar(keywords);

            hamGeneros = pasar(generos);
            hamGeneros.add(generos.get(0).toString());
            hamActores = pasar(actores);
            hamActores.add(actores.get(0).toString());
            hamKeywords = pasar(keywords);
            hamKeywords.add(keywords.get(0).toString());

            for (Movie peli : peliculas) {
                spamActoresOraciones.add(fromArrayString(peli.actores));
                hamActoresOraciones.add(fromArrayString(peli.actores));

                spamGenerosOraciones.add(fromArrayString(peli.genres));
                hamGenerosOraciones.add(fromArrayString(peli.genres));

                spamKeywordsOraciones.add(fromArrayString(peli.keywords));
                hamKeywordsOraciones.add(fromArrayString(peli.keywords));
            }
        }
        return peliculas;
    }

    public List<String> pasar(List<Object> l1) {
        List<String> temp = new ArrayList<>();
        for (Object dato : l1) {
            temp.add(dato.toString());
        }
        return temp;
    }

    public static void main( String[] args )
    {
        launch();
    }
}
