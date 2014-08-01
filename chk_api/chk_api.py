# hackathonvietnam2014 - team: vnluser
# author: Truc Le
from flask import Flask, session, Response
from flask.ext import restful
import dropbox
import json
app = Flask(__name__)
#api = restful.Api(app)


#TODO: add configuration layer for secret data
app_key = "fsdlsyzqfzmzf05"
app_secret = "d3u1ulosx8k2lfe"
flow = dropbox.client.DropboxOAuth2FlowNoRedirect(app_key, app_secret)
def get_auth_flow():
    redirect_uri = url_for('dropbox_auth_finish', _external=True)
    return DropboxOAuth2Flow(DROPBOX_APP_KEY, DROPBOX_APP_SECRET, redirect_uri,
                                     session, 'dropbox-auth-csrf-token')

def jd(res):
    return Response(response=json.dumps(res), status=200,mimetype="application/json",headers={"Access-Control-Allow-Origin":"*"})

#class Chk(restful.Resource):
@app.route("/chk/login")
def check_login():
    if "email" in session:
        return false
    else:
        return jd({"status":"forbidden","login_url":flow.start()})

@app.route("/chk/save")
def chk_save():
    chk_login = check_login()
    if chk_login:
        return chk_login

@app.route("/auth/login")
def auth_login():
    try:
        access_token, user_id, url_state = get_auth_flow().finish(request.args)
    except:
        abort(400)
    else:
        session["access_token"] = access_token
        session["email"] = user_id
        return jd({"status":"login_succeed","url_state":url_state})

"""
@app.route("lists/hashtag")
@app.route("lists/subscribe")
@app.route("favorities/create")
@app.route("lists/recommendation")
@app.route("lists/latest")
"""

#api.add_resource(HelloWorld, '/')

if __name__ == "__main__":
    app.run(host='0.0.0.0',debug=True)
