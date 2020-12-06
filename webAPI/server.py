
"""
from flask import Flask
app = Flask(__name__)

@app.route('/')
def hello_world():
    return 'Hello, World!'

if __name__ == '__main__':
    app.run() 
"""
import os, h5py
import tensorflow as tf
import numpy
from flask import Flask, request, jsonify
from PIL import Image

app = Flask(__name__)

@app.route("/", methods=["POST"])
def process_image():

    # Get internal path
    mypath = os.path.dirname(os.path.abspath(__file__))
    mypath_images = mypath
    
    # Append file name
    model_file_name = mypath + '\srcim_model.h5'

    # Read the model -> this is the right way to import a fully saved Keras model
    # Reference: https://stackoverflow.com/questions/35074549/how-to-load-a-model-from-an-hdf5-file-in-keras
    # Documentation: https://www.tensorflow.org/api_docs/python/tf/keras/models/load_model
    model = tf.keras.models.load_model(model_file_name, custom_objects=None, compile=True, options=None)

    file = request.files['image']
    # Read the image via file.stream
    img = Image.open(file.stream)

    # Save image
    # img_path = mypath + 'received.jpg'
    #file.save(img_path)

    # Pre-process image before prediction
    img = tf.keras.preprocessing.image.load_img(file.stream, target_size=(224, 224))
    img_array = tf.keras.preprocessing.image.img_to_array(img) / 255
    img_array = tf.expand_dims(img_array, 0) # Create a batch

    # Prediction
    # Documentation: https://www.tensorflow.org/api_docs/python/tf/keras/Model#predict
    output = model.predict(img_array)
    output_str = numpy.array2string(output)

    return jsonify({'msg': 'success', 'output': output_str})


if __name__ == "__main__":
    app.run()