from django.conf.urls import url
from django.contrib.auth.decorators import login_required

from . import views

urlpatterns = [
	# Index
	url(r'^$', views.IndexView.as_view(), name='index'),

	# Flight URLs
	url(r'^flights/()$', views.FlightsView.as_view(), name='flights'),
	url(r'^flights/(?P<pk>\d+)/$', views.DetailedFlightView.as_view(), name='detailed_flight'),
	url(r'^flights/(?P<departure>\w+)/$', views.FlightsView.as_view(), name='flights'),
	url(r'^return/(?P<flight_list>[\d+/]+)/*$', views.return_flights, name='return_flights'),

	# User URLs
	url(r'^login/$', views.LoginView.as_view(), name='login'),
	url(r'^logout/$', views.logout, name='logout'),
	url(r'^account/$', login_required(views.AccountView.as_view(), login_url='/flylo/login/'), name='account'),
	url(r'^account/flights$', views.AccountView.as_view(), name='myflights'),

	# Shop URLs
	url(r'^shoppingcart/$', views.ShoppingCartView.as_view(), name='shoppingcart'),
	url(r'^buy/$', views.buy, name='buy'),
	url(r'^checkout/$', views.CheckoutView.as_view(), name='checkout'),

	# API URLs
	url(r'^api/price/(?P<flight>\d+)/(?P<airline>\d+)/(?P<nseats>\d+)/(?P<type>\w)/$', views.api_price),
]
