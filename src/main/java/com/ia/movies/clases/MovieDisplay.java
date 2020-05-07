package com.ia.movies.clases;

import org.bson.types.ObjectId;

public class MovieDisplay {

    public ObjectId _id;

    public String color = "";

    public String director = "";

    public String actores = "";

    public String keywords = "";

    public String genres = "";

    public String rating = "";

    public String language = "";

    public int year = 0;

    public String title = "";

    public float score = 0;

    public int facebook_likes = 0;
    
    public int duration = 0;

    public String country = "";

    public MovieDisplay(Movie pelicula) {
        try {
            _id = pelicula._id;
            color = pelicula.color;
            director = pelicula.director;
            try {
                for (int i =  0; i < pelicula.actores.length; i++) {
                    actores += pelicula.actores[i] + ", ";
                }
                actores = actores.substring(0, actores.length() - 2);
            }
            catch (Exception ex) {

            }
            try {
                for (int i =  0; i < pelicula.genres.length; i++) {
                    genres += pelicula.genres[i] + ", ";
                }
                genres = genres.substring(0, genres.length() - 2);
            }
            catch (Exception ex) {
                
            }
            try {
                for (int i =  0; i < pelicula.keywords.length; i++) {
                    keywords += pelicula.keywords[i] + ", ";
                }
                keywords = keywords.substring(0, keywords.length() - 2);
            }
            catch (Exception ex) {
                
            }
            rating = pelicula.rating;
            language = pelicula.language;
            year = pelicula.year;
            title = pelicula.title;
            score = pelicula.score;
            facebook_likes = pelicula.facebook_likes;
            duration = pelicula.duration;
            country = pelicula.country;
        }
        catch (Exception ex) {
            System.out.println("error :c");
        }
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getActores() {
        return actores;
    }

    public void setActores(String actores) {
        this.actores = actores;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public int getFacebook_likes() {
        return facebook_likes;
    }

    public void setFacebook_likes(int facebook_likes) {
        this.facebook_likes = facebook_likes;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}