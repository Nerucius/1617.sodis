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
	template_name = 'account/login.html'

	def post(self, request):
		username = str(request.POST['username']).lower()
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


class SignupView(TemplateView):
	template_name = "account/signup.html"

	def post(self, request):
		from decimal import Decimal
		from django.contrib.auth.models import User
		username = str(request.POST.get('username')).lower()
		password = request.POST.get('password')
		email = request.POST.get('email')

		user = User.objects.create_user(username=username,
										first_name=username.title(),
										email=email,
										password=password)
		user.client.money = Decimal(5000.00)

		# Autologin and redirect
		auth.login(request, user)
		return HttpResponseRedirect(reverse('flylo:account'))


class AccountView(TemplateView):
	template_name = 'account/account.html'


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
	template_name = 'account/my_flights.html'

	def get_context_data(self, **kwargs):
		return self.get_my_flights()

	def get_my_flights(self):
		user = auth.get_user(self.request)
		client = Client.objects.get(user=user).pk

		context = {}
		context['my_flights'] = Reservation.objects.all().exclude(seat='').filter(client_id=client)
		return context


class CheckinView(TemplateView):
	template_name = 'account/checkin.html'

	@staticmethod
	def generate_seats_grid(reservation):
		context = {}
		context['reservation'] = reservation

		num_seats = reservation.flight.airplane.seats_economy
		if reservation.type == 'b':
			num_seats = reservation.flight.airplane.seats_business
		elif reservation.type == 'f':
			num_seats = reservation.flight.airplane.seats_first_class

		# Economy
		seats = {}
		for i in range(num_seats):
			name = reservation.type.upper() + "%02d"%(i+1)
			seats[name] = "disabled"

		context["seats"] = sorted(seats.iteritems())

		# context[reservation.type] = reservation.type
		# context['seats_economy'] = seats_economy
		# context['seats_business'] = seats_business
		# context['seats_first_class'] = seats_first_class

		return context

	def get_context_data(self, **kwargs):
		reservation = Reservation.objects.get(pk=kwargs['rpk'])
		context = self.generate_seats_grid(reservation)
		return context


class DetailedFlightView(TemplateView):
	template_name = 'detailed_flight.html'

	def get_context_data(self, **kwargs):
		return {'flight': Flight.objects.get(pk=kwargs['pk'])}


class ModifyCartView(View):

	def checkFreeSeats(self, flight, airline, type):
		# TODO

		Reservation.objects.filter(flight__pk=1, airline__code='IBE')

		return True

	def get(self, request):
		""" Used only for deleting reservations"""
		remove_res = int(request.GET.get('remove', None))
		if remove_res:
			Reservation.objects.filter(pk=remove_res).delete()
			request.session['reservations'] = [r for r in request.session['reservations'] if r is not remove_res]

		return HttpResponseRedirect(reverse('flylo:buy'))

	def post(self, request):
		from models import TYPE_MULT
		reservations = []
		return_flights_ids = []

		for key in request.POST:

			# When selecting going flight
			if key.startswith('selected_going'):
				# For all checked flights, get the form values
				fid = request.POST[key]

				# Return flight can be selected or not
				return_flight = request.POST.get('return_flight' + fid, None)
				if return_flight:
					return_flights_ids.append(fid)

				n_seats = int(request.POST.get('seats' + fid))
				type = request.POST.get('type' + fid)
				airline = request.POST.get('airline' + fid)

				flight = Flight.objects.get(pk=fid)
				airline = Airline.objects.get(code=airline)
				price = float(flight.price) * TYPE_MULT[type]  # TODO Note: sonia cast

				# TODO Before creating reservations, check if flight has enough free seats, otherwise redirect to error page

				# Create one reservation for every seat
				for i in range(n_seats):
					res = self.create_reservation(flight, airline, price, type)
					res.save()
					reservations.append(res.pk)

		# Append Reservations to existing ones
		request.session['reservations'] = request.session.get('reservations', [])
		for res in reservations:
			request.session['reservations'].append(res)

		# Finally, redirect to return flights if at least one return preference was set
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

	context = {'returns': []}

	for pk in flight_list:
		f = Flight.objects.get(pk=pk)
		arr = f.location_departure
		dep = f.location_arrival

		ret_flights = Flight.objects.filter(location_departure=dep, location_arrival=arr)

		context['returns'].append(
			{
				'flight': f,
				'return_flights': ret_flights
			}
		)

	return render(request, 'return_flights.html', context)


class CartView(TemplateView):
	template_name = 'shop/cart.html'

	def get_context_data(self, **kwargs):
		request = self.request
		context = {'reservations': []}

		for pk in request.session.get('reservations', []):
			try:
				context['reservations'].append(Reservation.objects.get(pk=pk))
			except Exception:
				request.session['reservations'].remove(pk)
				request.session.modified = True
		return context


class CheckoutView(TemplateView):
	template_name = "shop/checkout.html"

	def get(self, request, *args, **kwargs):
		context = {'reservations': []}

		# A pay "attempt" is registered
		if 'pay' in request.GET:
			client = User.objects.get(pk=request.user.pk).client

			reservations = []
			for rpk in request.session['reservations']:
				reservations.append(Reservation.objects.get(pk=rpk))

			total = sum([res.price for res in reservations])

			# IF the Client has enougb money
			if total < client.money:
				for res in reservations:
					res.paid = True
					res.client = client;
					res.save()
				client.money -= total
				client.save()
				del request.session['reservations']
				return HttpResponseRedirect(reverse('flylo:my_flights'))
			else:
				context['error'] = "Not enough funds in your account."

		reservations = request.session.get('reservations', False)
		if not reservations:
			return HttpResponseRedirect(reverse('flylo:index'))
		for pk in request.session.get('reservations'):
			context['reservations'].append(Reservation.objects.get(pk=pk))
		context['total'] = sum([res.price for res in context['reservations']])

		return render(request, self.template_name, context)


def api_set_money(request):
	money = request.POST.get('money')

	user = User.objects.get(pk=request.user.id)
	user.client.money = Decimal(money)
	user.save()

	return HttpResponse(user)


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
