from flask import Flask, request, jsonify
from flask_cors import CORS 
import os
import traceback

app = Flask(__name__)
CORS(app)  # Enable CORS for all routes

app.config['UPLOAD_FOLDER'] = 'uploads'
app.config['ALLOWED_EXTENSIONS'] = {'mp4'}

def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in app.config['ALLOWED_EXTENSIONS']

@app.route('/upload/', methods=['POST'])
def upload_file():
    try:
        my_files = request.files

        if not my_files:
            return jsonify({'error': 'No files received'}), 400

        for item in my_files:
            uploaded_file = my_files.get(item)

            if uploaded_file.filename == '':
                return jsonify({'error': 'No selected file'}), 400

            if uploaded_file and allowed_file(uploaded_file.filename):
                # Save the uploaded file to the uploads directory
                uploaded_file.save(os.path.join(app.config['UPLOAD_FOLDER'], uploaded_file.filename))
                return jsonify({'message': 'File uploaded successfully'}), 200
            else:
                return jsonify({'error': 'Invalid file type'}), 400
    except Exception as e:
        # Log the exception details
        traceback_str = traceback.format_exc()
        error_message = f"An error occurred: {str(e)}\n\n{traceback_str}"
        return jsonify({'error': error_message}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=False)
