from django.shortcuts import render
from django.http import HttpResponse, HttpResponseRedirect
from django.core.urlresolvers import reverse

from .forms import *
import random as rand


# Index view with a request, and a render response to index.html
def index(request):
	context = {}
	# In case the form has been completed, redirect to the filtered by departure flights page
	if request.method == 'POST':
		form = SelectDepartureForm(request.POST)
		if form.is_valid():
			id_departure = form.cleaned_data.get('departure')
			departure = form.get_departure(id_departure)
			return HttpResponseRedirect('flights/' + departure)

	# If not, send the empty form
	else:
		context['form'] = SelectDepartureForm()
	return render(request, 'index.html', context)


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


def shoppingcart(request):
	""" Backend view to add Selected flights to Cart. """
	reservations = []
	return_flights_ids = []
	request.session['reservations'] = []

	"""try:
		if request.session['reservations']:
			for pk in request.session['reservations']:
				reservations.append(pk)
	except Exception:  # not reservations in session
		pass"""

	for key in request.POST:

		# When selecting going flight
		if key.startswith('selected_going'):
			# For all checked flights, get the form values
			fid = request.POST[key]

			# Return flight can be selected or not
			try:
				return_flight = int(request.POST['return_flight' + fid])
				return_flights_ids.append(return_flight)
			except Exception:
				pass

			n_seats = int(request.POST['seats' + fid])
			type = request.POST['type' + fid]

			flight = Flight.objects.get(pk=fid)
			airline = rand.choice(flight.airlines.all())

			for i in range(n_seats):
				# TODO with backend information
				# Create one reservation for each seat
				res = Reservation()
				res.airline = airline
				res.price = 49.99
				res.flight = flight
				res.type = type
				res.save()
				reservations.append(res.pk)

		# When selecting return flight
		if key.startswith('selected_return'):
			# For all checked flights, get the form values
			fid = request.POST[key]

			n_seats = request.POST['seats' + fid]
			type = request.POST['type' + fid]

			flight = Flight.objects.get(pk=fid)
			airline = rand.choice(flight.airlines.all())

			for i in range(n_seats):
				# TODO with backend information
				# Create one reservation for each seat
				res = Reservation()
				res.airline = airline
				res.price = 49.99
				res.flight = flight
				res.type = type
				res.save()
				reservations.append(res.pk)

	request.session['reservations'] = reservations

	if len(return_flights_ids) == 0:
		return HttpResponseRedirect(reverse('flylo:buy'))
	else:
		url_parameters = ""
		for fid in return_flights_ids:
			url_parameters += str(fid) + "/"
		return HttpResponseRedirect('../return/' + url_parameters)


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
	context['session'] = request.session

	for pk in request.session['reservations']:
		context['reservations'].append(Reservation.objects.get(pk=pk))

	return render(request, 'buy.html', context)
