from flask import Flask
from flask import request
from flask import jsonify
from middleware import Middleware

app = Flask(__name__)
m = Middleware()


@app.route("/")
def hello():
    return "Hello World!"


@app.route("/register", methods=['POST'])
def register():
    username = request.json.get('username', None)
    password = request.json.get('password', None)
    return jsonify(m.register_user(username=username, password=password))


@app.route("/login", methods=['POST'])
def login():
    username = request.json.get('username', None)
    password = request.json.get('password', None)
    return jsonify(m.login_user(username=username, password=password))


@app.route('/send_game_data', methods=['POST'])
def send_game_data():
    username = request.json.get('username', None)
    score = request.json.get('score', None)
    return jsonify(m.create_game_record(username=username, score=score))


@app.route('/get_person_record', methods=['GET'])
def get_person_record():
    username = request.args['username']
    return jsonify(m.get_game_record_by_user(username=username))


@app.route('/get_global_record', methods=['GET'])
def get_global_record():
    return jsonify(m.get_best_game_record())



def run_server():
    app.run(host='0.0.0.0')

if __name__ == "__main__":
    run_server()