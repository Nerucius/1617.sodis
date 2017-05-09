from django.conf.urls import url

from . import views

urlpatterns = [
	url(r'^$', views.index, name='index'),
	url(r'^flights/()$', views.flights, name='flights'),
	url(r'^flights/(?P<pk>\d+)/$', views.detailed_flight, name='detailed_flight'),
	url(r'^flights/(?P<departure>\w+)/$', views.flights, name='flights'),

	url(r'^return/(?P<flight_list>[\d+/]+)/*$', views.return_flights, name='return_flights'),

	url(r'^shoppingcart/$', views.shoppingcart, name='shoppingcart'),
	url(r'^buy/$', views.buy, name='buy'),
]
