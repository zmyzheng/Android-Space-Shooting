import pymongo
from pymongo import MongoClient
import time,datetime


class Middleware():
    def __init__(self):
        client = MongoClient()
        db = client.plane_game
        self.user_collection = db.user_collection
        self.game_collection = db.game_collection

    def register_user(self, username, password):
        user = {
            "username": username,
            "password": password
        }

        if self.user_collection.find({"username": username}).count()>0:
            return {
                "status": "fail",
                "payload": "duplicate name"
            }
        else:
            self.user_collection.insert_one(user)
            return {
                "status": "success",
                "payload": ""
            }

    def remove_user(self, username, password):
        wr = self.user_collection.remove({
            'username': username,
            'password': password
        })
        return wr['n']

    def login_user(self, username, password):
        findres = self.user_collection.find_one({
            'username': username,
            'password': password
        })
        if findres:
            return {
                "status": "success",
                "payload": ""
            }
        else:
            return {
                "status": "fail",
                "payload": "wrong username or password"
            }

    def create_game_record(self, username, score):
        self.game_collection.insert_one({
            'username': username,
            'score': score,
            'time': time.time()
        })
        return {
            "status": "success",
            "payload": ""
        }

    def get_game_record_by_user(self, username):
        record = []
        for game in self.game_collection.find(
                {"username": username}, {"_id": 0}
            ).sort('score', pymongo.DESCENDING)[0:10]:
            date = datetime.datetime.fromtimestamp(game['time'])

            game['time'] = '.'.join([str(date.day), str(date.month), str(date.year)])
            record.append(game)

        return {
            "status": "success",
            "payload": record
        }

    def get_best_game_record(self):
        record = []
        for game in self.game_collection.find(
                {}, {"_id": 0}
            ).sort('score', pymongo.DESCENDING)[0:10]:
            date = datetime.datetime.fromtimestamp(game['time'])

            game['time'] = '.'.join([str(date.day), str(date.month), str(date.year)])
            record.append(game)
        return {
            "status": "success",
            "payload": record
        }

    def remove_game_record(self, username):
        wr = self.game_collection.remove({'username': username})
        return wr['n']

