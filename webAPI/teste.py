#!flask/bin/python

import requests

from flask import Flask
from flask_cors import CORS
import psutil

import os
import subprocess

from flask import request, send_from_directory, Response
import json
from werkzeug import utils

from os import listdir
from os.path import isfile, join, basename

from urllib.parse import urlencode
import pycurl

import glob
import socketio

import eventlet
import eventlet.wsgi

import socket
import zipfile
from io import StringIO



app = Flask(__name__)
CORS(app)



@app.route('/images', methods = ['POST'])			#recebe imagens e guarda-as com o mesmo nome, elimina as que estão na diretoria 
def handle_request_images():

    mypath = os.path.dirname(os.path.abspath(__file__))
    mypath_images = mypath
    mypath_images += "\data\\1\data_raw\Mission\\*.jpg"

    files = glob.glob(mypath_images)
    for f in files:
        os.remove(f)

    uploaded_files = request.files.getlist("file")
    i=0
    for i, elem in enumerate(uploaded_files):
        filename = utils.secure_filename(uploaded_files[i].filename)
        print("\nReceived image File name : " + uploaded_files[i].filename)
        mypath_save = os.path.dirname(os.path.abspath(__file__))
        mypath_save += "\data\\1\data_raw\Mission\\"
        mypath_save += filename
        uploaded_files[i].save(mypath_save)

    return "Images Uploaded Successfully"




@app.route('/sendfiles', methods = ['GET', 'POST'])			#/envia uma pasta 
def send_3d_data():

    mypath = os.path.dirname(os.path.abspath(__file__))
    path_mtl = mypath

    return send_from_directory(path_mtl, ZipName, as_attachment=True)




def lookingFile():					#Envia um Post (lado cliente)

                url = "http://192.168.1.3:5002/finishedODM"
                headers = {
                    'Content-type': 'application/json',
                }
                response = requests.post(url, headers=headers, data='')
                





app.run(host='0.0.0.0', port=5002)





