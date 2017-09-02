import webapp2
import FrontPage

app = webapp2.WSGIApplication([
    ('/', FrontPage.FrontPage),
], debug=True)
