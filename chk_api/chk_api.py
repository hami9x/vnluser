# hackathonvietnam2014 - team: vnluser
# author: Truc Le
from flask import * 
from flask.ext import restful
import dropbox
from unidecode import unidecode
import tempfile
import StringIO
import json
import redis
import urllib

app = Flask(__name__)
#api = restful.Api(app)


#TODO: add configuration layer for secret data
DROPBOX_APP_KEY     = "fsdlsyzqfzmzf05"
DROPBOX_APP_SECRET  = "d3u1ulosx8k2lfe"
app.secret_key      = "asd232x12ka5s12"
BACKEND_API         = "b.chk.vn"
R_POST_ID           = "post_id"
R_ITEMS             = "items"
R_USER              = "user"
R_POST              = "post"
R_ZUSER             = "zuser"
R_PUBLIC_POST       = "public_post"

def get_auth_flow():
    redirect_uri = "https://st.chk.vn/dropbox_login"
    return dropbox.client.DropboxOAuth2Flow(DROPBOX_APP_KEY, DROPBOX_APP_SECRET, redirect_uri, 
        session, "dropbox-auth-csrf-token")

#flow = dropbox.client.DropboxOAuth2FlowNoRedirect(DROPBOX_APP_KEY, DROPBOX_APP_SECRET)
flow = get_auth_flow() 

def jd(res):
    headers = {"Access-Control-Allow-Origin":"https://st.chk.vn","Access-Control-Allow-Credentials": "true","Access-Control-Allow-Methods": "PROPFIND, PROPPATCH, COPY, MOVE, DELETE, MKCOL, LOCK, UNLOCK, PUT, GETLIB, VERSION-CONTROL, CHECKIN, CHECKOUT, UNCHECKOUT, REPORT, UPDATE, CANCELUPLOAD, HEAD, OPTIONS, GET, POST","Access-Control-Allow-Headers":"Overwrite, Destination, Content-Type, Depth, User-Agent, X-File-Size, X-Requested-With, If-Modified-Since, X-File-Name, Cache-Control"}
    return Response(response=json.dumps(res), status=200,
        mimetype="application/json",headers=headers)

def get_redis():
    return redis.StrictRedis(host=BACKEND_API, port=6379, db=0) 

def is_login():
    if "db_user_id" in session:
        return True
    else:
        return False

@app.route("/chk/login")
def check_login():
    if "db_user_id" in session:
        return jd({"status":"ok","db_user_id":session["db_user_id"]})
    else:
        return jd({"status":"forbidden","login_url":flow.start()})

def get_post_id():
    r = get_redis()
    return r.incr(R_POST_ID)

def get_file_name(title, post_id):
    return str() + "_" + unidecode(urllib.quote(title.replace(" ","_")) + ".html") 

def create_file(req_json):
    tf = StringIO.StringIO()
    tf.write(json.dumps(req_json))
    return tf

@app.route("/chk/save", methods=['POST'])
def chk_save():
    if "access_token" not in session: 
        return check_login()
    req_json = request.get_json(force=True)
    title = req_json["title"]
    post_id = get_post_id()
    filename = get_file_name(title,post_id)
    tf = create_file(req_json)
    client = dropbox.client.DropboxClient(session["access_token"])
    client.put_file(filename,tf)
    r = get_redis()
    req_json["user_id"] = session["db_user_id"]
    req_json["post_id"] = post_id
    keywords = req_json["keywords"]
    dp_link = client.share(filename,short_url=False)
    dp_link = dp_link["url"].replace('www.dropbox.com', 'dl.dropboxusercontent.com', 1)
    link = req_json["link"]
    user_id = session["db_user_id"]
    stored_json = {"post_id": post_id, "title":title, "keywords":keywords, "dp_link": dp_link, "link": link}
    #Queue, will be replaced by kafka
    r.lpush(R_ITEMS, json.dumps(stored_json))
    
    #Latest 100 posts
    r.lpush(R_PUBLIC_POST, json.dumps(stored_json))
    r.ltrim(R_PUBLIC_POST, 0, 5000)
    
    #Store user posts 
    r.hset(R_USER + ":" + str(user_id),  R_POST + ":" + str(post_id), json.dumps(stored_json))
    #Store sorted set of post_id
    r.zadd(R_ZUSER + ":" + str(user_id), str(post_id), str(post_id))
    return jd({"status":"succeed"})



@app.route("/auth/login")
def auth_login():
    try:
        access_token, user_id, url_state = get_auth_flow().finish(request.args)
    except:
        return jd({"status":"can_not_login"})
    else:
        session["access_token"] = access_token
        session["db_user_id"] = user_id
        return jd({"status":"login_succeed","url_state":url_state,"user_id":user_id,"access_token":access_token})

@app.route("/auth/logout")
def auth_logout():
    session.clear()
    return jd({"status":"logout_succeed"})

@app.route("/lists/post")
def lists_post():
    limit = request.args.get('limit') if request.args.get('limit') else 10
    offset = request.args.get('offset') if request.args.get('offset') else 0
    r = get_redis()
    list_post = r.zrevrangebyscore(R_ZUSER + ":" + session["db_user_id"], "+inf", "-inf", start=offset, num=limit)
    res = []
    r = get_redis()
    for post_id in list_post:
        res.append(json.loads(r.hget(R_USER + ":" + str(session["db_user_id"]), R_POST + ":" + str(post_id))))
    return jd(res)

@app.route("/lists/public_post")
def lists_public():
    limit = request.args.get('limit') if request.args.get('limit') else 10
    offset = request.args.get('offset') if request.args.get('offset') else 0
    r = get_redis()
    list_post = r.lrange(R_PUBLIC_POST, offset, limit)
    res = []
    r = get_redis()
    for post in list_post:
        res.append(json.loads(post))
    return jd(res)

@app.route("/lists/recommendation")
def lists_recommendation():
    limit = 10
    offset = 0
    r = get_redis()
    #TODO: Get recommendation
    #list_recommendation = r.zrevrangebyscore(R_ZUSER + ":" + session["db_user_id"], "+inf", "-inf", start=offset, num=limit)
    res = []
    #r = get_redis()
    #for post_id in list_recommendation:
    #    res.append(json.loads(r.hget(R_USER + ":" + str(session["db_user_id"]), R_POST + ":" + str(post_id))))
    return jd(res)
"""
@app.route("lists/hashtag")
@app.route("lists/subscribe")
@app.route("favorities/create")
"""

if __name__ == "__main__":
    app.run(host='0.0.0.0', debug=True, ssl_context="adhoc")
