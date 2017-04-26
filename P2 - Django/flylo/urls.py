from django.conf.urls import url

from . import views

urlpatterns = [
	url(r'^$', views.index, name='index'),  # Index (home page)
	url(r'^flights/$', views.flights, name='flights'),  # List of flights
	url(r'^flights/(?P<departure>\w+)/$', views.flights, name='flights')  # List of flights with a specific departure
]
