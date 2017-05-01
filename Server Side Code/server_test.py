import requests
import pprint
import json

r = requests.get('http://54.236.38.109:5000/get_personal_record?username=test+user')
print (r.text)