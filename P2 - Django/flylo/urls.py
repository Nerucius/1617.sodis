from django.conf.urls import url

from . import views

urlpatterns = [
	url(r'^$', views.index, name='index'),
	url(r'^flights/()$', views.flights, name='flights'),
	url(r'^flights/(?P<departure>\w+)/$', views.flights, name='flights'),
	url(r'^shoppingcart/$', views.shoppingcart, name='shoppingcart'),
	url(r'^checkout/$', views.checkout, name='buy'),
]
