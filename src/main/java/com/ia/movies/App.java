package com.ia.movies;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import com.google.gson.Gson;

import com.ia.movies.clases.Movie;
import com.ia.movies.clases.MovieDisplay;

import java.util.List;
import java.util.ArrayList;


/**
 * Hello world!
 *
 */
public class App extends Application
{
    VBox contenedor_peliculas = new VBox();
    MongoClient mongo_client = null;
    List<MovieDisplay> a_desplegar = new ArrayList();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Recomendacion de peliculas");
        
        TableView tb = loadFirstTable();
        System.out.println("paso esto");
        
        Scene scene = new Scene(new StackPane(tb), 720, 480);
        stage.setScene(scene);
        stage.show();
    }

    public TableView loadFirstTable() {
        List<Movie> peliculas = getMovies();
        a_desplegar = new ArrayList();
        for (int i = 0; i < peliculas.size(); i++) {
            if (peliculas.get(i) != null) {
                a_desplegar.add(new MovieDisplay(peliculas.get(i)));
            }
        }
        String[] titulos = {
            "Titulo", "Director", "Año", "Generos", "Actores", 
            "Rating", "Idioma", "Puntuación"
        };
        String[] nombres = {
            "title", "director", "year", "genres", "actores", 
            "rating", "language", "score"
        };
        TableView tabla = new TableView();
        ObservableList data = FXCollections.observableList(a_desplegar);
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
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            cursor.close();
        }
        return peliculas;
    }

    public static void main( String[] args )
    {
        launch();
    }
}
