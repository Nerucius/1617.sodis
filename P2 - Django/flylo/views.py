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
	template_name = 'account/signup.html'

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
		context['my_flights'] = Reservation.objects.all().exclude(paid=False).filter(client_id=client)
		return context


# Methods related to the seats that are used in the view classes below (CheckinView, ModifyCartView)
def get_total_n_of_seats(flight, type):
	n_seats = flight.airplane.seats_economy
	if type == 'b':
		n_seats = flight.airplane.seats_business
	elif type == 'f':
		n_seats = flight.airplane.seats_first_class
	return n_seats


def get_disabled_seats(flight, type):
	disabled_seats = Reservation.objects.filter(flight__pk=flight.pk, type=type).values_list('seat').exclude(
		seat__exact=None)
	disabled_seats = [x[0] for x in disabled_seats]
	return disabled_seats


def get_n_free_seats(flight, type, airline=None):
	"""Returns the number of all the available flight seats (the reserved and the checked out)"""
	n_seats = get_total_n_of_seats(flight, type)
	n_reservations = len(Reservation.objects.filter(flight__pk=flight.pk, type=type).values_list('seat'))
	return n_seats - n_reservations


class CheckinView(TemplateView):
	template_name = 'account/checkin.html'

	@staticmethod
	def generate_seats_grid(reservation):
		context = {}
		context['reservation'] = reservation

		# Seats grid size depending on reservation class
		n_seats = get_total_n_of_seats(reservation.flight, reservation.type)

		# Dictionary with the form (k,v) = (seat_code, disabled/" " )
		seats = {}
		for i in range(n_seats):
			# Generate seat code (ex: E34, B02, F03)
			seat_code = reservation.type.upper() + "%02d" % (i + 1)

			# List of disabled seats of the flight of a specific class
			disabled_seats = get_disabled_seats(reservation.flight, reservation.type)

			# Set free seats
			if seat_code in disabled_seats:
				seats[seat_code] = "disabled"
			else:
				seats[seat_code] = ""

		context["seats"] = sorted(seats.iteritems())
		return context

	def get_context_data(self, **kwargs):
		try:
			reservation = Reservation.objects.get(pk=kwargs['rpk'])
		except Exception:
			reservation = None

		# Check if the reservation of the url exists
		if not reservation:
			context = {'error': True}

		else:
			# Check if the reservation is of the logged user
			# Check if the reservation is not already checked in
			user = auth.get_user(self.request)
			client = Client.objects.get(user=user).pk
			if client != reservation.client_id or reservation.checkin:
				context = {'error': True}
			else:
				context = self.generate_seats_grid(reservation)
		return context

	def post(self, request, **kwargs):
		"""Used for saving selected seat and name for checkin"""
		reservation = Reservation.objects.get(pk=kwargs['rpk'])

		# Check if the selected seat is really a free seat
		selected_seat = request.POST['seat']
		disabled_seats = get_disabled_seats(reservation.flight, reservation.type)

		if selected_seat in disabled_seats:
			# Return the same view with a msg: The selected seat is no more available. Please, choose another seat.
			context = self.generate_seats_grid(reservation)
			context['already_assigned'] = True
			context['forename'] = request.POST['forename']
			context['surname'] = request.POST['surname']
			return render(request, self.template_name, context)

		else:
			# Save the checkin information
			reservation.forename = request.POST['forename']
			reservation.surname = request.POST['surname']
			reservation.seat = request.POST['seat']
			reservation.checkin = True
			reservation.save()
			return HttpResponseRedirect('../flights/')


class ModifyCartView(View):
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

				selected_n_seats = int(request.POST.get('seats' + fid))
				type = request.POST.get('type' + fid)
				airline = request.POST.get('airline' + fid)

				flight = Flight.objects.get(pk=fid)
				airline = Airline.objects.get(code=airline)
				price = Decimal(flight.price) * Decimal(TYPE_MULT[type])
				price += airline.price

				n_free_seats = get_n_free_seats(flight, type)
				if selected_n_seats > n_free_seats:
					return render(request, 'flights.html', {'error': True, 'departure': flight.location_departure})

				# Create one reservation for every seat
				for i in range(selected_n_seats):
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

class ReturnFlights(TemplateView):
	template_name = 'return_flights.html'

	def get_context_data(self, **kwargs):
		flight_list = kwargs['flight_list']

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
			return context


class DetailedFlightView(TemplateView):
	template_name = 'detailed_flight.html'

	def get_context_data(self, **kwargs):
		context = {}
		flight = Flight.objects.get(pk=kwargs['pk'])

		context['flight'] = flight
		context['free_first_class'] = get_n_free_seats(flight, 'f')
		context['free_business'] = get_n_free_seats(flight, 'b')
		context['free_economy'] = get_n_free_seats(flight, 'e')

		return context


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
	template_name = 'shop/checkout.html'

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


class ComparatorView(TemplateView):
	template_name = "comparator.html"

	def get_context_data(self, flight, **kwargs):
		from PracticaWeb.settings import BASE_DIR
		""" Look up Flight to be Compared. """
		context = super(ComparatorView, self).get_context_data()
		context['flight'] = Flight.objects.get(pk=flight)
		context['flight_pk'] = flight

		urls = []
		with open(BASE_DIR+'/flylo/urls.txt', 'r') as f:
			for line in f:
				if len(line) <= 0 or line.startswith('#'):
					continue
				urls.append("'"+line.strip()+"'")
			f.close()

		context['urls'] = '['+', '.join(urls)+']'

		return contextgit


def api_set_money(request):
	money = request.POST.get('money')

	user = User.objects.get(pk=request.user.id)
	user.client.money = Decimal(money)
	user.save()

	return HttpResponse(user)


def api_test(request):
	return render(request, "api_test.html")
