from flask import redirect, url_for
from app import app

@app.route('/')
def root():
    return app.send_static_file('index.html')
@app.errorhandler(404)
def page_not_found(error):
    return app.send_static_file('index.html')
