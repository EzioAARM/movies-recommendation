import csv
import json
import pymongo

from pymongo import MongoClient
client = MongoClient('mongodb://localhost:27017/')
db = client.movie_metadata
collection = db.movies
json_mongo = {}
array_necesarios = []

with open('movie_metadata.csv') as csv_file:
    csv_reader = csv.reader(csv_file, delimiter=',')
    line_count = 0
    for row in csv_reader:
        if line_count > 0:
            json_mongo = {
                'color':  "N/A"  if row[0] == "" else row[0],
                'director': "N/A" if row[1] == "" else row[1],
                'actores': [
                    
                ],
                'keywords': [] if row[16] == "" else row[16].split('|'),
                'rating': "N/A" if row[21] == ""  else row[21],
                'language': "N/A" if row[19] == ""  else row[19],
                'year': "-1" if row[23] == "" else row[23],
                'genres': [] if row[9] == "" else row[9].split('|'),
                'title': "N/A" if row[11] == "" else row[11],
                'score': "-1" if row[25] == "" else row[25],
                'facebook-likes': "-1" if row[27] == "" else row[27],
                'duration': "-1" if row[3] == "" else row[3],
                'country': "N/A" if row[20] == "" else row[20]
            }
            if row[6] != '':
                json_mongo['actores'].append(row[6])
            if row[10] != '':
                json_mongo['actores'].append(row[10])
            if row[14] != '':
                json_mongo['actores'].append(row[14])
            array_necesarios.append(json_mongo)
            collection.insert(json_mongo)
        line_count = line_count + 1
print('se leyeron {line_count} lineas')