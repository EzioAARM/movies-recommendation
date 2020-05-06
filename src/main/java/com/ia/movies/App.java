package com.ia.movies;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import com.google.gson.Gson;

import com.ia.movies.clases.Movie;
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

    @Override
    public void start(Stage stage) {
        stage.setTitle("Recomendacion de peliculas");
        List<Movie> peliculas = getMovies();
        System.out.println(peliculas.get(0).toString());
        Scene scene = new Scene(new StackPane(), 720, 480);
        stage.setScene(scene);
        stage.show();
    }

    public List<Movie> getMovies() {
        List<Movie> peliculas = new ArrayList<Movie>();
        mongo_client = new MongoClient("localhost", 27017);
        MongoDatabase database = mongo_client.getDatabase("movie_metadata");
        MongoCollection<Document> collection = database.getCollection("movies");
        MongoCursor<Document> cursor = collection.find().iterator();
        System.out.println("paso esto");
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
