import os
from flask import Flask, request, jsonify
import requests

mypath = os.path.dirname(os.path.abspath(__file__))
mypath_images = mypath
mypath_images += "\product9.jpg"

url = 'http://127.0.0.1:5000/'
my_img = {'image': open(mypath_images, 'rb')}
r = requests.post(url, files=my_img)

# convert server response into JSON format.
print(r.json())