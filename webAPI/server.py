# on terminal:
# C:\path\to\app>set FLASK_APP=hello.py

"""
from flask import Flask
app = Flask(__name__)

@app.route('/')
def hello_world():
    return 'Hello, World!'

if __name__ == '__main__':
    app.run() 
"""
 
from flask import Flask, request, jsonify
from PIL import Image

app = Flask(__name__)

@app.route("/im_size", methods=["POST"])
def process_image():
    file = request.files['image']
    # Read the image via file.stream
    img = Image.open(file.stream)
    file.save('im-received.jpg')


    return jsonify({'msg': 'success', 'size': [img.width, img.height]})


if __name__ == "__main__":
    #app.run(debug=True)
    app.run()