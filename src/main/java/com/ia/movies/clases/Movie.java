package com.ia.movies.clases;

import org.bson.types.ObjectId;

public class Movie implements Comparable {

    public ObjectId _id;

    public String color;

    public String director;

    public String[] actores;

    public String[] keywords;

    public String[] genres;

    public String rating;

    public String language;

    public int year;

    public String title;

    public float score;

    public int facebook_likes;
    
    public int duration;

    public String country;

    @Override
    public int compareTo(Object o) {
        Movie pelicula = (Movie) o;
        return pelicula.title.compareTo(this.title);
    }
}