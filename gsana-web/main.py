# -*- coding: utf-8 -*-
#!/usr/bin/env python
#
# Copyright 2007 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#
from google.appengine.ext import ndb
import webapp2
import json

from jinja2 import Environment, FileSystemLoader

loader = Environment(loader=FileSystemLoader('html'))


def get_template(loader, template_name):
    """
    Gets template to be rendered.
    :param loader: loader is jinja environment instance
    :param template_name: name of template
    :returns: template to render
    """
    return loader.get_template(template_name)


class MainHandler(webapp2.RequestHandler):
    """
    Main handler
    """
    def get(self):
        self.response.out.write('Hello Gsana')


class OAuthCallback(webapp2.RequestHandler):
    """
    Main handler
    """
    def get(self):
        self.response.headers.add_header('Content-Type', 'text/html')
        t = get_template(loader, 'oauth.html')
        self.response.out.write(t.render())



app = webapp2.WSGIApplication([
    ('/', MainHandler),
    ('/oauth_callback/?', OAuthCallback)
], debug=True)
