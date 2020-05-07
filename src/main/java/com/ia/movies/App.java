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
        
        TableView<Movie> tabla = new TableView<Movie>();
        tabla.setEditable(false);

        TableColumn<Movie, String> title = new TableColumn<Movie, String>("Title");
        title.setCellValueFactory(
            new Callback<TableColumn.CellDataFeatures<Movie,String>,ObservableValue<String>>(){
            
                @Override
                public ObservableValue<String> call(CellDataFeatures<Movie, String> param) {
                    return new ReadOnlyStringWrapper(param.getValue().title);
                }
            }
        );
        TableColumn<Movie, String> duration = new TableColumn<Movie, String>("Duration");
        duration.setCellValueFactory(
            new Callback<TableColumn.CellDataFeatures<Movie,String>,ObservableValue<String>>(){
            
                @Override
                public ObservableValue<String> call(CellDataFeatures<Movie, String> param) {
                    return new ReadOnlyStringWrapper(String.valueOf(param.getValue().duration));
                }
            }
        );
        TableColumn<Movie, Integer> year = new TableColumn<Movie, Integer>("Year");
        year.setCellValueFactory(
            new Callback<TableColumn.CellDataFeatures<Movie,Integer>,ObservableValue<Integer>>(){
            
                @Override
                public ObservableValue<Integer> call(CellDataFeatures<Movie, Integer> param) {
                    return new ReadOnlyObjectWrapper(param.getValue().year);
                }
            }
        );
        TableColumn<Movie, String> color = new TableColumn<Movie, String>("Color");
        color.setCellValueFactory(
            new Callback<TableColumn.CellDataFeatures<Movie,String>,ObservableValue<String>>(){
            
                @Override
                public ObservableValue<String> call(CellDataFeatures<Movie, String> param) {
                    return new ReadOnlyStringWrapper(param.getValue().color);
                }
            }
        );
        TableColumn<Movie, String> rating = new TableColumn<Movie, String>("Rating");
        rating.setCellValueFactory(
            new Callback<TableColumn.CellDataFeatures<Movie,String>,ObservableValue<String>>(){
            
                @Override
                public ObservableValue<String> call(CellDataFeatures<Movie, String> param) {
                    return new ReadOnlyStringWrapper(param.getValue().rating);
                }
            }
        );
        TableColumn<Movie, String> director = new TableColumn<Movie, String>("Director");
        director.setCellValueFactory(
            new Callback<TableColumn.CellDataFeatures<Movie,String>,ObservableValue<String>>(){
            
                @Override
                public ObservableValue<String> call(CellDataFeatures<Movie, String> param) {
                    return new ReadOnlyStringWrapper(param.getValue().director);
                }
            }
        );
        TableColumn<Movie, String> language = new TableColumn<Movie, String>("Language");
        language.setCellValueFactory(
            new Callback<TableColumn.CellDataFeatures<Movie,String>,ObservableValue<String>>(){
            
                @Override
                public ObservableValue<String> call(CellDataFeatures<Movie, String> param) {
                    return new ReadOnlyStringWrapper(param.getValue().language);
                }
            }
        );
        TableColumn<Movie, String> genres = new TableColumn<Movie, String>("Genres");
        genres.setCellValueFactory(
            new Callback<TableColumn.CellDataFeatures<Movie,String>,ObservableValue<String>>(){
            
                @Override
                public ObservableValue<String> call(CellDataFeatures<Movie, String> param) {
                    String finalVal = "";
                    for (String genre : param.getValue().genres) {
                        finalVal += genre + ", ";
                    }
                    finalVal = finalVal.substring(0, finalVal.length() - 1);
                    return new ReadOnlyStringWrapper(finalVal);
                }
            }
        );
        TableColumn<Movie, String> actors = new TableColumn<Movie, String>("Actors");
        actors.setCellValueFactory(
            new Callback<TableColumn.CellDataFeatures<Movie,String>,ObservableValue<String>>(){
            
                @Override
                public ObservableValue<String> call(CellDataFeatures<Movie, String> param) {
                    String finalVal = "";
                    for (String actor : param.getValue().actores) {
                        finalVal += actor + ", ";
                    }
                    finalVal = finalVal.substring(0, finalVal.length() - 1);
                    return new ReadOnlyStringWrapper(finalVal);
                }
            }
        );
        TableColumn<Movie, Float> imdb_score = new TableColumn<Movie, Float>("IMDB Score");
        imdb_score.setCellValueFactory(
            new Callback<TableColumn.CellDataFeatures<Movie,Float>,ObservableValue<Float>>(){
            
                @Override
                public ObservableValue<Float> call(CellDataFeatures<Movie, Float> param) {
                    return new ReadOnlyObjectWrapper(String.valueOf(param.getValue().score));
                }
            }
        );
        TableColumn<Movie, Integer> facebook_likes = new TableColumn<Movie, Integer>("Facebooks likes");
        facebook_likes.setCellValueFactory(
            new Callback<TableColumn.CellDataFeatures<Movie,Integer>,ObservableValue<Integer>>(){
            
                @Override
                public ObservableValue<Integer> call(CellDataFeatures<Movie, Integer> param) {
                    return new ReadOnlyObjectWrapper(param.getValue().facebook_likes);
                }
            }
        );
        TableColumn<Movie, String> country = new TableColumn<Movie, String>("Country");

        tabla.getColumns().addAll(title, duration, year, color, rating, director, language, genres, actors, imdb_score, facebook_likes, country);

        ObservableList<Movie> movies_observable = FXCollections.observableArrayList();

        for (Movie peli : peliculas) {
            movies_observable.add(peli);
        }
        tabla.setItems(movies_observable);
        HBox contenedor = new HBox(tabla);
        Scene scene = new Scene(new StackPane(contenedor), 720, 480);
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
