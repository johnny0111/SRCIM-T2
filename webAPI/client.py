import os
from flask import Flask, request, jsonify
import requests


mypath = os.path.dirname(os.path.abspath(__file__))
mypath_images = mypath

""" mypath_images += "\product11.jpg" """
mypath_images += "/product10.jpg"
mypath_images = "D:/faculdade/SRCIM/GitHub/SRCIM-T2/webAPI/product1.jpg"

print(mypath_images)

url = 'http://127.0.0.1:5000/'
my_img = {'file': open(mypath_images, 'rb')}
r = requests.post(url, files=my_img)


# convert server response into JSON format.
print(r.json())