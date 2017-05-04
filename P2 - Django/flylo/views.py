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
	reservations = []
	if request.session['reservations']:
		for pk in request.session['reservations']:
			reservations.append(pk)

	for key in request.POST:
		if key.startswith('checkbox'):
			# For all checked flights, get the form values
			fid = request.POST[key]
			n_seats = int(request.POST['seats' + fid])
			type = request.POST['type' + fid]

			flight = Flight.objects.get(pk=fid)
			airline = rand.choice(flight.airlines.all())

			for i in range(n_seats):
				# Create one reservation for each seat
				res = Reservation()
				res.airline = airline
				res.price = 49.99
				res.flight = flight
				res.type = type
				res.save()
				reservations.append(res.pk)

	request.session['reservations'] = reservations

	return HttpResponseRedirect(reverse('flylo:buy'))


def buy(request):
	context = {'reservations': []}
	context['session'] = request.session

	for pk in request.session['reservations']:
		context['reservations'].append(Reservation.objects.get(pk=pk))

	return render(request, 'buy.html', context)
