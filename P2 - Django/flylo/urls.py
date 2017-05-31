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
	url(r'^return/(?P<flight_list>[\d+/]+)/*$', views.ReturnFlights.as_view(), name='return_flights'),

	# Comparator URL
	url(r'^comparator/(?P<flight>\d+)$', views.ComparatorView.as_view(), name='comparator'),

	# User URLs
	url(r'^login/$', views.LoginView.as_view(), name='login'),
	url(r'^logout/$', views.logout, name='logout'),
	url(r'^signup/$', views.SignupView.as_view(), name='signup'),
	url(r'^account/$', login_required(views.AccountView.as_view(), login_url='/flylo/login/'), name='account'),
	url(r'^account/flights/$', login_required(views.MyFlightsView.as_view(), login_url='/flylo/login/'),
		name='my_flights'),
	url(r'^account/checkin/(?P<rpk>\d+)$', login_required(views.CheckinView.as_view(), login_url='/flylo/login/'),
		name='checkin'),

	# Shop URLs
	url(r'^shoppingcart/$', views.ModifyCartView.as_view(), name='shoppingcart'),
	url(r'^cart/$', views.CartView.as_view(), name='buy'),
	url(r'^checkout/$', login_required(views.CheckoutView.as_view(), login_url='/flylo/login/'), name='checkout'),

	# API URLs
	url(r'^api/set_money/$', login_required(views.api_set_money), name='set_money'),

	# TODO DELETE TESTING VIEW
	url(r'^test/$', views.api_test)
]
