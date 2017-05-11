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
		return render(request, 'index.html', context)

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

		user = auth.authenticate(username=username, password=password)
		if user is not None and user.is_active:
			auth.login(request, user)
			return HttpResponseRedirect(reverse('flylo:account'))
		else:
			return render(request, self.template_name, {'error': True})


class AccountView(TemplateView):
	template_name = 'account.html'

	def get(self, request, **kwargs):
		return super(AccountView, self).get(request, kwargs)


def logout(request):
	auth.logout(request)
	return HttpResponseRedirect(reverse('flylo:index'))


def flights(request, departure=None):
	context = {}

	if departure:
		context['departure'] = departure
		context['flights'] = Flight.objects.filter(location_departure=departure)
	else:
		context['flights'] = Flight.objects.all()

	return render(request, 'flights.html', context)


def detailed_flight(request, pk=None):
	context = {}
	context['flight'] = Flight.objects.get(pk=pk)
	return render(request, 'detailed_flight.html', context)


class DetailedFlightView(TemplateView):
	template_name = 'detailed_flight.html'

	def get_context_data(self, **kwargs):
		pk = kwargs.get('pk')
		return {'flight': Flight.objests.get(pk=pk)}


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
				try:
					return_flight = str(request.POST['return_flight' + fid])
					return_flights_ids.append(return_flight)
				except Exception:
					pass

				n_seats = int(request.POST['seats' + fid])
				type = request.POST['type' + fid]

				flight = Flight.objects.get(pk=fid)
				airline = rand.choice(flight.airlines.all())

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
