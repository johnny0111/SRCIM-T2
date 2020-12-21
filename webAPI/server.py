
"""
from flask import Flask
app = Flask(__name__)

@app.route('/')
def hello_world():
    return 'Hello, World!'

if __name__ == '__main__':
    app.run() 
"""
import os
import tensorflow as tf
import numpy
from werkzeug import utils
from flask import Flask, request, jsonify
from PIL import Image

app = Flask(__name__)

@app.route("/", methods=["POST"])
def process_image():

    # Get internal path
    mypath = os.path.dirname(os.path.abspath(__file__))
    mypath_images = mypath
    
    # Append file name
    model_file_name = mypath + '\srcim_model2.h5'

    # Read the model -> this is the right way to import a fully saved Keras model
    # Reference: https://stackoverflow.com/questions/35074549/how-to-load-a-model-from-an-hdf5-file-in-keras
    # Documentation: https://www.tensorflow.org/api_docs/python/tf/keras/models/load_model
    model = tf.keras.models.load_model(model_file_name, custom_objects=None, compile=True, options=None)

    # Upload single file of tyoe 'image' 
    #file = request.files['image']
    # Read the image via file.stream
    #img = Image.open(file.stream)

    # Save image
    #img_path = mypath + 'received.jpg'
    #file.save(img_path)


    """ 
    *** Upload several files ***

        uploaded_files = request.files.getlist("file")
        i=0
        for i, elem in enumerate(uploaded_files):
            filename = utils.secure_filename(uploaded_files[i].filename)
            print("\nReceived image File name : " + uploaded_files[i].filename)
            mypath_save = os.path.dirname(os.path.abspath(__file__)) + filename
            uploaded_files[i].save(mypath_save)
    """

    # Prof will only ask for one at a time

    # We'll save the image instead of read it directly  from the file stream
    # since it was giving us some problems

    uploaded_file = request.files['file']
    filename = utils.secure_filename(uploaded_file.filename)
    print("\nReceived image File name : " + uploaded_file.filename)
    mypath_save = os.path.dirname(os.path.abspath(__file__)) + filename
    uploaded_file.save(mypath_save)


    # Pre-process image before prediction
    # Documentation: https://www.tensorflow.org/api_docs/python/tf/keras/preprocessing/image/load_img
    # Prediction
    # Documentation: https://www.tensorflow.org/api_docs/python/tf/keras/Model#predict
    img = tf.keras.preprocessing.image.load_img(mypath_save, target_size=(224, 224))
    img_array = tf.keras.preprocessing.image.img_to_array(img) / 255
    img_array = tf.expand_dims(img_array, 0) # Create a batch
    prediction = model.predict(img_array)
    if(prediction[0][0] < 0.80):
        prediction_str = "OK"
    
    else :
        prediction_str = "NOK"
        """ prediction_str = numpy.array2string(prediction) """
    
    return jsonify({'msg': 'success', 'output': prediction_str})


if __name__ == "__main__":
    app.run()