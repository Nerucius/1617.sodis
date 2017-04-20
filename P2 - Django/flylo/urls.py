from django.conf.urls import url

from . import views

urlpatterns = [
	# Index route
	url(r'^$', views.index, name='index'),
	url(r'^flights/$', views.flights, name='flights'),
	url(r'^flights/(?P<departure>\w+)/$', views.flights)
]
