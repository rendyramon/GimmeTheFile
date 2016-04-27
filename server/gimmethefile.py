import json
import urllib
import os
import youtube_dl
from flask import Flask, request, make_response

app = Flask(__name__)

@app.route('/gimmethefile', methods=['GET'])
def video_info():
    url = request.args.get('url', '')
    if len(url) < 1:
        return 'ERROR: url argument not found'
        
    url = urllib.parse.unquote(url)
        
    ydl_opts = {'playliststart': 1,
                'playlistend': 1}
                
    with youtube_dl.YoutubeDL(ydl_opts) as ydl:
        extracted = None
       
        try:
            extracted = ydl.extract_info(url, download=False)
            extracted = fix_extracted(extracted)
        
        except Exception as e:
            return make_response(('{}'.format(e), 400))
            
        if request.args.get('pretty', ''):
            return json.dumps(extracted, indent=4)
        
        response = make_response(json.dumps(extracted))
        response.headers['Content-Type'] = 'application/json'
        return response
        
def fix_extracted(e):
    if 'entries' in e:
        for k, v in e['entries'][0].items():
            e.update({k: v})
        del e['entries']
        
    if not 'formats' in e:
        e['formats'] = []
        e['formats'].append({})
        
        fields = ['http_headers', 'ext', 'format', 'protocol', 'url']
        
        for field in fields:
            if e.get(field):
                e['formats'][0].update({field: e[field]})
                
    return e
        
if __name__ == '__main__':
    port = int(os.environ.get("PORT", 5000))
    app.run(host='0.0.0.0', port=port, debug=True)