import webapp2
import Cache
import TemplateRenderer

class FrontPage(webapp2.RequestHandler):
	def get(self):
		self.response.write(TemplateRenderer.render_template(
			'main_page.html', content=Cache.get_cache_data()))
		
		
	def post(self):
		new_data = self.request.get("content")
		Cache.cache_data(new_data)
		self.redirect("/")