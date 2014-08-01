from app import app
app.run('0.0.0.0', debug=True, port=443, ssl_context='adhoc')
