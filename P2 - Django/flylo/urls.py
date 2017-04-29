from django.conf.urls import url

from . import views

urlpatterns = [
	# home page
	url(r'^$', views.index, name='index'),
	# flight list
	url(r'^flights/$', views.flights, name='flights'),
	url(r'^flights/(?P<departure>\w+)/$', views.flights, name='flights'),
]
