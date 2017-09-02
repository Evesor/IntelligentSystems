import jinja2
import os

template_dir = os.path.join(os.path.dirname(__file__), "templates")
jinja_env = jinja2.Environment(loader=jinja2.FileSystemLoader(template_dir), autoescape=True)
jinja_env_lim_escape = jinja2.Environment(loader=jinja2.FileSystemLoader(template_dir), autoescape=False)

'''
    Main template has following attributes:
        title = Title of the html page
        signup - Bool: turn on signup button
        login - Bool: turn on login button
        user = user name of logged in user
'''


def render_template(template, title="Wikki Now", *args, **kwargs):
    tem = jinja_env.get_template(template)
    return tem.render(title=title, **kwargs)


def render_template_selected_escape(template, title="Wikki Now", *args, **kwargs):
    tem = jinja_env_lim_escape.get_template(template)
    return tem.render(title=title, **kwargs)



