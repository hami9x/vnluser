import tornado.ioloop
import tornado.web
import urllib
import json
import nltk
from nltk.corpus import stopwords
from HTMLParser import HTMLParser
from textblob import TextBlob

def unique(a):
    """ return the list with duplicate elements removed """
    return list(set(a))

def intersect(a, b):
    """ return the intersection of two lists """
    return list(set(a) & set(b))

def union(a, b):
    """ return the union of two lists """
    return list(set(a) | set(b))

class MLStripper(HTMLParser):
    def __init__(self):
        self.reset()
        self.fed = []
    def handle_data(self, d):
        self.fed.append(d)
    def get_data(self):
        return ''.join(self.fed)

def strip_tags(html):
    s = MLStripper()
    s.feed(html)
    return s.get_data()

class MainHandler(tornado.web.RequestHandler):
    def get(self):
    	title = urllib.unquote(self.get_argument('title', default='', strip=True))
    	#do not trust the client 
    	content = strip_tags(urllib.unquote(self.get_argument('content', default='', strip=True)))

    	print(title)
    	print(content)

    	blob_title = TextBlob(title)
    	blob_content = TextBlob(content)
    	a = blob_title.noun_phrases
    	b = blob_content.noun_phrases

    	keywords = union(b, a)   
    	stopset = set(stopwords.words('english'))  
    	#print(keywords)  		
    	keywords = [w for w in keywords if not w in stopset]
    	#print(keywords)
    
        #self.write("<br> blob_title " + json.dumps(blob_title.noun_phrases))
        #self.write("<br> blob_content " + json.dumps(blob_content.noun_phrases))
        #self.write("<br> keywords " + json.dumps(keywords))
        self.write(json.dumps(keywords))

application = tornado.web.Application([
    (r"/keywords", MainHandler),
])

if __name__ == "__main__":
    application.listen(8888)
    tornado.ioloop.IOLoop.instance().start()