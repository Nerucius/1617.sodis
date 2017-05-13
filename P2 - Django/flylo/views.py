from django.contrib.auth.decorators import login_required
from django.shortcuts import render
from django.http import HttpResponse, HttpResponseRedirect, Http404
from django.core.urlresolvers import reverse

from django.views.generic import View, TemplateView

from django.contrib import auth

from .forms import *
import random as rand


class IndexView(TemplateView):
	template_name = 'index.html'

	def get(self, request):
		context = {'form': SelectDepartureForm()}
		return render(request, self.template_name, context)

	def post(self, request):
		form = SelectDepartureForm(request.POST)
		if form.is_valid():
			id_departure = form.cleaned_data.get('departure')
			departure = form.get_departure(id_departure)
			return HttpResponseRedirect('flights/' + departure)


class LoginView(TemplateView):
	template_name = 'login.html'

	def post(self, request):
		username = request.POST['username']
		password = request.POST['password']
		try:
			next = request.GET['next']
		except Exception:
			next = reverse('flylo:account')

		user = auth.authenticate(username=username, password=password)
		if user is not None and user.is_active:
			auth.login(request, user)
			return HttpResponseRedirect(next)

		else:
			return render(request, self.template_name, {'error': True})


class AccountView(TemplateView):
	template_name = 'account.html'

	def get(self, request, **kwargs):
		return super(AccountView, self).get(request, kwargs)


def logout(request):
	auth.logout(request)
	return HttpResponseRedirect(reverse('flylo:index'))


class FlightsView(TemplateView):
	template_name = 'flights.html'

	def get_context_data(self, **kwargs):
		context = super(FlightsView, self).get_context_data()
		departure = kwargs.get('departure')
		if departure:
			context['departure'] = departure
			context['flights'] = Flight.objects.filter(location_departure=departure)
		else:
			context['flights'] = Flight.objects.all()
		return context


class MyFlightsView(TemplateView):
	template_name = 'my_flights.html'

	def get_context_data(self, **kwargs):
		return self.get_my_flights()

	def get_my_flights(self):
		user = auth.get_user(self.request)
		client = Client.objects.get(user=user).pk

		context = {}
		context['my_flights'] = Reservation.objects.all().exclude(seat='').filter(client_id=client)
		return context


class DetailedFlightView(TemplateView):
	template_name = 'detailed_flight.html'

	def get_context_data(self, **kwargs):
		pk = kwargs.get('pk')
		return {'flight': Flight.objects.get(pk=pk)}


class ShoppingCartView(View):
	def get(self, request):
		""" Used only for deleting reservations"""
		remove_res = int(request.GET.get('remove', None))
		Reservation.objects.filter(pk=remove_res).delete()
		request.session['reservations'] = [r for r in request.session['reservations'] if r is not remove_res]

		return HttpResponseRedirect(reverse('flylo:buy'))

	def post(self, request):
		user = request.user

		reservations = []
		return_flights_ids = []

		for key in request.POST:

			# When selecting going flight
			if key.startswith('selected_going'):
				# For all checked flights, get the form values
				fid = request.POST[key]

				# Return flight can be selected or not
				return_flight = request.POST.get('return_flight'+fid, None)
				if return_flight:
					return_flights_ids.append(return_flight)

				n_seats = int(request.POST.get('seats' + fid))
				type = request.POST.get('type' + fid)
				airline = request.POST.get('airline' + fid)

				flight = Flight.objects.get(pk=fid)
				airline = Airline.objects.get(code=airline)

				for i in range(n_seats):
					res = self.create_reservation(flight, airline, 49.95, type)
					res.save()
					reservations.append(res.pk)

			# When selecting return flight
			if key.startswith('selected_return'):
				# For all checked flights, get the form values
				fid = request.POST[key]

				n_seats = int(request.POST['seats' + fid])
				type = request.POST['type' + fid]

				flight = Flight.objects.get(pk=fid)
				airline = rand.choice(flight.airlines.all())

				for i in range(n_seats):
					res = self.create_reservation(flight, airline, 49.95, type)
					res.save()
					reservations.append(res.pk)

		request.session['reservations'] = request.session.get('reservations', [])
		for res in reservations:
			request.session['reservations'].append(res)

		if len(return_flights_ids) == 0:
			return HttpResponseRedirect(reverse('flylo:buy'))
		else:
			url_parameters = "/".join(return_flights_ids)
			return HttpResponseRedirect('../return/' + url_parameters)

	@staticmethod
	def create_reservation(flight, airline, price, type):
		# type: (Flight, Airline, float, str) -> Reservation
		res = Reservation()
		res.airline = airline
		res.price = price
		res.flight = flight
		res.type = type
		return res


def return_flights(request, flight_list):
	flight_list = set(flight_list.split('/'))

	try:
		flight_list.remove('')
	except Exception:
		pass

	context = {}

	context["flights"] = []

	for pk in flight_list:
		f = Flight.objects.get(pk=pk)
		flight_number = f.flight_number
		arr = f.location_departure
		dep = f.location_arrival

		ret_flights = Flight.objects.filter(location_departure=dep, location_arrival=arr)

		context['flights'].append(
			{
				'flight_number': flight_number,
				'return_flights': ret_flights
			}
		)

	return render(request, 'return_flights.html', context)


def buy(request):
	context = {'reservations': []}

	for pk in request.session.get('reservations', []):
		try:
			context['reservations'].append(Reservation.objects.get(pk=pk))
		except Exception:
			request.session['reservations'].remove(pk)
			request.session.modified = True

	return render(request, 'buy.html', context)


class CheckoutView(TemplateView):
	template_name = "shop/checkout.html"

	def get(self, request):
		context = {'reservations': []}
		for pk in request.session['reservations']:
			context['reservations'].append(Reservation.objects.get(pk=pk))
		context['total'] = sum([res.price for res in context['reservations']])

		return render(request, self.template_name, context)


def api_price(request, flight, airline, nseats, type):
	""" Calculate the price of the given number of seats for a flight with a given airline.
		The price will vary in realtion to a series of factors:
		- base: base price of the flight
		- c: class ( 1 for E, 1.5 for B, 2.5 for F )
		- d: days remaining for departure ( -0.01 per day remaining, caps at -0.25)
		- f: how full the plane is for the given class (0.75 empty to 1.5 full)
		Final price = base * (c + d + f)
	"""
	import random
	class_mult = 1
	if type == 'b':
		class_mult = 1.5
	elif type == 'f':
		class_mult = 2.5

	price = round(class_mult * int(nseats) * (35 + random.random() * 10), 2)

	return HttpResponse(price)


# REST Api
from rest_framework import viewsets
from serializers import FlightSerializer, AirlineSerializer


class FlightViewSet(viewsets.ModelViewSet):
	serializer_class = FlightSerializer
	queryset = Flight.objects.all()

	def get_queryset(self):
		from datetime import datetime
		queryset = Flight.objects.all()

		dep = self.request.query_params.get('departure', None)
		arr = self.request.query_params.get('arrival', None)
		dept = self.request.query_params.get('departure_time', None)
		arrt = self.request.query_params.get('arrival_time', None)

		if dep:
			queryset = queryset.filter(location_departure=dep)
		if arr:
			queryset = queryset.filter(location_arrival=arr)
		if dept:
			dept = datetime.strptime(dept, "%Y-%m-%dT%H:%M:%SZ")
			queryset = queryset.filter(estimated_time_departure__ge=dept)
		if arrt:
			arrt = datetime.strptime(dept, "%Y-%m-%dT%H:%M:%SZ")
			queryset = queryset.filter(estimated_time_arrival__ge=arrt)

		return queryset
